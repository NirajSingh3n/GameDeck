package com.gamedeck.app.data

import android.content.Context

class ProfileStore(context: Context) {

    private val sp = context.applicationContext
        .getSharedPreferences("gamedeck_profile", Context.MODE_PRIVATE)

    var performanceMode: String
        get() = sp.getString("mode", "Balanced") ?: "Balanced"
        set(v) { sp.edit().putString("mode", v).apply() }

    var touchSensitivity: Int
        get() = sp.getInt("touch", 60)
        set(v) { sp.edit().putInt("touch", v).apply() }

    var aimSensitivity: Int
        get() = sp.getInt("aim", 50)
        set(v) { sp.edit().putInt("aim", v).apply() }

    var gyroSensitivity: Int
        get() = sp.getInt("gyro", 40)
        set(v) { sp.edit().putInt("gyro", v).apply() }

    var dndOnLaunch: Boolean
        get() = sp.getBoolean("dnd", true)
        set(v) { sp.edit().putBoolean("dnd", v).apply() }

    var overlayOnLaunch: Boolean
        get() = sp.getBoolean("overlay", true)
        set(v) { sp.edit().putBoolean("overlay", v).apply() }

    var triggersEnabled: Boolean
        get() = sp.getBoolean("triggers", false)
        set(v) { sp.edit().putBoolean("triggers", v).apply() }

    var voiceChanger: Boolean
        get() = sp.getBoolean("voice", false)
        set(v) { sp.edit().putBoolean("voice", v).apply() }

    var networkBoost: Boolean
        get() = sp.getBoolean("net", false)
        set(v) { sp.edit().putBoolean("net", v).apply() }

    var triggerLeftX: Float
        get() = sp.getFloat("tlx", 0.15f)
        set(v) { sp.edit().putFloat("tlx", v).apply() }

    var triggerLeftY: Float
        get() = sp.getFloat("tly", 0.45f)
        set(v) { sp.edit().putFloat("tly", v).apply() }

    var triggerRightX: Float
        get() = sp.getFloat("trx", 0.85f)
        set(v) { sp.edit().putFloat("trx", v).apply() }

    var triggerRightY: Float
        get() = sp.getFloat("try", 0.45f)
        set(v) { sp.edit().putFloat("try", v).apply() }
}
