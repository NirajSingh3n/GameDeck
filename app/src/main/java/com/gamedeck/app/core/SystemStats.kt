package com.gamedeck.app.core

import android.app.ActivityManager
import android.content.Context
import android.os.BatteryManager
import android.os.Environment
import android.os.StatFs
import java.io.File

object SystemStats {

    fun cpuTempC(): Float? {
        val dir = File("/sys/class/thermal")
        val zones = dir.listFiles() ?: return null
        for (z in zones) {
            if (!z.name.startsWith("thermal_zone")) continue
            try {
                val type = File(z, "type").readText().trim().lowercase()
                if (type.contains("cpu") || type.contains("soc") ||
                    type.contains("tsens") || type.contains("board") ||
                    type.contains("battery")
                ) {
                    val t = File(z, "temp").readText().trim().toFloatOrNull() ?: continue
                    return if (t > 1000) t / 1000f else t
                }
            } catch (e: Exception) {
                // ignore unreadable zones
            }
        }
        return null
    }

    fun ram(context: Context): Pair<Long, Long> {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val mi = ActivityManager.MemoryInfo()
        am.getMemoryInfo(mi)
        return mi.availMem to mi.totalMem
    }

    fun batteryPct(context: Context): Int {
        val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        return bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
    }

    fun storage(): Pair<Long, Long> {
        val s = StatFs(Environment.getDataDirectory().path)
        val b = s.blockSizeLong
        return (s.availableBlocksLong * b) to (s.blockCountLong * b)
    }
}
