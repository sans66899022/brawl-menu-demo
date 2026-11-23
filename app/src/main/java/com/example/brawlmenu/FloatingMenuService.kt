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
import android.widget.TextView
import android.widget.Toast

class FloatingMenuService : Service() {
    private lateinit var windowManager: WindowManager
    private var panel: View? = null
    private var bubble: View? = null
    private var panelParams: WindowManager.LayoutParams? = null
    private var bubbleParams: WindowManager.LayoutParams? = null

    private val weapons = arrayOf("Stick", "Toy Knife", "Tough Glove", "Ballet Shoes", "Burnt Pan", "Empty Gun", "Worn Dagger")
    private val defenses = arrayOf("Bandage", "Faded Ribbon", "Manly Bandanna", "Old Tutu", "Cloudy Glasses", "Stained Apron", "Temmie Armor")
    private var weaponIndex = 0
    private var defenseIndex = 0
    private lateinit var weaponButton: Button
    private lateinit var defenseButton: Button

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        startAsForeground()
        showPanel()
    }

    private fun startAsForeground() {
        val channelId = "undertale_equipment_demo"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Undertale Equipment Demo", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
        val openApp = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            if (Build.VERSION.SDK_INT >= 23) PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE else PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notification = (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, channelId)
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
        })
            .setContentTitle("Undertale Equipment Demo")
            .setContentText("Floating equipment menu is active")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(openApp)
            .setOngoing(true)
            .build()
        startForeground(1, notification)
    }

    private fun overlayParams(width: Int, height: Int): WindowManager.LayoutParams {
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }
        return WindowManager.LayoutParams(
            width, height, type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = dp(20)
            y = dp(140)
        }
    }

    private fun showPanel() {
        removeBubble()
        if (panel != null) return

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(14), dp(12), dp(14), dp(12))
            setBackgroundColor(Color.rgb(28, 29, 40))
        }

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val title = TextView(this).apply {
            text = "UNDERTALE  •  EQUIPMENT"
            textSize = 15f
            setTextColor(Color.WHITE)
            setPadding(dp(2), dp(8), dp(8), dp(8))
        }
        header.addView(title, LinearLayout.LayoutParams(0, -2, 1f))
        header.addView(Button(this).apply {
            text = "HIDE"
            setOnClickListener { hidePanel() }
        })
        root.addView(header)
        attachDrag(header)

        root.addView(TextView(this).apply {
            text = "Choose demo equipment. Tap the floating U to reopen this menu."
            textSize = 11f
            setTextColor(Color.LTGRAY)
            setPadding(dp(2), dp(4), dp(2), dp(10))
        })

        weaponButton = Button(this).apply {
            text = "WEAPON: ${weapons[weaponIndex]}"
            setOnClickListener {
                weaponIndex = (weaponIndex + 1) % weapons.size
                text = "WEAPON: ${weapons[weaponIndex]}"
                Toast.makeText(this@FloatingMenuService, "Demo weapon: ${weapons[weaponIndex]}", Toast.LENGTH_SHORT).show()
            }
        }
        root.addView(weaponButton, matchWidth())

        defenseButton = Button(this).apply {
            text = "DEFENSE: ${defenses[defenseIndex]}"
            setOnClickListener {
                defenseIndex = (defenseIndex + 1) % defenses.size
                text = "DEFENSE: ${defenses[defenseIndex]}"
                Toast.makeText(this@FloatingMenuService, "Demo defense: ${defenses[defenseIndex]}", Toast.LENGTH_SHORT).show()
            }
        }
        root.addView(defenseButton, matchWidth())

        root.addView(Button(this).apply {
            text = "CLOSE MENU"
            setOnClickListener { stopSelf() }
        }, matchWidth())

        root.addView(TextView(this).apply {
            text = "Standalone demo only — does not change Undertale or its save data."
            textSize = 10f
            setTextColor(Color.LTGRAY)
            setPadding(dp(2), dp(8), dp(2), dp(2))
        })

        panelParams = overlayParams(dp(286), WindowManager.LayoutParams.WRAP_CONTENT)
        panel = root
        windowManager.addView(root, panelParams)
    }

    private fun hidePanel() {
        panel?.let { view ->
            if (view.isAttachedToWindow) windowManager.removeView(view)
        }
        panel = null
        panelParams = null
        showBubble()
    }

    private fun showBubble() {
        if (bubble != null) return
        val chip = TextView(this).apply {
            text = "U"
            textSize = 20f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.rgb(115, 55, 150))
            setOnClickListener { showPanel() }
            contentDescription = "Tap to reopen Undertale equipment menu"
        }
        bubbleParams = overlayParams(dp(52), dp(52))
        bubble = chip
        windowManager.addView(chip, bubbleParams)
    }

    private fun removeBubble() {
        bubble?.let { view ->
            if (view.isAttachedToWindow) windowManager.removeView(view)
        }
        bubble = null
        bubbleParams = null
    }

    private fun attachDrag(view: View) {
        view.setOnTouchListener(object : View.OnTouchListener {
            private var startX = 0
            private var startY = 0
            private var touchX = 0f
            private var touchY = 0f

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                val params = panelParams ?: return false
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        startX = params.x
                        startY = params.y
                        touchX = event.rawX
                        touchY = event.rawY
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        params.x = startX + (event.rawX - touchX).toInt()
                        params.y = startY + (event.rawY - touchY).toInt()
                        panel?.let { windowManager.updateViewLayout(it, params) }
                        return true
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> return true
                }
                return false
            }
        })
    }

    private fun matchWidth() = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.WRAP_CONTENT
    )

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    override fun onDestroy() {
        panel?.let { if (it.isAttachedToWindow) windowManager.removeView(it) }
        removeBubble()
        panel = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}