package com.artwl.update;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;

import android.content.Intent;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.view.View;

import androidx.fragment.app.DialogFragment;

import org.jetbrains.annotations.NotNull;


public class UpdateDialog extends DialogFragment {

    UpdateProgress updateProgress;
    Handler mHandler;
    ProgressDialog pdLoading;


    @NotNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        pdLoading = new ProgressDialog(getActivity());
        updateProgress = new UpdateProgress(new Handler());
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setCancelable(false);
        builder.setTitle(R.string.newUpdateAvailable);
        assert getArguments() != null;
        builder.setMessage(getArguments().getString(Constants.APK_UPDATE_CONTENT));
        builder.setPositiveButton(R.string.dialogPositiveButton, (dialog, id) -> {
            // FIRE ZE MISSILES!
                goToDownload();
            dismiss();
        });
        builder.setNegativeButton(R.string.dialogNegativeButton, (dialog, id) -> {
            // User cancelled the dialog
            dismiss();
        });
        // Create the AlertDialog object and return it
        return builder.create();
    }


    private void goToDownload() {

        pdLoading.setMessage("\tDownloading apk...");
        pdLoading.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pdLoading.setCancelable(false);
        pdLoading.show();
        Intent intent = new Intent(getActivity().getApplicationContext(), DownloadService.class);
        intent.putExtra("receiver", updateProgress);
        intent.putExtra(Constants.APK_DOWNLOAD_URL, getArguments().getString(Constants.APK_DOWNLOAD_URL));
        intent.putExtra(Constants.APK_IS_AUTO_INSTALL, getArguments().getBoolean(Constants.APK_IS_AUTO_INSTALL));
        intent.putExtra(Constants.APK_CHECK_EXTERNAL, getArguments().getBoolean(Constants.APK_CHECK_EXTERNAL));
        getActivity().startService(intent);

    }

    private class UpdateProgress extends ResultReceiver {

        public UpdateProgress(Handler handler) {
            super(handler);
        }

        @Override
        public void onReceiveResult(int resultCode, Bundle resultData) {
            super.onReceiveResult(resultCode, resultData);
            if (resultCode == DownloadService.DOWNLOAD_PROGRESS) {
                int progress = resultData.getInt("progress");
                pdLoading.setProgress(progress);
            }
            if (resultCode == DownloadService.DOWNLOAD_SUCCESS){
                pdLoading.dismiss();
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    private void hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        View decorView = getActivity().getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }
}
