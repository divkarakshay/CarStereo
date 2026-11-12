package com.cardumper

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.Socket
import java.util.concurrent.atomic.AtomicBoolean

class SerialDumpService : Service() {

    companion object {
        private const val TAG = "SerialDumpService"
        private const val NOTIFICATION_CHANNEL_ID = "serial_dump_channel"
        private const val NOTIFICATION_ID = 1
        const val ACTION_START = "com.cardumper.ACTION_START"
        const val ACTION_STOP = "com.cardumper.ACTION_STOP"
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
    private var totalBytesWritten = 0L

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val port = intent?.extras?.getString(EXTRA_SERIAL_PORT, "/dev/ttyS0") ?: "/dev/ttyS0"
                val outputFile = intent?.extras?.getString(EXTRA_OUTPUT_FILE, "/sdcard/SerialDump/serial_dump.txt")
                    ?: "/sdcard/SerialDump/serial_dump.txt"
                val pcHost = intent?.getStringExtra(EXTRA_PC_HOST)
                val pcPort = intent?.getStringExtra(EXTRA_PC_PORT)?.toInt()
                startDumping(port, outputFile, pcHost, pcPort)
            }
            ACTION_STOP -> {
                stopDumping()
                stopSelf()
            }
        }
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun startDumping(port: String, outputFile: String, pcHost: String?, pcPort: Int?) {
        if (isRunning.get()) {
            Log.w(TAG, "Service already running")
            return
        }

        isRunning.set(true)
        totalBytesWritten = 0

        if (pcHost != null && pcPort != null) {
            try {
                pcSocket = Socket(pcHost, pcPort)
                pcOutputStream = pcSocket?.getOutputStream()
                Log.i(TAG, "Streaming data to PC at ${pcHost}:${pcPort}")
            } catch (e: IOException) {
                Log.e(TAG, "Failed to connect to PC ${pcHost}:${pcPort}: ${e.message}")
            }
        }

        dumpThread = Thread {
            try {
                // Open serial port
                val serialFile = File(port)
                if (!serialFile.exists()) {
                    Log.e(TAG, "Serial port not found: $port")
                    // Try common alternatives
                    val alternatives = listOf("/dev/ttyS1", "/dev/ttyS2", "/dev/ttyS3", "/dev/ttyS4", "/dev/ttyS5")
                    var found = false
                    for (alt in alternatives) {
                        if (File(alt).exists()) {
                            Log.i(TAG, "Trying alternative: $alt")
                            inputStream = File(alt).inputStream()
                            found = true
                            break
                        }
                    }
                    if (!found) {
                        Log.e(TAG, "No serial port found")
                        return@Thread
                    }
                } else {
                    inputStream = serialFile.inputStream()
                }

                // Open output file
                val outputDir = File(outputFile).parentFile
                if (outputDir != null && !outputDir.exists()) {
                    outputDir.mkdirs()
                }
                fileOutputStream = FileOutputStream(outputFile, true)

                Log.i(TAG, "Started dumping from $port to $outputFile")

                val buffer = ByteArray(4096)
                while (isRunning.get()) {
                    val bytesRead = inputStream!!.read(buffer)
                    if (bytesRead > 0) {
                        fileOutputStream!!.write(buffer, 0, bytesRead)
                        totalBytesWritten += bytesRead
                        pcOutputStream?.write(buffer, 0, bytesRead)
                        pcOutputStream?.flush()
                        if (totalBytesWritten % 1024 == 0L) {
                            Log.d(TAG, "Bytes written: $totalBytesWritten")
                        }
                        // Broadcast data
                        val dataString = String(buffer, 0, bytesRead)
                        val broadcast = Intent("com.cardumper.SERIAL_DATA")
                        broadcast.putExtra("bytes", totalBytesWritten)
                        broadcast.putExtra("data", dataString)
                        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast)
                    } else if (bytesRead == -1) {
                        Log.w(TAG, "End of stream reached")
                        break
                    }
                }
            } catch (e: IOException) {
                Log.e(TAG, "IOException: ${e.message}", e)
            } catch (e: Exception) {
                Log.e(TAG, "Exception: ${e.message}", e)
            } finally {
                pcSocket?.close()
                closeResources()
                Log.i(TAG, "Dump stopped. Total bytes: $totalBytesWritten")
                isRunning.set(false)
                stopSelf()
            }
        }
        dumpThread?.start()

        // Show foreground notification
        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Serial Dump")
            .setContentText("Dumping serial data to $outputFile")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
        startForeground(NOTIFICATION_ID, notification)
    }

    private fun stopDumping() {
        isRunning.set(false)
        dumpThread?.interrupt()
        closeResources()
    }

    private fun closeResources() {
        try {
            inputStream?.close()
            fileOutputStream?.close()
        } catch (e: IOException) {
            Log.e(TAG, "Error closing resources: ${e.message}")
        }
        inputStream = null
        fileOutputStream = null
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            "Serial Dump",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Serial data dump service"
        }
        val notificationManager = getSystemService(NotificationManager::class.java) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
