package com.cardumper

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import java.io.File

class SerialDumpActivity : AppCompatActivity() {

    private lateinit var btnStart: Button
    private lateinit var btnStop: Button
    private lateinit var btnShare: Button
    private lateinit var etCommand: EditText
    private lateinit var btnRunCommand: Button
    private lateinit var tvStatus: TextView
    private lateinit var tvBytes: TextView
    private lateinit var tvData: TextView

    private var totalBytes = 0L
    private var lastLine = ""

    private val dataReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val bytes = intent?.getLongExtra("bytes", 0L) ?: 0L
            val data = intent?.getStringExtra("data") ?: ""
            totalBytes = bytes
            tvBytes.text = "Bytes: $bytes"
            if (data.isNotEmpty()) {
                lastLine = data
                tvData.text = "Last: $data"
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_serial_dump)

        btnStart = findViewById(R.id.btnStart)
        btnStop = findViewById(R.id.btnStop)
        btnShare = findViewById(R.id.btnShare)
        etCommand = findViewById(R.id.etCommand)
        btnRunCommand = findViewById(R.id.btnRunCommand)
        tvStatus = findViewById(R.id.tvStatus)
        tvBytes = findViewById(R.id.tvBytes)
        tvData = findViewById(R.id.tvData)

        // Register receiver
        val filter = IntentFilter("com.cardumper.SERIAL_DATA")
        LocalBroadcastManager.getInstance(this).registerReceiver(dataReceiver, filter)

        btnStart.setOnClickListener {
            startDump()
        }

        btnStop.setOnClickListener {
            stopDump()
        }

        btnShare.setOnClickListener {
            shareFile()
        }

        btnRunCommand.setOnClickListener {
            val cmd = etCommand.text.toString().trim()
            if (cmd.isEmpty()) {
                updateStatus("Empty command")
                return@setOnClickListener
            }
            val intent = Intent(this, SerialDumpService::class.java).apply {
                action = SerialDumpService.ACTION_EXECUTE_COMMAND
                putExtra(SerialDumpService.EXTRA_COMMAND, cmd)
            }
            startService(intent)
            updateStatus("Running CMD")
        }

        updateStatus("Ready")
    }

    private fun startDump() {
        val intent = Intent(this, SerialDumpService::class.java).apply {
            action = SerialDumpService.ACTION_START
            putExtra(SerialDumpService.EXTRA_SERIAL_PORT, "/dev/ttyS0")
            putExtra(SerialDumpService.EXTRA_OUTPUT_FILE, "/sdcard/SerialDump/serial_dump.txt")
            putExtra(SerialDumpService.EXTRA_PC_HOST, "10.74.10.163")
            putExtra(SerialDumpService.EXTRA_PC_PORT, 8080)
        }
        startService(intent)
        updateStatus("Dumping...")
        Toast.makeText(this, "Started dumping", Toast.LENGTH_SHORT).show()
    }

    private fun stopDump() {
        val intent = Intent(this, SerialDumpService::class.java).apply {
            action = SerialDumpService.ACTION_STOP
        }
        startService(intent)
        updateStatus("Stopped")
        Toast.makeText(this, "Stopped dumping", Toast.LENGTH_SHORT).show()
    }

    private fun shareFile() {
        val file = File("/sdcard/SerialDump/serial_dump.txt")
        if (!file.exists()) {
            Toast.makeText(this, "No dump file yet", Toast.LENGTH_SHORT).show()
            return
        }

        val uri = FileProvider.getUriForFile(
            this,
            "${applicationInfo.packageName}.provider",
            file
        )

        val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(android.content.Intent.EXTRA_TEXT, uri.toString())
            putExtra(android.content.Intent.EXTRA_SUBJECT, "Serial Dump Data")
        }
        startActivity(android.content.Intent.createChooser(shareIntent, "Share Dump"))
    }

    private fun updateStatus(status: String) {
        tvStatus.text = "Status: $status"
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(dataReceiver)
    }
}
