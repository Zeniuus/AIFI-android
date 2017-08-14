package com.zeniuus.www.reactiontagging.managers;

import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.zeniuus.www.reactiontagging.activities.MainActivity;
import com.zeniuus.www.reactiontagging.activities.VideoHorizontalActivity;
import com.zeniuus.www.reactiontagging.networks.HttpRequestHandler;
import com.zeniuus.www.reactiontagging.objects.Feedback;
import com.zeniuus.www.reactiontagging.objects.Prompt;
import com.zeniuus.www.reactiontagging.prompts.CustomQuestionPrompt;
import com.zeniuus.www.reactiontagging.types.PromptType;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

/**
 * Created by zeniuus on 2017. 8. 14..
 */

public class PromptManager {
    Context context;
    String userId, videoName;
    ArrayList<Prompt> prompts;
    CustomQuestionPrompt customQuestionPrompt;
    Object newPrompt;

    public PromptManager(Context context, String userId, String videoName) {
        this.context = context;
        this.userId = userId;
        this.videoName = videoName;

        prompts = new ArrayList<>();

//        String result = new HttpRequestHandler("GET",
//                MainActivity.SERVER_URL + "/get_prompt/" + videoName,
//                "")
//                .doHttpRequest();
//        Log.d("server", "get prompt result: " + result);
//        try {
//            JSONArray jsonArray = new JSONObject(result).getJSONArray("prompt");
//            for (int i = 0; i < jsonArray.length(); i++) {
//                JSONObject jsonObject = jsonArray.getJSONObject(i);
//                Prompt prompt = new Prompt(
//                        PromptType.values()[jsonObject.getInt("type")],
//                        jsonObject.getInt("time"),
//                        jsonObject.getString("question")
//                );
//                prompts.add(prompt);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        prompts.add(new Prompt(PromptType.CUSTOM, 2000, "question 1"));
        prompts.add(new Prompt(PromptType.CUSTOM, 6000, "question 2"));
        prompts.add(new Prompt(PromptType.CUSTOM, 10000, "question 3"));

        Collections.sort(prompts, comparator);
    }

    public ArrayList<Prompt> getPrompts() { return prompts; }

    public void checkPrompt(int current) {
        Iterator<Prompt> iter = prompts.iterator();
        while (iter.hasNext()) {
            Prompt prompt = iter.next();
            if (prompt.getShownState() || prompt.getPromptTime() < current - 500) return;
            else if (prompt.getPromptTime() < current) {
                executePrompt(prompt);
                prompt.setShownState(true);
                return;
            }
        }
    }

    public void clearShownStates(int newCurrent) {
        Iterator<Prompt> iter = prompts.iterator();
        while (iter.hasNext()) {
            Prompt prompt = iter.next();
            if (prompt.getPromptTime() >= newCurrent) prompt.setShownState(false);
            else return;
        }
    }

    public void executePrompt(Prompt prompt) {
        View.OnClickListener cancelListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "cancel button clicked", Toast.LENGTH_SHORT).show();
                Dialog.class.cast(newPrompt).dismiss();
                ((VideoHorizontalActivity) context).videoStart();
            }
        },
        submitListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "submit button clicked", Toast.LENGTH_SHORT).show();
                Dialog.class.cast(newPrompt).dismiss();
                ((VideoHorizontalActivity) context).videoStart();
            }
        };

        switch (prompt.getPromptType()) {
            case CUSTOM:
                newPrompt = new CustomQuestionPrompt(context, prompt, cancelListener, submitListener);
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                ((VideoHorizontalActivity) context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Dialog.class.cast(newPrompt).show();
                        ((VideoHorizontalActivity) context).videoPause();
                    }
                });
            }
        }).run();
    }

    private Comparator<Prompt> comparator = new Comparator<Prompt>() {
        @Override
        public int compare(Prompt o1, Prompt o2) {
            if (o1.getPromptTime() < o2.getPromptTime()) return 1;
            else if (o1.getPromptTime() == o2.getPromptTime()) return 0;
            else return -1;
        }
    };;
}
