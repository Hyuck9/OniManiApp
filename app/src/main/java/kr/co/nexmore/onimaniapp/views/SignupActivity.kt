package kr.co.nexmore.onimaniapp.views

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.activity_signup.*
import kr.co.nexmore.onimaniapp.R
import kr.co.nexmore.onimaniapp.common.utils.DateUtil
import kr.co.nexmore.onimaniapp.models.User

class SignupActivity : AppCompatActivity() {

    companion object {
        private const val PICK_FROM_ALBUM = 10
    }

    private var splashBackground: String? = null

    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        FirebaseRemoteConfig.getInstance().apply {
            splashBackground = getString(getString(R.string.rc_color))
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = Color.parseColor(splashBackground)
        }


        signup_iv_profile.setOnClickListener { profileImageClicked() }

        signup_btn_signup.apply {
            setBackgroundColor(Color.parseColor(splashBackground))
            setOnClickListener { signUpButtonClicked() }
        }

    }

    /**
     * 프로필 이미지 등록 버튼 이벤트
     */
    private fun profileImageClicked() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = MediaStore.Images.Media.CONTENT_TYPE
        startActivityForResult(intent, PICK_FROM_ALBUM)
    }

    /**
     * 회원가입 버튼 이벤트
     */
    private fun signUpButtonClicked() {
        val name = signup_et_nickname.text.toString()
        val email = signup_et_email.text.toString()
        val password = signup_et_password.text.toString()

        if ( name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty() ) {
            imageUri?.let {
                createUser(email, password, name)
            }
        }
    }

    /**
     * Firebase Authentication에 User Email 등록
     * Firebase Storage에 User Profile Image 등록
     * Firebase Database에 User 정보 등록
     */
    private fun createUser(email: String, password: String, name: String) {
        FirebaseAuth.getInstance()
                .createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task: Task<AuthResult> ->
                    val uid = task.result.user.uid
                    FirebaseStorage.getInstance()
                            .getReference("userImages")
                            .child(uid)
                            .putFile(imageUri!!)
                            .addOnCompleteListener { taskSnapshot: Task<UploadTask.TaskSnapshot> ->
                                if ( taskSnapshot.isSuccessful ) {
                                    taskSnapshot.result.storage.downloadUrl.addOnSuccessListener { uri ->
                                        val user = User()
                                        user.profileUrl = uri.toString()
                                        user.email = email
                                        user.uid = uid
                                        user.nickName = name
                                        user.joinedDate = DateUtil.currentDate

                                        FirebaseDatabase.getInstance().getReference("users").child(uid).setValue(user).addOnCompleteListener { task ->
                                            //TODO: 로딩 프로그레스 만들기
                                            if ( task.isSuccessful ) {
                                                this@SignupActivity.finish()
                                            } else {
                                                Toast.makeText(this, "가입 실패 (DB 오류)", Toast.LENGTH_SHORT).show()
                                            }
                                        }

                                    }
                                } else {
                                    Toast.makeText(this, "사진 업로드 실패", Toast.LENGTH_SHORT).show()
                                }
                            }
                }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.d("SignupActivity", "onActivityResult")
        if ( requestCode == PICK_FROM_ALBUM && resultCode == RESULT_OK ) {
            //TODO: 추후 이미지 리사이징 처리
            signup_iv_profile.setImageURI(data!!.data)  // 가운데 뷰를 바꿈
            imageUri = data.data                        // 이미지 경로 원본
            Log.d("SignupActivity", "onActivityResult - imageUri : $imageUri")
        }
    }
}
