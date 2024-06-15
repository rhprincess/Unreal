package io.twinkle.unreal.data

import android.content.pm.PackageInfo
import android.graphics.Bitmap

data class EnableAppInfo(val info: PackageInfo, val firstFrame: Bitmap)