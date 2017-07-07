package com.zeniuus.www.reactiontagging.networks;

import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by zeniuus on 2017. 7. 5..
 */

public class HttpRequestHandler {
    String reqType;
    String strUrl;
    String data;

    public HttpRequestHandler(String reqType, String strUrl, String data) {
        this.reqType = reqType;
        this.strUrl = strUrl;
        this.data = data;
    }

    public String doHttpRequest() {
        final String[] result = new String[1];
        Thread t  = new Thread() {
            @Override
            public void run() {
                try {
                    URL url = new URL(strUrl);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod(reqType);
                    conn.setDoInput(true);
                    if (reqType.compareTo("POST") == 0) {
                        conn.setDoOutput(true);
                        conn.setRequestProperty("Accept", "application/json");
                        conn.setRequestProperty("Content-Type", "application/json");

                        Log.d("data", data);

                        OutputStream os = conn.getOutputStream();
                        os.write(data.getBytes("UTF-8"));
                        os.flush();
                        os.close();
                    }

                    InputStream is = conn.getInputStream();
                    StringBuilder builder = new StringBuilder();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                    String line;

                    while ((line = reader.readLine()) != null) {
                        builder.append(line + '\n');
                    }

                    reader.close();

                    result[0] = builder.toString();
                } catch (Exception e) {
                    Log.d("exception", e.toString());
                }
            }
        };

        t.start();
        try {
            t.join();
        } catch (Exception e) {
            Log.d("exception", e.toString());
        }

        return result[0];
    }
}
