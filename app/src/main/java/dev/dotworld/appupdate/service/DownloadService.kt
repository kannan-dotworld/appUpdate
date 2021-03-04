package dev.dotworld.appupdate.service

import android.app.IntentService
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.ResultReceiver
import android.util.Log
import dev.dotworld.appupdate.utils.Constants
import dev.dotworld.appupdate.utils.StorageUtils
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.internal.closeQuietly
import okio.BufferedSink
import okio.BufferedSource
import okio.buffer
import okio.sink
import java.io.File

class DownloadService(name: String = "DownloadService") : IntentService(name) {
companion object {
    var DOWNLOAD_PROGRESS = 2
    var DOWNLOAD_SUCCESS = 3
}

    private val TAG = DownloadService::class.java.simpleName

    var bundle = Bundle()



    override fun onCreate() {
        var context: Context
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val description = "Download Update Notification"
        }
    }

    override fun onHandleIntent(intent: Intent?) {
        Log.d(TAG, "onHandleIntent: ")

        val receiver = intent?.getParcelableExtra<ResultReceiver>("receiver")
        val urlStr = intent?.getStringExtra(Constants.APK_DOWNLOAD_URL)
        val isAutoInstall = intent!!.getBooleanExtra(Constants.APK_IS_AUTO_INSTALL, false)
        val checkExternal = intent.getBooleanExtra(Constants.APK_CHECK_EXTERNAL, true)
        var sink: BufferedSink? = null
        var source: BufferedSource? = null
        try {
            // apk local file paths.
//            val dir = StorageUtils.getCacheDirectory(this, checkExternal)
            val apkName = urlStr!!.substring(urlStr.lastIndexOf("/") + 1)
            val apkFile = File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), apkName)
            if (apkFile.exists()) {
                apkFile.delete()
            }
            val client = OkHttpClient()
            val request: Request = Request.Builder().url(urlStr)
                .addHeader("Charset", "UTF-8")
                .addHeader("Connection", "Keep-Alive")
                .addHeader("Charset", "UTF-8")
                .addHeader("Accept-Encoding", "gzip, deflate")
                .build()
            val response = client.newCall(request).execute()
            val body = response.body
            val contentLength = body!!.contentLength()
            source = body.source()
            sink = apkFile.sink().buffer()
            val sinkBuffer = sink.buffer()
            var totalBytesRead: Long = 0
            val bufferSize = 8 * 1024
            var bytesRead: Long
            var lastProgress = -1
            while (source.read(sinkBuffer, bufferSize.toLong()).also { bytesRead = it } != -1L) {
                sink.emit()
                totalBytesRead += bytesRead
                val progress = (totalBytesRead * 100 / contentLength).toInt()
                if (progress != lastProgress) {
                    lastProgress = progress
                    //  DOWNLOAD_PROGRESS = progress;
                    bundle.putInt("progress", lastProgress)
                    receiver?.send(DOWNLOAD_PROGRESS, bundle)
                    //  updateProgress(lastProgress);
                }
            }
            sink.flush()
            apkFile.setReadable(true, false)
            Log.d(TAG, String.format("Download Apk to %s", apkFile))

            // mBuilder.setContentText(getString(R.string.download_success)).setProgress(0, 0, false);
            val fileUri = StorageUtils.getFileUri(this, apkFile)
            val installAPKIntent = Intent(Intent.ACTION_VIEW)
            installAPKIntent.setDataAndType(fileUri, "application/vnd.android.package-archive")
            receiver!!.send(DOWNLOAD_SUCCESS, Bundle.EMPTY)
            if (isAutoInstall) {
                installAPKIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                if (Build.VERSION.SDK_INT >= 24) {
                    intent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true)
                    installAPKIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                startActivity(installAPKIntent)
                //   mNotifyManager.cancel(notificationId);
                return
            }
            val command = arrayOf("chmod", "777", apkFile.toString())
            val builder = ProcessBuilder(*command)
            builder.start()
        } catch (e: Exception) {
            Log.e(TAG, "download apk file error", e)
        } finally {
            sink!!.closeQuietly()
            source!!.closeQuietly()
        }

    }


}