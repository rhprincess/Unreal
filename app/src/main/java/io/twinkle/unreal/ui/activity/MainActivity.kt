package io.twinkle.unreal.ui.activity

import android.Manifest
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.view.Window
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.color.DynamicColors
import com.google.android.material.color.MaterialColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.twinkle.unreal.R
import io.twinkle.unreal.databinding.ActivityMainBinding
import io.twinkle.unreal.util.ThemeUtil.colorTheme
import io.twinkle.unreal.util.ThemeUtil.getColorThemeStyleRes
import io.twinkle.unreal.util.ThemeUtil.getNightTheme
import io.twinkle.unreal.util.ThemeUtil.getNightThemeStyleRes
import io.twinkle.unreal.util.ThemeUtil.isSystemAccent
import rikka.material.app.MaterialActivity


class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private val permissions13 = arrayOf(
        Manifest.permission.READ_MEDIA_IMAGES,
        Manifest.permission.READ_MEDIA_AUDIO,
        Manifest.permission.READ_MEDIA_VIDEO
    )
    private val permissions = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    private val multiplePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { map ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissions13.forEach { per ->
                    if (map[per] != true) {
                        Log.d(getString(R.string.app_name), "$per not granted")
                        //未同意授权
                        if (!shouldShowRequestPermissionRationale(per)) {
                            //用户拒绝权限并且系统不再弹出请求权限的弹窗
                            //这时需要我们自己处理，比如自定义弹窗告知用户为何必须要申请这个权限
                            Log.e(
                                getString(R.string.app_name),
                                "$per not granted and should not show rationale"
                            )
                        }
                    }
                }.let {
                    if (!Environment.isExternalStorageManager()) {
                        MaterialAlertDialogBuilder(this)
                            .setTitle("所有文件权限")
                            .setMessage("您需要授予所有文件权限来使用本软件，如果未进行授予，将无法使用本软件功能，是否去授予？")
                            .setNegativeButton("取消") { dialogInterface, i ->
                                dialogInterface.dismiss()
                            }
                            .setPositiveButton("确定") { dialogInterface, i ->
                                val intent = Intent(
                                    Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
                                )
                                intent.setData(Uri.parse("package:$packageName"))
                                startActivity(intent)
                                dialogInterface.dismiss()
                            }
                            .show()
                    }
                }
            } else {
                permissions.forEach { per ->
                    if (map[per] != true) {
                        Log.d(getString(R.string.app_name), "$per not granted")
                        //未同意授权
                        if (!shouldShowRequestPermissionRationale(per)) {
                            //用户拒绝权限并且系统不再弹出请求权限的弹窗
                            //这时需要我们自己处理，比如自定义弹窗告知用户为何必须要申请这个权限
                            Log.e(
                                getString(R.string.app_name),
                                "$per not granted and should not show rationale"
                            )
                        }
                    }
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val navView: BottomNavigationView = binding.navView

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
        val navController = navHostFragment.navController

        navView.setBackgroundColor(
            MaterialColors.getColor(
                navView,
                R.attr.colorSurfaceContainerLow
            )
        )
        NavigationUI.setupWithNavController(navView, navController)

        fetchPermission()
    }

    private fun fetchPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            println(Environment.isExternalStorageManager())
            multiplePermissions.launch(
                arrayOf(
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_AUDIO,
                    Manifest.permission.READ_MEDIA_VIDEO
                )
            )
        } else {
            multiplePermissions.launch(
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            )
        }
    }

    override fun onApplyUserThemeResource(theme: Resources.Theme, isDecorView: Boolean) {
        if (!isSystemAccent) {
            theme.applyStyle(getColorThemeStyleRes(this), true)
        } else {
            DynamicColors.applyToActivityIfAvailable(this)
        }
        theme.applyStyle(getNightThemeStyleRes(this), true)
        theme.applyStyle(
            rikka.material.preference.R.style.ThemeOverlay_Rikka_Material3_Preference,
            true
        )
    }

    override fun computeUserThemeKey(): String {
        return colorTheme + getNightTheme(this)
    }

    override fun onApplyTranslucentSystemBars() {
        super.onApplyTranslucentSystemBars()
        val window: Window = window
        window.statusBarColor = Color.TRANSPARENT
    }
}