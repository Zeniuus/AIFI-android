package com.zeniuus.www.reactiontagging.activities;

import android.content.Intent;
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

public class LoginActivity extends AppCompatActivity {
    EditText userId;
    EditText userPw;
    Button loginBtn;
    Button signinBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        userId = (EditText) findViewById(R.id.user_id);
        userPw = (EditText) findViewById(R.id.user_password);
        loginBtn = (Button) findViewById(R.id.login_btn);
        signinBtn = (Button) findViewById(R.id.signin_btn);

        signinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, SigninActivity.class);
                startActivity(intent);
            }
        });

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userIdText = userId.getText().toString();
                String userPwText = userPw.getText().toString();

                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.accumulate("userId", userIdText);
                    jsonObject.accumulate("userPw", userPwText);
                    String result = new HttpRequestHandler("POST", MainActivity.SERVER_URL + "/try_login", jsonObject.toString()).doHttpRequest();
                    JSONObject jsonResult = new JSONObject(result);
                    if (jsonResult.getBoolean("success")) {
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.putExtra("userId", userIdText);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, "Login failed; Wrong ID or password. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Log.d("exception", e.toString());
                }
            }
        });
    }
}
