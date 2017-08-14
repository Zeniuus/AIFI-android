package com.zeniuus.www.reactiontagging.prompts;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.zeniuus.www.reactiontagging.R;
import com.zeniuus.www.reactiontagging.objects.Prompt;

/**
 * Created by zeniuus on 2017. 8. 13..
 */

public class CustomQuestionPrompt extends Dialog {
    Context context;
    String promptQuestion;
    View.OnClickListener cancelListener;
    View.OnClickListener submitListener;

    TextView promptQuestionView;
    EditText promptAnswerInputView;
    Button cancelBtn;
    Button submitBtn;

    public CustomQuestionPrompt(Context context, String promptQuestion,
                                View.OnClickListener cancelListener,
                                View.OnClickListener submitListener) {
        super(context, android.R.style.Theme_Translucent_NoTitleBar);

        this.context = context;
        this.promptQuestion = promptQuestion;
        this.cancelListener = cancelListener;
        this.submitListener = submitListener;
    }

    public CustomQuestionPrompt(Context context, Prompt prompt,
                                View.OnClickListener cancelListener,
                                View.OnClickListener submitListener) {
        super(context, android.R.style.Theme_Translucent_NoTitleBar);

        this.context = context;
        this.promptQuestion = prompt.getPromptQuestion();
        this.cancelListener = cancelListener;
        this.submitListener = submitListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        WindowManager.LayoutParams lpWindow = new WindowManager.LayoutParams();
        lpWindow.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        lpWindow.dimAmount = 0.4f;
        getWindow().setAttributes(lpWindow);

        setContentView(R.layout.custom_question_prompt);

        promptQuestionView = (TextView) findViewById(R.id.prompt_question_text);
        promptQuestionView.setText(promptQuestion);

        promptAnswerInputView = (EditText) findViewById(R.id.prompt_answer_input);

        cancelBtn = (Button) findViewById(R.id.cancel_btn);
        cancelBtn.setOnClickListener(cancelListener);

        submitBtn = (Button) findViewById(R.id.submit_btn);
        submitBtn.setOnClickListener(submitListener);
    }
}
