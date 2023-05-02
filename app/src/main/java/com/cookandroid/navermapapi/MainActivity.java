package com.cookandroid.navermapapi;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.overlay.CircleOverlay;
import com.naver.maps.map.util.FusedLocationSource;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    // 위치 권한 허가 코드
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private static final int REQUEST_BACKGROUND_LOCATION = 100;

    // 위치 반환 구현체
    private FusedLocationSource locationSource;
    // 네이버 지도 객체
    private NaverMap naverMap;
    // 위치 권한 목록 (0. 정확, 1. 대략)
    private static final String[] PERMISSION = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };
    // 핸들러 : 백그라운드 작동 위함
    private Handler mhandler = new Handler(Looper.getMainLooper());

    //원형 영역 객체
    private CircleOverlay mCircleOverlay;
    private double CIRCLE_CENTER_LATITUDE = 37.3852172;
    private double CIRCLE_CENTER_LONGITUDE = 126.9352657;
    private final float GEOFENCE_RADIUS = 200;

    private boolean isRunnableRunning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e("RequestTest_1_1", "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 지도 프래그먼트 생성
        FragmentManager fm = getSupportFragmentManager();
        MapFragment mapFragment = (MapFragment)fm.findFragmentById(R.id.map);
        if (mapFragment == null) {
            mapFragment = MapFragment.newInstance();
            fm.beginTransaction().add(R.id.map, mapFragment).commit();
        }
        mapFragment.getMapAsync(this);
        Log.e("RequestTest_1_2", "fragmentCreate");

        // 위치 권한 물음
        locationSource = new FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE);

        // 포그라운드 서비스 시작
        Intent serviceIntent = new Intent(this, MyForegroundService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.e("RequestTest_3_1", "onRequestPermissionResult");
        if (locationSource != null && locationSource.onRequestPermissionsResult(requestCode, permissions, grantResults)) {
            if (!locationSource.isActivated()) {
                Log.e("RequestTest_3_2", "Permission_Deny");
                naverMap.setLocationTrackingMode(LocationTrackingMode.None);
            }
            else {
                Log.e("RequestTest_3_3", "Permission_Ok");
                naverMap.setLocationTrackingMode(LocationTrackingMode.Follow);
            }
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        Log.e("RequestTest_2_1", "onMapReady");
        // 네이버 지도 생성
        this.naverMap = naverMap;
        naverMap.setLocationSource(locationSource);
        Log.e("RequestTest_2_2", "mapCreate");

        // 위치 권한 요청
        ActivityCompat.requestPermissions(this, PERMISSION, LOCATION_PERMISSION_REQUEST_CODE);

        // 내 위치 반환
        requestMyLocation(naverMap);

        // 역 근처 200m 표시
        LatLng geofenceCenter = new LatLng(CIRCLE_CENTER_LATITUDE, CIRCLE_CENTER_LONGITUDE);

        mCircleOverlay = new CircleOverlay();
        mCircleOverlay.setCenter(geofenceCenter);
        mCircleOverlay.setRadius(GEOFENCE_RADIUS);
        mCircleOverlay.setColor(0x220000FF);
        mCircleOverlay.setOutlineColor(0xFF0000FF);
        mCircleOverlay.setOutlineWidth(2);
        mCircleOverlay.setMap(naverMap);

        // 서비스 실행
        Intent serviceIntent = new Intent(this, MyForegroundService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        }
    }

    public void requestMyLocation(NaverMap naverMap) {
        Log.e("RequestTest_4_1", "requestMyLocation");
        mhandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // 백그라운드에서 작성하는 로직 수행
                // 위치 변경 시 표시
                naverMap.addOnLocationChangeListener(location ->
                        Log.e("RequestTest_4_2", "위도 : " + location.getLatitude() +
                                ", 경도 : " + location.getLongitude()));
                naverMap.addOnLocationChangeListener(location ->
                        intoThePoint(location));
            }
        }, 1000);
    }

    public void intoThePoint(Location location) {
        Log.e("RequestTest_5_1", "intoThePoint");
        double myLatitude = location.getLatitude();
        double myLongitude = location.getLongitude();

        float[] distance = new float[1];
        Location.distanceBetween(myLatitude, myLongitude, CIRCLE_CENTER_LATITUDE, CIRCLE_CENTER_LONGITUDE, distance);

        if (distance[0] <= GEOFENCE_RADIUS) {
            Log.e("RequestTest_5_2", "inPoint");
        } else {
            Log.e("RequestTest_5_3", "outPoint");
        }
    }
    private void saveLocationPermissionGranted(boolean granted) {
        SharedPreferences sharedPreferences = getSharedPreferences("location_permission", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("granted", granted);
        editor.apply();
    }
}