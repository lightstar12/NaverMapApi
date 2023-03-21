package com.cookandroid.navermapapi;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.location.Geocoder;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraPosition;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.MapView;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.Buffer;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private MapView mapView;
    private static NaverMap naverMap;
    private LatLng myLatLng = new LatLng(37.3399, 126.733);
    TextView textView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnLatLng = (Button) findViewById(R.id.btnLatLng);

        mapView = (MapView) findViewById(R.id.mapView);

        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        btnLatLng.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        requestGeocode();
                    }
                }).start();

                naverMap.moveCamera(CameraUpdate.scrollTo(myLatLng));
            }
        });
    }
    public void requestGeocode() {
        TextView textView = (TextView) findViewById(R.id.textView);

        try {
            BufferedReader bufferedReader;
            StringBuilder stringBuilder = new StringBuilder();
            String addr = "안양동 명학역";
            String query = "https://naveropenapi.apigw.ntruss.com/map-geocode/v2/geocode?query=" + URLEncoder.encode(addr, "UTF-8");
            URL url = new URL(query);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            if (conn != null) {
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                conn.setRequestMethod("GET");
                conn.setRequestProperty("X-NCP-APIGW-API-KEY-ID", "1a4moto0jq");
                conn.setRequestProperty("X-NCP-APIGW-API-KEY", "CuER5Kov8SAwhboKQMabhNekT9dY6cpoLIEbSnhz");
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

                textView.setText("위도:" + y + "경도:" + x);
                myLatLng = new LatLng(Double.parseDouble(y), Double.parseDouble(x));
                bufferedReader.close();
                conn.disconnect();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void requestDirections5() {
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
                conn2.setRequestProperty("X-NCP-APIGW-API-KEY", "CuER5Kov8SAwhboKQMabhNekT9dY6cpoLIEbSnhz");
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
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        this.naverMap = naverMap;

        CameraUpdate cameraUpdate = CameraUpdate.scrollTo(myLatLng);
        naverMap.moveCamera(cameraUpdate);

    }

}