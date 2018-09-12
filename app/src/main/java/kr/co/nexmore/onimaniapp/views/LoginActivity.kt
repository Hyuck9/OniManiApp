package kr.co.nexmore.onimaniapp.views

import android.content.Intent
import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import kotlinx.android.synthetic.main.activity_login.*
import kr.co.nexmore.onimaniapp.R

class LoginActivity : AppCompatActivity() {

    private lateinit var mFirebaseRemoteConfig: FirebaseRemoteConfig
    private lateinit var mFirebaseAuth: FirebaseAuth
    private lateinit var mAuthStateListener: FirebaseAuth.AuthStateListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
        mFirebaseAuth = FirebaseAuth.getInstance()
        mFirebaseAuth.signOut()

        val splashBackground = mFirebaseRemoteConfig.getString(getString(R.string.rc_color))
        window.statusBarColor = Color.parseColor(splashBackground)

        // 로그인 인터페이스 리스너
        mAuthStateListener = FirebaseAuth.AuthStateListener { _ ->
            // mFirebaseAuth.currentUser != null 작업 수행 (Safe calls + let 사용)
            mFirebaseAuth.currentUser?.let {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }

        login_a_btn_login.setBackgroundColor(Color.parseColor(splashBackground))
        login_a_btn_signup.setBackgroundColor(Color.parseColor(splashBackground))

        login_a_btn_login.setOnClickListener {
            loginEvent()
        }
        login_a_btn_signup.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }
    }

    private fun loginEvent() {
        val id = login_a_et_id.text.toString()
        val pw = login_a_et_password.text.toString()

        if ( id.isEmpty() || pw.isEmpty() ) {
            Toast.makeText(this, "아이디와 비밀번호를 입력해 주세요.", Toast.LENGTH_LONG).show()
            return
        }
        mFirebaseAuth.signInWithEmailAndPassword(id, pw).addOnCompleteListener {
            if (!it.isSuccessful) {
                // 로그인 실패
                Toast.makeText(this, it.exception!!.message , Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        mFirebaseAuth.addAuthStateListener(mAuthStateListener)
    }

    override fun onStop() {
        super.onStop()
        mFirebaseAuth.removeAuthStateListener(mAuthStateListener)
    }

}
