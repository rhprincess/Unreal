package io.twinkle.unreal.util

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.StyleRes
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.color.DynamicColors
import io.twinkle.unreal.MyApp
import io.twinkle.unreal.R
import rikka.core.util.ResourceUtils

object ThemeUtil {
    private val colorThemeMapLight: MutableMap<String?, Int> = HashMap()
    private val colorThemeMapDark: MutableMap<String?, Int> = HashMap()
    private var preferences: SharedPreferences? = null
    const val MODE_NIGHT_FOLLOW_SYSTEM = "MODE_NIGHT_FOLLOW_SYSTEM"
    const val MODE_NIGHT_NO = "MODE_NIGHT_NO"
    const val MODE_NIGHT_YES = "MODE_NIGHT_YES"

    init {
        preferences = MyApp.getPreferences()
        colorThemeMapLight["DEFAULT"] = R.style.AppTheme
        colorThemeMapLight["SAKURA"] = R.style.ThemeOverlay_Light_MaterialSakura
        colorThemeMapLight["MATERIAL_RED"] = R.style.ThemeOverlay_Light_MaterialRed
        colorThemeMapLight["MATERIAL_PINK"] = R.style.ThemeOverlay_Light_MaterialPink
        colorThemeMapLight["MATERIAL_PURPLE"] = R.style.ThemeOverlay_Light_MaterialPurple
        colorThemeMapLight["MATERIAL_DEEP_PURPLE"] = R.style.ThemeOverlay_Light_MaterialDeepPurple
        colorThemeMapLight["MATERIAL_INDIGO"] = R.style.ThemeOverlay_Light_MaterialIndigo
        colorThemeMapLight["MATERIAL_BLUE"] = R.style.ThemeOverlay_Light_MaterialBlue
        colorThemeMapLight["MATERIAL_LIGHT_BLUE"] = R.style.ThemeOverlay_Light_MaterialLightBlue
        colorThemeMapLight["MATERIAL_CYAN"] = R.style.ThemeOverlay_Light_MaterialCyan
        colorThemeMapLight["MATERIAL_TEAL"] = R.style.ThemeOverlay_Light_MaterialTeal
        colorThemeMapLight["MATERIAL_GREEN"] = R.style.ThemeOverlay_Light_MaterialGreen
        colorThemeMapLight["MATERIAL_LIGHT_GREEN"] = R.style.ThemeOverlay_Light_MaterialLightGreen
        colorThemeMapLight["MATERIAL_LIME"] = R.style.ThemeOverlay_Light_MaterialLime
        colorThemeMapLight["MATERIAL_YELLOW"] = R.style.ThemeOverlay_Light_MaterialYellow
        colorThemeMapLight["MATERIAL_AMBER"] = R.style.ThemeOverlay_Light_MaterialAmber
        colorThemeMapLight["MATERIAL_ORANGE"] = R.style.ThemeOverlay_Light_MaterialOrange
        colorThemeMapLight["MATERIAL_DEEP_ORANGE"] = R.style.ThemeOverlay_Light_MaterialDeepOrange
        colorThemeMapLight["MATERIAL_BROWN"] = R.style.ThemeOverlay_Light_MaterialBrown
        colorThemeMapLight["MATERIAL_BLUE_GREY"] = R.style.ThemeOverlay_Light_MaterialBlueGrey

        colorThemeMapDark["DEFAULT"] = R.style.AppTheme
        colorThemeMapDark["SAKURA"] = R.style.ThemeOverlay_Dark_MaterialSakura
        colorThemeMapDark["MATERIAL_RED"] = R.style.ThemeOverlay_Dark_MaterialRed
        colorThemeMapDark["MATERIAL_PINK"] = R.style.ThemeOverlay_Dark_MaterialPink
        colorThemeMapDark["MATERIAL_PURPLE"] = R.style.ThemeOverlay_Dark_MaterialPurple
        colorThemeMapDark["MATERIAL_DEEP_PURPLE"] = R.style.ThemeOverlay_Dark_MaterialDeepPurple
        colorThemeMapDark["MATERIAL_INDIGO"] = R.style.ThemeOverlay_Dark_MaterialIndigo
        colorThemeMapDark["MATERIAL_BLUE"] = R.style.ThemeOverlay_Dark_MaterialBlue
        colorThemeMapDark["MATERIAL_LIGHT_BLUE"] = R.style.ThemeOverlay_Dark_MaterialLightBlue
        colorThemeMapDark["MATERIAL_CYAN"] = R.style.ThemeOverlay_Dark_MaterialCyan
        colorThemeMapDark["MATERIAL_TEAL"] = R.style.ThemeOverlay_Dark_MaterialTeal
        colorThemeMapDark["MATERIAL_GREEN"] = R.style.ThemeOverlay_Dark_MaterialGreen
        colorThemeMapDark["MATERIAL_LIGHT_GREEN"] = R.style.ThemeOverlay_Dark_MaterialLightGreen
        colorThemeMapDark["MATERIAL_LIME"] = R.style.ThemeOverlay_Dark_MaterialLime
        colorThemeMapDark["MATERIAL_YELLOW"] = R.style.ThemeOverlay_Dark_MaterialYellow
        colorThemeMapDark["MATERIAL_AMBER"] = R.style.ThemeOverlay_Dark_MaterialAmber
        colorThemeMapDark["MATERIAL_ORANGE"] = R.style.ThemeOverlay_Dark_MaterialOrange
        colorThemeMapDark["MATERIAL_DEEP_ORANGE"] = R.style.ThemeOverlay_Dark_MaterialDeepOrange
        colorThemeMapDark["MATERIAL_BROWN"] = R.style.ThemeOverlay_Dark_MaterialBrown
        colorThemeMapDark["MATERIAL_BLUE_GREY"] = R.style.ThemeOverlay_Dark_MaterialBlueGrey
    }

    private const val THEME_DEFAULT = "DEFAULT"
    private const val THEME_BLACK = "BLACK"
    private val isBlackNightTheme: Boolean
        get() = preferences!!.getBoolean("black_dark_theme", false)
    val isSystemAccent: Boolean
        get() = DynamicColors.isDynamicColorAvailable() && preferences!!.getBoolean(
            "follow_system_accent",
            true
        )

    fun getNightTheme(context: Context): String {
        return if (isBlackNightTheme
            && ResourceUtils.isNightMode(context.resources.configuration)
        ) THEME_BLACK else THEME_DEFAULT
    }

    @StyleRes
    fun getNightThemeStyleRes(context: Context): Int {
        return when (getNightTheme(context)) {
            THEME_BLACK -> R.style.ThemeOverlay_Black
            THEME_DEFAULT -> R.style.ThemeOverlay
            else -> R.style.ThemeOverlay
        }
    }

    val colorTheme: String?
        get() = if (isSystemAccent) {
            "SYSTEM"
        } else preferences!!.getString("theme_color", "DEFAULT")

    @StyleRes
    fun getColorThemeStyleRes(context: Context): Int {
        return if (ResourceUtils.isNightMode(context.resources.configuration)) {
            colorThemeMapDark[colorTheme]
                ?: R.style.ThemeOverlay_Dark_MaterialSakura
        } else {
            colorThemeMapLight[colorTheme]
                ?: R.style.ThemeOverlay_Light_MaterialSakura
        }
    }

    fun getDarkTheme(mode: String?): Int {
        return when (mode) {
            MODE_NIGHT_FOLLOW_SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            MODE_NIGHT_YES -> AppCompatDelegate.MODE_NIGHT_YES
            MODE_NIGHT_NO -> AppCompatDelegate.MODE_NIGHT_NO
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
    }

    val darkTheme: Int
        get() = getDarkTheme(preferences!!.getString("dark_theme", MODE_NIGHT_FOLLOW_SYSTEM))
}