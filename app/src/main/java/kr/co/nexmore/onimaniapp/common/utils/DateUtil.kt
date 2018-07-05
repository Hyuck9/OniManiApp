package kr.co.nexmore.onimaniapp.common.utils

import java.text.SimpleDateFormat
import java.util.*

object DateUtil {

    /**
     * 현재 시간 리턴
     * @return yyyy-MM-dd HH:mm:ss
     */
    val currentDate: String
        get() {
            val calendar = Calendar.getInstance()
            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA)
            return formatter.format(calendar.time)
        }

}