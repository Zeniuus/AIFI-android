package com.zeniuus.www.reactiontagging.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.zeniuus.www.reactiontagging.R;
import com.zeniuus.www.reactiontagging.networks.HttpRequestHandler;

import org.json.JSONObject;

/**
 * Created by zeniuus on 2017. 7. 11..
 */

public class SignupActivity extends AppCompatActivity {
    EditText userId;
    EditText userPw;
    EditText confirmPw;
    Button signupBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        userId = (EditText) findViewById(R.id.user_id);
        userPw = (EditText) findViewById(R.id.user_password);
        confirmPw = (EditText) findViewById(R.id.confirm_password);
        signupBtn = (Button) findViewById(R.id.signin_btn);

        signupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userIdText = userId.getText().toString();
                String userPwText = userPw.getText().toString();
                String confirmPwText = confirmPw.getText().toString();

                if (userPwText.compareTo(confirmPwText) == 0) {
                    String result = new HttpRequestHandler("GET", MainActivity.SERVER_URL + "/check_duplicate_id/" + userIdText, "").doHttpRequest();
                    Log.d("http req result", result);

                    try {
                        JSONObject jsonResult = new JSONObject(result);
                        if (jsonResult.getBoolean("duplicated")) {
                            Toast.makeText(SignupActivity.this, "Duplicated ID. Please try again with another ID.", Toast.LENGTH_SHORT).show();
                        } else {
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.accumulate("userId", userIdText);
                            jsonObject.accumulate("userPw", userPwText);

                            result = new HttpRequestHandler("POST", MainActivity.SERVER_URL + "/new_user", jsonObject.toString()).doHttpRequest();
                            jsonResult = new JSONObject(result);
                            if (jsonResult.getString("result").compareTo("success") == 0) {
                                Toast.makeText(SignupActivity.this, "Successfuly signed in", Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                Toast.makeText(SignupActivity.this, "Sign in failed with unknown error... Please try again.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    } catch (Exception e) {
                        Log.d("exception", e.toString());
                    }
                } else {
                    Toast.makeText(SignupActivity.this, "Password does not match with confirm password.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
