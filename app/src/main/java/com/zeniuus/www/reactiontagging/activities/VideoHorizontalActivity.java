package com.zeniuus.www.reactiontagging.activities;

import android.app.Service;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.zeniuus.www.reactiontagging.R;
import com.zeniuus.www.reactiontagging.helpers.SoftKeyboard;
import com.zeniuus.www.reactiontagging.managers.FeedbackManager;
import com.zeniuus.www.reactiontagging.objects.Feedback;

import org.json.JSONArray;
import org.json.JSONException;
import org.w3c.dom.Text;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import static android.view.MotionEvent.ACTION_UP;
import static java.lang.Thread.sleep;

/**
 * Created by zeniuus on 2017. 8. 10..
 */

public class VideoHorizontalActivity extends AppCompatActivity {

    TextView titleView;
    VideoView videoView;

    LinearLayout progressLayout;
    ProgressBar progressBar;
    ImageView playPauseIconView;
    TextView playTimeTextView;
    EditText myFeedbackInputView;
    Button myFeedbackSubmitBtn;

    LinearLayout feedbackListLayout;
    TextView feedbackListBtn;
    ListView feedbackListView;

    FeedbackListAdapter feedbackListAdapter;
    ArrayList<Feedback> feedbackList;

//    SoftKeyboard softKeyboard;
    FeedbackManager feedbackManager;

    String videoName;
    String userId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_horizontal_video);

        videoName = getIntent().getStringExtra("video name");
        userId = getIntent().getStringExtra("userId");

//        softKeyboard = new SoftKeyboard
//                ((ViewGroup) findViewById(android.R.id.content), (InputMethodManager) getSystemService(Service.INPUT_METHOD_SERVICE));
//        softKeyboard.setSoftKeyboardCallback(new SoftKeyboard.SoftKeyboardChanged()
//        {
//            @Override
//            public void onSoftKeyboardHide()
//            {
//
//            }
//
//            @Override
//            public void onSoftKeyboardShow()
//            {
//
//            }
//        });
        feedbackManager = new FeedbackManager(videoName, userId, this);

        titleView = (TextView) findViewById(R.id.title);
//        titleView.setText(videoName);

        videoView = (VideoView) findViewById(R.id.video_view);
        videoView.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.cat_behavior));
        videoView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == ACTION_UP
                        && event.getX() >= 0
                        && event.getY() >= 0
                        && event.getX() <= videoView.getWidth()
                        && event.getY() <= videoView.getHeight()) {
//                    if (videoView.isPlaying()) videoPause();
//                    else {
//                        videoStart();
//                        new ProgressController().execute();
//                    }
                    if (progressLayout.getVisibility() == View.GONE) {
                        titleView.setVisibility(View.VISIBLE);
                        progressLayout.setVisibility(View.VISIBLE);
                    } else {
                        titleView.setVisibility(View.GONE);
                        progressLayout.setVisibility(View.GONE);
                    }
                }

                return true;
            }
        });

        progressLayout = (LinearLayout) findViewById(R.id.progress_layout);
        progressLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        progressBar.setProgress(0);
        progressBar.setMax(100);
        progressBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN
                        || event.getAction() == MotionEvent.ACTION_MOVE) {
                    videoPause();
                    moveProgressBar(event);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
//                    startVideo();
//                    new ProgressController().execute();
                }
                return true;
            }
        });

        playPauseIconView = (ImageView) findViewById(R.id.play_pause_icon);
        playPauseIconView.setImageResource(R.drawable.ic_play);
        playPauseIconView.setTag(R.drawable.ic_play);
        playPauseIconView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((int) v.getTag() == R.drawable.ic_play) videoStart();
                else videoPause();
            }
        });

        playTimeTextView = (TextView) findViewById(R.id.play_time);

        myFeedbackInputView = (EditText) findViewById(R.id.my_feedback_input);
        myFeedbackInputView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP)
                    videoPause();
                return false;
            }
        });

        myFeedbackSubmitBtn = (Button) findViewById(R.id.my_feedback_submit_btn);
        myFeedbackSubmitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("event", "give a new feedback");

                int startTime = videoView.getCurrentPosition();

                if (feedbackManager.addItem(
                        Integer.toString(startTime),
                        Integer.toString(startTime + 5000),
                        myFeedbackInputView.getText().toString()) == -2)
                    Toast.makeText(VideoHorizontalActivity.this, "Please give a richer feedback", Toast.LENGTH_SHORT).show();
                else {
                    myFeedbackInputView.setText("");
//                    softKeyboard.closeSoftKeyboard();
                }
            }
        });

        feedbackListLayout = (LinearLayout) findViewById(R.id.feedback_list_layout);
        feedbackListLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        feedbackListBtn = (TextView) findViewById(R.id.feedback_list_btn);
        feedbackListBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (feedbackListLayout.getVisibility() == View.GONE) {
                        feedbackListLayout.setVisibility(View.VISIBLE);
                        feedbackListBtn.setText(">");
                    } else {
                        feedbackListLayout.setVisibility(View.GONE);
                        feedbackListBtn.setText("<");
                    }
                }

                return true;
            }
        });

        feedbackListView = (ListView) findViewById(R.id.feedback_list);
        feedbackList = new ArrayList<>();
        feedbackListAdapter = new FeedbackListAdapter(this, R.layout.list_item_feedback_list, feedbackList);
        feedbackListView.setAdapter(feedbackListAdapter);
    }

    private void videoPause() {
        playPauseIconView.setImageResource(R.drawable.ic_play);
        playPauseIconView.setTag(R.drawable.ic_play);
        videoView.pause();
    }

    private void videoStart() {
        playPauseIconView.setImageResource(R.drawable.ic_pause);
        playPauseIconView.setTag(R.drawable.ic_pause);
        videoView.start();
        new ProgressController().execute();
    }

    public void updateFeedback() {
        int current = videoView.getCurrentPosition();
        ArrayList<Feedback> feedbacks = feedbackManager.getFeedbacksAtTime(current);

        feedbackList.clear();

        Iterator<Feedback> iter = feedbacks.iterator();
        while (iter.hasNext()) {
            feedbackList.add(iter.next());
        }

        feedbackListAdapter.notifyDataSetChanged();
    }

    public void addFeedback(Feedback feedback) {
        feedbackList.add(feedback);
        Collections.sort(feedbackList, FeedbackManager.mComparator);
        feedbackListAdapter.notifyDataSetChanged();
    }

    private class ProgressController extends AsyncTask<Void, Void, Void> {
        int duration = 0;
        int current = 0;
        boolean isProgressRunning = true;

        @Override
        public void onPreExecute() {
            duration = videoView.getDuration();
        }

        @Override
        public Void doInBackground(Void... params) {
            do {
                publishProgress();

                try {
                    sleep(100);
                } catch (InterruptedException e) {
                    Log.e("exception", e.toString());
                }
            } while (isProgressRunning);

            return null;
        }

        @Override
        public void onProgressUpdate(Void... params) {
            current = videoView.getCurrentPosition();
            Log.d("progress", "current : " + current);
            Log.d("progress", "duration : " + duration);
            progressBar.setProgress((current * 100) / duration);
            Log.d("progress", "current progress : " + progressBar.getProgress());

            updateFeedback();
//            showEmojiFeedback(emojiFeedbackManager.getFeedbacksAtTime(current));
//            updateMyFeedback();
            playTimeTextView.setText(milisecToMinSec(current) + " / " + milisecToMinSec(videoView.getDuration()));

            if (!videoView.isPlaying() || progressBar.getProgress() >= 100)
                isProgressRunning = false;
        }
    }


    public static String milisecToMinSec(int milisec) {
        return milisec / 60000 + ":" + (milisec % 60000) / 1000;
    }

    private void moveProgressBar(MotionEvent event) {
        int width = progressBar.getWidth();
        float x = event.getX();
        progressBar.setProgress((int)((x * 100) / width));
        videoView.seekTo((int)(videoView.getDuration() * (x / width)));
        updateFeedback();
        playTimeTextView.setText(milisecToMinSec((int)(videoView.getDuration() * (x / width))) + " / " + milisecToMinSec(videoView.getDuration()));
    }

    private class FeedbackListAdapter extends ArrayAdapter<Feedback> {
        public FeedbackListAdapter(@NonNull Context context, @LayoutRes int resource, ArrayList<Feedback> data) {
            super(context, resource, data);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View v = convertView;

            if (v == null) {
                LayoutInflater li = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = li.inflate(R.layout.list_item_feedback_list, null);
            }

            TextView feedbackTextView = (TextView) v.findViewById(R.id.feedback_text);
            feedbackTextView.setText(getItem(position).toString());

            final LinearLayout feedbackReplyLayout = (LinearLayout) v.findViewById(R.id.feedback_reply_layout);

            ListView feedbackReplyListView = (ListView) v.findViewById(R.id.feedback_reply_list);
            ArrayList<String> feedbackReplies = new ArrayList<>();
            JSONArray repliesJSON = getItem(position).getThread();
            try {
                for (int i = 0; i < repliesJSON.length(); i++) {
                    feedbackReplies.add(repliesJSON.getJSONObject(i).getString("threadFeedback"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            feedbackReplyListView.setAdapter(new FeedbackReplyListAdapter(VideoHorizontalActivity.this, android.R.layout.simple_list_item_1, feedbackReplies));

            TextView expandBtn = (TextView) v.findViewById(R.id.expand_btn);
            expandBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (feedbackReplyLayout.getVisibility() == View.GONE)
                        feedbackReplyLayout.setVisibility(View.VISIBLE);
                    else
                        feedbackReplyLayout.setVisibility(View.GONE);
                }
            });

            final EditText feedbackReplyInput = (EditText) v.findViewById(R.id.feedback_reply_input);
            feedbackReplyInput.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                        String reply;
                        if ((reply = feedbackReplyInput.getText().toString()).compareTo("") == 0)
                            Toast.makeText(VideoHorizontalActivity.this, "Please give a richer feedback", Toast.LENGTH_SHORT).show();
                        else
                            feedbackManager.giveThreadFeedback(getItem(position), reply);
                    }
                    return true;
                }
            });

            return v;
        }
    }

    private class FeedbackReplyListAdapter extends ArrayAdapter<String> {
        public FeedbackReplyListAdapter(@NonNull Context context, @LayoutRes int resource, ArrayList<String> data) {
            super(context, resource, data);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;

            if (v == null) {
                LayoutInflater li = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = li.inflate(android.R.layout.simple_list_item_1, null);
            }

            TextView feedbackReplyView = (TextView) v.findViewById(android.R.id.text1);
            feedbackReplyView.setText("feedback reply!");
            feedbackReplyView.setTextColor(Color.WHITE);

            return v;
        }
    }
}
