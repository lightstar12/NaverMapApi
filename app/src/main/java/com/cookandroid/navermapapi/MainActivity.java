package com.cookandroid.navermapapi;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapView;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.NaverMapSdk;
import com.naver.maps.map.OnMapReadyCallback;
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
    private static String SECRET_KEY_ID = "iRCAHtqQHp2j9PiZKo0OFz0oBCEIyGaZaNda3wyn";
    // GEOCODE URL
    private static String GEOCODE_URL = "https://naveropenapi.apigw.ntruss.com/map-geocode/v2/geocode?query=";
    public String station_address;
    // DIRECTION5 URL
    private static String DIRECTION5_URL = "https://naveropenapi.apigw.ntruss.com/map-direction/v1/driving?start=";
    // DIRECTION5 Latlng
    public String start_latlng = "126.793438,37.429655";
    public String goal_latlng = "126.792732,37.431915";
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
            R.array.list_of_location_4, R.array.list_of_location_5, R.array.list_of_location_6
    };
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private static final String[] PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };
    private String[] test;
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

        btnLatLng.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        requestGeocode();
                        requestDirections5(new Runnable() {
                            @Override
                            public void run() {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        requestPathoverlay();
                                        naverMap.moveCamera(CameraUpdate.scrollTo(myLatLng));
                                        path.setMap(naverMap);
                                    }
                                });
                            }
                        });
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

    }

    public void requestGeocode() {
        String test_station = "을지로4가";
        String station = "5";
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
        TextView textView = (TextView) findViewById(R.id.textView);
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

                textView.setText("위도:" + y + " 경도:" + x);
                myLatLng = new LatLng(Double.parseDouble(y), Double.parseDouble(x));
                bufferedReader.close();
                conn.disconnect();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void requestDirections5(Runnable callback) {
        try {
            BufferedReader bufferedReader2;
            StringBuilder stringBuilder2 = new StringBuilder();
            String query2 = DIRECTION5_URL + URLEncoder.encode(start_latlng, "UTF-8")
                    + "&goal=" + URLEncoder.encode(goal_latlng, "UTF-8");
            URL url2 = new URL(query2);
            HttpURLConnection conn2 = (HttpURLConnection) url2.openConnection();
            if (conn2 != null) {
                conn2.setConnectTimeout(5000);
                conn2.setReadTimeout(5000);
                conn2.setRequestMethod("GET");
                conn2.setRequestProperty("X-NCP-APIGW-API-KEY-ID", API_KEY_ID);
                conn2.setRequestProperty("X-NCP-APIGW-API-KEY", SECRET_KEY_ID);
                conn2.setDoInput(true);

                int responseCode2 = conn2.getResponseCode();

                if (responseCode2 == 200) {
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
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (callback != null) {
            callback.run();
        }
    }

    public void requestPathoverlay() {
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
}