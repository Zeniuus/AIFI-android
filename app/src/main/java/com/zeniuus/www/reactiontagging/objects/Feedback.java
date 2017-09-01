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
    private boolean isQuestion;
    private int startTime;
    private int endTime;
    private String feedback;
    private String question;
    private JSONArray like;
    private JSONArray thread;

    public Feedback(String userId, boolean isQuestion, String start_time, String end_time, String feedback, JSONArray like, JSONArray thread) {
        this.userId = userId;
        this.isQuestion = isQuestion;
        this.startTime = Integer.parseInt(start_time);
        this.endTime = Integer.parseInt(end_time);
        this.feedback = feedback;
        this.question = "";
        this.like = like;
        this.thread = thread;
        Log.d("feedback", "feedback well created");
    }

    public Feedback(String userId, boolean isQuestion, String start_time, String end_time, String feedback, String question, JSONArray answers) {
        this.userId = userId;
        this.isQuestion = isQuestion;
        this.startTime = Integer.parseInt(start_time);
        this.endTime = Integer.parseInt(end_time);
        this.feedback = feedback;
        this.question = question;
        this.like = new JSONArray();
        this.thread = answers;
        Log.d("feedback", "feedback well created");
    }

    public String getUserId() { return userId; }

    public boolean isQuestion() { return isQuestion; };

    public int getStartTime() { return startTime; }

    public int getEndTime() { return endTime; }

    public String getFeedback() { return feedback; }

    public String getQuestion() { return question; }

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

    public String getFeedbackText() {
        if (!isQuestion()) return VideoActivity.milisecToMinSec(startTime) + " - " + feedback;
        else return "Presenter's question\n" + VideoActivity.milisecToMinSec(startTime) + " - " + question;
    }

    public String toString() {
        return userId + "//" + isQuestion + "//" +  startTime + "//" + endTime + "//" + feedback + "//" + question + "//" + like.toString() + "//" + thread.toString();
    }
}
