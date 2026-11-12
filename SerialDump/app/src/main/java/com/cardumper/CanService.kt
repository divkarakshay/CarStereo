package com.cardumper

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.concurrent.atomic.AtomicBoolean

/**
 * A single CAN frame received from the vehicle's MCU.
 *
 * @param id   CAN identifier (11-bit base identifier or 29-bit extended identifier)
 * @param data Frame payload bytes
 */
data class CanFrame(
    val id: Int,
    val data: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CanFrame) return false
        return id == other.id && data.contentEquals(other.data)
    }

    override fun hashCode(): Int = id * 31 + java.util.Arrays.hashCode(data)

    override fun toString(): String =
        "CanFrame(id=0x${id.toString(16)}, data=${data.joinToString(",") { "%02X".format(it) }})"
}

/**
 * Foreground service that reads CAN data from the stereo MCU over the serial
 * interface, writes the raw dump to disk, and exposes parsed [CanFrame]s via [LiveData].
 *
 * Architecture notes (see CONVERSATION_SUMMARY.md):
 * - The Allwinner T507 has no native CAN; the separate MCU (T5Q.5.1-...) handles CAN
 *   and exposes it over a proprietary serial protocol.
 * - TODO(reverse-engineer): Once the MCU serial protocol is decoded, replace the
 *   placeholder frame handling in [readSerialLoop] with real CAN frame parsing
 *   (see TASKS.md Phase 1: "Identify engine state CAN ID" / "Identify AC control CAN IDs").
 */
class CanService : Service() {

    companion object {
        private const val TAG = "CanService"
        private const val NOTIFICATION_CHANNEL_ID = "can_service_channel"
        private const val NOTIFICATION_ID = 2

        const val ACTION_START = "com.cardumper.ACTION_CAN_START"
        const val ACTION_STOP = "com.cardumper.ACTION_CAN_STOP"
        const val EXTRA_SERIAL_PORT = "extra_serial_port"
        const val EXTRA_OUTPUT_FILE = "extra_output_file"
    }

    private val isRunning = AtomicBoolean(false)
    private var dumpThread: Thread? = null
    private var inputStream: InputStream? = null
    private var fileOutputStream: FileOutputStream? = null

    private val framesLiveData = MutableLiveData<List<CanFrame>>(emptyList())

    /** Streams the latest CAN frames observed from the MCU. */
    fun frames: LiveData<List<CanFrame>> get() = framesLiveData

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startDumping(
                intent?.getStringExtra(EXTRA_SERIAL_PORT) ?: "/dev/ttyS0",
                intent?.getStringExtra(EXTRA_OUTPUT_FILE) ?: "/sdcard/SerialDump/can_dump.txt"
            )
            ACTION_STOP -> stopDumping()
        }
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun startDumping(port: String, outputFile: String) {
        if (isRunning.get()) {
            Log.w(TAG, "Service already running")
            return
        }

        isRunning.set(true)
        dumpThread = Thread { readSerialLoop(port, outputFile) }
        dumpThread?.start()

        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("CAN Bus")
            .setContentText("Listening on $port")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
        startForeground(NOTIFICATION_ID, notification)
    }

    private fun readSerialLoop(port: String, outputFile: String) {
        try {
            val input: InputStream = openInputStream(port)

            val outputFileObj = File(outputFile)
            val outputDir = outputFileObj.parentFile
            if (outputDir != null && !outputDir.exists()) {
                outputDir.mkdirs()
            }
            val output = FileOutputStream(outputFile, true)

            Log.i(TAG, "CanService started on $port -> $outputFile")
            val buffer = ByteArray(4096)

            while (isRunning.get()) {
                val bytesRead = input.read(buffer)
                if (bytesRead > 0) {
                    // Persist raw dump
                    output.write(buffer, 0, bytesRead)
                    output.flush()

                    // Parse incoming bytes into CAN frames and expose via LiveData.
                    // TODO(reverse-engineer): Replace placeholder parsing with the
                    // real MCU serial protocol once CAN IDs are identified (Phase 1).
                    val frame = CanFrame(
                        id = 0,
                        data = buffer.copyOfRange(0, bytesRead)
                    )
                    val current = framesLiveData.value ?: emptyList()
                    framesLiveData.postValue(current + frame)

                    // Broadcast for quick UI updates
                    val broadcast = Intent("com.cardumper.SERIAL_DATA")
                    broadcast.putExtra("bytes", framesLiveData.value?.size ?: 0)
                    broadcast.putExtra("data", frame.toString())
                    sendBroadcast(broadcast)
                } else if (bytesRead == -1) {
                    Log.w(TAG, "End of stream reached")
                    break
                }
            }

            output.close()
            Log.i(TAG, "CanService stopped. Frames: ${framesLiveData.value?.size ?: 0}")
        } catch (e: IOException) {
            Log.e(TAG, "IOException: ${e.message}", e)
        } catch (e: Exception) {
            Log.e(TAG, "Exception: ${e.message}", e)
        } finally {
            closeResources()
            isRunning.set(false)
            stopSelf()
        }
    }

    private fun openInputStream(port: String): InputStream {
        val serialFile = File(port)
        if (!serialFile.exists()) {
            val alternatives = listOf(
                "/dev/ttyS1", "/dev/ttyS2", "/dev/ttyS3",
                "/dev/ttyS4", "/dev/ttyS5"
            )
            for (alt in alternatives) {
                if (File(alt).exists()) {
                    Log.i(TAG, "Serial port not found: $port. Trying alternative: $alt")
                    return File(alt).inputStream()
                }
            }
            throw IOException("No serial port found. Tried: $port")
        }
        return serialFile.inputStream()
    }

    private fun stopDumping() {
        isRunning.set(false)
        dumpThread?.interrupt()
        closeResources()
        stopSelf()
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
            "CAN Bus Service",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Listening for CAN data from the car stereo MCU"
        }
        val notificationManager = NotificationManager(this)
        notificationManager.createNotificationChannel(channel)
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning.set(false)
        dumpThread?.interrupt()
        closeResources()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
