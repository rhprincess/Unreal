package io.twinkle.unreal.ui.adapter

import android.annotation.SuppressLint
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.l4digital.fastscroll.FastScroller
import io.twinkle.unreal.MyApp
import io.twinkle.unreal.R
import io.twinkle.unreal.data.AppUnrealConfig
import io.twinkle.unreal.data.EnableMap
import io.twinkle.unreal.databinding.AppItemBinding
import io.twinkle.unreal.ui.fragment.AppsFragment
import io.twinkle.unreal.ui.fragment.AppsFragmentDirections
import io.twinkle.unreal.util.Settings
import io.twinkle.unreal.util.settings
import io.twinkle.unreal.vcampApp
import kotlinx.coroutines.launch
import java.text.Collator
import java.util.Locale
import java.util.Objects

class AppAdapter(
    private val fragment: AppsFragment,
    private var packageInfos: List<PackageInfo>
) :
    RecyclerView.Adapter<AppAdapter.ViewHolder>(), FastScroller.SectionIndexer {

    private var filteredInfos: List<PackageInfo> = listOf()
    var isLoaded = false
    private var sortWay = AppSort.UPDATE_TIME
    private var lastSortWay = sortWay
    private var hideNotCameraApps = true
    private var lastHideNotCameraApps = hideNotCameraApps
    private var hideSystemApps = true
    private var lastHideSystemApps = hideSystemApps
    private var reversed = false

    init {
        filteredInfos = packageInfos
        fragment.lifecycleScope.launch {
            vcampApp.settings.data.collect {
                hideNotCameraApps = it[Settings.HIDE_NOT_CAMERA_APPS] ?: true
                hideSystemApps = it[Settings.HIDE_SYSTEM_APPS] ?: true
                sortWay = AppSort.valueOf(it[Settings.APPS_SORT] ?: AppSort.UPDATE_TIME.name)
                filteredInfos =
                    if (hideSystemApps) filteredInfos.filter { pi -> pi.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM == 0 } else filteredInfos
                filteredInfos = if (hideNotCameraApps) filteredInfos.filter { pi ->
                    if (pi.requestedPermissions != null) pi.requestedPermissions.contains(
                        android.Manifest.permission.CAMERA
                    ) else false
                } else filteredInfos
                sortBy(sortWay)
            }
        }
    }

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    class ViewHolder(val binding: AppItemBinding) : RecyclerView.ViewHolder(binding.root)

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            AppItemBinding.inflate(
                LayoutInflater.from(viewGroup.context),
                viewGroup,
                false
            )
        )
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val binding = viewHolder.binding
        val context = viewHolder.itemView.context
        val pi = filteredInfos[position]
        val map = MyApp.getAppConfigs()
        val isCameraApp =
            if (pi.requestedPermissions != null) pi.requestedPermissions.contains(
                android.Manifest.permission.CAMERA
            ) else false

        binding.appIcon.tag = pi

        if (map[pi.applicationInfo.packageName]?.status == true) {
            binding.enabledText.visibility = View.VISIBLE
        } else {
            binding.enabledText.visibility = View.GONE
        }

        Glide.with(binding.appIcon).load(pi)
            .into(object : CustomTarget<Drawable?>() {
                override fun onLoadCleared(placeholder: Drawable?) {
                    if (binding.appIcon.tag == pi)
                        binding.appIcon.setImageDrawable(context.packageManager.defaultActivityIcon)
                }

                override fun onLoadFailed(errorDrawable: Drawable?) {
                    if (binding.appIcon.tag == pi)
                        binding.appIcon.setImageDrawable(context.packageManager.defaultActivityIcon)
                }

                override fun onResourceReady(
                    resource: Drawable,
                    transition: Transition<in Drawable?>?
                ) {
                    if (binding.appIcon.tag == pi)
                        binding.appIcon.setImageDrawable(resource)
                }
            })

        binding.itemRoot.setOnClickListener {
            fragment.safeNavigate(
                AppsFragmentDirections.actionAppsFragmentToAppDetailFragment(
                    appPackageName = pi.applicationInfo.packageName
                )
            )
        }
        binding.appNameText.text = pi.applicationInfo.loadLabel(context.packageManager)
            .toString()
        binding.packageNameText.text = pi.applicationInfo.packageName
        binding.cameraAppIcon.visibility = if (isCameraApp) View.VISIBLE else View.GONE
        binding.versionText.text = context.getString(
            R.string.version_template,
            pi.versionName.toString(),
            pi.longVersionCode.toString()
        )
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = filteredInfos.size

    override fun getItemId(position: Int): Long {
        return Objects.hash(
            filteredInfos[position],
            position,
            filteredInfos[position].toString(),
            System.currentTimeMillis()
        )
            .toLong()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun sortBy(way: AppSort) {
        lastSortWay = sortWay
        sortWay = way
        isLoaded = false
        fragment.runAsync {
            fragment.lifecycleScope.launch {
                vcampApp.settings.edit {
                    it[Settings.APPS_SORT] = way.name
                }
            }
            val sortedResult = when (way) {
                AppSort.APP_NAME -> filteredInfos.sortedWith(Comparators.byLabel)
                AppSort.INSTALL_TIME -> filteredInfos.sortedWith(Comparators.byInstallTime)
                AppSort.UPDATE_TIME -> filteredInfos.sortedWith(Comparators.byUpdateTime)
                AppSort.PACKAGE_NAME -> filteredInfos.sortedWith(Comparators.byPackageName)
            }
            val appConfigs = MyApp.getAppConfigs()
            filteredInfos = sortedResult.sortedWith(Comparators.byEnabled(appConfigs))
            if (reversed) filteredInfos = filteredInfos.reversed()
            fragment.runOnUiThread {
                isLoaded = true
                if (appConfigs.size > 0) {
                    fragment.setEnableCount(appConfigs.values.count { it.status })
                } else {
                    fragment.setEnableCount(0)
                }
                notifyDataSetChanged()
            }
        }
    }

    fun refresh() {
        // 使用异步进行更新
        fragment.runAsync {
            isLoaded = false
            val apps: List<PackageInfo> = vcampApp.packageManager.getInstalledPackages(
                PackageManager.GET_PERMISSIONS or PackageManager.GET_CONFIGURATIONS
            )
            // 如果无任何数据变化
            if ((lastSortWay == sortWay) and (lastHideSystemApps == hideSystemApps) and (lastHideNotCameraApps == hideNotCameraApps)) {
                if (packageInfos.containsAll(apps)) {
                    isLoaded = true
                    return@runAsync
                }
            }
            packageInfos = apps
            filteredInfos = apps
            filteredInfos =
                if (hideSystemApps) filteredInfos.filter { pi -> pi.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM == 0 } else packageInfos
            filteredInfos = if (hideNotCameraApps) filteredInfos.filter { pi ->
                if (pi.requestedPermissions != null) pi.requestedPermissions.contains(
                    android.Manifest.permission.CAMERA
                ) else false
            } else filteredInfos
            sortBy(sortWay)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun onOptionsItemSelected(item: MenuItem) {
        val itemId = item.itemId
        when (itemId) {
            R.id.refresh_data -> {
                refresh()
            }

            R.id.item_filter_system -> {
                item.setChecked(!item.isChecked)
                lastHideSystemApps = hideSystemApps
                hideSystemApps = item.isChecked
                refresh()
                fragment.lifecycleScope.launch {
                    vcampApp.settings.edit {
                        it[Settings.HIDE_SYSTEM_APPS] = item.isChecked
                    }
                }
            }

            R.id.item_filter_not_camera_apps -> {
                item.setChecked(!item.isChecked)
                lastHideNotCameraApps = hideNotCameraApps
                hideNotCameraApps = item.isChecked
                refresh()
                fragment.lifecycleScope.launch {
                    vcampApp.settings.edit {
                        it[Settings.HIDE_NOT_CAMERA_APPS] = item.isChecked
                    }
                }
            }

            R.id.item_sort_by_name -> {
                item.setChecked(true)
                sortBy(AppSort.APP_NAME)
            }

            R.id.item_sort_by_install_time -> {
                item.setChecked(true)
                sortBy(AppSort.INSTALL_TIME)
            }

            R.id.item_sort_by_package_name -> {
                item.setChecked(true)
                sortBy(AppSort.PACKAGE_NAME)
            }

            R.id.item_sort_by_update_time -> {
                item.setChecked(true)
                sortBy(AppSort.UPDATE_TIME)
            }

            R.id.reverse -> {
                item.setChecked(!item.isChecked)
                reversed = item.isChecked
                fragment.runAsync {
                    isLoaded = false
                    filteredInfos = filteredInfos.reversed()
                    fragment.runOnUiThread {
                        isLoaded = true
                        notifyDataSetChanged()
                    }
                }

            }
        }
    }

    fun onPrepareMenu(menu: Menu) {
        fragment.lifecycleScope.launch {
            vcampApp.settings.data.collect {
                println(AppSort.APP_NAME.name)
                when (it[Settings.APPS_SORT]) {
                    AppSort.APP_NAME.name -> menu.findItem(R.id.item_sort_by_name)
                        .setChecked(true)

                    AppSort.UPDATE_TIME.name -> menu.findItem(R.id.item_sort_by_update_time)
                        .setChecked(true)

                    AppSort.PACKAGE_NAME.name -> menu.findItem(R.id.item_sort_by_package_name)
                        .setChecked(true)

                    AppSort.INSTALL_TIME.name -> menu.findItem(R.id.item_sort_by_install_time)
                        .setChecked(true)
                }
                menu.findItem(R.id.item_filter_system)
                    .setChecked(it[Settings.HIDE_SYSTEM_APPS] ?: true)
                menu.findItem(R.id.item_filter_not_camera_apps)
                    .setChecked(it[Settings.HIDE_NOT_CAMERA_APPS] ?: true)
            }
        }
    }

    object Comparators {

        private val pm: PackageManager = vcampApp.packageManager
        val byLabel = Comparator<PackageInfo> { o1, o2 ->
            val n1 = o1.applicationInfo.loadLabel(pm).toString().lowercase(Locale.getDefault())
            val n2 = o2.applicationInfo.loadLabel(pm).toString().lowercase(Locale.getDefault())
            Collator.getInstance(Locale.getDefault()).compare(n1, n2)
        }
        val byPackageName = Comparator<PackageInfo> { o1, o2 ->
            val n1 = o1.packageName.lowercase(Locale.getDefault())
            val n2 = o2.packageName.lowercase(Locale.getDefault())
            Collator.getInstance(Locale.getDefault()).compare(n1, n2)
        }
        val byInstallTime = Comparator<PackageInfo> { o1, o2 ->
            val n1 = o1.firstInstallTime
            val n2 = o2.firstInstallTime
            n2.compareTo(n1)
        }
        val byUpdateTime = Comparator<PackageInfo> { o1, o2 ->
            val n1 = o1.lastUpdateTime
            val n2 = o2.lastUpdateTime
            n2.compareTo(n1)
        }

        fun byEnabled(configMap: LinkedHashMap<String, AppUnrealConfig>) =
            Comparator<PackageInfo> { o1, o2 ->
                val n1 = configMap[o1.applicationInfo.packageName]?.status ?: false
                val n2 = configMap[o2.applicationInfo.packageName]?.status ?: false
                n2.compareTo(n1)
            }
    }

    enum class AppSort {
        APP_NAME, INSTALL_TIME, UPDATE_TIME, PACKAGE_NAME
    }

    override fun getSectionText(position: Int): CharSequence =
        filteredInfos[position].applicationInfo.loadLabel(
            vcampApp.packageManager
        ).first().uppercase()

}
