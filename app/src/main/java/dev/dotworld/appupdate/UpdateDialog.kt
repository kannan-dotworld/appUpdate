package dev.dotworld.appupdate

import android.app.AlertDialog
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.ResultReceiver
import android.util.Log
import android.view.View
import androidx.fragment.app.DialogFragment
import dev.dotworld.appupdate.service.DownloadService
import dev.dotworld.appupdate.utils.Constants
import kotlin.math.log

class UpdateDialog: DialogFragment() {

companion object{
    var updateProgress: UpdateProgress? = null
    var pdLoading: ProgressDialog? = null
    val TAG =UpdateDialog::class.java.simpleName
}



    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        Log.d(TAG, "onCreateDialog: ")
        // Use the Builder class for convenient dialog construction
        pdLoading = ProgressDialog(activity)
        updateProgress = UpdateProgress(Handler())
        val builder = AlertDialog.Builder(activity)
        builder.setCancelable(false)
        builder.setTitle(R.string.newUpdateAvailable)
        if (BuildConfig.DEBUG && arguments == null) {
            error("Assertion failed")
        }
        builder.setMessage(arguments!!.getString(Constants.APK_UPDATE_CONTENT))
        builder.setPositiveButton(R.string.dialogPositiveButton) { dialog, id ->
            goToDownload()
            dismiss()
        }
        builder.setNegativeButton(R.string.dialogNegativeButton) { dialog, id ->
            // User cancelled the dialog
            dismiss()
        }
        // Create the AlertDialog object and return it
        return builder.create()
    }


    private fun goToDownload() {
        Log.d(TAG, "goToDownload: ")
        pdLoading?.setMessage("\tDownloading apk...")
        pdLoading?.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
        pdLoading?.setCancelable(false)
        pdLoading?.show()
        val intent = Intent(activity?.applicationContext, DownloadService::class.java)
        intent.putExtra("receiver", updateProgress)
        intent.putExtra(Constants.APK_DOWNLOAD_URL,arguments?.getString(Constants.APK_DOWNLOAD_URL))
        intent.putExtra(Constants.APK_IS_AUTO_INSTALL,arguments?.getBoolean(Constants.APK_IS_AUTO_INSTALL)  )
        intent.putExtra(Constants.APK_CHECK_EXTERNAL,arguments?.getBoolean(Constants.APK_CHECK_EXTERNAL))
        activity?.startService(intent)
    }

    class UpdateProgress(handler: Handler?) : ResultReceiver(handler) {
        public override fun onReceiveResult(resultCode: Int, resultData: Bundle) {
            Log.d(TAG, "onReceiveResult: ")
            super.onReceiveResult(resultCode, resultData)
            if (resultCode == DownloadService.DOWNLOAD_PROGRESS) {
                val progress = resultData.getInt("progress")
                pdLoading?.setProgress(progress)
            }
            if (resultCode == DownloadService.DOWNLOAD_SUCCESS) {
                pdLoading?.dismiss()
            }
        }
    }

    override fun onStart() {
        Log.d(TAG, "onStart: ")
        super.onStart()
    }

}