package com.zeniuus.www.reactiontagging.prompts;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.zeniuus.www.reactiontagging.R;
import com.zeniuus.www.reactiontagging.activities.MainActivity;
import com.zeniuus.www.reactiontagging.activities.VideoHorizontalActivity;
import com.zeniuus.www.reactiontagging.networks.HttpRequestHandler;
import com.zeniuus.www.reactiontagging.objects.Prompt;

import org.json.JSONObject;

/**
 * Created by zeniuus on 2017. 8. 13..
 */

public class CustomQuestionPrompt extends Dialog {
    Context context;
    Prompt prompt;
    View.OnClickListener cancelListener;
    View.OnClickListener submitListener;
    String userId;
    String videoName;

    TextView promptQuestionView;
    EditText promptAnswerInputView;
    Button cancelBtn;
    Button submitBtn;

    public CustomQuestionPrompt(Context context, Prompt prompt,
                                View.OnClickListener cancelListener,
                                View.OnClickListener submitListener) {
        super(context, android.R.style.Theme_Translucent_NoTitleBar);

        this.context = context;
        this.prompt = prompt;
        this.cancelListener = cancelListener;
        this.submitListener = submitListener;
    }

    public CustomQuestionPrompt(Context context, Prompt prompt, String userId, String videoName) {
        super(context, android.R.style.Theme_Translucent_NoTitleBar);

        this.context = context;
        this.prompt = prompt;
        this.userId = userId;
        this.videoName = videoName;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        WindowManager.LayoutParams lpWindow = new WindowManager.LayoutParams();
        lpWindow.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        lpWindow.dimAmount = 0.4f;
        getWindow().setAttributes(lpWindow);

        setContentView(R.layout.custom_question_prompt);

        promptQuestionView = (TextView) findViewById(R.id.prompt_question_text);
        promptQuestionView.setText(prompt.getPromptQuestion());

        promptAnswerInputView = (EditText) findViewById(R.id.prompt_answer_input);

        cancelBtn = (Button) findViewById(R.id.cancel_btn);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "cancel button clicked", Toast.LENGTH_SHORT).show();
                dismiss();
                ((VideoHorizontalActivity) context).videoStart();
            }
        });

        submitBtn = (Button) findViewById(R.id.submit_btn);
        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String answer = promptAnswerInputView.getText().toString();
                promptAnswerInputView.setText("");

                JSONObject newAnswerJSON = new JSONObject();
                try {
                    newAnswerJSON.accumulate("type", prompt.getPromptType().ordinal());
                    newAnswerJSON.accumulate("time", prompt.getPromptTime());
                    newAnswerJSON.accumulate("question", prompt.getPromptQuestion());
                    newAnswerJSON.accumulate("userId", userId);
                    newAnswerJSON.accumulate("answer", answer);

                    String result = new HttpRequestHandler("POST",
                            MainActivity.SERVER_URL + "/new_prompt_answer/" + videoName,
                            newAnswerJSON.toString()).doHttpRequest();

                    JSONObject resultJSON = new JSONObject(result);
                    if (resultJSON.getString("success").compareTo("success") == 0)
                        Log.d("result", "success");
                    else
                        Log.d("result", "fail");
                } catch (Exception e) {
                    e.printStackTrace();
                }

                Toast.makeText(context, "submit button clicked", Toast.LENGTH_SHORT).show();
                dismiss();
                ((VideoHorizontalActivity) context).videoStart();
            }
        });
    }
}
