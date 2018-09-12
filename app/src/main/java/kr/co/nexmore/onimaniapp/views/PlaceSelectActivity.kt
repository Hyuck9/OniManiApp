package kr.co.nexmore.onimaniapp.views

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import kotlinx.android.synthetic.main.activity_place_select.*
import kr.co.nexmore.onimaniapp.R
import kr.co.nexmore.onimaniapp.models.User
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapReverseGeoCoder
import net.daum.mf.map.api.MapView

class PlaceSelectActivity : AppCompatActivity(), MapReverseGeoCoder.ReverseGeoCodingResultListener, MapView.MapViewEventListener {

    companion object {
        private const val REQUEST_LOCATION = 100
    }

    private lateinit var mFriendList: MutableList<User>

    private lateinit var mReverseGeoCoder: MapReverseGeoCoder

    private var mLatitude: Double = 0.0
    private var mLongitude: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_place_select)

        @Suppress("UNCHECKED_CAST")
        mFriendList = intent.getSerializableExtra("myFriends") as MutableList<User>

        place_select_a_map_view.setMapViewEventListener(this)
        place_select_a_btn_next.setOnClickListener {
            val address = if ( place_select_a_et_address.text.toString().isEmpty() ) {
                place_select_a_et_address.hint.toString()
            } else  {
                place_select_a_et_address.text.toString()
            }
            val intent = Intent(this, CreateRoomActivity::class.java)
            intent.putExtra("myFriends", ArrayList(mFriendList))
            intent.putExtra("address", address)
            intent.putExtra("longitude", mLongitude)
            intent.putExtra("latitude", mLatitude)
            startActivity(intent)
            finish()
        }
    }



    private fun onFinishReverseGeoCoding(address: String) {
        place_select_a_et_address.hint = address
    }

    /**
     * MapReverseGeoCoder 클래스를 이용하여 지도 좌표로 주소 정보를 가져올 수 있다.
     * -> 주소를 찾은 경우
     * */
    override fun onReverseGeoCoderFoundAddress(mapReverseGeoCoder: MapReverseGeoCoder?, addressString: String?) {
        onFinishReverseGeoCoding(addressString!!)
    }

    /**
     * MapReverseGeoCoder 클래스를 이용하여 지도 좌표로 주소 정보를 가져올 수 있다.
     * -> 호출에 실패한 경우
     * */
    override fun onReverseGeoCoderFailedToFindAddress(mapReverseGeoCoder: MapReverseGeoCoder?) {
        onFinishReverseGeoCoding("주소 정보 없음")
    }



    /**
     * MapViewEventListener interface를 구현하는 객체를 MapView 객체에 등록하여
     * 지도 이동/확대/축소, 지도 화면 터치(Single Tap / Double Tap / Long Press) 이벤트를 통보받을 수 있다.
     * -> MapView가 사용가능 한 상태가 되었음을 알려준다.
     *    onMapViewInitialized()가 호출된 이후에 MapView 객체가 제공하는 지도 조작 API들을 사용할 수 있다.
     * */
    override fun onMapViewInitialized(mapView: MapView?) {
        place_select_a_map_view.setMapCenterPointAndZoomLevel(MapPoint.mapPointWithGeoCoord(37.56640625, 126.97787475585938), 5, true)
        val mapPointGeo = place_select_a_map_view.mapCenterPoint.mapPointGeoCoord!!
        mLatitude = mapPointGeo.latitude
        mLongitude = mapPointGeo.longitude
        Log.d("lhg", "onMapViewInitialized - 현재 위치 : $mLatitude, $mLongitude")

        mReverseGeoCoder = MapReverseGeoCoder(getString(R.string.kakao_app_key), mapView!!.mapCenterPoint, this, this)
        mReverseGeoCoder.startFindingAddress()
    }

    /**
     * MapViewEventListener interface를 구현하는 객체를 MapView 객체에 등록하여
     * 지도 이동/확대/축소, 지도 화면 터치(Single Tap / Double Tap / Long Press) 이벤트를 통보받을 수 있다.
     * -> 지도 중심 좌표가 이동한 경우 호출된다.
     * */
    override fun onMapViewCenterPointMoved(mapView: MapView?, mapCenterPoint: MapPoint?) {
    }

    /**
     * MapViewEventListener interface를 구현하는 객체를 MapView 객체에 등록하여
     * 지도 이동/확대/축소, 지도 화면 터치(Single Tap / Double Tap / Long Press) 이벤트를 통보받을 수 있다.
     * -> 지도 확대/축소 레벨이 변경된 경우 호출된다.
     * */
    override fun onMapViewZoomLevelChanged(mapView: MapView?, zoomLevel: Int) {
    }

    /**
     * MapViewEventListener interface를 구현하는 객체를 MapView 객체에 등록하여
     * 지도 이동/확대/축소, 지도 화면 터치(Single Tap / Double Tap / Long Press) 이벤트를 통보받을 수 있다.
     * -> 사용자가 지도 위를 터치한 경우 호출된다.
     * */
    override fun onMapViewSingleTapped(mapView: MapView?, mapPoint: MapPoint?) {
    }

    /**
     * MapViewEventListener interface를 구현하는 객체를 MapView 객체에 등록하여
     * 지도 이동/확대/축소, 지도 화면 터치(Single Tap / Double Tap / Long Press) 이벤트를 통보받을 수 있다.
     * -> 사용자가 지도 위 한 지점을 더블 터치한 경우 호출된다.
     * */
    override fun onMapViewDoubleTapped(mapView: MapView?, mapPoint: MapPoint?) {
    }

    /**
     * MapViewEventListener interface를 구현하는 객체를 MapView 객체에 등록하여
     * 지도 이동/확대/축소, 지도 화면 터치(Single Tap / Double Tap / Long Press) 이벤트를 통보받을 수 있다.
     * -> 사용자가 지도 위 한 지점을 길게 누른 경우(long press) 호출된다.
     * */
    override fun onMapViewLongPressed(mapView: MapView?, mapPoint: MapPoint?) {
    }

    /**
     * MapViewEventListener interface를 구현하는 객체를 MapView 객체에 등록하여
     * 지도 이동/확대/축소, 지도 화면 터치(Single Tap / Double Tap / Long Press) 이벤트를 통보받을 수 있다.
     * -> 사용자가 지도 드래그를 시작한 경우 호출된다.
     * */
    override fun onMapViewDragStarted(mapView: MapView?, mapPoint: MapPoint?) {
        place_select_a_iv_selected_location.setImageResource(R.drawable.map_pin2)
    }

    /**
     * MapViewEventListener interface를 구현하는 객체를 MapView 객체에 등록하여
     * 지도 이동/확대/축소, 지도 화면 터치(Single Tap / Double Tap / Long Press) 이벤트를 통보받을 수 있다.
     * -> 사용자가 지도 드래그를 끝낸 경우 호출된다.
     * */
    override fun onMapViewDragEnded(mapView: MapView?, mapPoint: MapPoint?) {
        place_select_a_iv_selected_location.setImageResource(R.drawable.map_pin)
    }

    /**
     * MapViewEventListener interface를 구현하는 객체를 MapView 객체에 등록하여
     * 지도 이동/확대/축소, 지도 화면 터치(Single Tap / Double Tap / Long Press) 이벤트를 통보받을 수 있다.
     * -> 지도의 이동이 완료된 경우 호출된다.
     * */
    override fun onMapViewMoveFinished(mapView: MapView?, mapPoint: MapPoint?) {
        val mapPointGeo = mapView!!.mapCenterPoint.mapPointGeoCoord
        mLatitude = mapPointGeo.latitude
        mLongitude = mapPointGeo.longitude
        mReverseGeoCoder = MapReverseGeoCoder(getString(R.string.kakao_app_key), mapView.mapCenterPoint, this, this)
        mReverseGeoCoder.startFindingAddress()
    }
}
