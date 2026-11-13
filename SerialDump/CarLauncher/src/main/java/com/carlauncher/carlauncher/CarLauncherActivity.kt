package com.carlauncher.carlauncher

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class CarLauncherActivity : AppCompatActivity() {

    private lateinit var tvStatus: TextView
    private lateinit var tvBytes: TextView
    private lateinit var tvData: TextView
    private lateinit var btnStart: Button
    private lateinit var btnStop: Button
    private lateinit var btnAcMode: Button
    private lateinit var tvAcState: TextView
    private lateinit var tvNowPlaying: TextView
    private lateinit var btnPlayPause: Button
    private lateinit var btnNext: Button
    private lateinit var btnPrev: Button
    private lateinit var btnShowApps: Button
    private lateinit var lvApps: ListView

    private val dataReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val bytes = intent?.getLongExtra("bytes", 0L) ?: 0L
            val data = intent?.getStringExtra("data") ?: ""
            tvBytes.text = "Bytes: $bytes"
            tvData.text = if (data.isNotEmpty()) "Last: $data" else "Waiting for data..."
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_car_launcher)

        tvStatus = findViewById(R.id.tvStatus)
        tvBytes = findViewById(R.id.tvBytes)
        tvData = findViewById(R.id.tvData)
        btnStart = findViewById(R.id.btnStart)
        btnStop = findViewById(R.id.btnStop)
        btnAcMode = findViewById(R.id.btnAcMode)
        tvAcState = findViewById(R.id.tvAcState)
        tvNowPlaying = findViewById(R.id.tvNowPlaying)
        btnPlayPause = findViewById(R.id.btnPlayPause)
        btnNext = findViewById(R.id.btnNext)
        btnPrev = findViewById(R.id.btnPrev)
        btnShowApps = findViewById(R.id.btnShowApps)
        lvApps = findViewById(R.id.lvApps)

        val filter = IntentFilter("com.carlauncher.carlauncher.SERIAL_DATA")
        registerReceiver(dataReceiver, filter)

        btnStart.setOnClickListener {
            val intent = Intent(this, CarLauncherService::class.java).apply {
                action = CarLauncherService.ACTION_START
                putExtra(CarLauncherService.EXTRA_SERIAL_PORT, "/dev/ttyS0")
                putExtra(CarLauncherService.EXTRA_OUTPUT_FILE, "/sdcard/SerialDump/serial_dump.txt")
                putExtra(CarLauncherService.EXTRA_PC_HOST, "10.74.10.163")
                putExtra(CarLauncherService.EXTRA_PC_PORT, 8080)
            }
            startService(intent)
            tvStatus.text = "Status: Dumping..."
            Toast.makeText(this, "Started dumping", Toast.LENGTH_SHORT).show()
        }

        btnStop.setOnClickListener {
            val intent = Intent(this, CarLauncherService::class.java).apply {
                action = CarLauncherService.ACTION_STOP
            }
            startService(intent)
            tvStatus.text = "Status: Stopped"
            Toast.makeText(this, "Stopped dumping", Toast.LENGTH_SHORT).show()
        }

        btnAcMode.setOnClickListener {
            val modes = listOf("OFF", "AC", "HEAT", "FAN")
            val current = tvAcState.text.toString().substringAfterLast(' ').ifEmpty { "OFF" }
            val idx = modes.indexOf(current).let { if (it < 0) 0 else it }
            val nextIdx = (idx + 1) % modes.size
            tvAcState.text = "AC State: ${modes[nextIdx]}"
        }

        btnPlayPause.setOnClickListener {
            val playing = tvNowPlaying.text.toString().contains("Playing")
            tvNowPlaying.text = if (playing) "Not playing" else "Playing"
        }

        btnNext.setOnClickListener {
            val tracks = listOf("Track 1", "Track 2", "Track 3", "Track 4")
            val current = tvNowPlaying.text.toString().substringAfterLast(' ').ifEmpty { "Track 1" }
            val idx = tracks.indexOf(current).let { if (it < 0) 0 else it }
            val nextIdx = (idx + 1) % tracks.size
            tvNowPlaying.text = "Playing: ${tracks[nextIdx]}"
        }

        btnPrev.setOnClickListener {
            val tracks = listOf("Track 1", "Track 2", "Track 3", "Track 4")
            val current = tvNowPlaying.text.toString().substringAfterLast(' ').ifEmpty { "Track 1" }
            val idx = tracks.indexOf(current).let { if (it < 0) 0 else it }
            val prevIdx = if (idx - 1 < 0) tracks.size - 1 else idx - 1
            tvNowPlaying.text = "Playing: ${tracks[prevIdx]}"
        }

        btnShowApps.setOnClickListener {
            val pm = getPackageManager()
            val apps = ArrayList<String>()
            val installed = pm.getInstalledPackages(0)
            for (info in installed) {
                if (info.packageName == packageName) continue
                try {
                    val launchIntent = pm.getLaunchIntentForPackage(info.packageName)
                    if (launchIntent != null) {
                        val appName = info.applicationInfo.loadLabel(pm).toString()
                        apps.add("$appName ($${info.packageName})")
                    }
                } catch (e: Exception) {
                    // skip
                }
            }
            val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, apps)
            lvApps.adapter = adapter
            Toast.makeText(this, "Found ${apps.size} apps", Toast.LENGTH_SHORT).show()
        }

        tvStatus.text = "Status: Ready"
        tvAcState.text = "AC State: OFF"
        tvNowPlaying.text = "Not playing"
    }

    override fun onDestroy() {
        unregisterReceiver(dataReceiver)
        super.onDestroy()
    }
}
