package com.zeniuus.www.reactiontagging.objects;

import android.util.Log;

import com.zeniuus.www.reactiontagging.activities.VideoActivity;

/**
 * Created by zeniuus on 2017. 6. 30..
 */

public class Feedback {
    private String userId;
    private int startTime;
    private int endTime;
    private String feedback;

    public Feedback(String userId, String start_time, String end_time, String feedback) {
        this.userId = userId;
        this.startTime = Integer.parseInt(start_time);
        this.endTime = Integer.parseInt(end_time);
        this.feedback = feedback;
        Log.d("feedback", "feedback well created");
    }

    public String getUserId() { return userId; }

    public int getStartTime() { return startTime; }

    public int getEndTime() { return endTime; }

    public String getFeedback() { return feedback; }

    public String toString() { return VideoActivity.milisecToMinSec(startTime) + " - " + feedback; }
}
