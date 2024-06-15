package io.twinkle.unreal.ui.fragment

import android.os.Bundle
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.NavOptions
import androidx.navigation.Navigator
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.transition.MaterialFadeThrough
import io.twinkle.unreal.MyApp
import java.util.concurrent.Callable
import java.util.concurrent.Future
import java.util.concurrent.FutureTask


open class BaseFragment : Fragment() {

    fun navigateUp() {
        getNavController().navigateUp()
    }

    fun getNavController(): NavController {
        return NavHostFragment.findNavController(this)
    }

    fun safeNavigate(@IdRes resId: Int): Boolean {
        return try {
            getNavController().navigate(resId)
            true
        } catch (ignored: IllegalArgumentException) {
            false
        }
    }

    fun safeNavigate(direction: NavDirections?): Boolean {
        return try {
            getNavController().navigate(direction!!)
            true
        } catch (ignored: IllegalArgumentException) {
            false
        }
    }

    fun runAsync(runnable: () -> Unit) {
        MyApp.getExecutorService().submit(runnable)
    }

    fun <T> runAsync(callable: Callable<T>?): Future<T> {
        return MyApp.getExecutorService().submit(callable)
    }

    fun runOnUiThread(runnable: () -> Unit) {
        MyApp.getMainHandler().post(runnable)
    }

    private fun runOnUiThread(runnable: Runnable) {
        MyApp.getMainHandler().post(runnable)
    }

    fun <T> runOnUiThread(callable: Callable<T>?): Future<T> {
        val task = FutureTask(callable)
        runOnUiThread(task)
        return task
    }

}