package com.zeniuus.www.reactiontagging.objects;

import android.util.Log;

/**
 * Created by zeniuus on 2017. 6. 30..
 */

public class Feedback {
    int start_time;
    int end_time;
    String feedback;

    public Feedback(String start_time, String end_time, String feedback) {
        this.start_time = Integer.parseInt(start_time);
        this.end_time = Integer.parseInt(end_time);
        this.feedback = feedback;
        Log.d("feedback", "feedback well created");
    }

    public int getStartTime() { return start_time; }

    public int getEndTime() { return end_time; }

    public String getFeedback() { return feedback; }
}
