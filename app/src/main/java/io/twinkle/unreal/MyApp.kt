package io.twinkle.unreal

import android.app.Application
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.os.Environment
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.asLiveData
import androidx.preference.PreferenceManager
import com.blankj.utilcode.util.FileUtils
import com.google.android.material.color.DynamicColors
import com.google.android.material.color.DynamicColorsOptions
import io.twinkle.unreal.data.AppUnrealConfig
import io.twinkle.unreal.data.EnableAppInfo
import io.twinkle.unreal.util.Settings
import io.twinkle.unreal.util.ThemeUtil
import io.twinkle.unreal.util.settings
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import java.io.File
import java.io.IOException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


lateinit var vcampApp: MyApp

class MyApp : Application() {

    private var pref: SharedPreferences? = null

    override fun onCreate() {
        super.onCreate()
        vcampApp = this
        pref = PreferenceManager.getDefaultSharedPreferences(this)
        try {
            val noMedia = File("$UNREAL_DIR.nomedia")
            if (!noMedia.exists()) noMedia.createNewFile()
        } catch (_: IOException) {
        }
        AppCompatDelegate.setDefaultNightMode(ThemeUtil.darkTheme)
    }

    companion object {
        private val executorService: ExecutorService = Executors.newCachedThreadPool()
        private val MainHandler: Handler = Handler(Looper.getMainLooper())
        val UNREAL_DIR =
            Environment.getExternalStorageDirectory().path + "/Android/data/${BuildConfig.APPLICATION_ID}/files/"

        fun getPreferences(): SharedPreferences? {
            return vcampApp.pref
        }

        fun getExecutorService(): ExecutorService {
            return executorService
        }

        fun getMainHandler(): Handler {
            return MainHandler
        }

        fun getAppConfigs(): LinkedHashMap<String, AppUnrealConfig> {
            return getAppConfigs(File(UNREAL_DIR + "configs.json"))
        }

        fun getAppConfigs(configFile: File): LinkedHashMap<String, AppUnrealConfig> {
            return if (configFile.exists() && configFile.readText()
                    .isNotEmpty()
            ) Json.decodeFromString(configFile.readText()) else linkedMapOf()
        }

        @OptIn(DelicateCoroutinesApi::class)
        fun writeAppConfigs(
            packageName: String,
            appConfigs: LinkedHashMap<String, AppUnrealConfig>,
            config: (AppUnrealConfig) -> AppUnrealConfig
        ) {
            appConfigs[packageName] =
                config(appConfigs[packageName] ?: AppUnrealConfig())
            val json = Json {
                prettyPrint = true
                ignoreUnknownKeys = true
            }
            GlobalScope.launch {
                val jsonFile =
                    File(UNREAL_DIR + "configs.json")
                val jsonStr = json.encodeToString(
                    serializer<LinkedHashMap<String, AppUnrealConfig>>(),
                    appConfigs
                )
                if (jsonFile.exists()) {
                    jsonFile.writeText(jsonStr)
                } else {
                    jsonFile.createNewFile()
                    jsonFile.writeText(jsonStr)
                }
            }
        }
    }

    fun getFirstFrame(
        path: String = UNREAL_DIR + "unreal.mp4",
        dest: String = UNREAL_DIR + "unreal.mp4"
    ): Bitmap {
        val media = MediaMetadataRetriever()
        media.setDataSource(path)
        val bitmap = media.getFrameAtTime(1, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
        return if (bitmap != null) {
            FileUtils.copy(path, dest)
            bitmap
        } else {
            Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        }
    }

    fun getEnableAppsInfo(): List<EnableAppInfo> {
        val enableApps = arrayListOf<EnableAppInfo>()
        getAppConfigs().forEach { (key, value) ->
            if (value.status) {
                val info =
                    vcampApp.packageManager.getPackageInfo(key, PackageManager.GET_PERMISSIONS)
                val path = UNREAL_DIR + "apps/" + key + "/unreal.mp4"
                val unrealFile = File(path)
                val bitmap: Bitmap? = if (unrealFile.exists()) {
                    val media = MediaMetadataRetriever()
                    media.setDataSource(path)
                    media.getFrameAtTime(1, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                } else {
                    null
                }
                enableApps.add(
                    EnableAppInfo(
                        info = info,
                        firstFrame = bitmap ?: Bitmap.createBitmap(
                            100,
                            100,
                            Bitmap.Config.ARGB_8888
                        )
                    )
                )
            }
        }
        return enableApps
    }
}