package com.gamedeck.app.core

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable

data class GameInfo(
    val name: String,
    val packageName: String,
    val icon: Drawable
)

object GameDetector {

    private val knownGames = setOf(
        "com.pubg.imobile",       // BGMI
        "com.tencent.ig",         // PUBG Mobile Global
        "com.pubg.krmobile",      // PUBG KR
        "com.vng.pubgmobile",     // PUBG VN
        "com.rekoo.pubgm",        // PUBG TW
        "com.pubgmobile",         // generic
        "com.activision.callofduty.shooter",
        "com.mobile.legends",
        "com.garena.free.fire",
        "com.garena.game.kgvn",
        "com.supercell.clashroyale",
        "com.supercell.clashofclans",
        "com.supercell.brawlstars",
        "com.ea.gp.fifamobile",
        "com.ea.games.r3_row",
        "com.dts.freefireth",
        "com.pearlabyss.blackdesertm",
        "com.miHoYo.GenshinImpact",
        "com.tencent.mobileqq"
    )

    fun detect(pm: PackageManager): List<GameInfo> {
        val out = mutableListOf<GameInfo>()
        val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        for (a in apps) {
            if (a.packageName == "com.gamedeck.app") continue
            val isGame = (a.category == ApplicationInfo.CATEGORY_GAME) ||
                    knownGames.contains(a.packageName)
            if (!isGame) continue
            try {
                val name = pm.getApplicationLabel(a).toString()
                val icon = pm.getApplicationIcon(a)
                out.add(GameInfo(name, a.packageName, icon))
            } catch (e: Exception) {
                // skip broken entries
            }
        }
        return out.sortedBy { it.name.lowercase() }
    }
}
