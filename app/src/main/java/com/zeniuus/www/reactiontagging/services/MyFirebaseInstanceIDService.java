package com.zeniuus.www.reactiontagging.services;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.zeniuus.www.reactiontagging.activities.MainActivity;
import com.zeniuus.www.reactiontagging.networks.HttpRequestHandler;

/**
 * Created by zeniuus on 2017. 8. 19..
 */

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {
    private static final String TAG = "AIFI";

    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        sendRegistrationToServer(refreshedToken);
    }

    private void sendRegistrationToServer(String refreshedToken) {
        String result = new HttpRequestHandler(
                "POST", MainActivity.SERVER_URL + "/token_refreshed/" + "1234", refreshedToken).doHttpRequest();
        Log.d(TAG, result);
    }
}
