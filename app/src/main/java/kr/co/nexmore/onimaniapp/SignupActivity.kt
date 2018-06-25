package kr.co.nexmore.onimaniapp

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.activity_signup.*
import kr.co.nexmore.onimaniapp.models.User

const val PICK_FROM_ALBUM = 10

class SignupActivity : AppCompatActivity() {

    private var splashBackground: String? = null

    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        val mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
        splashBackground = mFirebaseRemoteConfig.getString(getString(R.string.rc_color))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = Color.parseColor(splashBackground)
        }


        signup_iv_profile.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = MediaStore.Images.Media.CONTENT_TYPE
            startActivityForResult(intent, PICK_FROM_ALBUM)
        }

        signup_btn_signup.setBackgroundColor(Color.parseColor(splashBackground))
        signup_btn_signup.setOnClickListener {

            if ( signup_et_email.text.toString().isEmpty() ) {
                return@setOnClickListener
            }

            FirebaseAuth.getInstance()
                    .createUserWithEmailAndPassword(signup_et_email.text.toString(),
                            signup_et_password.text.toString())
                    .addOnCompleteListener(this) { task: Task<AuthResult> ->
                        var uid = task.result.user.uid
                        FirebaseStorage.getInstance().getReference("userImages").child(uid).putFile(imageUri!!).addOnCompleteListener {taskSnapshot: Task<UploadTask.TaskSnapshot> ->
                            @Suppress("DEPRECATION")
                            val imageUrl = taskSnapshot.result.downloadUrl.toString()

                            val user = User()
                            user.nickName = signup_et_name.text.toString()
                            user.profileUrl = imageUrl

                            FirebaseDatabase.getInstance().getReference("users").child(uid).setValue(user)
                        }

                    }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.d("SignupActivity", "onActivityResult")
        if ( requestCode == PICK_FROM_ALBUM && resultCode == RESULT_OK ) {
            signup_iv_profile.setImageURI(data!!.data)  // 가운데 뷰를 바꿈
            imageUri = data.data                        // 이미지 경로 원본
            Log.d("SignupActivity", "onActivityResult - imageUri : $imageUri")
        }
    }
}
