package com.example.brawlmenu

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
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
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast

class FloatingMenuService : Service() {
    private lateinit var windowManager: WindowManager
    private var panel: View? = null
    private lateinit var params: WindowManager.LayoutParams
    private var level = 99
    private var exp = 0
    private var hp = 20
    private lateinit var levelText: TextView
    private lateinit var expText: TextView
    private lateinit var hpText: TextView

    override fun onCreate() {
        super.onCreate()
        startAsForeground()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        showPanel()
    }

    private fun startAsForeground() {
        val channelId = "undertale_level_demo"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Undertale Level Demo", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
        val openApp = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or if (Build.VERSION.SDK_INT >= 23) PendingIntent.FLAG_IMMUTABLE else 0
        )
        val notification = (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) Notification.Builder(this, channelId) else Notification.Builder(this))
            .setContentTitle("Undertale Level 99 Demo")
            .setContentText("Standalone stats-menu demo is running")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(openApp)
            .setOngoing(true)
            .build()
        startForeground(1, notification)
    }

    private fun showPanel() {
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 14, 16, 14)
            setBackgroundColor(Color.rgb(28, 28, 34))
        }
        val header = TextView(this).apply {
            text = "UNDERTALE • LV 99  —  DRAG"
            textSize = 16f
            setTextColor(Color.WHITE)
            setPadding(4, 8, 4, 14)
        }
        container.addView(header)
        val disclaimer = TextView(this).apply {
            text = "Standalone demo • changes are local only"
            textSize = 11f
            setTextColor(Color.LTGRAY)
        }
        container.addView(disclaimer)

        levelText = addStat(container, "LEVEL: 99")
        expText = addStat(container, "EXP: 0")
        hpText = addStat(container, "HP: 20 / 20")

        addSlider(container, "EXP amount", 0, 99999, exp) { value ->
            exp = value
            expText.text = "EXP: $exp"
        }
        addSlider(container, "HP amount", 1, 999, hp) { value ->
            hp = value
            hpText.text = "HP: $hp / 20"
        }

        container.addView(Button(this).apply {
            text = "SET DISPLAYED LEVEL TO 99"
            setOnClickListener {
                level = 99
                levelText.text = "LEVEL: $level"
                Toast.makeText(this@FloatingMenuService, "Demo level set to 99", Toast.LENGTH_SHORT).show()
            }
        })
        container.addView(Button(this).apply {
            text = "RESET DEMO STATS"
            setOnClickListener {
                level = 1
                exp = 0
                hp = 20
                levelText.text = "LEVEL: $level"
                expText.text = "EXP: $exp"
                hpText.text = "HP: $hp / 20"
                Toast.makeText(this@FloatingMenuService, "Demo stats reset", Toast.LENGTH_SHORT).show()
            }
        })
        container.addView(Button(this).apply {
            text = "CLOSE MENU"
            setOnClickListener { stopSelf() }
        })

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

        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 24
            y = 140
        }
        panel = container
        windowManager.addView(container, params)
    }

    private var startX = 0f
    private var startY = 0f
    private var originalX = 0
    private var originalY = 0

    private fun addStat(parent: LinearLayout, initial: String): TextView {
        return TextView(this).apply {
            text = initial
            textSize = 15f
            setTextColor(Color.WHITE)
            setPadding(4, 8, 4, 8)
            parent.addView(this)
        }
    }

    private fun addSlider(parent: LinearLayout, title: String, min: Int, max: Int, initial: Int, changed: (Int) -> Unit) {
        parent.addView(TextView(this).apply {
            text = title
            textSize = 12f
            setTextColor(Color.LTGRAY)
        })
        parent.addView(SeekBar(this).apply {
            this.max = max - min
            progress = initial - min
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    changed(progress + min)
                }
                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
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
