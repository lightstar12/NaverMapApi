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
import android.location.Location;
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
    private MapView mapView;
    private static NaverMap naverMap;
    private LatLng myLatLng = new LatLng(37.3399, 126.733);
    List<String> pathpathpath = new ArrayList<>();
    PathOverlay path = new PathOverlay();
    List<LatLng> LatLngs = new ArrayList<>();

    private FusedLocationSource locationSource;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private static final String[] PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    ArrayList mPendingIntentList;

    String intentKey = "coffeeProximity";

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
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        BroadcastReceiver proximityAlertReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Toast.makeText(getApplicationContext(), "목표 근접 중", Toast.LENGTH_LONG).show();
            }
        };

        Intent proximityIntent = new Intent("com.example.PROXIMITY_ALERT");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0
                , proximityIntent, PendingIntent.FLAG_MUTABLE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.addProximityAlert(37.3852172, 126.9352657, 1000, 1, pendingIntent);

        registerReceiver(proximityAlertReceiver, new IntentFilter("com.example.PROXIMITY_ALERT"));

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
                // run과 그 밑에 코드가 동시에 실행 됨. run 이후에 실행되게 콜백함수를 사용해야 함.
            }
        });
    }

    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {

        this.naverMap = naverMap;
        naverMap.setLocationSource(locationSource);
        ActivityCompat.requestPermissions(this, PERMISSIONS, LOCATION_PERMISSION_REQUEST_CODE);
//        CameraUpdate cameraUpdate = CameraUpdate.scrollTo(myLatLng);
//        naverMap.moveCamera(cameraUpdate);

    }

    public void requestGeocode() {
        TextView textView = (TextView) findViewById(R.id.textView);
        try {
            BufferedReader bufferedReader;
            StringBuilder stringBuilder = new StringBuilder();
            String addr = "만안구 성결대학로 53";
            String query = "https://naveropenapi.apigw.ntruss.com/map-geocode/v2/geocode?query=" + URLEncoder.encode(addr, "UTF-8");
            URL url = new URL(query);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            if (conn != null) {
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                conn.setRequestMethod("GET");
                conn.setRequestProperty("X-NCP-APIGW-API-KEY-ID", "1a4moto0jq");
                conn.setRequestProperty("X-NCP-APIGW-API-KEY", "iRCAHtqQHp2j9PiZKo0OFz0oBCEIyGaZaNda3wyn");
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
            String startaddr = "126.9266623,37.3799";
            String goaladdr = "126.9352657,37.3852172";
            String query2 = "https://naveropenapi.apigw.ntruss.com/map-direction/v1/driving?start="
                    + URLEncoder.encode(startaddr, "UTF-8")
                    + "&goal=" + URLEncoder.encode(goaladdr, "UTF-8");
            URL url2 = new URL(query2);
            HttpURLConnection conn2 = (HttpURLConnection) url2.openConnection();
            if (conn2 != null) {
                conn2.setConnectTimeout(5000);
                conn2.setReadTimeout(5000);
                conn2.setRequestMethod("GET");
                conn2.setRequestProperty("X-NCP-APIGW-API-KEY-ID", "1a4moto0jq");
                conn2.setRequestProperty("X-NCP-APIGW-API-KEY", "iRCAHtqQHp2j9PiZKo0OFz0oBCEIyGaZaNda3wyn");
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
                pathpathpath = Arrays.asList(pathpath.split("\\],\\["));
                LatLngs.clear();
                for (int i = 0; i < pathpathpath.size(); i++) {
                    String[] paTH = pathpathpath.get(i).split(",");
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
        // 212 줄 코드가 문제임
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