package io.twinkle.unreal.ui.adapter

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.ColorUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.animation.AnimationUtils.lerp
import com.google.android.material.carousel.MaskableFrameLayout
import com.google.android.material.color.MaterialColors
import io.twinkle.unreal.MyApp
import io.twinkle.unreal.R
import io.twinkle.unreal.data.EnableAppInfo
import io.twinkle.unreal.databinding.ItemCarouselBinding
import io.twinkle.unreal.ui.activity.PlayerActivity
import io.twinkle.unreal.ui.fragment.HomeFragment
import io.twinkle.unreal.vcampApp
import java.io.File
import java.util.Objects

class CarouselAdapter(private val fragment: HomeFragment, private val apps: List<EnableAppInfo>) :
    RecyclerView.Adapter<CarouselAdapter.ViewHolder>() {
    class ViewHolder(val binding: ItemCarouselBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemCarouselBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return apps.size
    }

    override fun getItemId(position: Int): Long {
        return Objects.hash(apps[position], position, apps.toString()).toLong()
    }

    @SuppressLint("RestrictedApi", "SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val binding = holder.binding
        val app = apps[position]
        val context = holder.itemView.context
        binding.carouselImageView.setImageBitmap(app.firstFrame)

        Palette.from(app.firstFrame).generate {
            if (it != null) {
                val color =
                    it.getDominantColor(MaterialColors.getColor(context, R.attr.colorSurface, null))
                if (ColorUtils.isLightColor(color)) {
                    binding.title.setTextColor(Color.BLACK)
                } else {
                    binding.title.setTextColor(Color.WHITE)
                }
                val gradientDrawable = GradientDrawable(
                    GradientDrawable.Orientation.BOTTOM_TOP,
                    intArrayOf(
                        MaterialColors.compositeARGBWithAlpha(color, 235),
                        MaterialColors.compositeARGBWithAlpha(color, 215),
                        Color.TRANSPARENT
                    )
                )
                gradientDrawable.setGradientCenter(0.5f, 0.65f)
                gradientDrawable.gradientType = GradientDrawable.LINEAR_GRADIENT
                binding.carouselImageView.foreground = gradientDrawable
            }
        }

        Glide.with(binding.carouselAppIcon).load(app.info)
            .into(object : CustomTarget<Drawable?>() {
                override fun onLoadCleared(placeholder: Drawable?) {}

                override fun onLoadFailed(errorDrawable: Drawable?) {
                    binding.carouselAppIcon.setImageDrawable(context.packageManager.defaultActivityIcon)
                }

                override fun onResourceReady(
                    resource: Drawable,
                    transition: Transition<in Drawable?>?
                ) {
                    binding.carouselAppIcon.setImageDrawable(resource)
                }
            })

        binding.title.text = app.info.applicationInfo.loadLabel(context.packageManager)
            .toString()

        (holder.itemView as MaskableFrameLayout).setOnMaskChangedListener { maskRect ->
            // Any custom motion to run when mask size changes
            binding.appInfoGroup.translationX = maskRect.left
            binding.appInfoGroup.alpha = lerp(1F, 0F, 0F, 80F, maskRect.left)
        }

        holder.itemView.setOnClickListener {
            val unrealFile =
                File(MyApp.UNREAL_DIR + "apps/" + app.info.applicationInfo.packageName + "/unreal.mp4")
            val intent = Intent()
            intent.setClass(fragment.requireContext(), PlayerActivity::class.java)
            intent.putExtra(
                "title",
                app.info.applicationInfo.loadLabel(vcampApp.packageManager)
                    .toString() + "的视频"
            )
            intent.putExtra("video", unrealFile.absolutePath)
            fragment.startActivity(intent)
        }
    }

}