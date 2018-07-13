package kr.co.nexmore.onimaniapp.views

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import kotlinx.android.synthetic.main.activity_login.*
import kr.co.nexmore.onimaniapp.R

class LoginActivity : AppCompatActivity() {

    private lateinit var mFirebaseRemoteConfig: FirebaseRemoteConfig

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
        val splashBackground = mFirebaseRemoteConfig.getString(getString(R.string.rc_color))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = Color.parseColor(splashBackground)
        }

        login_btn_login.setBackgroundColor(Color.parseColor(splashBackground))
        login_btn_signup.setBackgroundColor(Color.parseColor(splashBackground))

        login_btn_signup.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }
    }
}
