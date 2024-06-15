package io.twinkle.unreal.ui.fragment

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.twinkle.unreal.BuildConfig
import io.twinkle.unreal.MyApp
import io.twinkle.unreal.R
import io.twinkle.unreal.databinding.FragmentSettingsHostBinding
import io.twinkle.unreal.util.CollapseState
import io.twinkle.unreal.util.addStateListener

class SettingsHostFragment : BaseFragment() {

    private var _binding: FragmentSettingsHostBinding? = null
    private val binding get() = _binding!!
    private var preferences: SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferences = MyApp.getPreferences()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsHostBinding.inflate(inflater, container, false)
        val root: View = binding.root
        binding.toolbarLayout.title = getString(R.string.title_settings)
        val appBar = binding.appBar
        binding.toolbarLayout.subtitle = getString(
            R.string.version_tem,
            BuildConfig.VERSION_NAME,
            BuildConfig.VERSION_CODE,
            BuildConfig.BUILD_TYPE
        )
        if (preferences?.getBoolean("show_appbar_scrim", true) == true) {
            appBar.setLiftable(true)
            appBar.addStateListener { state, _ ->
                appBar.setLifted(state == CollapseState.COLLAPSED)
            }
        } else {
            appBar.setLiftable(false)
        }
        childFragmentManager.beginTransaction().replace(R.id.settings_container, SettingsFragment())
            .commit()
        requireContext().theme.applyStyle(
            rikka.material.preference.R.style.ThemeOverlay_Rikka_Material3_Preference,
            true
        );

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}