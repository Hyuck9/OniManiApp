package kr.co.nexmore.onimaniapp.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_appoint.*
import kotlinx.android.synthetic.main.item_appoint.view.*
import kr.co.nexmore.onimaniapp.R
import kr.co.nexmore.onimaniapp.models.Meet

class AppointListAdapter : RecyclerView.Adapter<AppointListAdapter.Holder>() {

    private val mMeetList = mutableListOf<Meet>()
    private var lastPosition = -1

    fun addItem(meet: Meet) {
        this.mMeetList.add(Meet())
        notifyDataSetChanged()
    }

    fun getItem(position: Int) = this.mMeetList[position]

    fun removeItem(item: Meet) {
        val position = getItemPosition(item.meetId!!)
        mMeetList.removeAt(position)
        notifyDataSetChanged()
    }

    /** 해당 room의 position 알아오기 */
    private fun getItemPosition(roomId: String) : Int {
        var position = 0
        this.mMeetList.forEach { meet ->
            if (meet.meetId!! == roomId) {
                return position
            }
            position++
        }
        return -1
    }

    /** 새로운 방 생성 시 애니메이션 셋팅 */
    private fun setAnimation(view: View?, position: Int) {
        if ( position > lastPosition ) {
            val animation = AnimationUtils.loadAnimation(view!!.context, android.R.anim.slide_in_left)
            view.startAnimation(animation)
            lastPosition = position
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val retView = LayoutInflater.from(parent.context).inflate(R.layout.item_appoint, parent, false)
        return Holder(retView)
    }

    override fun onBindViewHolder(holder: AppointListAdapter.Holder, position: Int) {
        mMeetList[position].let { meet ->
            with(holder) {
//                title.text = meet.title
//                place.text = meet.place
//                time.text = meet.time
//                setAnimation(rootView, position)
                appoint_i_tv_title.text = meet.title
                appoint_i_tv_place.text = meet.place
                appoint_i_tv_time.text = meet.time
                setAnimation(appoint_i_ll_rootView, position)
            }
        }
    }

    override fun getItemCount(): Int = mMeetList.size

//    inner class Holder(override val containerView: View?) : RecyclerView.ViewHolder(containerView), LayoutContainer {
//        val thumbnail = itemView.appoint_i_iv_thumb!!
//        val title = itemView.appoint_i_tv_title!!
//        val place = itemView.appoint_i_tv_place!!
//        val time = itemView.appoint_i_tv_time!!
//        val rootView = itemView.appoint_i_ll_rootView!!
//    }

    inner class Holder(view: View) : AndroidExtentionsViewHolder(view)

}