package io.twinkle.unreal.data

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder

@Serializable
data class AppUnrealConfig(
    var status: Boolean = false,
    var cameraSizes: ArrayList<CameraSize> = arrayListOf()
)

@Serializable
data class CameraSize(var width: Int = 0, var height: Int = 0)

fun LinkedHashMap<String, AppUnrealConfig>.writeCameraSize(
    packageName: String,
    width: Int,
    height: Int
) {
    this[packageName]?.cameraSizes?.let {
        if (!it.toString().contains(width.toString()) && !it.toString()
                .contains(height.toString())
        ) {
            it.add(
                CameraSize(
                    width = width,
                    height = height
                )
            )
        }
    }
}
