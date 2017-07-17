package com.zeniuus.www.reactiontagging.objects;

import android.util.Log;

import com.zeniuus.www.reactiontagging.activities.VideoActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zeniuus on 2017. 6. 30..
 */

public class Feedback {
    private String userId;
    private int startTime;
    private int endTime;
    private String feedback;
    private JSONArray like;
    private JSONArray thread;

    public Feedback(String userId, String start_time, String end_time, String feedback, JSONArray like, JSONArray thread) {
        this.userId = userId;
        this.startTime = Integer.parseInt(start_time);
        this.endTime = Integer.parseInt(end_time);
        this.feedback = feedback;
        this.like = like;
        this.thread = thread;
        Log.d("feedback", "feedback well created");
    }

    public String getUserId() { return userId; }

    public int getStartTime() { return startTime; }

    public int getEndTime() { return endTime; }

    public String getFeedback() { return feedback; }

    public JSONArray getLike() { return like; }

    public void giveLike(String userId) { like.put(userId); }

    public JSONArray getThread() { return thread; }

    public void giveThreadFeedback(String userId, String feedback) {
        try {
            JSONObject threadFeedback = new JSONObject();
            threadFeedback.accumulate("userId", userId);
            threadFeedback.accumulate("feedback", feedback);
            threadFeedback.accumulate("like", new JSONArray());

            thread.put(threadFeedback);
        } catch (Exception e) {
            Log.d("exception", e.toString());
        }
    }

    public void giveLikeToThreadFeedback(int index, String userId) {
        try {
            JSONObject jsonObject = thread.getJSONObject(index);
            JSONArray jsonArray = jsonObject.getJSONArray("like");
            jsonArray.put(userId);
            jsonObject.put("like", jsonArray);
//            thread.remove(index);
            thread.put(index, jsonObject);
            Log.d("json object", thread.toString());
        } catch (Exception e) {
            Log.d("exception2", e.toString());
        }
    }

    public String toString() { return VideoActivity.milisecToMinSec(startTime) + " - " + feedback; }
}
