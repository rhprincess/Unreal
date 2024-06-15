package io.twinkle.unreal.util

import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.Transformation
import com.google.android.material.appbar.AppBarLayout
import kotlin.math.abs


enum class CollapseState {
    COLLAPSED, EXPANDED, PARTIALLY_EXPANDED
}

fun AppBarLayout.addStateListener(onStateChange: (state: CollapseState, verticalOffset: Int) -> Unit) {
    addOnOffsetChangedListener { appBarLayout, verticalOffset ->
        when {
            verticalOffset == 0 -> onStateChange(CollapseState.EXPANDED, verticalOffset)
            abs(verticalOffset) >= appBarLayout.totalScrollRange -> onStateChange(
                CollapseState.COLLAPSED,
                verticalOffset
            )

            else -> onStateChange(CollapseState.PARTIALLY_EXPANDED, verticalOffset)
        }
    }
}

infix fun <R> Boolean.then(block: Boolean.() -> R?): R? {
    if (this) return block()
    return null
}

fun <R> Boolean.then(block: () -> R?, otherwise: () -> R?): R? {
    return if (this) block()
    else otherwise()
}

//展开
fun View.expand(targetHeight: Int) {
    layoutParams.height = 1
    visibility = View.VISIBLE
    val a: Animation = object : Animation() {
        override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
            layoutParams.height =
                if (interpolatedTime == 1f) targetHeight else (targetHeight * interpolatedTime).toInt()
            requestLayout()
        }

        override fun willChangeBounds(): Boolean {
            return true
        }
    }
    a.duration = (targetHeight / context.resources.displayMetrics.density).toInt().toLong() * 2
    startAnimation(a)
}

//收起
fun View.collapse() {
    val initialHeight = measuredHeight
    val a: Animation = object : Animation() {
        override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
            if (interpolatedTime == 1f) {
                visibility = View.GONE
            } else {
                layoutParams.height = initialHeight - (initialHeight * interpolatedTime).toInt()
                requestLayout()
            }
        }

        override fun willChangeBounds(): Boolean {
            return true
        }
    }
    a.duration = (initialHeight / context.resources.displayMetrics.density).toInt().toLong() * 2
    startAnimation(a)
}