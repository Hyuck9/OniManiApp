package kr.co.nexmore.onimaniapp.models

import java.io.Serializable

class User : Serializable {

    var uid: String? = null
    var email: String? = null
    var nickName: String? = null
    var profileUrl: String? = null
    var thumbUrl: String? = null
    var joinedDate: String? = null
    var isSelection: Boolean = false
    var latitude: Double? = 0.0
    var longitude: Double? = 0.0
    var memberIndex: Int? = null
}
