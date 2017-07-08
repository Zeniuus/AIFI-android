package com.zeniuus.www.reactiontagging.activities;

import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.zeniuus.www.reactiontagging.R;
import com.zeniuus.www.reactiontagging.managers.EmojiFeedbackManager;
import com.zeniuus.www.reactiontagging.managers.FeedbackManager;
import com.zeniuus.www.reactiontagging.objects.EmojiFeedback;
import com.zeniuus.www.reactiontagging.objects.Feedback;
import com.zeniuus.www.reactiontagging.types.Emoji;

import org.w3c.dom.Text;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static android.view.MotionEvent.ACTION_UP;
import static java.lang.Thread.sleep;

public class VideoActivity extends AppCompatActivity {
    VideoView videoView;
    Button button;
    ProgressBar progressBar;
    TextView title;
    TextView playTime;
//    TextView feedbackView;
    TextView emojiLIKE;
    TextView emojiLOVE;
    TextView emojiHAHA;
    TextView emojiWOW;
    TextView emojiSAD;
    TextView emojiANGRY;
    ListView suggestionFeedbackListView;

    FeedbackManager feedbackManager;
    EmojiFeedbackManager emojiFeedbackManager;

    final static String[] SUGGESTION_FEEDBACK = {
            "typo in slide",
            "too fast",
            "cannot understand"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        String videoName = getIntent().getStringExtra("video name");

        feedbackManager = new FeedbackManager(videoName, this);
        emojiFeedbackManager = new EmojiFeedbackManager(videoName);

        videoView = (VideoView) findViewById(R.id.video_view);
        videoView.setVideoPath(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + File.separator + "Video" + File.separator + videoName);
        videoView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == ACTION_UP
                        && event.getX() >= 0
                        && event.getY() >= 0
                        && event.getX() <= videoView.getWidth()
                        && event.getY() <= videoView.getHeight()) {
                    if (videoView.isPlaying()) videoView.pause();
                    else {
                        videoView.start();
                        new ProgressController().execute();
                    }
                }

                return true;
            }
        });
//        videoView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//            @Override
//            public void onGlobalLayout() {
//                RelativeLayout videoLayout = (RelativeLayout) findViewById(R.id.video_layout);
//                videoLayout.getLayoutParams().width = videoView.getWidth();
//                videoLayout.getLayoutParams().height = videoView.getHeight();
//                Log.d("size", "videoView width: " + videoView.getWidth());
//            }
//        });

        button = (Button) findViewById(R.id.feedback_submit_btn);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText feedback = (EditText) findViewById(R.id.feedback);
                int startTime = videoView.getCurrentPosition();

                int result;
                if ((result = feedbackManager.addItem(
                        Integer.toString(startTime),
                        Integer.toString(startTime + 5000),
                        feedback.getText().toString())) == -1)
                    Toast.makeText(VideoActivity.this, "Please time integer value in start time & end time", Toast.LENGTH_SHORT).show();
                else if (result == -2)
                    Toast.makeText(VideoActivity.this, "Please give a richer feedback", Toast.LENGTH_SHORT).show();
                else {
                    feedback.setText("");
                }
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
                    videoView.pause();
                    moveProgressBar(event);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
//                    videoView.start();
//                    new ProgressController().execute();
                }

                return true;
            }
        });

        title = (TextView) findViewById(R.id.title);
        title.setText(videoName);

        playTime = (TextView) findViewById(R.id.play_time);
        playTime.setText("0:00 / " + videoView.getDuration() / 60000 + ":" + (videoView.getDuration() % 60000) / 1000);

//        feedbackView = (TextView) findViewById(R.id.feedback_display);

        emojiLIKE = (TextView) findViewById(R.id.emoji_like);
        emojiLOVE = (TextView) findViewById(R.id.emoji_love);
        emojiHAHA = (TextView) findViewById(R.id.emoji_haha);
        emojiWOW = (TextView) findViewById(R.id.emoji_wow);
        emojiSAD = (TextView) findViewById(R.id.emoji_sad);
        emojiANGRY = (TextView) findViewById(R.id.emoji_angry);

        emojiLIKE.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emojiFeedbackManager.addItem(videoView.getCurrentPosition(), Emoji.LIKE);
            }
        });
        emojiLOVE.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emojiFeedbackManager.addItem(videoView.getCurrentPosition(), Emoji.LOVE);
            }
        });
        emojiHAHA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emojiFeedbackManager.addItem(videoView.getCurrentPosition(), Emoji.HAHA);
            }
        });
        emojiWOW.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emojiFeedbackManager.addItem(videoView.getCurrentPosition(), Emoji.WOW);
            }
        });
        emojiSAD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emojiFeedbackManager.addItem(videoView.getCurrentPosition(), Emoji.SAD);
            }
        });
        emojiANGRY.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emojiFeedbackManager.addItem(videoView.getCurrentPosition(), Emoji.ANGRY);
            }
        });

        suggestionFeedbackListView = (ListView) findViewById(R.id.suggestion_feedback_list_view);
        suggestionFeedbackListView.setAdapter(
                new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, SUGGESTION_FEEDBACK)
        );
        suggestionFeedbackListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String feedback = suggestionFeedbackListView.getItemAtPosition(position).toString();
                int startTime = videoView.getCurrentPosition();
                feedbackManager.addItem(
                        Integer.toString(startTime),
                        Integer.toString(startTime + 5000),
                        feedback
                );
            }
        });
    }

//    private void showFeedback(ArrayList<Feedback> feedbacks) {
////        Log.d("function call", "showFeedback()");
//        String feedbackStr = "";
//        Iterator<Feedback> iter = feedbacks.iterator();
//
//        if (!iter.hasNext())
//            feedbackStr = "currently no feedback";
//        else {
//            while (iter.hasNext())
//                feedbackStr += iter.next().getFeedback() + '\n';
//
//            feedbackStr = feedbackStr.substring(0, feedbackStr.length() - 1);
//        }
//
////        Log.d("data", "feedbackStr: " + feedbackStr);
//
//        feedbackView.setText(feedbackStr);
//    }

    private void showEmojiFeedback(int[] emojiCnt) {
        emojiLIKE.setText(" " + emojiCnt[0]);
        emojiLOVE.setText(" " + emojiCnt[1]);
        emojiHAHA.setText(" " + emojiCnt[2]);
        emojiWOW.setText(" " + emojiCnt[3]);
        emojiSAD.setText(" " + emojiCnt[4]);
        emojiANGRY.setText(" " + emojiCnt[5]);

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
//            Log.d("progress", "current : " + current);
//            Log.d("progress", "duration : " + duration);
            progressBar.setProgress((current * 100) / duration);
//            Log.d("progress", "current progress : " + progressBar.getProgress());

//            showFeedback(feedbackManager.getFeedbacksAtTime(current));
            showEmojiFeedback(emojiFeedbackManager.getFeedbacksAtTime(current));
            playTime.setText(milisecToMinSec(current) + " / " + milisecToMinSec(videoView.getDuration()));

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
        Log.d("width", Integer.toString(width));
        Log.d("x", Float.toString(x));
        Log.d("set progress", Integer.toString((int)((x * 100) / width)));
        Log.d("seek to", Integer.toString((int)(videoView.getDuration() * (x / width))));
    }
}