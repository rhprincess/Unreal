import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("androidx.navigation.safeargs.kotlin")
    id("com.google.devtools.ksp") version "1.9.23-1.0.20"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.23"
    id("dev.rikka.tools.materialthemebuilder")
}

android {
    namespace = "io.twinkle.unreal"
    compileSdk = 34

    defaultConfig {
        applicationId = "io.twinkle.unreal"
        minSdk = 29
        targetSdk = 34
        versionCode = releaseDay().toInt()
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        buildConfig = true
        viewBinding = true
    }
}

materialThemeBuilder {
    themes {
        for ((name, color) in listOf(
            "Red" to "F44336",
            "Pink" to "E91E63",
            "Purple" to "9C27B0",
            "DeepPurple" to "673AB7",
            "Indigo" to "3F51B5",
            "Blue" to "2196F3",
            "LightBlue" to "03A9F4",
            "Cyan" to "00BCD4",
            "Teal" to "009688",
            "Green" to "4FAF50",
            "LightGreen" to "8BC3A4",
            "Lime" to "CDDC39",
            "Yellow" to "FFEB3B",
            "Amber" to "FFC107",
            "Orange" to "FF9800",
            "DeepOrange" to "FF5722",
            "Brown" to "795548",
            "BlueGrey" to "607D8F",
            "Sakura" to "FF9CA8"
        )) {
            create("Material$name") {
                lightThemeFormat = "ThemeOverlay.Light.%s"
                darkThemeFormat = "ThemeOverlay.Dark.%s"
                primaryColor = "#$color"
            }
        }
    }
    // Add Material Design 3 color tokens (such as palettePrimary100) in generated theme
    // rikka.material >= 2.0.0 provides such attributes
    generatePalette = true
}

fun releaseDay(): String {
    return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
}

dependencies {
    // 基础依赖
    implementation(libs.yukihookapi.api)
    compileOnly(libs.xposed.api)
    ksp(libs.com.highcapable.yukihookapi.ksp.xposed)
    ksp(libs.ksp)

    implementation(libs.collapsingtoolbarlayout.subtitle)
    implementation(libs.glide)

    // AppIcon Loader
    implementation(libs.appiconloader.glide)
    // DataStore
    implementation(libs.androidx.datastore.preferences)
    // Serialization
    implementation(libs.kotlinx.serialization.json)
    // Android Utils
    implementation(libs.utilcodex)
    // FastScroll
    implementation(libs.fastscroll)

    // Media3 ExoPlayer for handling media playback
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.ui)

    implementation("dev.rikka.rikkax.material:material:2.7.0") {
        exclude(group = "dev.rikka.rikkax.appcompat")
    }
    implementation("dev.rikka.rikkax.material:material-preference:2.0.0") {
        exclude(group = "dev.rikka.rikkax.appcompat")
    }

    implementation(libs.androidx.palette.ktx)
    implementation(libs.androidx.preference.ktx)
    implementation(libs.androidx.swiperefreshlayout)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)

    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}