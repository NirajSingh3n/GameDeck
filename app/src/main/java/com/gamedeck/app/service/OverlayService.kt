package com.gamedeck.app.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Choreographer
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.gamedeck.app.data.ProfileStore

/**
 * Game Genie style floating overlay:
 * - live FPS meter bubble (Choreographer)
 * - expandable control panel (DND, brightness, record, launch PUBG, triggers)
 * - L/R shoulder trigger buttons that dispatch real taps via TriggerService
 */
class OverlayService : Service() {

    private lateinit var wm: WindowManager
    private lateinit var store: ProfileStore

    private var fpsText: TextView? = null
    private var panel: LinearLayout? = null
    private var leftTrigger: View? = null
    private var rightTrigger: View? = null

    private var frames = 0
    private var lastFpsTime = 0L

    private val density: Float get() = resources.displayMetrics.density

    private val frameCallback = object : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            frames++
            if (frameTimeNanos - lastFpsTime >= 1_000_000_000L) {
                fpsText?.text = "⚡ $frames FPS"
                frames = 0
                lastFpsTime = frameTimeNanos
            }
            Choreographer.getInstance().postFrameCallback(this)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        store = ProfileStore(this)
        wm = getSystemService(WINDOW_SERVICE) as WindowManager
        startFg()
        addBubble()
        if (store.triggersEnabled) addTriggers()
        Choreographer.getInstance().postFrameCallback(frameCallback)
    }

    private fun startFg() {
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(
            NotificationChannel("overlay", "GameDeck Overlay", NotificationManager.IMPORTANCE_LOW)
        )
        val n = NotificationCompat.Builder(this, "overlay")
            .setContentTitle("GameDeck Turbo active")
            .setContentText("Tap the bubble for the Game Genie panel")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .build()
        if (Build.VERSION.SDK_INT >= 29) {
            startForeground(1, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            startForeground(1, n)
        }
    }

    private fun overlayParams(w: Int, h: Int, focusable: Boolean): WindowManager.LayoutParams {
        val type = if (Build.VERSION.SDK_INT >= 26)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else
            WindowManager.LayoutParams.TYPE_PHONE
        return WindowManager.LayoutParams(
            w, h, type,
            if (focusable) 0 else WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
    }

    private fun addBubble() {
        val size = (56 * density).toInt()
        val tv = TextView(this).apply {
            text = "⚡ 0 FPS"
            setTextColor(Color.WHITE)
            textSize = 12f
            gravity = Gravity.CENTER
            background = roundRect(0xE6131722.toInt(), 28f)
            setOnTouchListener(dragListener())
        }
        val lp = overlayParams(size, size, focusable = false).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 40
            y = 140
        }
        fpsText = tv
        wm.addView(tv, lp)
    }

    private fun dragListener() = object : View.OnTouchListener {
        private var dx = 0f
        private var dy = 0f
        private var moved = false
        override fun onTouch(v: View, e: MotionEvent): Boolean {
            when (e.action) {
                MotionEvent.ACTION_DOWN -> {
                    dx = v.x - e.rawX
                    dy = v.y - e.rawY
                    moved = false
                }
                MotionEvent.ACTION_MOVE -> {
                    val nx = e.rawX + dx
                    val ny = e.rawY + dy
                    if (kotlin.math.abs(nx - v.x) > 8 || kotlin.math.abs(ny - v.y) > 8) moved = true
                    v.x = nx
                    v.y = ny
                }
                MotionEvent.ACTION_UP -> if (!moved) togglePanel()
            }
            return true
        }
    }

    private fun togglePanel() {
        if (panel != null) {
            try { wm.removeView(panel) } catch (e: Exception) {}
            panel = null
            return
        }
        val pad = (12 * density).toInt()
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(pad, pad, pad, pad)
            background = roundRect(0xF20B0E14.toInt(), 16f)
        }
        layout.addView(TextView(this).apply {
            text = "🎮 GameDeck Turbo"
            setTextColor(0xFF00E5FF.toInt())
            textSize = 16f
        }, matchWrap())

        layout.addView(TextView(this).apply {
            text = "Brightness"
            setTextColor(Color.WHITE)
        }, matchWrap())
        layout.addView(SeekBar(this).apply {
            max = 255
            progress = try {
                Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS)
            } catch (e: Exception) { 128 }
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(s: SeekBar?, p: Int, fromUser: Boolean) {
                    if (fromUser && Settings.System.canWrite(this@OverlayService)) {
                        try {
                            Settings.System.putInt(
                                contentResolver,
                                Settings.System.SCREEN_BRIGHTNESS, p
                            )
                        } catch (e: Exception) {}
                    }
                }
                override fun onStartTrackingTouch(s: SeekBar?) {}
                override fun onStopTrackingTouch(s: SeekBar?) {}
            })
        }, matchWrap())

        fun btn(label: String, onClick: (View) -> Unit) {
            val b = Button(this)
            b.text = label
            b.setOnClickListener(onClick)
            layout.addView(b, matchWrap())
        }

        btn(if (isDnd()) "🔕 Do Not Disturb: ON" else "🔔 Do Not Disturb: OFF") {
            toggleDnd()
            refreshPanel()
        }
        btn(if (store.triggersEnabled) "🎯 Shoulder Triggers: ON" else "🎯 Shoulder Triggers: OFF") {
            store.triggersEnabled = !store.triggersEnabled
            if (store.triggersEnabled) addTriggers() else removeTriggers()
            refreshPanel()
        }
        btn("⏺ Screen Record") {
            sendBroadcast(Intent("com.gamedeck.START_RECORDING"))
            refreshPanel()
        }
        btn("🚀 Launch PUBG") {
            launchPUBG()
            refreshPanel()
        }
        btn("✖ Close Panel") { togglePanel() }
        btn("⏻ Stop Overlay") { stopSelf() }

        val w = (300 * density).toInt()
        val lp = overlayParams(w, WindowManager.LayoutParams.WRAP_CONTENT, focusable = true).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 60
            y = 240
        }
        panel = layout
        try { wm.addView(layout, lp) } catch (e: Exception) { panel = null }
    }

    private fun refreshPanel() {
        if (panel != null) {
            togglePanel()
            togglePanel()
        }
    }

    private fun addTriggers() {
        if (leftTrigger != null) return
        val size = (64 * density).toInt()
        val top = (110 * density).toInt()
        val margin = (18 * density).toInt()

        leftTrigger = createTrigger("L", 0xE600E5FF.toInt()).apply {
            setOnClickListener { sendTap(store.triggerLeftX, store.triggerLeftY) }
        }
        rightTrigger = createTrigger("R", 0xE6FF3D5A.toInt()).apply {
            setOnClickListener { sendTap(store.triggerRightX, store.triggerRightY) }
        }
        wm.addView(leftTrigger, overlayParams(size, size, focusable = false).apply {
            gravity = Gravity.TOP or Gravity.START
            x = margin
            y = top
        })
        wm.addView(rightTrigger, overlayParams(size, size, focusable = false).apply {
            gravity = Gravity.TOP or Gravity.END
            x = margin
            y = top
        })
    }

    private fun removeTriggers() {
        try { leftTrigger?.let { wm.removeView(it) } } catch (e: Exception) {}
        try { rightTrigger?.let { wm.removeView(it) } } catch (e: Exception) {}
        leftTrigger = null
        rightTrigger = null
    }

    private fun createTrigger(text: String, color: Int): TextView =
        TextView(this).apply {
            this.text = text
            textSize = 14f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            background = roundRect(color, 32f)
        }

    private fun sendTap(xf: Float, yf: Float) {
        val m = resources.displayMetrics
        sendBroadcast(Intent("com.gamedeck.TAP").apply {
            putExtra("x", xf * m.widthPixels)
            putExtra("y", yf * m.heightPixels)
        })
    }

    private fun isDnd(): Boolean {
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        return nm.currentInterruptionFilter == NotificationManager.INTERRUPTION_FILTER_NONE
    }

    private fun toggleDnd() {
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (!nm.isNotificationPolicyAccessGranted) {
            toast("Grant Do Not Disturb access first (Settings tab)")
            return
        }
        nm.setInterruptionFilter(
            if (isDnd()) NotificationManager.INTERRUPTION_FILTER_ALL
            else NotificationManager.INTERRUPTION_FILTER_NONE
        )
    }

    private fun launchPUBG() {
        val pkgs = listOf(
            "com.pubg.imobile", "com.tencent.ig", "com.pubg.krmobile",
            "com.vng.pubgmobile", "com.pubgmobile"
        )
        for (p in pkgs) {
            val i = packageManager.getLaunchIntentForPackage(p)
            if (i != null) {
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(i)
                return
            }
        }
        toast("PUBG not installed")
    }

    private fun roundRect(color: Int, radiusDp: Float): GradientDrawable =
        GradientDrawable().apply {
            setColor(color)
            cornerRadius = radiusDp * density
        }

    private fun matchWrap(): LinearLayout.LayoutParams =
        LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

    private fun toast(s: String) = Toast.makeText(this, s, Toast.LENGTH_SHORT).show()

    override fun onDestroy() {
        Choreographer.getInstance().removeFrameCallback(frameCallback)
        try { fpsText?.let { wm.removeView(it) } } catch (e: Exception) {}
        try { panel?.let { wm.removeView(it) } } catch (e: Exception) {}
        removeTriggers()
        super.onDestroy()
    }

    companion object {
        fun start(ctx: Context) {
            ContextCompat.startForegroundService(ctx, Intent(ctx, OverlayService::class.java))
        }
    }
}
