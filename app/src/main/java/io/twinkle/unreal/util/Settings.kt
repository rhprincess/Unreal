package io.twinkle.unreal.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

object Settings {
    const val TAG = "Settings"
    val APPS_SORT = stringPreferencesKey("apps_sort")
    val HIDE_SYSTEM_APPS = booleanPreferencesKey("hide_system_apps")
    val HIDE_NOT_CAMERA_APPS = booleanPreferencesKey("hide_not_camera_apps")
    val FORCE_SHOW_PERMISSION_LACK = booleanPreferencesKey("force_show_permission_lack")
    val DISABLE_TEMPORARILY = booleanPreferencesKey("disable_temporarily")
    val PLAY_AUDIO = booleanPreferencesKey("play_audio")
    val FORCE_PRIVATE_DIR = booleanPreferencesKey("force_private_dir")
    val DISABLE_TOAST = booleanPreferencesKey("disable_toast")
    val SYSTEM_THEME = booleanPreferencesKey("system_theme")
}

val Context.settings: DataStore<Preferences> by preferencesDataStore(name = Settings.TAG)