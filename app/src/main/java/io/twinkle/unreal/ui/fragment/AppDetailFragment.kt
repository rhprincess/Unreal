package io.twinkle.unreal.ui.fragment

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.setPadding
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.UriUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.transition.MaterialContainerTransform
import com.google.android.material.transition.MaterialElevationScale
import com.google.android.material.transition.MaterialFadeThrough
import com.google.android.material.transition.MaterialSharedAxis
import io.twinkle.unreal.MyApp
import io.twinkle.unreal.R
import io.twinkle.unreal.databinding.FragmentAppDetailBinding
import io.twinkle.unreal.ui.activity.PlayerActivity
import io.twinkle.unreal.util.CollapseState
import io.twinkle.unreal.util.addStateListener
import io.twinkle.unreal.vcampApp
import java.io.File

class AppDetailFragment : BaseFragment() {

    private var preferences: SharedPreferences? = null
    private var _binding: FragmentAppDetailBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private var packageName = ""
    private lateinit var packageInfo: PackageInfo
    private var isCameraApp = false

    private val selectVideoLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) {
            it?.let { uri ->
                val filePath = UriUtils.uri2File(uri).absolutePath
                runAsync {
                    vcampApp.getFirstFrame(
                        filePath,
                        dest = MyApp.UNREAL_DIR + "apps/" + packageName + "/unreal.mp4"
                    ).let {
                        if (_binding != null) {
                            runOnUiThread { binding.firstFrame.setImageBitmap(it) }
                        }
                    }
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferences = MyApp.getPreferences()

        sharedElementEnterTransition = MaterialContainerTransform()

        arguments?.let {
            packageName = it.getString("appPackageName") ?: ""
            if (packageName.isEmpty()) {
                if (!safeNavigate(R.id.action_app_detail_fragment_to_apps_fragment)) {
                    safeNavigate(R.id.app_detail_nav);
                }
            }
            packageInfo =
                vcampApp.packageManager.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS)
            isCameraApp =
                if (packageInfo.requestedPermissions != null) packageInfo.requestedPermissions.contains(
                    Manifest.permission.CAMERA
                ) else false
            val unrealDirectory =
                File(MyApp.UNREAL_DIR + "apps/" + packageName)
            if (!unrealDirectory.exists()) {
                unrealDirectory.mkdirs()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAppDetailBinding.inflate(inflater, container, false)
        val root: View = binding.root
        binding.toolbarLayout.title = getString(R.string.app_config)
        val appBar = binding.appBar
        if (preferences?.getBoolean("show_appbar_scrim", true) == true) {
            appBar.setLiftable(true)
            appBar.addStateListener { state, _ ->
                appBar.setLifted(state == CollapseState.COLLAPSED)
            }
        } else {
            appBar.setLiftable(false)
        }
        val appItem = binding.appItemContent
        val mainSwitchBar = binding.mainSwitchBar
        if (!isCameraApp) {
            binding.appItemContainer.setPadding(0)
            val warningBar = binding.warningBar
            warningBar.visibility = View.VISIBLE
            warningBar.message =
                "此应用为非相机应用，您接下来的操作可能都将无法生效，请谨慎启用！"
        }

        appItem.itemRoot.setOnClickListener {
            AppUtils.launchAppDetailsSettings(packageName)
        }

        binding.selectGlobalVideo.setOnClickListener {
            if (isCameraApp) {
                selectVideoLauncher.launch("video/*")
            } else {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("非相机应用")
                    .setMessage("此应用为非相机应用，选择视频后虚拟摄像头可能会没有任何效果，您确定要继续吗？")
                    .setCancelable(false)
                    .setNegativeButton("取消") { dialogInterface, i ->
                        dialogInterface.dismiss()
                    }
                    .setPositiveButton("确定") { dialogInterface, i ->
                        selectVideoLauncher.launch("video/*")
                        dialogInterface.dismiss()
                    }
                    .show()
            }
        }

        binding.expandButton.setOnClickListener {
            val unrealFile =
                File(MyApp.UNREAL_DIR + "apps/" + packageName + "/unreal.mp4")
            val intent = Intent()
            intent.setClass(requireContext(), PlayerActivity::class.java)
            intent.putExtra(
                "title",
                packageInfo.applicationInfo.loadLabel(requireContext().packageManager)
                    .toString() + "的视频"
            )
            intent.putExtra("video", unrealFile.absolutePath)
            requireActivity().startActivity(intent)
        }

        runAsync {
            val unrealFile =
                File(MyApp.UNREAL_DIR + "apps/" + packageName + "/unreal.mp4")
            if (unrealFile.exists()) {
                vcampApp.getFirstFrame(unrealFile.path).let {
                    if (_binding != null)
                        runOnUiThread { binding.firstFrame.setImageBitmap(it) }
                }
            }
        }

        Glide.with(appItem.appIcon).load(packageInfo)
            .into(object : CustomTarget<Drawable?>() {
                override fun onLoadCleared(placeholder: Drawable?) {}

                override fun onLoadFailed(errorDrawable: Drawable?) {
                    appItem.appIcon.setImageDrawable(requireContext().packageManager.defaultActivityIcon)
                }

                override fun onResourceReady(
                    resource: Drawable,
                    transition: Transition<in Drawable?>?
                ) {
                    appItem.appIcon.setImageDrawable(resource)
                }
            })

        appItem.appNameText.text =
            packageInfo.applicationInfo.loadLabel(requireContext().packageManager)
                .toString()
        appItem.packageNameText.text = packageInfo.applicationInfo.packageName
        appItem.cameraAppIcon.visibility = if (isCameraApp) View.VISIBLE else View.GONE
        appItem.versionText.text = requireContext().getString(
            R.string.version_template,
            packageInfo.versionName.toString(),
            packageInfo.longVersionCode.toString()
        )

        mainSwitchBar.title = getString(R.string.enabled)

        val enableFile =
            File(MyApp.UNREAL_DIR + "apps/" + packageName + "/enabled")
        val appConfigs = MyApp.getAppConfigs()
        mainSwitchBar.isChecked = enableFile.exists()
        mainSwitchBar.addOnSwitchChangeListener { _, isChecked ->
            if (isChecked) {
                if (!isCameraApp) {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle("非相机应用")
                        .setMessage("此应用为非相机应用，启用Unreal的虚拟摄像头可能会没有任何效果，您确定要继续吗？")
                        .setCancelable(false)
                        .setNegativeButton("取消") { dialogInterface, i ->
                            mainSwitchBar.isChecked = false
                            dialogInterface.dismiss()
                            MyApp.writeAppConfigs(packageName, appConfigs) {
                                it.copy(status = false)
                            }
                        }
                        .setPositiveButton("确定") { dialogInterface, i ->
                            if (!enableFile.exists()) {
                                enableFile.createNewFile()
                                MyApp.writeAppConfigs(packageName, appConfigs) {
                                    it.copy(status = true)
                                }
                            }
                            dialogInterface.dismiss()
                        }
                        .show()
                } else {
                    if (!enableFile.exists()) {
                        enableFile.createNewFile()
                        MyApp.writeAppConfigs(packageName, appConfigs) {
                            it.copy(status = true)
                        }
                    }
                }
            } else {
                if (enableFile.exists()) {
                    enableFile.delete()
                    MyApp.writeAppConfigs(packageName, appConfigs) {
                        it.copy(status = false)
                    }
                }
            }
        }

        binding.toolbar.setNavigationOnClickListener {
            navigateUp()
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}