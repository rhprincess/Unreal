package io.twinkle.unreal.ui.activity

import android.content.res.Resources
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.media3.ui.PlayerView.ControllerVisibilityListener
import com.google.android.material.color.DynamicColors
import io.twinkle.unreal.R
import io.twinkle.unreal.databinding.ActivityPlayerBinding
import io.twinkle.unreal.util.ThemeUtil
import java.io.File

class PlayerActivity : BaseActivity() {

    private lateinit var binding: ActivityPlayerBinding
    private lateinit var player: ExoPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        player = ExoPlayer.Builder(this).build()

        val exoPlayer = binding.exoPlayer
        val appBar = binding.appBar
        val toolbar = binding.toolbar

        exoPlayer.player = player

        val title = intent.getStringExtra("title") ?: "播放器"
        val videoFilePath = intent.getStringExtra("video") ?: ""
        toolbar.title = title

        toolbar.setNavigationOnClickListener {
            finish()
        }

        exoPlayer.setControllerVisibilityListener(ControllerVisibilityListener {
            if (it == PlayerView.VISIBLE) {
                appBar.setExpanded(true, true)
            } else {
                appBar.setExpanded(false, true)
            }
        })

        player.addMediaItem(MediaItem.fromUri(Uri.fromFile(File(videoFilePath))))
        player.prepare()
        player.playWhenReady = true
    }

    override fun onDestroy() {
        super.onDestroy()
        player.stop()
        player.release()
    }

}