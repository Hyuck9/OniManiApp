package kr.co.nexmore.onimaniapp.views

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.fragment_appoint.*

import kr.co.nexmore.onimaniapp.R
import kr.co.nexmore.onimaniapp.adapters.AppointListAdapter
import kr.co.nexmore.onimaniapp.common.utils.ItemClickSupport
import kr.co.nexmore.onimaniapp.models.Meet


class AppointFragment : Fragment() {

    private lateinit var mCurrentUser: FirebaseUser
    private lateinit var mFirebaseDatabase: FirebaseDatabase
    private lateinit var mMeetsDBRef: DatabaseReference
    private lateinit var mMemberDBRef: DatabaseReference

    private lateinit var mAppointListAdapter: AppointListAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val retView = inflater.inflate(R.layout.fragment_appoint, container, false)

        mCurrentUser = FirebaseAuth.getInstance().currentUser!!
        mFirebaseDatabase = FirebaseDatabase.getInstance()

        mMeetsDBRef = mFirebaseDatabase.getReference("users").child(mCurrentUser.uid).child("meets");
        mMemberDBRef = mFirebaseDatabase.getReference("meet_members")

        mAppointListAdapter = AppointListAdapter()

        return retView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
        addMeetListener()
    }

    /** 리사이클러뷰 초기 셋팅 */
    private fun initRecyclerView() {
        appoint_f_rv_meet_list.run {
            layoutManager = GridLayoutManager(activity, 2)
            adapter = mAppointListAdapter

            /** item Click Listener */
            ItemClickSupport.addTo(this).setOnItemClickListener(object: ItemClickSupport.OnItemClickListener {
                override fun onItemClicked(recyclerView: RecyclerView, position: Int, v: View) {
                    /** 방 입장 */
                    val meet = mAppointListAdapter.getItem(position)
                    val intent = Intent(activity, MeetingActivity::class.java)
                    intent.putExtra("meet_id", meet.meetId)
                    startActivity(intent)
                }
            })
            /** item Long Click Listener */
            ItemClickSupport.addTo(this).setOnItemLongClickListener(object: ItemClickSupport.OnItemLongClickListener {
                override fun onItemLongClicked(recyclerView: RecyclerView, position: Int, v: View): Boolean {
                    val meet = mAppointListAdapter.getItem(position)
                    /** 방 나가기 */
                    leaveRoom(meet)
                    return true
                }
            })
        }
    }

    /** FirebaseDatabase에서 친구목록을 가지고와서 mFriendListAdapter에 추가 */
    private fun addMeetListener() {
        mMeetsDBRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
                // 방 생성 (DB에서 읽어와 그려주기)
                dataSnapshot.getValue(Meet::class.java)?.let { friend ->
                    drawUI(friend)
                }

            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {
                // 방 업데이트
            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                // 방 삭제
                val meet = dataSnapshot.getValue(Meet::class.java)
                mAppointListAdapter.removeItem(meet!!)
            }

            override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {}

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    /** 방 삭제 로직 */
    fun leaveRoom(meet: Meet) {
        Snackbar.make(view!!, "선택된 방을 나가시겠습니까?", Snackbar.LENGTH_LONG).setAction("예") {
            // 내 방 목록 제거
            // users > {user_id} > meets > {meet_id} 제거
            mMeetsDBRef.child(meet.meetId!!).removeValue{ _, _ ->
                // 방 멤버 목록에서 제거
                // meeting_members > {plan_id} > {user_id} 제거
                mMemberDBRef.child(meet.meetId!!).child(mCurrentUser.uid).removeValue { _, _ ->
                    // TODO: 방 삭제 후처리 로직 여기에 구현
                }
            }
        }.show()
    }

    /** 방 목록이 없을 때 보여줄 문구 GONE/VISIBLE 설정 */
    private fun initVisible() {
        if ( mAppointListAdapter.itemCount > 0 ) {
            appoint_f_rv_meet_list.visibility = View.VISIBLE
            appoint_f_tv_none_meet.visibility = View.GONE
        } else {
            appoint_f_rv_meet_list.visibility = View.GONE
            appoint_f_tv_none_meet.visibility = View.VISIBLE
        }
    }

    /** 방 생성 UI 그려주기 */
    private fun drawUI(meet: Meet) {
        mAppointListAdapter.addItem(meet)
        initVisible()
    }

}
