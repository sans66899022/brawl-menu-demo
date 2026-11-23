package com.example.brawlmenu

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
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
    private lateinit var mapDemo: SampleMapView
    private lateinit var status: TextView
    private var espEnabled = false
    private var teleportStep = 0

    override fun onCreate() {
        super.onCreate()
        startAsForeground()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        showPanel()
    }

    private fun startAsForeground() {
        val channelId = "among_us_demo"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Among Us demo menu", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
        val openApp = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        @Suppress("DEPRECATION")
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) Notification.Builder(this, channelId) else Notification.Builder(this)
        val notification = builder.setContentTitle("Among Us Demo Menu")
            .setContentText("Standalone sample-map demo is running")
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
            setBackgroundColor(Color.rgb(28, 32, 48))
        }
        val header = TextView(this).apply {
            text = "AMONG US MENU  •  DRAG HERE"
            textSize = 14f
            setTextColor(Color.WHITE)
            setPadding(4, 6, 4, 10)
        }
        container.addView(header)
        status = TextView(this).apply {
            text = "Standalone demo map — not connected to a match."
            textSize = 11f
            setTextColor(Color.LTGRAY)
            setPadding(4, 2, 4, 8)
        }
        container.addView(status)
        mapDemo = SampleMapView(this)
        container.addView(mapDemo, LinearLayout.LayoutParams(300, 190))

        header.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    startX = event.rawX; startY = event.rawY
                    originalX = params.x; originalY = params.y
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

        container.addView(Switch(this).apply {
            text = "ESP — sample-map markers"
            textSize = 13f
            setTextColor(Color.WHITE)
            setPadding(4, 6, 4, 6)
            setOnCheckedChangeListener { _, checked ->
                espEnabled = checked
                mapDemo.showMarkers = checked
                mapDemo.invalidate()
                status.text = if (checked) "Demo ESP ON: sample markers shown on this map." else "Demo ESP OFF."
            }
        })
        container.addView(Button(this).apply {
            text = "Teleport — move demo character"
            setOnClickListener {
                teleportStep = (teleportStep + 1) % 4
                mapDemo.playerPosition = teleportStep
                mapDemo.invalidate()
                status.text = "Demo teleport: sample character moved to map point ${teleportStep + 1}."
            }
        })
        container.addView(TextView(this).apply {
            text = "Demo only. No live player tracking, match ESP, game injection or in-game teleporting."
            textSize = 10f
            setTextColor(Color.LTGRAY)
            setPadding(4, 6, 4, 8)
        })
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
        ).apply { gravity = Gravity.TOP or Gravity.START; x = 24; y = 140 }
        panel = container
        windowManager.addView(container, params)
    }

    override fun onDestroy() {
        panel?.let { if (it.isAttachedToWindow) windowManager.removeView(it) }
        panel = null
        stopForeground(STOP_FOREGROUND_REMOVE)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}

private class SampleMapView(context: Context) : View(context) {
    var showMarkers = false
    var playerPosition = 0
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val positions = arrayOf(
        floatArrayOf(0.22f, 0.25f), floatArrayOf(0.76f, 0.25f),
        floatArrayOf(0.24f, 0.76f), floatArrayOf(0.76f, 0.74f)
    )

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat(); val h = height.toFloat()
        paint.color = Color.rgb(45, 51, 70)
        canvas.drawRoundRect(0f, 0f, w, h, 16f, 16f, paint)
        paint.color = Color.rgb(95, 108, 132)
        paint.style = Paint.Style.STROKE; paint.strokeWidth = 3f
        canvas.drawRoundRect(w*.08f, h*.12f, w*.92f, h*.88f, 12f, 12f, paint)
        canvas.drawLine(w*.5f, h*.12f, w*.5f, h*.88f, paint)
        canvas.drawLine(w*.08f, h*.5f, w*.92f, h*.5f, paint)
        paint.style = Paint.Style.FILL
        paint.color = Color.WHITE; paint.textSize = 12f
        canvas.drawText("SAMPLE MAP", 12f, 18f, paint)
        if (showMarkers) {
            for (i in positions.indices) {
                val p = positions[i]
                paint.color = Color.rgb(255, 92, 92)
                canvas.drawCircle(w*p[0], h*p[1], 9f, paint)
                paint.color = Color.WHITE; paint.textSize = 9f
                canvas.drawText("P${i+1}", w*p[0]+10f, h*p[1]+3f, paint)
            }
        }
        val p = positions[playerPosition]
        paint.color = Color.rgb(75, 220, 235)
        canvas.drawCircle(w*p[0], h*p[1], 12f, paint)
        paint.color = Color.BLACK; paint.textSize = 9f
        canvas.drawText("YOU", w*p[0]-9f, h*p[1]+3f, paint)
    }
}
