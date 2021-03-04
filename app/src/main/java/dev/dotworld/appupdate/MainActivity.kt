package dev.dotworld.appupdate

import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import com.artwl.update.UpdateChecker
import com.artwl.update.entity.UpdateDescription


private const val APP_UPDATE_SERVER_URL = "http://192.168.29.49:3001/download/getdeatils"
private const val APK_IS_AUTO_INSTALL = true
class MainActivity : AppCompatActivity() {

    private val TAG=MainActivity::class.java.simpleName
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.d(TAG, "onCreate: ")
        findViewById<Button>(R.id.update).setOnClickListener {
            Log.d(TAG, "onclick: ")

            UpdateChecker.checkForDialog(
                this@MainActivity,
                APP_UPDATE_SERVER_URL,
                APK_IS_AUTO_INSTALL,
                false
            );
        }

    }


}