package kr.co.nexmore.onimaniapp.views

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_create_room.*
import kr.co.nexmore.onimaniapp.R
import kr.co.nexmore.onimaniapp.adapters.SelectFriendAdapter
import kr.co.nexmore.onimaniapp.common.utils.DateUtil
import kr.co.nexmore.onimaniapp.common.utils.ItemClickSupport
import kr.co.nexmore.onimaniapp.models.Meet
import kr.co.nexmore.onimaniapp.models.User

class CreateRoomActivity : AppCompatActivity() {

    private lateinit var mCurrentUser: FirebaseUser
    private lateinit var mFirebaseDatabase: FirebaseDatabase
    private lateinit var mAllUserDBRef: DatabaseReference
    private lateinit var mMyMeetsDBRef: DatabaseReference
    private lateinit var mMemberDBRef: DatabaseReference

    private lateinit var mFriendList: MutableList<User>
    private lateinit var mSelectFriendAdapter: SelectFriendAdapter

    private lateinit var mAddress: String
    private var mLongitude: Double = 0.0
    private var mLatitude: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_room)

        mCurrentUser = FirebaseAuth.getInstance().currentUser!!
        mFirebaseDatabase = FirebaseDatabase.getInstance()
        mAllUserDBRef = mFirebaseDatabase.getReference("users")
        mMyMeetsDBRef = mFirebaseDatabase.getReference("users").child(mCurrentUser.uid).child("meets")

        mFriendList = intent.getSerializableExtra("myFriends") as MutableList<User>
        mAddress = intent.getStringExtra("address")
        mLongitude = intent.getDoubleExtra("longitude", 0.0)
        mLatitude = intent.getDoubleExtra("latitude", 0.0)

        mSelectFriendAdapter = SelectFriendAdapter(mFriendList)
        mSelectFriendAdapter.allUnSelect()

        initRecyclerView()
        initButton()
    }

    /**
     * 리사이클러뷰 초기 셋팅
     */
    private fun initRecyclerView() {
        create_room_a_rv_add_friend_list.layoutManager = LinearLayoutManager(this)
        create_room_a_rv_add_friend_list.adapter = mSelectFriendAdapter
        ItemClickSupport.addTo(create_room_a_rv_add_friend_list).setOnItemClickListener(object: ItemClickSupport.OnItemClickListener {
            override fun onItemClicked(recyclerView: RecyclerView, position: Int, v: View) {
                selectionModeItemClick(position)
            }
        })
    }

    /**
     * 체크박스 클릭 이벤트
     */
    private fun selectionModeItemClick(position: Int) {
        mSelectFriendAdapter.getItem(position).let { friend ->
            friend.isSelection = !friend.isSelection
            mSelectFriendAdapter.notifyItemChanged(position)
        }
    }

    /**
     * 버튼 이벤트 셋팅
     */
    private fun initButton() {
        create_room_a_btn_ok.setOnClickListener {
            create_room_a_et_title.text.toString().let { title ->
                if ( title.isNotEmpty() ) {
                    if ( mSelectFriendAdapter.getSelectionUserCount() > 0 ) {
                        createRoom(title)
                    } else {
                        Toast.makeText(this, "친구를 선택해 주세요.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "제목을 입력해 주세요.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /**
     * 방 생성
     */
    private fun createRoom(title: String) {
        val meetId = mMyMeetsDBRef.push().key
        mMemberDBRef = mFirebaseDatabase.getReference("meet_members").child(meetId!!)
        val meet = Meet().apply {
            this.meetId = meetId
            this.title = title
            this.time = DateUtil.currentDate
            this.place = mAddress
            this.longitude = mLongitude
            this.latitude = mLatitude
        }

        val uidList = mSelectFriendAdapter.getSelectedUids().toMutableList()
        uidList.add(mCurrentUser.uid)

        uidList.forEachIndexed { index, uid ->
            mAllUserDBRef.child(uid!!).addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val member = snapshot.getValue(User::class.java).apply {
                        this!!.longitude = 0.0
                        this.latitude = 0.0
                        this.memberIndex = index
                    }
                    mMemberDBRef.child(member!!.uid!!)
                            .setValue(member) { _, _ ->
                                snapshot.ref.child("meets").child(meetId).setValue(meet)
                            }
                }

                override fun onCancelled(error: DatabaseError) {
                }

            })
        }

        //TODO: 방 생성 후 입장 로직 구현
        finish()
    }
}
