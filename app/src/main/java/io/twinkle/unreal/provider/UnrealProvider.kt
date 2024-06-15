package io.twinkle.unreal.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.util.Log
import io.twinkle.unreal.R
import io.twinkle.unreal.util.then
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class UnrealProvider : ContentProvider() {

    companion object {
        private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH)
        const val AUTHORITY = "io.twinkle.unreal.provider"
        const val VIDEO = "video"
        val VIDEO_CODE = VIDEO.hashCode()
        const val UNREAL_CONFIGS = "configs"
        val UNREAL_CONFIGS_CODE = UNREAL_CONFIGS.hashCode()
        const val APP = "app"
        val APP_CODE = APP.hashCode()
    }

    init {
        uriMatcher.addURI(AUTHORITY, VIDEO, VIDEO_CODE)
        uriMatcher.addURI(AUTHORITY, UNREAL_CONFIGS, UNREAL_CONFIGS_CODE)
        uriMatcher.addURI(AUTHORITY, APP, APP_CODE)
    }

    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor? {
        val externalFilesDir = context?.getExternalFilesDir(null)?.absolutePath ?: return null
        val code = uriMatcher.match(uri)
        return when (code) {
            VIDEO_CODE -> {
                val unrealFile = File(externalFilesDir, "unreal.mp4")
                if (!unrealFile.exists()) {
                    try {
                        // 使用 try-with-resources 语句确保资源被正确关闭
                        context?.resources?.openRawResource(R.raw.unreal)?.use { inputStream ->
                            FileOutputStream(unrealFile).use { fileOutputStream ->
                                inputStream.copyTo(fileOutputStream)
                            }
                        }
                        ParcelFileDescriptor.open(unrealFile, ParcelFileDescriptor.MODE_READ_ONLY)
                    } catch (e: IOException) {
                        e.printStackTrace()
                        null
                    }
                } else {
                    ParcelFileDescriptor.open(unrealFile, ParcelFileDescriptor.MODE_READ_ONLY)
                }
            }

            UNREAL_CONFIGS_CODE -> {
                val configFile = File(externalFilesDir, "configs.json")
                if (!configFile.exists()) {
                    try {
                        configFile.createNewFile()
                        ParcelFileDescriptor.open(configFile, ParcelFileDescriptor.MODE_READ_ONLY)
                    } catch (e: IOException) {
                        e.printStackTrace()
                        null
                    }
                } else {
                    ParcelFileDescriptor.open(configFile, ParcelFileDescriptor.MODE_READ_ONLY)
                }
            }

            APP_CODE -> analyzeIndividualApp(uri, externalFilesDir)

            else -> return null
        }
    }

    override fun query(
        uri: Uri, projection: Array<String>?, selection: String?,
        selectionArgs: Array<String>?, sortOrder: String?
    ): Cursor {
        // 创建MatrixCursor
        val cursor = MatrixCursor(arrayOf("_id", "display_name", "size", "date_modified", "file"))
        val path = context?.getExternalFilesDir(null)!!.absolutePath
        val file = File(path, "unreal.mp4")
        // 获取视频文件夹路径
        cursor.addRow(arrayOf(0, file.name, file.length(), file.lastModified(), file))
        // Log.d("cursor", "query: $file")
        return cursor
    }

    private fun analyzeIndividualApp(uri: Uri, externalFileDir: String?): ParcelFileDescriptor? {
        val appSpacialScope = uri.path?.substringAfter("content://$AUTHORITY/")
        val list = appSpacialScope?.split("/")
        val packageName = (list != null && list.size > 1).then { list?.get(0) }
        val type = (list != null && list.size >= 2) then { list?.get(1) }
        return externalFileDir?.let { dir ->
            if (packageName != null && type != null) {
                val appDir = File("$dir/$packageName/")
                if (!appDir.exists()) appDir.mkdirs()
                val typedFile = type.let { t ->
                    when (t) {
                        VIDEO -> File(appDir, "unreal.mp4")

                        UNREAL_CONFIGS -> File(appDir, "configs.json")

                        else -> File(dir, "unknown")
                    }
                }
                if (!typedFile.exists()) {
                    try {
                        if (type == VIDEO) {
                            context?.resources?.openRawResource(R.raw.unreal)?.use { inputStream ->
                                FileOutputStream(typedFile).use { fileOutputStream ->
                                    inputStream.copyTo(fileOutputStream)
                                }
                            }
                        } else {
                            typedFile.createNewFile()
                        }
                        ParcelFileDescriptor.open(typedFile, ParcelFileDescriptor.MODE_READ_ONLY)
                    } catch (e: IOException) {
                        e.printStackTrace()
                        null
                    }
                } else {
                    ParcelFileDescriptor.open(typedFile, ParcelFileDescriptor.MODE_READ_ONLY)
                }
            } else {
                null
            }
        }
    }

    override fun onCreate(): Boolean {
        return true
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int = 0

    override fun getType(uri: Uri): String? = null

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null

    override fun update(
        uri: Uri, values: ContentValues?, selection: String?,
        selectionArgs: Array<String>?
    ): Int = 0
}