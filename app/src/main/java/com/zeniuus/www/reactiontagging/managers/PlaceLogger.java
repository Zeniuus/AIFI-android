package com.zeniuus.www.reactiontagging.managers;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import com.zeniuus.www.reactiontagging.activities.MainActivity;
import com.zeniuus.www.reactiontagging.networks.HttpRequestHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

import static java.lang.Thread.sleep;


/**
 * Created by zeniuus on 2017. 8. 30..
 */

public class PlaceLogger {
    static final String TAG = "PlaceLogger";
    Context context;
    String userId, videoName;
    int videoTime;
    Boolean logFlag;
    Location mLocation;
    LocationManager locationManager;
    LocationListener locationListener;

    public PlaceLogger(Context context, String userId, String videoName) {
        this.context = context;
        this.userId = userId;
        this.videoName = videoName;
        this.videoTime = 0;
        logFlag = false;
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                mLocation = location;
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };
    }

    @TargetApi(23)
    public void execute() {
        if (context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, locationListener);
            logFlag = true;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        while (true) {
                            mLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            sendLog();

                            try {
                                sleep(10000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }).start();
        }
    }

    public void cancel() {
        locationManager.removeUpdates(locationListener);
        logFlag = false;
        sendLog();
    }

    public void setVideoTime(int videoTime) { this.videoTime = videoTime; }

    private void sendLog() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.accumulate("userId", userId);
            jsonObject.accumulate("videoName", videoName);
            jsonObject.accumulate("latitude", mLocation.getLatitude());
            jsonObject.accumulate("longitude", mLocation.getLongitude());
            jsonObject.accumulate("date", java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime()));
            jsonObject.accumulate("videoTime", Integer.toString(videoTime));
            Log.d(TAG, jsonObject.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
//        String result = new HttpRequestHandler("POST", MainActivity.SERVER_URL + "/log", jsonObject.toString()).doHttpRequest();
//        Log.d(TAG, result);
    }
}
