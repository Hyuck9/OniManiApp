package kr.co.nexmore.onimaniapp.views

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kr.co.nexmore.onimaniapp.R

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import kr.co.nexmore.onimaniapp.common.transitions.FabTransform
import kr.co.nexmore.onimaniapp.models.User
import java.util.ArrayList

class MainActivity : AppCompatActivity() {

    private lateinit var mPagerAdapter : ViewPagerAdapter

    private lateinit var mFirebaseAuth: FirebaseAuth
    private lateinit var mFirebaseDatabase: FirebaseDatabase
    private lateinit var mUserDBRef: DatabaseReference

    private var isFabOpen = false
    private var isFriendDeleteMode = false
    private lateinit var fabOpen: Animation
    private lateinit var fabClose:Animation
    private lateinit var fabRClockWise:Animation
    private lateinit var fabRAntiClockWise:Animation

    private var mFriendList = mutableListOf<User>()

    companion object {
        const val FIND_FRIEND_REQUEST_CODE = 107
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(main_a_toolbar)
        main_a_toolbar.setLogo(R.mipmap.ic_launcher)
        main_a_tabs.setupWithViewPager(main_c_viewpager)
        this.setUpViewPager()

        initAnimation()
        setupFab()

        /*val i = Intent(this, MyLocationService::class.java)
        startService(i)*/
    }

    /** 친구 검색 창 에서 받아온 email로 친구 추가 로직 실행 */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if ( requestCode == FIND_FRIEND_REQUEST_CODE ) {
            if ( resultCode == Activity.RESULT_OK ) {
                // 친구목록 탭으로 이동
                main_c_viewpager.setCurrentItem(2, true)

                val inputEmail = data!!.getStringExtra("email")
                Log.d("MainActivity", "onActivityResult Success - [resultCode : $resultCode], [inputEmail : $inputEmail]")

                (mPagerAdapter.getItem(1) as FriendFragment).searchFriend(inputEmail)

            } else {
                Log.d("MainActivity", "onActivityResult Failure - [resultCode : $resultCode]")
            }
            fabCloseAction()
        }

    }

    /** FloatingActionButton 클릭 리스너 설정 */
    @SuppressLint("RestrictedApi")
    private fun setupFab() {
        /* 메인 fab 클릭 리스너 (애니메이션 기능) */
        main_a_fab_main.setOnClickListener {
            if ( isFriendDeleteMode ) {
                onClickFriendDelete()
            } else {
                if ( !isFabOpen ) {
                    fabOpenAction()
                } else {
                    fabCloseAction()
                }
            }
        }

        /* 친구추가 fab 클릭 리스너 (친구 추가 다이얼로그 열기) */
        main_a_fab_add_friend.setOnClickListener {
            val intent = Intent(this@MainActivity, AddFriendActivity::class.java)
            val color = ContextCompat.getColor(this@MainActivity, R.color.fab2_color)
            FabTransform.addExtras(intent, color, R.drawable.ic_person_add_white_24dp)
            val optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(this@MainActivity, main_a_fab_add_friend, getString(R.string.transition_name_add_friend))
            startActivityForResult(intent, FIND_FRIEND_REQUEST_CODE, optionsCompat.toBundle())
        }

        /* 방 생성 fab 클릭 리스너 */
        main_a_fab_create_room.setOnClickListener {

        }
    }

    /** 메인 fab 클릭 액션 */
    private fun fabOpenAction() {
        main_a_fab_main.startAnimation(fabRClockWise)
        main_a_fab_add_friend.startAnimation(fabOpen)
        main_a_fab_create_room.startAnimation(fabOpen)
        main_a_fab_add_friend.isClickable = true
        main_a_fab_create_room.isClickable = true
        isFabOpen = true
    }

    /** 메인 fab 취소 액션 */
    private fun fabCloseAction() {
        main_a_fab_main.startAnimation(fabRAntiClockWise)
        main_a_fab_add_friend.startAnimation(fabClose)
        main_a_fab_create_room.startAnimation(fabClose)
        main_a_fab_add_friend.isClickable = false
        main_a_fab_create_room.isClickable = false
        isFabOpen = false
    }

    /** 친구 삭제 다이얼로그 */
    private fun onClickFriendDelete() {
        val currentFragment = mPagerAdapter.getItem(1) as FriendFragment

        AlertDialog.Builder(this)
                .setMessage("친구를 삭제하시겠습니까?")
                .setPositiveButton("예") { dialog, _ ->
                    currentFragment.deleteFriend()
                    dialog.dismiss()
                    fabFriendUnDeleteMode()
                }
                .setNegativeButton("아니오") { dialog, _ ->
                    currentFragment.cancelDeleteFriend()
                    dialog.dismiss()
                    fabFriendUnDeleteMode()
                }
                .show()
    }

    /** 친구 삭제 모드 */
    fun fabFriendDeleteMode() {
        isFriendDeleteMode = true
        main_a_fab_main.startAnimation(fabRClockWise)
    }
    /** 친구 삭제 모드 해제 */
    private fun fabFriendUnDeleteMode() {
        isFriendDeleteMode = false
        main_a_fab_main.startAnimation(fabRAntiClockWise)
    }

    /** FloatingActionButton Animation 초기설정 */
    private fun initAnimation() {
        fabOpen = AnimationUtils.loadAnimation(this, R.anim.fab_open)
        fabClose = AnimationUtils.loadAnimation(this, R.anim.fab_close)
        fabRClockWise = AnimationUtils.loadAnimation(this, R.anim.rotate_clockwise)
        fabRAntiClockWise = AnimationUtils.loadAnimation(this, R.anim.rotate_anticlockwise)
    }

    /** viewPager 설정 (친구목록창, 방목록창) */
    private fun setUpViewPager() {
        mPagerAdapter = ViewPagerAdapter(supportFragmentManager)
        mPagerAdapter.addFragment(AppointFragment(), "약속")
        mPagerAdapter.addFragment(FriendFragment(), "친구")
        main_c_viewpager.adapter = mPagerAdapter
    }

    /** 방생성 시 방 멤버 추가?????????????????????????? */
    fun addMember(friend: User) {
        mFriendList.add(friend)
    }


    /** ViewPagerAdapter 이너 클래스 */
    private inner class ViewPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        private val fragmentList = ArrayList<Fragment>()
        private val fragmentTitleList = ArrayList<String>()

        override fun getItem(position: Int): Fragment {
            return fragmentList[position]
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return fragmentTitleList[position]
        }

        override fun getCount(): Int {
            return fragmentList.size
        }

        fun addFragment(fragment: Fragment, title: String) {
            fragmentList.add(fragment)
            fragmentTitleList.add(title)
        }
    }

}
