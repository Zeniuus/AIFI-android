package com.zeniuus.www.reactiontagging.activities;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.VideoView;

import com.zeniuus.www.reactiontagging.R;

import org.w3c.dom.Text;

import static android.view.MotionEvent.ACTION_UP;
import static java.lang.Thread.sleep;

/**
 * Created by zeniuus on 2017. 8. 10..
 */

public class VideoHorizontalActivity extends AppCompatActivity {
    VideoView videoView;
    ProgressBar progressBar;
    TextView playTimeTextView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_horizontal_video);

        videoView = (VideoView) findViewById(R.id.video_view);
        videoView.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.cat_behavior));

        progressBar = (ProgressBar) findViewById(R.id.progress_bar);

        playTimeTextView = (TextView) findViewById(R.id.play_time);

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

//            updateFeedback(feedbackManager.getFeedbacksAtTime(current));
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
//        updateFeedback(feedbackManager.getFeedbacksAtTime((int)(videoView.getDuration() * (x / width))));
        playTimeTextView.setText(milisecToMinSec((int)(videoView.getDuration() * (x / width))) + " / " + milisecToMinSec(videoView.getDuration()));
    }

}
