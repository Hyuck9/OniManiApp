package kr.co.nexmore.onimaniapp.views

import android.os.Bundle
import android.support.annotation.NonNull
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.fragment_friends.*
import kotlinx.android.synthetic.main.fragment_friends.view.*
import kr.co.nexmore.onimaniapp.R
import kr.co.nexmore.onimaniapp.adapters.FriendListAdapter
import kr.co.nexmore.onimaniapp.models.User


/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [FriendFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [FriendFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class FriendFragment : Fragment() {

    private lateinit var mCurrentUser: FirebaseUser
    private lateinit var mFirebaseDatabase: FirebaseDatabase
    private lateinit var mAllUserDBRef: DatabaseReference
    private lateinit var mMyDBRef: DatabaseReference
    private lateinit var mMyFriendsDBRef: DatabaseReference

    private var mFriendListAdapter: FriendListAdapter? = null

    private lateinit var mainCtx : MainActivity

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val retView = inflater.inflate(R.layout.fragment_friends, container, false)

        mCurrentUser = FirebaseAuth.getInstance().currentUser!!
        mFirebaseDatabase = FirebaseDatabase.getInstance()

        mAllUserDBRef = mFirebaseDatabase.getReference("users")
        mMyDBRef = mAllUserDBRef.child(mCurrentUser.uid)
        mMyFriendsDBRef = mMyDBRef.child("friends")

        mFriendListAdapter = FriendListAdapter()

        mainCtx = activity as MainActivity

        /* 리사이클러뷰 초기 셋팅 */
        retView.friends_f_rv_friend_list.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = mFriendListAdapter
        }

        addFriendListener()

        return retView
    }


    /**
     * FirebaseDatabase에서 친구목록을 가지고와서 mFriendListAdapter에 추가
     */
    private fun addFriendListener() {
        mMyFriendsDBRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {

                dataSnapshot.getValue(User::class.java)?.let { friend ->
                    mainCtx.addMember(friend)
                    drawUI(friend)
                }

            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {}

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {}

            override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {}

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun drawUI(friend: User) {
        mFriendListAdapter!!.addItem(friend)
    }

    fun deleteFriend() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun cancelDeleteFriend() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    /** 친구 추가 시 받아온 email 주소로 FireBaseDatabase 검색 */
    fun searchFriend(@NonNull inputEmail: String?) {

        if ( inputEmail.equals(mCurrentUser.email) ) {
            Snackbar.make(friends_f_rv_friend_list, "본인은 친구로 등록할 수 없습니다.", Snackbar.LENGTH_LONG).show()
            return
        }

        // FireBaseDatabase 에서 자신의 친구 목록을 조회하여 이미 등록된 친구인지 판단
        mMyFriendsDBRef.addListenerForSingleValueEvent(object: ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.iterator().forEach {
                    val user = it.getValue(User::class.java)

                    if ( user!!.email.equals(inputEmail) ) {
                        Snackbar.make(friends_f_rv_friend_list, "이미 등록된 친구입니다.", Snackbar.LENGTH_LONG).show()
                        return
                    }
                }

                // 여기까지 왔으면 등록되지 않은 친구
                userValidation(inputEmail)
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }

    /** FireBaseDatabase 에서 users db에 존재하는지 확인. 존재하지 않으면 미 가입자 */
    private fun userValidation(inputEmail: String?) {
        mAllUserDBRef.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userCount = snapshot.childrenCount
                var loopCount = 1

                snapshot.children.iterator().forEach {
                    val user = it.getValue(User::class.java)
                    if ( user!!.email.equals(inputEmail) ) {
                        // user db에 inputEmail이 존재 하면 친구 추가
                        // TODO: 친구 수락 기능 넣어야 함
                        addFriend(user)
                    } else {
                        // TODO: return 위치 변경 테스트
                        if ( userCount <= loopCount++ ) {
                            Snackbar.make(friends_f_rv_friend_list, "가입을 하지 않은 사용자입니다.", Snackbar.LENGTH_LONG).show()
                            return
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }

    /** 친구 등록 로직 */
    private fun addFriend(friend: User) {
        mMyFriendsDBRef.push().setValue(friend) { databaseError, databaseReference ->
            // 내 정보를 가져온다.
            mMyDBRef.addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.getValue(User::class.java).let { me ->
                        // 상대방에게 내 정보 등록
                        // users/{상대방 uid}/friends/{내 uid}/내정보 등록
                        mAllUserDBRef.child(friend.uid!!).child("friends").push().setValue(me)
                        Snackbar.make(friends_f_rv_friend_list, "친구등록이 완료되었습니다.", Snackbar.LENGTH_LONG).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }

            })
        }
    }

}
