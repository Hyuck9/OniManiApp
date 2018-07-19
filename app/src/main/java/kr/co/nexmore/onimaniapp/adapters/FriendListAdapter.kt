package kr.co.nexmore.onimaniapp.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.item_friend.view.*
import kr.co.nexmore.onimaniapp.R
import kr.co.nexmore.onimaniapp.models.User

class FriendListAdapter : RecyclerView.Adapter<FriendListAdapter.Holder>() {

    companion object {
        const val UNSELECTION_MODE = 1
        const val SELECTION_MODE = 2
    }

    private var selectionMode = UNSELECTION_MODE

    private val friendList = mutableListOf<User>()

    fun setSelectionMode(selectionMode: Int) {
        this.selectionMode = selectionMode
        notifyDataSetChanged()
    }

    fun getSelectionMode(): Int {
        return this.selectionMode
    }

    fun addItem(friend: User) {
        this.friendList.add(friend)
        notifyDataSetChanged()
    }

    fun getItem(position: Int): User = this.friendList[position]


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val retView = LayoutInflater.from(parent.context).inflate(R.layout.item_friend, parent, false)
        return Holder(retView)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        friendList[position].let { friend ->
            with(holder) {
                nickNameView.text = friend.nickName
                emailView.text = friend.email
                friend.profileUrl?.let { url ->
                    Glide.with(itemView.context)
                            .load(url)
                            // .apply(RequestOptions().circleCrop()) RoundedImageView 사용으로 인해 해당 옵션 사용 안함
                            .into(thumbnail)
                }

                friendSelectedView.run {
                    isChecked = friend.isSelection
                    setOnCheckedChangeListener { _, isChecked ->
                        friend.isSelection = isChecked
                    }
                    visibility = if ( getSelectionMode() == UNSELECTION_MODE ) {
                        View.GONE
                    } else {
                        View.VISIBLE
                    }
                }
                /*friendSelectedView.isChecked = friend.isSelection
                friendSelectedView.setOnCheckedChangeListener { _, isChecked ->
                    friend.isSelection = isChecked
                }
                if ( this@FriendListAdapter.selectionMode == UNSELECTION_MODE ) {
                    friendSelectedView.visibility = View.GONE
                } else {
                    friendSelectedView.visibility = View.VISIBLE
                }*/

                itemView.setOnClickListener(null)
                itemView.setOnLongClickListener(null)

            }
        }
    }

    override fun getItemCount(): Int = friendList.size


    inner class Holder(view: View?) : RecyclerView.ViewHolder(view) {
        val cardView = itemView.friend_i_cardView!!
        val thumbnail = itemView.friend_i_thumb!!
        val friendSelectedView = itemView.friend_i_checkbox!!
        val nickNameView = itemView.friend_i_tv_name!!
        val emailView = itemView.friend_i_tv_email!!
    }
}