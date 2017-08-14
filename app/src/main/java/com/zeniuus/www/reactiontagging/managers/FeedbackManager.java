package com.zeniuus.www.reactiontagging.managers;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.zeniuus.www.reactiontagging.activities.MainActivity;
import com.zeniuus.www.reactiontagging.activities.VideoActivity;
import com.zeniuus.www.reactiontagging.activities.VideoHorizontalActivity;
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
    String userId;
    Context context;
    Socket mSocket;

    public FeedbackManager(String videoName, final String userId, Context context) {
        feedbacks = new ArrayList<>();
        this.videoName = videoName;
        this.userId = userId;
        this.context = context;

        try {
            mSocket = IO.socket(MainActivity.SERVER_URL);
            mSocket.connect();
        } catch (Exception e) {
            Log.d("exception", e.toString());
        }

//        final VideoActivity videoActivity = (VideoActivity) context;
        final VideoHorizontalActivity videoHorizontalActivity = (VideoHorizontalActivity) context;

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
                            ((JSONObject) args[0]).getString("userId"),
                            ((JSONObject) args[0]).getInt("startTime") + "",
                            ((JSONObject) args[0]).getInt("endTime") + "",
                            ((JSONObject) args[0]).getString("feedback"),
                            ((JSONObject) args[0]).getJSONArray("like"),
                            ((JSONObject) args[0]).getJSONArray("thread")
                    );
                    feedbacks.add(feedback);
                    Collections.sort(feedbacks, mComparator);
//                    videoActivity.addFeedback(feedback);
//                    videoHorizontalActivity.addFeedback(feedback);
                    videoHorizontalActivity.updateFeedback();
                } catch (Exception e) {
                    Log.d("exception", e.toString());
                }
            }
        }).on("feedback like", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Iterator<Feedback> iter = feedbacks.iterator();

                try {
                    Feedback feedback = new Feedback(
                            ((JSONObject) args[0]).getString("userId"),
                            ((JSONObject) args[0]).getInt("startTime") + "",
                            ((JSONObject) args[0]).getInt("endTime") + "",
                            ((JSONObject) args[0]).getString("feedback"),
                            ((JSONObject) args[0]).getJSONArray("like"),
                            null
                    );

                    while (iter.hasNext()) {
                        Feedback temp = iter.next();
                        if (isSame(temp, feedback)) {
                            temp.giveLike(((JSONObject) args[0]).getString("likeUserId"));
                            break;
                        }
                    }
                } catch (Exception e) {
                    Log.d("exception", e.toString());
                }
            }
        }).on("thread feedback addition", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Iterator<Feedback> iter = feedbacks.iterator();

                try {
                    Feedback feedback = new Feedback(
                            ((JSONObject) args[0]).getString("userId"),
                            ((JSONObject) args[0]).getInt("startTime") + "",
                            ((JSONObject) args[0]).getInt("endTime") + "",
                            ((JSONObject) args[0]).getString("feedback"),
                            ((JSONObject) args[0]).getJSONArray("like"),
                            null
                    );

                    while (iter.hasNext()) {
                        Feedback temp = iter.next();
                        if (isSame(temp, feedback)) {
                            temp.giveThreadFeedback(
                                    ((JSONObject) args[0]).getString("threadUserId"),
                                    ((JSONObject) args[0]).getString("threadFeedback"));

//                            videoActivity.updateThreadFeedback(temp);
//                            videoHorizontalActivity.updateThreadFeedback(temp);
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    videoHorizontalActivity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            videoHorizontalActivity.updateFeedback();
                                        }
                                    });
                                }
                            }).run();
                            break;
                        }
                    }


                } catch (Exception e) {
                    Log.d("exception", e.toString());
                }
            }
        }).on("thread feedback like", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Iterator<Feedback> iter = feedbacks.iterator();
                Log.d("data log", "data from server: " + ((JSONObject) args[0]).toString());

                try {
                    Feedback feedback = new Feedback(
                            ((JSONObject) args[0]).getString("userId"),
                            ((JSONObject) args[0]).getInt("startTime") + "",
                            ((JSONObject) args[0]).getInt("endTime") + "",
                            ((JSONObject) args[0]).getString("feedback"),
                            ((JSONObject) args[0]).getJSONArray("like"),
                            null
                    );

                    while (iter.hasNext()) {
                        Feedback temp = iter.next();
                        if (isSame(temp, feedback)) {
                            temp.giveLikeToThreadFeedback(
                                    ((JSONObject) args[0]).getInt("threadIndex"),
                                    ((JSONObject) args[0]).getString("likeUserId"));
                            break;
                        }
                    }
                } catch (Exception e) {
                    Log.d("exception1", e.toString());
                }
            }
        });

        String result = new HttpRequestHandler("GET",
                MainActivity.SERVER_URL + "/get_feedback/" + videoName,
                "")
                .doHttpRequest();
        Log.d("server", "get feedback result: " + result);
        try {
            JSONArray jsonArray = new JSONObject(result).getJSONArray("feedback");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Feedback feedback = new Feedback(
                        jsonObject.getString("userId"),
                        jsonObject.getInt("startTime") + "",
                        jsonObject.getInt("endTime") + "",
                        jsonObject.getString("feedback"),
                        jsonObject.getJSONArray("like"),
                        jsonObject.getJSONArray("thread"));
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
            jsonObject.accumulate("userId", userId);
            jsonObject.accumulate("startTime", Integer.parseInt(startTime));
            jsonObject.accumulate("endTime", Integer.parseInt(endTime));
            jsonObject.accumulate("feedback", feedback);

            String result = new HttpRequestHandler("POST", MainActivity.SERVER_URL + "/new_feedback/" + videoName, jsonObject.toString()).doHttpRequest();
            Log.d("server", "sending feedback result: " + result);
            Toast.makeText(context, VideoActivity.milisecToMinSec(Integer.parseInt(startTime)) + " - " + feedback, Toast.LENGTH_SHORT).show();
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

    public ArrayList<Feedback> getFeedbacksOfPerson(String userId) {
        ArrayList<Feedback> onesFeedback = new ArrayList<>();
        Iterator<Feedback> iter = feedbacks.iterator();

        while (iter.hasNext()) {
            Feedback feedback = iter.next();
            if (feedback.getUserId().compareTo(userId) == 0)
                onesFeedback.add(feedback);
        }

        return onesFeedback;
    }

    public void giveLikeToFeedback(Feedback feedback) {
        Iterator<Feedback> iter = feedbacks.iterator();
        while (iter.hasNext()) {
            Feedback temp = iter.next();
            if (isSame(temp, feedback)) {
                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.accumulate("userId", feedback.getUserId());
                    jsonObject.accumulate("startTime", feedback.getStartTime());
                    jsonObject.accumulate("endTime", feedback.getEndTime());
                    jsonObject.accumulate("feedback", feedback.getFeedback());
                    jsonObject.accumulate("like", feedback.getLike());
                    jsonObject.accumulate("likeUserId", userId);

                    String result = new HttpRequestHandler
                            ("POST", MainActivity.SERVER_URL + "/give_like_to_feedback/" + videoName, jsonObject.toString())
                            .doHttpRequest();
                    Log.d("server", "giving like to feedback result: " + result);
                    Toast.makeText(context, "give like to: " + feedback.getFeedback(), Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Log.d("exception", e.toString());
                }
            }
        }
    }

    public void giveLikeToThreadFeedback(Feedback feedback, int index) {
        Iterator<Feedback> iter = feedbacks.iterator();
        while (iter.hasNext()) {
            Feedback temp = iter.next();
            if (isSame(temp, feedback)) {
                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.accumulate("userId", feedback.getUserId());
                    jsonObject.accumulate("startTime", feedback.getStartTime());
                    jsonObject.accumulate("endTime", feedback.getEndTime());
                    jsonObject.accumulate("feedback", feedback.getFeedback());
                    jsonObject.accumulate("like", feedback.getLike());
                    jsonObject.accumulate("threadIndex", index);
                    jsonObject.accumulate("likeUserId", userId);

                    String result = new HttpRequestHandler
                            ("POST", MainActivity.SERVER_URL + "/give_like_to_thread_feedback/" + videoName, jsonObject.toString())
                            .doHttpRequest();
                    Log.d("server", "giving like to thread feedback result: " + result);
                    Toast.makeText(context, "give like to: " + feedback.getThread().getJSONObject(index).getString("feedback"), Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Log.d("exception", e.toString());
                }
            }
        }
    }

    public void giveThreadFeedback(Feedback feedback, String threadFeedback) {
        Iterator<Feedback> iter = feedbacks.iterator();
        while (iter.hasNext()) {
            Feedback temp = iter.next();
            if (isSame(temp, feedback)) {
                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.accumulate("userId", feedback.getUserId());
                    jsonObject.accumulate("startTime", feedback.getStartTime());
                    jsonObject.accumulate("endTime", feedback.getEndTime());
                    jsonObject.accumulate("feedback", feedback.getFeedback());
                    jsonObject.accumulate("like", feedback.getLike());
                    jsonObject.accumulate("threadUserId", userId);
                    jsonObject.accumulate("threadFeedback", threadFeedback);

                    String result = new HttpRequestHandler
                            ("POST", MainActivity.SERVER_URL + "/new_thread_feedback/" + videoName, jsonObject.toString())
                            .doHttpRequest();
                    Log.d("server", "new thread feedback result: " + result);
                    Toast.makeText(context, "new thread feedback: " + threadFeedback, Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Log.d("exception", e.toString());
                }
            }
        }
    }

    public void remove(Feedback feedback) {
        Iterator<Feedback> iter = feedbacks.iterator();
        while (iter.hasNext()) {
            Feedback temp = iter.next();
            if (isSame(temp, feedback)) {
                feedbacks.remove(temp);
                break;
            }
        }
    }

    public final static Comparator<Feedback> mComparator = new Comparator<Feedback>() {
        @Override
        public int compare(Feedback o1, Feedback o2) {
            if (o1.getStartTime() < o2.getStartTime()) return -1;
            else if (o1.getStartTime() == o2.getStartTime()) return 0;
            else return 1;
        }
    };

    private boolean isSame(Feedback feedback1, Feedback feedback2) {
        if (feedback1.getUserId().compareTo(feedback2.getUserId()) == 0
                && feedback1.getStartTime() == feedback2.getStartTime()
                && feedback1.getEndTime() == feedback2.getEndTime()
                && feedback1.getFeedback().compareTo(feedback2.getFeedback()) == 0
                && feedback1.getLike().toString().compareTo(feedback2.getLike().toString()) == 0)
            return true;
        else return false;
    }
}
