package com.carlauncher.serialdump

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.io.FileInputStream
import java.util.concurrent.Executors

class SerialDumpService : Service() {
    private val executor = Executors.newSingleThreadExecutor()
    private var running = false
    private val TAG = "SerialDumpService"

    override fun onCreate() {
        super.onCreate()
        running = true
        startForeground(1, buildNotification())
        executor.execute { readSerialLoop() }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        running = false
        executor.shutdownNow()
        super.onDestroy()
    }

    private fun buildNotification(): Notification {
        val channelId = "serial_dump_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Serial Dump",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Serial Dump Service")
            .setContentText("Reading data from MCU")
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .build()
    }

    private fun readSerialLoop() {
        // Attempt to open the serial port. The exact path may vary.
        val serialPath = "/dev/ttyS0"
        val file = File(serialPath)
        if (!file.exists()) {
            // Try common alternative
            val alt = File("/dev/ttyS1")
            if (alt.exists()) {
                readFromPort(alt)
            }
            return
        }
        readFromPort(file)
    }

    private fun readFromPort(file: File) {
        try {
            val input: InputStream = FileInputStream(file)
            val buffer = ByteArray(1024)
            val outputFile = File(getExternalFilesDir(null), "serial_dump.txt")
            val output: OutputStream = FileOutputStream(outputFile, true)
            while (running) {
                val read = input.read(buffer)
                if (read > 0) {
                    output.write(buffer, 0, read)
                    output.flush()
                    // Broadcast the new data length for UI update
                    val intent = Intent("com.carlauncher.SERIAL_DATA")
                    intent.putExtra("bytes", read)
                    sendBroadcast(intent)
                }
            }
            input.close()
            output.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}
