package com.carlauncher.carlauncher

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.Socket
import java.util.concurrent.atomic.AtomicBoolean

class CarLauncherService : Service() {

    companion object {
        const val ACTION_START = "com.carlauncher.carlauncher.ACTION_START"
        const val ACTION_STOP = "com.carlauncher.carlauncher.ACTION_STOP"
        const val EXTRA_SERIAL_PORT = "extra_serial_port"
        const val EXTRA_OUTPUT_FILE = "extra_output_file"
        const val EXTRA_PC_HOST = "extra_pc_host"
        const val EXTRA_PC_PORT = "extra_pc_port"
    }

    private val isRunning = AtomicBoolean(false)
    private var dumpThread: Thread? = null
    private var inputStream: InputStream? = null
    private var fileOutputStream: FileOutputStream? = null
    private var pcSocket: Socket? = null
    private var pcOutputStream: OutputStream? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(1, buildNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startDumping(intent)
            ACTION_STOP -> stopDumping()
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        stopDumping()
        super.onDestroy()
    }

    private fun startDumping(intent: Intent) {
        if (isRunning.get()) return
        isRunning.set(true)

        val port = intent.getStringExtra(EXTRA_SERIAL_PORT) ?: "/dev/ttyS0"
        val outputFile = intent.getStringExtra(EXTRA_OUTPUT_FILE)
            ?: File(getExternalFilesDir(null), "serial_dump.txt").path
        val pcHost = intent.getStringExtra(EXTRA_PC_HOST)
        val pcPort = intent.getIntExtra(EXTRA_PC_PORT, 8080)

        pcHost?.let {
            try {
                pcSocket = Socket(it, pcPort)
                pcOutputStream = pcSocket?.getOutputStream()
            } catch (e: IOException) {
                // PC not reachable yet; dump continues
            }
        }

        dumpThread = Thread {
            try {
                val serialFile = File(port)
                val stream: InputStream = if (serialFile.exists()) {
                    FileInputStream(serialFile)
                } else {
                    FileInputStream("/dev/ttyS1")
                }
                inputStream = stream
                fileOutputStream = FileOutputStream(outputFile, true)

                val buffer = ByteArray(1024)
                while (isRunning.get()) {
                    val read = stream.read(buffer)
                    if (read > 0) {
                        fileOutputStream?.write(buffer, 0, read)
                        fileOutputStream?.flush()
                        pcOutputStream?.write(buffer, 0, read)
                        pcOutputStream?.flush()
                        val broadcast = Intent("com.carlauncher.carlauncher.SERIAL_DATA")
                        broadcast.putExtra("bytes", read.toLong())
                        broadcast.putExtra("data", String(buffer, 0, read))
                        sendBroadcast(broadcast)
                    } else if (read == -1) {
                        break
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        dumpThread?.start()
    }

    private fun stopDumping() {
        isRunning.set(false)
        dumpThread?.interrupt()
        pcSocket?.close()
        inputStream?.close()
        fileOutputStream?.close()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "com.carlauncher.carlauncher.channel",
                "Car Launcher Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        val channelId = "com.carlauncher.carlauncher.channel"
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Car Launcher Service")
            .setContentText("Reading data from MCU")
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .build()
    }
}
