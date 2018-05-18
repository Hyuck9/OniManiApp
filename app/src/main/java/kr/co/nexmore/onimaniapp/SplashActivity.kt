package kr.co.nexmore.onimaniapp

import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import kotlinx.android.synthetic.main.activity_splash.*

class SplashActivity : AppCompatActivity() {

    private lateinit var mFirebaseRemoteConfig: FirebaseRemoteConfig

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
        val configSettings = FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build()
        mFirebaseRemoteConfig.setConfigSettings(configSettings)

        // 인앱 기본값 설정
        mFirebaseRemoteConfig.setDefaults(R.xml.default_config)

        mFirebaseRemoteConfig.fetch(3600)
                .addOnCompleteListener(this) {
                    Log.d("SplashActivity", "addOnCompleteListener")
                    if ( it.isSuccessful ) {
                        Log.d("SplashActivity", "addOnCompleteListener - isSuccessful")
                        mFirebaseRemoteConfig.activateFetched()
                    } else {
                        Log.d("SplashActivity", "addOnCompleteListener - failure")
                    }
                    displayMessage()
                }
    }

    private fun displayMessage() {
        val splashBackground = mFirebaseRemoteConfig.getString("splash_background")
        val caps = mFirebaseRemoteConfig.getBoolean("splash_message_caps")
        val splashMessage = mFirebaseRemoteConfig.getString("splash_message")

        splashactivity_linearLayout.setBackgroundColor(Color.parseColor(splashBackground))

        if ( caps ) {
            val builder = AlertDialog.Builder(this)
            builder.setMessage(splashMessage).setPositiveButton("확인"){ _, _ -> finish() }
            builder.create().show()
        }
    }
}
