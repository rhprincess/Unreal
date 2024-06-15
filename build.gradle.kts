// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
        // 作为 Xposed 模块使用务必添加，其它情况可选
        maven("https://api.xposed.info/")
        // MavenCentral 有 2 小时缓存，若无法集成最新版本请添加此地址
        maven("https://s01.oss.sonatype.org/content/repositories/releases/")
    }
    dependencies {
        classpath(libs.com.android.tools.build.gradle6)
        classpath(libs.org.jetbrains.kotlin.kotlin.gradle.plugin2)
        classpath(libs.androidx.navigation.navigation.safe.args.gradle.plugin2)
        classpath(libs.dev.rikka.tools.materialthemebuilder.gradle.plugin)
        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
}