package com.example.brawlmenu

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast

class FloatingMenuService : Service() {
    private lateinit var windowManager: WindowManager
    private var panel: View? = null
    private lateinit var params: WindowManager.LayoutParams
    private var startX = 0f
    private var startY = 0f
    private var originalX = 0
    private var originalY = 0

    override fun onCreate() {
        super.onCreate()
        startAsForeground()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        showPanel()
    }

    private fun startAsForeground() {
        val channelId = "brawl_menu_demo"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Floating menu", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
        val openApp = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        @Suppress("DEPRECATION")
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, channelId)
        } else {
            Notification.Builder(this)
        }
        val notification = builder
            .setContentTitle("Brawl Menu Demo")
            .setContentText("Floating menu is running")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(openApp)
            .setOngoing(true)
            .build()
        startForeground(1, notification)
    }

    private fun showPanel() {
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(14, 12, 14, 12)
            setBackgroundColor(Color.rgb(31, 39, 63))
        }
        val header = TextView(this).apply {
            text = "BRAWL MENU  •  DRAG HERE"
            textSize = 14f
            setTextColor(Color.WHITE)
            setPadding(4, 6, 4, 14)
        }
        container.addView(header)
        header.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    startX = event.rawX
                    startY = event.rawY
                    originalX = params.x
                    originalY = params.y
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    params.x = originalX + (event.rawX - startX).toInt()
                    params.y = originalY + (event.rawY - startY).toInt()
                    windowManager.updateViewLayout(container, params)
                    true
                }
                else -> true
            }
        }
        addToggle(container, "Auto Dodge — UNAVAILABLE")
        addToggle(container, "Auto Attack — UNAVAILABLE")
        addToggle(container, "Demo switch 3")
        container.addView(Button(this).apply {
            text = "Close menu"
            setOnClickListener { stopSelf() }
        })
        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 32
            y = 160
        }
        panel = container
        windowManager.addView(container, params)
    }

    private fun addToggle(parent: LinearLayout, label: String) {
        val automationUnavailable = label.contains("UNAVAILABLE")
        parent.addView(Switch(this).apply {
            text = label
            textSize = 14f
            setTextColor(if (automationUnavailable) Color.LTGRAY else Color.WHITE)
            setPadding(4, 8, 4, 8)
            if (automationUnavailable) {
                isEnabled = false
                alpha = 0.65f
            } else {
                setOnCheckedChangeListener { _, checked ->
                    val state = if (checked) "ON" else "OFF"
                    Toast.makeText(this@FloatingMenuService, "$label: $state", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    override fun onDestroy() {
        panel?.let { if (it.isAttachedToWindow) windowManager.removeView(it) }
        panel = null
        stopForeground(STOP_FOREGROUND_REMOVE)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
