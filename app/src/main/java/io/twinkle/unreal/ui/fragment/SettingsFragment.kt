package io.twinkle.unreal.ui.fragment

import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.lifecycleScope
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.color.DynamicColors
import io.twinkle.unreal.MyApp
import io.twinkle.unreal.R
import io.twinkle.unreal.ui.activity.MainActivity
import io.twinkle.unreal.util.Settings
import io.twinkle.unreal.util.ThemeUtil
import io.twinkle.unreal.util.ThemeUtil.getDarkTheme
import io.twinkle.unreal.util.settings
import io.twinkle.unreal.vcampApp
import kotlinx.coroutines.launch
import rikka.core.util.ResourceUtils
import rikka.material.preference.MaterialSwitchPreference
import java.io.File


class SettingsFragment : PreferenceFragmentCompat() {

    private val disableFile =
        File(MyApp.UNREAL_DIR + "disable.jpg")
    private val forceShowFile =
        File(MyApp.UNREAL_DIR + "force_show.jpg")
    private val playSoundFile =
        File(MyApp.UNREAL_DIR + "no-silent.jpg")
    private val forcePrivateDirFile =
        File(MyApp.UNREAL_DIR + "private_dir.jpg")
    private val disableToastFile =
        File(MyApp.UNREAL_DIR + "no_toast.jpg")
    private var parentFragment: SettingsHostFragment? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
        parentFragment = getParentFragment() as SettingsHostFragment?
        registerPreferenceListener()
    }

    // 注册监听
    private fun registerPreferenceListener() {
        findPreference<MaterialSwitchPreference>("force_show_permission_lack")?.setOnPreferenceChangeListener { _, newValue ->
            val checked = newValue as Boolean
            parentFragment?.runAsync {
                if (forceShowFile.exists() && !checked) {
                    forceShowFile.delete()
                } else {
                    forceShowFile.createNewFile()
                }
                lifecycleScope.launch {
                    vcampApp.settings.edit {
                        it[Settings.FORCE_SHOW_PERMISSION_LACK] = checked
                    }
                }
            }
            true
        }

        findPreference<MaterialSwitchPreference>("disable_temporarily")?.setOnPreferenceChangeListener { _, newValue ->
            val checked = newValue as Boolean
            parentFragment?.runAsync {
                if (disableFile.exists() && !checked) {
                    disableFile.delete()
                } else {
                    disableFile.createNewFile()
                }
                lifecycleScope.launch {
                    vcampApp.settings.edit {
                        it[Settings.DISABLE_TEMPORARILY] = checked
                    }
                }
            }
            true
        }

        findPreference<MaterialSwitchPreference>("play_audio")?.setOnPreferenceChangeListener { _, newValue ->
            val checked = newValue as Boolean
            parentFragment?.runAsync {
                if (playSoundFile.exists() && !checked) {
                    playSoundFile.delete()
                } else {
                    playSoundFile.createNewFile()
                }
                lifecycleScope.launch {
                    vcampApp.settings.edit {
                        it[Settings.PLAY_AUDIO] = checked
                    }
                }
            }
            true
        }

        findPreference<MaterialSwitchPreference>("force_private_dir")?.setOnPreferenceChangeListener { _, newValue ->
            val checked = newValue as Boolean
            parentFragment?.runAsync {
                if (forcePrivateDirFile.exists() && !checked) {
                    forcePrivateDirFile.delete()
                } else {
                    forcePrivateDirFile.createNewFile()
                }
                lifecycleScope.launch {
                    vcampApp.settings.edit {
                        it[Settings.FORCE_PRIVATE_DIR] = checked
                    }
                }
            }
            true
        }

        findPreference<MaterialSwitchPreference>("disable_toast")?.setOnPreferenceChangeListener { _, newValue ->
            val checked = newValue as Boolean
            parentFragment?.runAsync {
                if (disableToastFile.exists() && !checked) {
                    disableToastFile.delete()
                } else {
                    disableToastFile.createNewFile()
                }
                lifecycleScope.launch {
                    vcampApp.settings.edit {
                        it[Settings.DISABLE_TOAST] = checked
                    }
                }
            }
            true
        }

        findPreference<Preference>("dark_theme")?.setOnPreferenceChangeListener { _: Preference?, newValue: Any? ->
            if (MyApp.getPreferences()
                    ?.getString("dark_theme", ThemeUtil.MODE_NIGHT_FOLLOW_SYSTEM)
                    ?.equals(newValue) != true
            ) {
                AppCompatDelegate.setDefaultNightMode(getDarkTheme(newValue as String?))
            }
            true
        }

        findPreference<Preference>("black_dark_theme")?.setOnPreferenceChangeListener { _: Preference?, _: Any? ->
            val activity =
                activity as MainActivity?
            if (activity != null && ResourceUtils.isNightMode(resources.configuration)) {
                activity.recreate()
            }
            true
        }

        val primary_color = findPreference<Preference>("theme_color")
        if (primary_color != null) {
            primary_color.onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { _: Preference?, _: Any? ->
                    val activity =
                        activity as MainActivity?
                    activity?.recreate()
                    true
                }
        }

        // Theme
        val prefFollowSystemAccent =
            findPreference<MaterialSwitchPreference>("follow_system_accent")
        if (prefFollowSystemAccent != null && DynamicColors.isDynamicColorAvailable()) {
            if (primary_color != null) {
                primary_color.isVisible = !prefFollowSystemAccent.isChecked
            }
            prefFollowSystemAccent.isVisible = true
            prefFollowSystemAccent.setOnPreferenceChangeListener { _: Preference?, _: Any? ->
                val activity =
                    activity as MainActivity?
                activity?.recreate()
                true
            }
        }

        // UI
        findPreference<MaterialSwitchPreference>("show_appbar_scrim")?.setOnPreferenceChangeListener { _, _ ->
            val activity =
                activity as MainActivity?
            activity?.recreate()
            true
        }
    }

}