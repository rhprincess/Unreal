package io.twinkle.unreal.ui.activity

import android.content.res.Resources
import android.graphics.Color
import android.view.Window
import com.google.android.material.color.DynamicColors
import io.twinkle.unreal.util.ThemeUtil
import rikka.material.app.MaterialActivity

open class BaseActivity : MaterialActivity() {
    override fun onApplyUserThemeResource(theme: Resources.Theme, isDecorView: Boolean) {
        if (!ThemeUtil.isSystemAccent) {
            theme.applyStyle(ThemeUtil.getColorThemeStyleRes(this), true)
        } else {
            DynamicColors.applyToActivityIfAvailable(this)
        }
        theme.applyStyle(ThemeUtil.getNightThemeStyleRes(this), true)
        theme.applyStyle(
            rikka.material.preference.R.style.ThemeOverlay_Rikka_Material3_Preference,
            true
        )
    }

    override fun computeUserThemeKey(): String {
        return ThemeUtil.colorTheme + ThemeUtil.getNightTheme(this)
    }

    override fun onApplyTranslucentSystemBars() {
        super.onApplyTranslucentSystemBars()
        val window: Window = window
        window.statusBarColor = Color.TRANSPARENT
    }

}