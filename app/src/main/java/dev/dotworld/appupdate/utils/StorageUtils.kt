package dev.dotworld.appupdate.utils

import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException

class StorageUtils {




companion object {
    private val EXTERNAL_STORAGE_PERMISSION = "android.permission.WRITE_EXTERNAL_STORAGE"

    private val TAG = StorageUtils::class.java.simpleName

    fun getCacheDirectory(context: Context, checkExternal: Boolean): File? {
        Log.d(TAG, "getCacheDirectory: ")
        var appCacheDir: File? = null
        if (checkExternal && Environment.MEDIA_MOUNTED == Environment.getExternalStorageState() && hasExternalStoragePermission(
                context
            )
        ) {
            appCacheDir = getExternalCacheDir(context)
        }
        if (appCacheDir == null) {
            appCacheDir = context.cacheDir
        }
        if (appCacheDir == null) {
            Log.d(TAG, "Can't define system cache directory! The app should be re-installed.")
        }
        return appCacheDir
    }

    fun getFileUri(context: Context, apkFile: File?): Uri? {
        Log.d(TAG, "getFileUri: ")
        return if (Build.VERSION.SDK_INT >= 24) {
            FileProvider.getUriForFile(
                context, context.packageName,
                apkFile!!
            )
        } else {
            Uri.fromFile(apkFile)
        }
    }

    private fun getExternalCacheDir(context: Context): File? {
        Log.d(TAG, "getExternalCacheDir: ")
        val dataDir = File(File(Environment.getExternalStorageDirectory(), "Android"), "data")
        val appCacheDir = File(File(dataDir, context.packageName), "cache")
        if (!appCacheDir.exists()) {
            if (!appCacheDir.mkdirs()) {
                Log.d(TAG, "Unable to create external cache directory")
                return null
            }
            try {
                File(appCacheDir, ".nomedia").createNewFile()
            } catch (e: IOException) {
                Log.d(TAG, "Can't create \".nomedia\" file in application external cache directory")
            }
        }
        return appCacheDir
    }

    private fun hasExternalStoragePermission(context: Context): Boolean {
        Log.d(TAG, "hasExternalStoragePermission: ")
        val perm = context.checkCallingOrSelfPermission(EXTERNAL_STORAGE_PERMISSION)
        return perm == PackageManager.PERMISSION_GRANTED
    }
}
}