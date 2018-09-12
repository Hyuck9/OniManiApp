package kr.co.nexmore.onimaniapp.views

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.*
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.util.DisplayMetrics
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_meeting.*
import kr.co.nexmore.onimaniapp.R
import kr.co.nexmore.onimaniapp.models.Meet
import kr.co.nexmore.onimaniapp.models.User
import net.daum.mf.map.api.MapPOIItem
import net.daum.mf.map.api.MapPoint
import java.io.IOException
import java.io.InputStream
import java.net.URL


class MeetingActivity : AppCompatActivity() {

    private lateinit var mCurrentUser: FirebaseUser
    private lateinit var mFirebaseDatabase: FirebaseDatabase
    private lateinit var mMyDBRef: DatabaseReference
    private lateinit var mMyMeetsDBRef: DatabaseReference
    private lateinit var mMemberDBRef: DatabaseReference

    private lateinit var mLocationManager: LocationManager

    private val mMemberMarkerList = mutableListOf<MapPOIItem>()
    private val mThumbnailList = mutableListOf<Bitmap?>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meeting)

        val meetId = intent.getStringExtra("meet_id")

        mLocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        mCurrentUser = FirebaseAuth.getInstance().currentUser!!

        initDBRef(meetId)
        initAppointmentPlace()
        requestMyLocation()
        memberLocationListener()
    }

    /**
     * FireBase DB Reference 셋팅
     */
    private fun initDBRef(meetId: String) {
        mFirebaseDatabase = FirebaseDatabase.getInstance()
        mMyDBRef = mFirebaseDatabase.getReference("users").child(mCurrentUser.uid)
        mMyMeetsDBRef = mMyDBRef.child("meets").child(meetId)
        mMemberDBRef = mFirebaseDatabase.getReference("meet_members").child(meetId)
    }

    /**
     * Firebase DB에서 약속 장소 가지고와 지도에 표시
     * */
    private fun initAppointmentPlace() {
        mMyMeetsDBRef.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val meet = snapshot.getValue(Meet::class.java)!!
                showPlaceMarker(meet)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("initAppointmentPlace", "Database Error : [${error.code}] ${error.message}")
            }

        })
    }

    /**
     * 약속 장소 지도에 마커로 표시
     */
    private fun showPlaceMarker(meet: Meet) {
        val name = meet.place!!
        val placeMarker = MapPOIItem().apply {
            itemName = name
            tag = 0
            mapPoint = MapPoint.mapPointWithGeoCoord(meet.latitude!!, meet.longitude!!)
            markerType = MapPOIItem.MarkerType.BluePin
            selectedMarkerType = MapPOIItem.MarkerType.RedPin
        }

        meeting_a_mapView.addPOIItem(placeMarker)
        meeting_a_mapView.selectPOIItem(placeMarker, true)
        meeting_a_mapView.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(meet.latitude!!, meet.longitude!!), true)
    }

    /**
     * 방 멤버들 현재 위치 지도에 마커로 표시
     */
    private fun showMemberMarker(position: Int) {
        val memberMarker = mMemberMarkerList[position]
        meeting_a_mapView.removePOIItem(memberMarker)
        meeting_a_mapView.addPOIItem(memberMarker)
        meeting_a_mapView.selectPOIItem(memberMarker, true)
    }

    /**
     * 위치 측위 리스너
     */
    private val mLocationListener = object: LocationListener {
        override fun onLocationChanged(location: Location?) {
            val longitude = location!!.longitude  //경도
            val latitude = location.latitude      //위도
            val altitude = location.altitude      //고도
            val accuracy = location.accuracy        //정확도
            val provider = location.provider       //위치제공자

            Log.d("locationListener",
                    """위치정보 : $provider
                |위도 : $longitude
                |경도 : $latitude
                |고도 : $altitude
                |정확도 : $accuracy
            """.trimMargin())

            updateMyLocation(latitude, longitude)
        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
        override fun onProviderEnabled(provider: String?) {}
        override fun onProviderDisabled(provider: String?) {}
    }

    /**
     * 측위된 위경도값 FireBase DB에 저장
     */
    private fun updateMyLocation(latitude:Double, longitude:Double) {
        val geoPoint = mutableMapOf<String, Any>()
        geoPoint["latitude"] = latitude
        geoPoint["longitude"] = longitude
        mMyDBRef.updateChildren(geoPoint)
        mMemberDBRef.child(mCurrentUser.uid).updateChildren(geoPoint)
    }

    /**
     * 내 위치 측위 요청
     */
    private fun requestMyLocation() {
        val minTime:Long = 5_000
        val minDistance = 0F

        /* TODO : TedPermission 등 이용하여 퍼미션 부분 추후 수정
        TedPermission.with(this)
                .setPermissionListener(permissionlistener)
                .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
                .setPermissions(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                .check()*/

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        mLocationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                minTime,
                minDistance,
                mLocationListener
        )
        mLocationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                minTime,
                minDistance,
                mLocationListener
        )
    }

    /**
     * 방 멤버들 FireBase DB에 위치정보 변경될 경우 데이터 받는 리스너
     */
    private fun memberLocationListener() {
        mMemberDBRef.addChildEventListener(object: ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, s: String?) {
                val user = snapshot.getValue(User::class.java)!!
                val bitmap = BitmapFactory.decodeResource(resources, R.drawable.hg_icon)
                val memberMarker = createMarker(user, bitmap)
                mMemberMarkerList.add(memberMarker)
                mThumbnailList.add(null)
//                showMemberMarker(user.memberIndex)
//                downloadTask.execute(user)

            }

            override fun onChildChanged(snapshot: DataSnapshot, s: String?) {
                val user = snapshot.getValue(User::class.java)!!

                if ( mThumbnailList[user.memberIndex] != null ) {
                    mMemberMarkerList[user.memberIndex] = createMarker(user, mThumbnailList[user.memberIndex]!!)
                    showMemberMarker(user.memberIndex)
                } else {
                    downloadTask.execute(user)
                }

                /*
                mMemberMarkerList[user.memberIndex] = mMemberMarkerList[user.memberIndex].apply {
                    itemName = user.nickName
                    mapPoint = MapPoint.mapPointWithGeoCoord(user.latitude, user.longitude)
                }
                showMemberMarker(user.memberIndex)*/
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {}

            override fun onChildMoved(snapshot: DataSnapshot, s: String?) {}

            override fun onCancelled(snapshot: DatabaseError) {}

        })
    }


    /*private val permissionlistener: PermissionListener = object : PermissionListener {
        override fun onPermissionGranted() {
            Toast.makeText(this@MeetingActivity, "Permission Granted", Toast.LENGTH_SHORT).show()
        }

        override fun onPermissionDenied(deniedPermissions: List<String>) {
            Toast.makeText(this@MeetingActivity, "Permission Denied\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show()
        }


    }*/


    private fun createMarker(user: User, bitmap: Bitmap): MapPOIItem {
        return MapPOIItem().apply {
            itemName = user.nickName
            tag = 1
            mapPoint = MapPoint.mapPointWithGeoCoord(user.latitude, user.longitude)
            markerType = MapPOIItem.MarkerType.CustomImage
            customImageBitmap = bitmap
            isCustomImageAutoscale = false
            setCustomImageAnchor(0.5F, 0.5F)
        }
    }

    override fun onPause() {
        downloadTask.cancel(true)
        super.onPause()
    }

    @SuppressLint("StaticFieldLeak")
    val downloadTask = object: AsyncTask<User, Int, Int>() {
        override fun doInBackground(vararg params: User?): Int {
            val user = params[0]!!
            val url = URL(user.profileUrl)
            val bitmap = BitmapFactory.decodeStream(url.content as InputStream?)

            val memberMarker = createMarker(user, bitmap)
            mMemberMarkerList[user.memberIndex] = memberMarker
            mThumbnailList[user.memberIndex] = bitmap

            return user.memberIndex
        }

        override fun onPostExecute(result: Int?) {
            showMemberMarker(result!!)
        }
    }


}
