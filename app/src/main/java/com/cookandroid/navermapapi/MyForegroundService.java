package com.cookandroid.navermapapi;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

public class MyForegroundService extends Service implements LocationListener {
    private static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "ForegroundServiceChannel";
    private LocationManager locationManager;
    private static final long MIN_TIME = 1000; // 위치 업데이트 간격 (1초)
    private static final float MIN_DISTANCE = 10; // 위치 업데이트 최소 거리 (10미터)

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 서비스가 시작될 때 수행할 작업
        Log.e("RequestTest_S_1", "startService");

        // Notification Channel 생성
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Foreground Service Channel";
            String description = "Channel for Foreground Service";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            // 알림 채널 설정(소리, 진동, 라이트)
            channel.setSound(null, null);
            channel.enableVibration(false);
            channel.enableLights(false);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        // Notification 생성
        Notification notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Foreground Service Title")
                .setContentText("Foreground Service Content")
                .setSmallIcon(R.drawable.ic_notification)
                .build();

        // Foreground Service 시작
        startForeground(NOTIFICATION_ID, notificationBuilder);

        // 위치 정보 업데이트 시작
        startLocationUpdates();

        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        // 바인드된 서비스에 대한 처리
        return null;
    }

    @Override
    public void onDestroy() {
        // 서비스가 종료될 때 수행할 작업
        super.onDestroy();

        // Foreground Service 종료
        stopForeground(true);

        // 위치 정보 업데이트 중지
        stopLocationUpdates();
    }

    // 위치 정보 업데이트 시작
    private void startLocationUpdates() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DISTANCE, this);
        }
    }

    // 위치 정보 업데이트 중지
    private void stopLocationUpdates() {
        if (locationManager != null) {
            locationManager.removeUpdates(this);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        // 위치가 업데이트될 때 호출되는 콜백 메서드
        Log.e("RequestTest_S_2", "onLocationChanged: " + location.getLatitude() + ", " + location.getLongitude());
        // 위치 정보를 받아와서 처리하는 로직을 추가하세요.
        // 예를 들어, 위치 정보를 서버에 전송하거나, 앱 내에서 활용하는 등의 작업을 수행할 수 있습니다.
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // 위치 공급자의 상태가 변경될 때 호출되는 콜백 메서드
    }

    @Override
    public void onProviderEnabled(String provider) {
        // 위치 공급자가 사용 가능한 상태로 변경될 때 호출되는 콜백 메서드
    }

    @Override
    public void onProviderDisabled(String provider) {
        // 위치 공급자가 사용 불가능한 상태로 변경될 때 호출되는 콜백 메서드
    }
}
