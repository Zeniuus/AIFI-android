package com.zeniuus.www.reactiontagging.managers;

import android.util.Log;

import com.zeniuus.www.reactiontagging.networks.HttpRequestHandler;
import com.zeniuus.www.reactiontagging.objects.Feedback;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * Created by zeniuus on 2017. 6. 30..
 */

public class FeedbackManager {
    ArrayList<Feedback> feedbacks;
    String videoName;
    Socket mSocket;

    public FeedbackManager(String videoName) {
        feedbacks = new ArrayList<>();
        this.videoName = videoName;

        try {
            mSocket = IO.socket("http://143.248.197.157:3000");
            mSocket.connect();
        } catch (Exception e) {
            Log.d("exception", e.toString());
        }

        mSocket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.d("socket", "socket connected!");
            }
        }).on("connected", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.d("socket", (String) args[0]);
            }
        }).on("feedback addition", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.d("socket", "feedback addition");
                try {
                    Feedback feedback = new Feedback(
                            ((JSONObject) args[0]).getInt("startTime") + "",
                            ((JSONObject) args[0]).getInt("endTime") + "",
                            ((JSONObject) args[0]).getString("feedback")
                    );
                    feedbacks.add(feedback);
                    Collections.sort(feedbacks, mComparator);
                } catch (Exception e) {
                    Log.d("exception", e.toString());
                }
            }
        });

        String result = new HttpRequestHandler("GET",
                "http://143.248.197.157:3000/get_feedback/" + videoName,
                "")
                .doHttpRequest();
        Log.d("server", "get feedback result: " + result);
        try {
            JSONArray jsonArray = new JSONObject(result).getJSONArray("feedback");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Feedback feedback = new Feedback(
                        jsonObject.getInt("startTime") + "",
                        jsonObject.getInt("endTime") + "",
                        jsonObject.getString("feedback"));
                feedbacks.add(feedback);
            }
        } catch (Exception e) {
            Log.d("exception", e.toString());
        }
    }

    public int getSize() {
        return feedbacks.size();
    }

    public Feedback getItem(int i) {
        if (i < getSize()) return feedbacks.get(i);
        else return null;
    }

    public int addItem(String startTime, String endTime, String feedback) {
        try {
            Integer.parseInt(startTime);
            Integer.parseInt(endTime);
        } catch (NumberFormatException e) {
            return -1;
        }

        if (feedback.compareTo("") == 0)
            return -2;

//        feedbacks.add(new Feedback(startTime, endTime, feedback));
//        Log.d("feedback", "feedback well inserted");
//        Collections.sort(feedbacks, mComparator);

        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.accumulate("startTime", Integer.parseInt(startTime));
            jsonObject.accumulate("endTime", Integer.parseInt(endTime));
            jsonObject.accumulate("feedback", feedback);

            String result = new HttpRequestHandler("POST", "http://143.248.197.157:3000/new_feedback/" + videoName, jsonObject.toString()).doHttpRequest();
            Log.d("server", "sending feedback result: " + result);
        } catch (Exception e) {
            Log.d("exception", e.toString());
        }

        return 0;
    }

    public ArrayList<Feedback> getFeedbacksAtTime(int time) {
        ArrayList<Feedback> currFeedbacks = new ArrayList<>();
        Iterator<Feedback> iter = feedbacks.iterator();

        while (iter.hasNext()) {
            Feedback feedback = iter.next();
            if (feedback.getStartTime() <= time && feedback.getEndTime() >= time)
                currFeedbacks.add(feedback);
        }

        return currFeedbacks;
    }

    private final static Comparator<Feedback> mComparator = new Comparator<Feedback>() {
        @Override
        public int compare(Feedback o1, Feedback o2) {
            if (o1.getStartTime() < o2.getStartTime()) return -1;
            else if (o1.getStartTime() == o2.getStartTime()) return 0;
            else return 1;
        }
    };
}