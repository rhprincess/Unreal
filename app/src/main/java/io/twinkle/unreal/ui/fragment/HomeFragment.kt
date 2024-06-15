package io.twinkle.unreal.ui.fragment

import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.TransitionManager
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.UriUtils
import com.google.android.material.card.MaterialCardView
import com.google.android.material.carousel.CarouselLayoutManager
import com.google.android.material.carousel.CarouselSnapHelper
import com.google.android.material.color.MaterialColors
import com.google.android.material.transition.MaterialArcMotion
import com.google.android.material.transition.MaterialContainerTransform
import io.twinkle.unreal.BuildConfig
import io.twinkle.unreal.MyApp
import io.twinkle.unreal.R
import io.twinkle.unreal.databinding.FragmentHomeBinding
import io.twinkle.unreal.ui.activity.PlayerActivity
import io.twinkle.unreal.ui.adapter.CarouselAdapter
import io.twinkle.unreal.util.CollapseState
import io.twinkle.unreal.util.ModuleStatus
import io.twinkle.unreal.util.addStateListener
import io.twinkle.unreal.util.collapse
import io.twinkle.unreal.util.expand
import io.twinkle.unreal.vcampApp
import java.io.File

class HomeFragment : BaseFragment() {

    private var preferences: SharedPreferences? = null
    private var _binding: FragmentHomeBinding? = null
    private val unrealFile = File(
        MyApp.UNREAL_DIR + "unreal.mp4"
    )

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private var isActivated: Boolean = false

    private var adapter: CarouselAdapter = CarouselAdapter(this, listOf())

    private val selectVideoLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) {
            it?.let { uri ->
                val filePath = UriUtils.uri2File(uri).absolutePath
                runAsync {
                    vcampApp.getFirstFrame(filePath).let {
                        runOnUiThread {
                            binding.firstFrame.setImageBitmap(it)
                        }
                    }
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isActivated = ModuleStatus.isActivated()
        preferences = MyApp.getPreferences()
    }

    @OptIn(UnstableApi::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val toolbar = binding.toolbar
        val expandButton = binding.expandButton
        toolbar.title = getString(R.string.app_name)

        initDashBoard()

        expandButton.setOnClickListener {
            val intent = Intent()
            intent.setClass(requireContext(), PlayerActivity::class.java)
            intent.putExtra("title", "全局视频")
            intent.putExtra("video", unrealFile.absolutePath)
            requireActivity().startActivity(intent)
        }

        binding.selectGlobalVideo.setOnClickListener {
            selectVideoLauncher.launch("video/*")
        }

        val carouselRecyclerView = binding.carouselRecyclerView
        val progressView = binding.progressView

        val appBar = binding.appBar
        if (preferences?.getBoolean("show_appbar_scrim", true) == true) {
            appBar.setLiftable(true)
            appBar.addStateListener { state, _ ->
                appBar.setLifted(state == CollapseState.COLLAPSED)
            }
        } else {
            appBar.setLiftable(false)
        }

        runAsync {
            if (_binding != null) {
                progressView.visibility = View.VISIBLE
                adapter = CarouselAdapter(this, vcampApp.getEnableAppsInfo())
                runOnUiThread {
                    carouselRecyclerView.adapter = adapter
                    val snapHelper = CarouselSnapHelper()
                    val carouselLayoutManager = CarouselLayoutManager()
                    carouselLayoutManager.carouselAlignment = CarouselLayoutManager.ALIGNMENT_CENTER
                    carouselLayoutManager.orientation = RecyclerView.HORIZONTAL
                    snapHelper.attachToRecyclerView(carouselRecyclerView)
                    progressView.visibility = View.GONE
                }
            }
        }
        return binding.root
    }

    private fun initDashBoard() {
        val activatedSubtext = binding.activatedSubtext
        activatedSubtext.text = getString(
            R.string.version_tem,
            BuildConfig.VERSION_NAME,
            BuildConfig.VERSION_CODE,
            BuildConfig.BUILD_TYPE
        )
        val firstFrame = binding.firstFrame
        runAsync {
            if (unrealFile.exists()) {
                vcampApp.getFirstFrame(unrealFile.path).let {
                    if (_binding != null)
                        runOnUiThread {
                            firstFrame.setImageBitmap(it)
                        }
                }
            }
        }

        if (isActivated) {
            val colorOnPrimaryContainer = MaterialColors.getColor(
                this.context,
                R.attr.colorOnPrimaryContainer,
                ""
            )
            val activatedText = binding.activatedText
            activatedText.text = getString(R.string.is_activated)
            activatedText.setTextColor(colorOnPrimaryContainer)
            activatedSubtext.setTextColor(colorOnPrimaryContainer)
            binding.statusPanel.setCardBackgroundColor(
                MaterialColors.getColor(
                    this.context,
                    R.attr.colorPrimaryContainer,
                    ""
                )
            )
            binding.activatedIcon.apply {
                imageTintList = ColorStateList.valueOf(colorOnPrimaryContainer)
                setImageDrawable(
                    ResourcesCompat.getDrawable(
                        resources,
                        R.drawable.outline_done_all_24,
                        null
                    )
                )
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}