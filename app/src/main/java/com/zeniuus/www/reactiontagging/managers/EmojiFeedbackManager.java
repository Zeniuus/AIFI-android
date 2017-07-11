package com.zeniuus.www.reactiontagging.managers;

import android.content.Context;
import android.util.Log;

import com.zeniuus.www.reactiontagging.activities.MainActivity;
import com.zeniuus.www.reactiontagging.activities.VideoActivity;
import com.zeniuus.www.reactiontagging.networks.HttpRequestHandler;
import com.zeniuus.www.reactiontagging.objects.EmojiFeedback;
import com.zeniuus.www.reactiontagging.types.Emoji;

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
 * Created by zeniuus on 2017. 7. 5..
 */

public class EmojiFeedbackManager {
    ArrayList<EmojiFeedback> feedbacks;
    String videoName;
    Context context;
    Socket mSocket;

    public EmojiFeedbackManager(String videoName, Context context) {
        feedbacks = new ArrayList<>();
        this.videoName = videoName;
        this.context = context;

        try {
            mSocket = IO.socket(MainActivity.SERVER_URL);
            mSocket.connect();
        } catch (Exception e) {
            Log.d("exception", e.toString());
        }

        final VideoActivity videoActivity = (VideoActivity) context;

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
        }).on("emoji feedback addition", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.d("socket", "emoji feedback addition");
                try {
                    EmojiFeedback emojiFeedback = new EmojiFeedback(
                            ((JSONObject) args[0]).getInt("startTime"),
                            Emoji.values()[((JSONObject) args[0]).getInt("emoji")]
                    );
                    feedbacks.add(emojiFeedback);
                    Collections.sort(feedbacks, mComparator);
                    videoActivity.refreshEmojiFeedback();
                } catch (Exception e) {
                    Log.d("exception", e.toString());
                }
            }
        });

        String result = new HttpRequestHandler("GET",
                MainActivity.SERVER_URL + "/get_emoji_feedback/" + videoName,
                "")
                .doHttpRequest();
        Log.d("server", "get emoji feedback result: " + result);
        try {
            JSONArray jsonArray = new JSONObject(result).getJSONArray("emojiFeedback");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                EmojiFeedback emojiFeedback = new EmojiFeedback(
                        jsonObject.getInt("startTime"),
                        Emoji.values()[jsonObject.getInt("emoji")]);
                feedbacks.add(emojiFeedback);
            }
            Collections.sort(feedbacks, mComparator);
        } catch (Exception e) {
            Log.d("exception", e.toString());
        }
    }

    public int getSize() {
        return feedbacks.size();
    }

    public EmojiFeedback getItem(int i) {
        if (i < getSize()) return feedbacks.get(i);
        else return null;
    }

    public int addItem(int startTime, Emoji emoji) {
//        feedbacks.add(new EmojiFeedback(startTime, emoji));
//        Log.d("feedback", "feedback well inserted");
//        Collections.sort(feedbacks, mComparator);

        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.accumulate("startTime", startTime);
            jsonObject.accumulate("emoji", emoji.ordinal());

            String result = new HttpRequestHandler("POST",
                    MainActivity.SERVER_URL + "/new_emoji_feedback/" + videoName,
                    jsonObject.toString())
                    .doHttpRequest();
            Log.d("server", "sending emoji feedback result: " + result);

        } catch (Exception e) {
            Log.d("exception", e.toString());
        }

        return 0;
    }

    public int[] getFeedbacksAtTime(int time) {
        int[] emojiCnt = { 0, 0, 0, 0, 0, 0 };
        Iterator<EmojiFeedback> iter = feedbacks.iterator();

        while (iter.hasNext()) {
            EmojiFeedback feedback = iter.next();
            if (feedback.getStartTime() <= time && feedback.getStartTime() + 5000 >= time) {
                switch (feedback.getEmojiType()) {
                    case LIKE: {
                        emojiCnt[0]++;
                        break;
                    }
                    case LOVE: {
                        emojiCnt[1]++;
                        break;
                    }
                    case HAHA: {
                        emojiCnt[2]++;
                        break;
                    }
                    case WOW: {
                        emojiCnt[3]++;
                        break;
                    }
                    case SAD: {
                        emojiCnt[4]++;
                        break;
                    }
                    case ANGRY: {
                        emojiCnt[5]++;
                        break;
                    }
                }
            }
        }

        return emojiCnt;
    }

    private final static Comparator<EmojiFeedback> mComparator = new Comparator<EmojiFeedback>() {
        @Override
        public int compare(EmojiFeedback o1, EmojiFeedback o2) {
            if (o1.getStartTime() < o2.getStartTime()) return -1;
            else if (o1.getStartTime() == o2.getStartTime()) return 0;
            else return 1;
        }
    };
}
