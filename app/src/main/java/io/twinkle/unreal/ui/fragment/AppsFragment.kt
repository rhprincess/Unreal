package io.twinkle.unreal.ui.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.MenuProvider
import androidx.core.view.size
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
import com.google.android.material.transition.MaterialSharedAxis
import com.l4digital.fastscroll.FastScroller
import com.l4digital.fastscroll.FastScroller.FastScrollListener
import io.twinkle.unreal.MyApp
import io.twinkle.unreal.R
import io.twinkle.unreal.databinding.FragmentAppsBinding
import io.twinkle.unreal.ui.activity.MainActivity
import io.twinkle.unreal.ui.adapter.AppAdapter
import io.twinkle.unreal.util.CollapseState
import io.twinkle.unreal.util.addStateListener
import io.twinkle.unreal.vcampApp
import java.util.Objects

class AppsFragment : BaseFragment(), MenuProvider {

    private var preferences: SharedPreferences? = null
    private var _binding: FragmentAppsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val apps: List<PackageInfo> = vcampApp.packageManager.getInstalledPackages(
        PackageManager.GET_PERMISSIONS or PackageManager.GET_CONFIGURATIONS
    )
    private lateinit var adapter: AppAdapter

    private val observer: AdapterDataObserver = object : AdapterDataObserver() {
        override fun onChanged() {
            binding.swipeRefreshLayout.isRefreshing = !adapter.isLoaded
            binding.toolbarLayout.title =
                getString(R.string.title_apps) + "(" + adapter.itemCount + ")"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adapter = AppAdapter(this, apps.sortedWith(AppAdapter.Comparators.byInstallTime))
        preferences = MyApp.getPreferences()
    }

    @SuppressLint("ResourceType")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAppsBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val recyclerView = binding.recyclerView
        val swipeRefreshLayout = binding.swipeRefreshLayout
        val toolbar = binding.toolbar
        val appBar = binding.appBar
        if (preferences?.getBoolean("show_appbar_scrim", true) == true) {
            appBar.setLiftable(true)
            appBar.addStateListener { state, _ ->
                appBar.setLifted(state == CollapseState.COLLAPSED)
            }
        } else {
            appBar.setLiftable(false)
        }
        binding.toolbarLayout.title = getString(R.string.title_apps) + "(" + adapter.itemCount + ")"
        binding.toolbarLayout.subtitle = getString(R.string.apps_enabled_count, 0)
        recyclerView.id = Objects.hash(recyclerView, recyclerView.fastScroller)
        swipeRefreshLayout.setProgressViewEndTarget(true, swipeRefreshLayout.progressViewEndOffset)
        toolbar.addMenuProvider(this)
        adapter.registerAdapterDataObserver(observer)

        recyclerView.fastScroller.setFastScrollListener(object : FastScrollListener {
            override fun onFastScrollStart(fastScroller: FastScroller) {
                appBar.setExpanded(false, true)
            }

            override fun onFastScrollStop(fastScroller: FastScroller) {
                if (!recyclerView.recyclerView.canScrollVertically(-1)) {
                    appBar.setExpanded(true, true)
                }
            }
        })

        val layoutManager = LinearLayoutManager(context)
        layoutManager.orientation = RecyclerView.VERTICAL
        recyclerView.setAdapter(adapter)
        recyclerView.setLayoutManager(layoutManager)

        swipeRefreshLayout.setOnRefreshListener {
            adapter.refresh()
        }
        return root
    }

    fun setEnableCount(count: Int) {
        if (_binding != null) {
            binding.toolbarLayout.subtitle = getString(R.string.apps_enabled_count, count)
        }
    }

    override fun onResume() {
        super.onResume()
        adapter.refresh()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        adapter.refresh()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        adapter.unregisterAdapterDataObserver(observer)
        _binding = null
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.app_filter_menu, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        adapter.onOptionsItemSelected(menuItem)
        return true
    }

    override fun onPrepareMenu(menu: Menu) {
        adapter.onPrepareMenu(menu)
    }

}