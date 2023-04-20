package com.cookandroid.navermapapi;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapView;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.overlay.CircleOverlay;
import com.naver.maps.map.overlay.PathOverlay;
import com.naver.maps.map.util.FusedLocationSource;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    // API KEY VALUE
    private static String API_KEY_ID = "1a4moto0jq";
    private static String SECRET_KEY_ID = "18eAC0S23t8PKTRUivjbNb7EP1WsX3grAZ2iXOwE";
    // GEOCODE URL
    private static String GEOCODE_URL = "https://naveropenapi.apigw.ntruss.com/map-geocode/v2/geocode?query=";
    public String station_address;
    // DIRECTION5 URL
    private static String DIRECTION5_URL = "https://naveropenapi.apigw.ntruss.com/map-direction/v1/driving?start=";
    // DIRECTION5 Latlng
    public String start_latlng = "126.9266623,37.3799000";
    public String goal_latlng = "126.9352657,37.3852172";
    // NAVER MAP VIEW
    private MapView mapView;
    private static NaverMap naverMap;
    // Chasing Location
    private LatLng myLatLng;
    // OVERLAY PATH
    PathOverlay path = new PathOverlay();
    // PATH LIST(String)
    List<String> paths = new ArrayList<>();
    // PATH LIST(LatLng)
    List<LatLng> LatLngs = new ArrayList<>();
    // Find my location
    private FusedLocationSource locationSource;
    // THE WHOLE STATION
    int[] resourceIds = new int[]{
            R.array.list_of_location_1, R.array.list_of_location_2, R.array.list_of_location_3,
            R.array.list_of_location_4, R.array.list_of_location_5, R.array.list_of_location_6,
            R.array.list_of_location_7, R.array.list_of_location_8, R.array.list_of_location_9
    };
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private static final String[] PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };
    private String[] test;

    private CircleOverlay mCircleOverlay;
    private final float GEOFENCE_RADIUS = 200;

    private String start_addr;

    private Handler mHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnLatLng = (Button) findViewById(R.id.btnLatLng);

        mapView = (MapView) findViewById(R.id.mapView);

        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        locationSource = new FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE);

        /* 추가 부분 */

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        // 내 위치 갱신
        requestMylocation();
        btnLatLng.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        requestDirections5();
                    }
                }).start();
            }
        });
    }

    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        this.naverMap = naverMap;
        naverMap.setLocationSource(locationSource);
        ActivityCompat.requestPermissions(this, PERMISSIONS, LOCATION_PERMISSION_REQUEST_CODE);

        // 역 근처 200m 표시
        LatLng geofenceCenter = new LatLng(37.3852172, 126.9352657);

        mCircleOverlay = new CircleOverlay();
        mCircleOverlay.setCenter(geofenceCenter);
        mCircleOverlay.setRadius(GEOFENCE_RADIUS);
        mCircleOverlay.setColor(0x220000FF);
        mCircleOverlay.setOutlineColor(0xFF0000FF);
        mCircleOverlay.setOutlineWidth(2);
        mCircleOverlay.setMap(naverMap);

        // 역 근처에 갔을 시 토스트 메시지 출력
        final boolean[] isToastShown = {false};
        naverMap.addOnLocationChangeListener(location -> {
            if (location.getLatitude() > 37.3852172 - 0.001539 &&
                    location.getLatitude() < 37.3852172 + 0.001539 &&
                    location.getLongitude() > 126.9352657 - 0.0019823 &&
                    location.getLongitude() < 126.9352657 + 0.0019823)
            {
                if (!isToastShown[0]) { // 토스트 메시지가 표시되지 않은 경우에만 실행
                    Toast toast = Toast.makeText(getApplicationContext(), "지하철역 반경 200m 안에 있습니다.", Toast.LENGTH_SHORT);
                    toast.show();

                    Log.e("RequestTest_2", "Toast");

//                    requestDirections5();

                    isToastShown[0] = true; // 변수 업데이트
                }
            } else {
                isToastShown[0] = false; // 변수 업데이트
            }
        });
    }

    public void requestGeocode() {
        String test_station = "금정";
        String station = "4";
        int stringId = getResources().getIdentifier("list_of_location_" + station, "array", getPackageName());
        test = getResources().getStringArray(stringId);
        for (int t = 0; t < test.length; t++) {
            String[] subway_Info = test[t].split("_");
            String station_name = subway_Info[0];
            if (station_name.equals(test_station)) {
                station_address = subway_Info[1];
                break;
            } else {
                continue;
            }
        }
        try {
            BufferedReader bufferedReader;
            StringBuilder stringBuilder = new StringBuilder();
            String query = GEOCODE_URL + URLEncoder.encode(station_address, "UTF-8");
            URL url = new URL(query);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            if (conn != null) {
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                conn.setRequestMethod("GET");
                conn.setRequestProperty("X-NCP-APIGW-API-KEY-ID", API_KEY_ID);
                conn.setRequestProperty("X-NCP-APIGW-API-KEY", SECRET_KEY_ID);
                conn.setDoInput(true);

                int responseCode = conn.getResponseCode();

                if (responseCode == 200) {
                    bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                } else {
                    bufferedReader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                }

                String line = null;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line + "/n");
                }

                int indexFirst;
                int indexLast;

                indexFirst = stringBuilder.indexOf("\"x\":\"");
                indexLast = stringBuilder.indexOf("\",\"y\":");
                String x = stringBuilder.substring(indexFirst + 5, indexLast);

                indexFirst = stringBuilder.indexOf("\"y\":\"");
                indexLast = stringBuilder.indexOf("\",\"distance\":");
                String y = stringBuilder.substring(indexFirst + 5, indexLast);

                myLatLng = new LatLng(Double.parseDouble(y), Double.parseDouble(x));
                bufferedReader.close();
                conn.disconnect();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void requestDirections5() {
        Log.e("RequestTest_2_1", "requestDirection5");
        try {
            BufferedReader bufferedReader2;
            StringBuilder stringBuilder2 = new StringBuilder();
            String query2 = DIRECTION5_URL + URLEncoder.encode(start_latlng, "UTF-8")
                    + "&goal=" + URLEncoder.encode(goal_latlng, "UTF-8");
            URL url2 = new URL(query2);
            HttpURLConnection conn2 = (HttpURLConnection) url2.openConnection();
            Log.e("RequestTest_innertest_1", "OK");
            if (conn2 != null) {
                conn2.setConnectTimeout(5000);
                conn2.setReadTimeout(5000);
                conn2.setRequestMethod("GET");
                conn2.setRequestProperty("X-NCP-APIGW-API-KEY-ID", API_KEY_ID);
                conn2.setRequestProperty("X-NCP-APIGW-API-KEY", SECRET_KEY_ID);
                conn2.setDoInput(true);
                Log.e("RequestTest_innertest_2", "OK");
                int responseCode2 = conn2.getResponseCode();

                if (responseCode2 == 200) {
                    Log.e("RequestTest_innertest_3", "OK");
                    bufferedReader2 = new BufferedReader(new InputStreamReader(conn2.getInputStream()));
                } else {
                    bufferedReader2 = new BufferedReader(new InputStreamReader(conn2.getErrorStream()));
                }

                String line2 = null;
                while ((line2 = bufferedReader2.readLine()) != null) {
                    stringBuilder2.append(line2 + "\n");
                }

                int indexFirst2;
                int indexLast2;

                indexFirst2 = stringBuilder2.indexOf("\"path\":");
                indexLast2 = stringBuilder2.indexOf("\"section\":");
                String pathpath = stringBuilder2.substring(indexFirst2 + 9, indexLast2 - 3);
                paths = Arrays.asList(pathpath.split("\\],\\["));
                LatLngs.clear();
                for (int i = 0; i < paths.size(); i++) {
                    String[] paTH = paths.get(i).split(",");
                    LatLngs.add(new LatLng(Double.parseDouble(paTH[1]), Double.parseDouble(paTH[0])));
                }
                bufferedReader2.close();
                conn2.disconnect();
                Log.e("RequestTest_2_2", start_latlng);
            }
        } catch (Exception e) {
            Log.e("RequestTest_2_3", "Direction5_Error");
            e.printStackTrace();
        }
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                requestPathoverlay();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        path.setMap(naverMap);
                    }
                });
            }
        });
    }

    public void requestPathoverlay() {
        Log.e("RequestTest_3", "requestPathoverlay");
        Location requestLocation = locationSource.getLastLocation();
        naverMap.moveCamera(CameraUpdate.scrollTo(new LatLng(requestLocation.getLatitude(),
                requestLocation.getLongitude())));
        path.setCoords(LatLngs);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (locationSource != null && locationSource.onRequestPermissionsResult(requestCode, permissions, grantResults)) {
            if (!locationSource.isActivated()) {
                naverMap.setLocationTrackingMode(LocationTrackingMode.None);
            } else {
                naverMap.setLocationTrackingMode(LocationTrackingMode.Follow);
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public void requestMylocation(){
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // 백그라운드에서 작동하는 로직 수행
                Location lastLocation = locationSource.getLastLocation();
                TextView textView = (TextView) findViewById(R.id.textView);

                if (lastLocation != null) {
                    double latitude = lastLocation.getLatitude();
                    double longitude = lastLocation.getLongitude();
                    start_addr = String.valueOf(longitude) + ',' + String.valueOf(latitude);

                    Log.e("RequestTest_1", start_addr);
                    // 위도와 경도 값을 사용하여 원하는 작업 수행
                } else {
                    // 마지막으로 알려진 위치 정보가 없는 경우 처리할 작업 수행
                }
                // UI 업데이트를 위한 핸들러 호출
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        // UI 업데이트 코드 작성
                        textView.setText(start_addr);
                        Log.e("RequestTest_1_1", "requestMyLocation");
                    }
                });
                // 다음 1초 뒤에 다시 실행
                mHandler.postDelayed(this, 1000);
            }
        }, 1000); // 초기 실행은 1초 후에 시작

    }
}