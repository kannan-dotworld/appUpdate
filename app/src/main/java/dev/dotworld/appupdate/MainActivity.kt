package dev.dotworld.appupdate

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.AsyncTask
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import com.google.gson.Gson
import dev.dotworld.appupdate.entity.PackageDetails
import dev.dotworld.appupdate.utils.Constants
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.concurrent.TimeUnit


private const val APP_UPDATE_SERVER_URL = "http://192.168.29.49:3001/download/getdeatils"
private const val APK_IS_AUTO_INSTALL = true

private const val mIsAutoInstall = true
private const val mCheckExternal = false
class MainActivity : AppCompatActivity() {

    private val TAG=MainActivity::class.java.simpleName
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.d(TAG, "onCreate: ")
        findViewById<Button>(R.id.update).setOnClickListener {
            Log.d(TAG, "onclick: ")
            checkForUpdates(APP_UPDATE_SERVER_URL)

        }
//        checkForUpdates(APP_UPDATE_SERVER_URL)
    }

    private fun checkForUpdates(url: String) {
        try {
            Get_response.execute(url)
        } catch (e: Exception) {
            Log.e(TAG, e.toString())
        }
    }

    @SuppressLint("StaticFieldLeak")
     private  val Get_response = object  :AsyncTask<String?, String?, String>() {


        override fun doInBackground(vararg params: String?): String {
            Log.d(TAG, "doInBackground: ")
            return try {
                val client: OkHttpClient = OkHttpClient.Builder()
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .writeTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(15, TimeUnit.SECONDS)
                    .callTimeout(15, TimeUnit.SECONDS)
                    .retryOnConnectionFailure(false)
                    .build()
                val request: Request = Request.Builder()
                    .url(params[0]!!)
                    .build()
                val response = client.newCall(request).execute()
                response.body!!.string()
            } catch (ex: IOException) {
                Log.d("Get_response", "Failed to get update json", ex)
                ""
            }
        }

        override fun onPostExecute(s: String) {
            Log.d(TAG, "onPostExecute: ")
            super.onPostExecute(s)
            if (!TextUtils.isEmpty(s)) {
                parseJson(s)
            }
        }




     }

    private fun parseJson(json: String) {
        try {
            Log.d(TAG, "parseJson: $json")
            val description = Gson().fromJson(json, PackageDetails::class.java)
            if (description != null) {
                try {
                    val versionCode: Float = this.packageManager
                        .getPackageInfo(this.packageName, 0).versionName.toFloat()
                    Log.d(TAG, "parseJson:versionCode $versionCode")
                    val new_current_ver_code = description.versionCode?.toFloat()
                    val result: Boolean = new_current_ver_code!! > versionCode

                    if (result) {
                        Log.d(TAG, "parseJson: update found")
                        showDialog(description.updateMessage + "\t" + "version" + "\t" + new_current_ver_code,
                            description.url!!,
                            mIsAutoInstall,
                            mCheckExternal
                        )

                    } else {

                        Log.i(
                            TAG,
                            "app_no_new_update".toString() + "Remote: " + description.versionCode
                        )
                    }
                } catch (e: PackageManager.NameNotFoundException) {
                    Log.e(TAG, "parse json error" + e.message)
                } catch (e: java.lang.Exception) {
                    Log.e(TAG, e.toString())
                }
            }
        } catch (e: java.lang.Exception) {
            Log.e(TAG, e.toString())
        }
    }

    private fun showDialog(
        content: String,
        apkUrl: String,
        isAutoInstall: Boolean,
        checkExternal: Boolean
    ) {
        Log.d(TAG, "showDialog: ")
        val d = UpdateDialog()
        val args = Bundle()
        args.putString(Constants.APK_UPDATE_CONTENT, content)
        args.putString(Constants.APK_DOWNLOAD_URL, apkUrl)
        args.putBoolean(Constants.APK_IS_AUTO_INSTALL, isAutoInstall)
        args.putBoolean(Constants.APK_CHECK_EXTERNAL, checkExternal)
        d.arguments = args

        val ft: FragmentTransaction = this.supportFragmentManager.beginTransaction()
        ft.add(d, this.javaClass.simpleName)
        ft.commitAllowingStateLoss()
    }

}