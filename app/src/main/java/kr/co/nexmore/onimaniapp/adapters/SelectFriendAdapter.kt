package kr.co.nexmore.onimaniapp.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.item_select_friends.*
import kr.co.nexmore.onimaniapp.R
import kr.co.nexmore.onimaniapp.models.User

class SelectFriendAdapter(private var friendList: MutableList<User>) : RecyclerView.Adapter<SelectFriendAdapter.Holder>() {

    /**
     * 선택된 친구 개수
     */
    fun getSelectionUserCount(): Int {
        var selectedCount = 0
        friendList.forEach { user ->
            if ( user.isSelection ) {
                selectedCount++
            }
        }
        return selectedCount
    }

    /**
     * 전체 해제
     */
    fun allUnSelect() {
        friendList.forEach { user ->
            user.isSelection = false
        }
    }

    /**
     * 선택된 User의 uid 목록
     */
    fun getSelectedUids(): Array<String?> {
        val selectedUids = arrayOfNulls<String>(getSelectionUserCount())
        var i = 0
        friendList.forEach { user ->
            if ( user.isSelection ) {
                selectedUids[i++] = user.uid
            }
        }
        return selectedUids
    }

    /**
     * 특정 User 가져오기
     */
    fun getItem(position: Int): User = friendList[position]

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val retView = LayoutInflater.from(parent.context).inflate(R.layout.item_select_friends, parent, false)
        return Holder(retView)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val friend = getItem(position)
        with(holder) {
            select_friend_i_name.text = friend.nickName
            friend.profileUrl?.let { url ->
                Glide.with(itemView)
                        .load(url)
                        .into(select_friend_i_thumb)
            }
            select_friend_i_checkbox.setOnCheckedChangeListener(null)
            select_friend_i_checkbox.isChecked = friend.isSelection
            select_friend_i_checkbox.setOnCheckedChangeListener { _, isChecked -> friend.isSelection = isChecked }
        }
    }

    override fun getItemCount(): Int = friendList.size


    inner class Holder(view: View) : AndroidExtensionsViewHolder(view)
}