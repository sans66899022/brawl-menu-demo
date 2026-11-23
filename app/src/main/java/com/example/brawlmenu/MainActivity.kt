package com.example.brawlmenu

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {
    private lateinit var status: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(28, 32, 28, 32)
            setBackgroundColor(Color.rgb(20, 25, 42))
        }
        root.addView(TextView(this).apply {
            text = "UNDERTALE EQUIPMENT DEMO"
            textSize = 24f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
        }, matchWrap())
        root.addView(TextView(this).apply {
            text = "A small floating menu for choosing demo weapons and defense items."
            textSize = 15f
            setTextColor(Color.LTGRAY)
            gravity = Gravity.CENTER
            setPadding(0, 12, 0, 24)
        }, matchWrap())
        status = TextView(this).apply {
            textSize = 14f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(0, 8, 0, 20)
        }
        root.addView(status, matchWrap())
        root.addView(Button(this).apply {
            text = "Grant overlay permission"
            setOnClickListener {
                if (!Settings.canDrawOverlays(this@MainActivity)) {
                    startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
                } else {
                    Toast.makeText(this@MainActivity, "Permission already granted", Toast.LENGTH_SHORT).show()
                    updateStatus()
                }
            }
        }, matchWrap())
        root.addView(Button(this).apply {
            text = "Open floating menu"
            setOnClickListener {
                if (Settings.canDrawOverlays(this@MainActivity)) {
                    startForegroundService(Intent(this@MainActivity, FloatingMenuService::class.java))
                    Toast.makeText(this@MainActivity, "Floating equipment menu opened", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@MainActivity, "Grant overlay permission first", Toast.LENGTH_LONG).show()
                }
            }
        }, matchWrap())
        root.addView(TextView(this).apply {
            text = "Standalone demo only. It does not connect to or modify Undertale."
            textSize = 12f
            setTextColor(Color.LTGRAY)
            gravity = Gravity.CENTER
            setPadding(0, 24, 0, 0)
        }, matchWrap())
        setContentView(root)
        updateStatus()
    }

    override fun onResume() {
        super.onResume()
        if (::status.isInitialized) updateStatus()
    }

    private fun updateStatus() {
        status.text = if (Settings.canDrawOverlays(this)) {
            "Overlay permission: granted"
        } else {
            "Overlay permission: not granted yet"
        }
    }

    private fun matchWrap() = LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
    )
}