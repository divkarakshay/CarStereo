package com.cardumper

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class SerialDumpReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "SerialDumpReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val bytes = intent.getLongExtra("bytes", 0)
        val data = intent.getStringExtra("data") ?: ""
        Log.d(TAG, "Received: bytes=$bytes, data=$data")
    }
}