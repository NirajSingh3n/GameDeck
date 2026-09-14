package com.gamedeck.app.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Path
import android.view.accessibility.AccessibilityEvent
import androidx.core.content.ContextCompat

/**
 * Accessibility service that performs real screen taps at requested coordinates.
 * This is what powers the L/R "AirTrigger" style shoulder buttons.
 */
class TriggerService : AccessibilityService() {

    private val tapReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val x = intent?.getFloatExtra("x", 0f) ?: 0f
            val y = intent?.getFloatExtra("y", 0f) ?: 0f
            if (x > 0f && y > 0f) dispatchTap(x, y)
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        ContextCompat.registerReceiver(
            this,
            tapReceiver,
            IntentFilter("com.gamedeck.TAP"),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    override fun onUnbind(intent: Intent?): Boolean {
        try { unregisterReceiver(tapReceiver) } catch (e: Exception) {}
        return super.onUnbind(intent)
    }

    private fun dispatchTap(x: Float, y: Float) {
        val path = Path().apply { moveTo(x, y) }
        val stroke = GestureDescription.StrokeDescription(path, 0, 80)
        dispatchGesture(
            GestureDescription.Builder().addStroke(stroke).build(),
            null, null
        )
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}

    override fun onInterrupt() {}
}
