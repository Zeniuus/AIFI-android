package com.zeniuus.www.reactiontagging.activities;

import android.content.DialogInterface;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutCompat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.zeniuus.www.reactiontagging.R;
import com.zeniuus.www.reactiontagging.adapters.FeedbackHistoryAdapter;
import com.zeniuus.www.reactiontagging.managers.EmojiFeedbackManager;
import com.zeniuus.www.reactiontagging.managers.FeedbackManager;
import com.zeniuus.www.reactiontagging.networks.HttpRequestHandler;
import com.zeniuus.www.reactiontagging.objects.EmojiFeedback;
import com.zeniuus.www.reactiontagging.objects.Feedback;
import com.zeniuus.www.reactiontagging.types.Emoji;

import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static android.view.MotionEvent.ACTION_UP;
import static java.lang.Thread.sleep;

public class VideoActivity extends AppCompatActivity {
    VideoView videoView;
    TextView pauseText;
    ProgressBar progressBar;
    EditText feedbackInput;
    Button button;
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
    ListView feedbackHistoryList;
    DrawerLayout drawerLayout;
    ActionBarDrawerToggle actionBarDrawerToggle;
    FeedbackHistoryAdapter mAdapter;

    FeedbackManager feedbackManager;
    public EmojiFeedbackManager emojiFeedbackManager;
    String videoName;
    String userId;
    ArrayList<Feedback> myFeedback;


    final static String[] SUGGESTION_FEEDBACK = {
            "need more description",
            "weak connection",
            "no reference",
            "lack of visual aids",
            "too fast",
            "typo in slide"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        final View contentView = this.findViewById(android.R.id.content);

        contentView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {

                Rect r = new Rect();
                contentView.getWindowVisibleDisplayFrame(r);
                int screenHeight = contentView.getRootView().getHeight();

                // r.bottom is the position above soft keypad or device button.
                // if keypad is shown, the r.bottom is smaller than that before.
                int keypadHeight = screenHeight - r.bottom;

                Log.d("keypad height", "" + keypadHeight);

                LinearLayout suggestionFeedbackLayout = (LinearLayout) findViewById(R.id.suggestion_feedback_layout);

                if (keypadHeight > screenHeight * 0.15) { // 0.15 ratio is perhaps enough to determine keypad height.
                    suggestionFeedbackLayout.setVisibility(View.GONE);
                } else {
                    suggestionFeedbackLayout.setVisibility(View.VISIBLE);
                }
            }
        });

        videoName = getIntent().getStringExtra("video name");
        userId = getIntent().getStringExtra("userId");

        feedbackManager = new FeedbackManager(videoName, userId, this);
        emojiFeedbackManager = new EmojiFeedbackManager(videoName, userId, this);

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
                    if (videoView.isPlaying()) pauseVideo();
                    else {
                        startVideo();
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

        pauseText = (TextView) findViewById(R.id.pause_text);

        feedbackInput = (EditText) findViewById(R.id.feedback);
        feedbackInput.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP)
                    pauseVideo();
                return false;
            }
        });

        button = (Button) findViewById(R.id.feedback_submit_btn);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int startTime = videoView.getCurrentPosition();

                if (feedbackManager.addItem(
                        Integer.toString(startTime),
                        Integer.toString(startTime + 5000),
                        feedbackInput.getText().toString()) == -2)
                    Toast.makeText(VideoActivity.this, "Please give a richer feedback", Toast.LENGTH_SHORT).show();
                else {
                    feedbackInput.setText("");
                    new ProgressController().execute();
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
                    pauseVideo();
                    moveProgressBar(event);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
//                    startVideo();
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

        feedbackHistoryList = (ListView) findViewById(R.id.feedback_history_list);
        myFeedback = new ArrayList<>();
        mAdapter = new FeedbackHistoryAdapter(this, android.R.layout.simple_list_item_1, myFeedback);
        feedbackHistoryList.setAdapter(mAdapter);
//        feedbackHistoryList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Log.d("listview", "clicked : " + position);
//            }
//        });

        feedbackHistoryList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                Log.d("feedback history list", "item clicked: " + position);
                String feedback = feedbackHistoryList.getItemAtPosition(position).toString();
                // Use the Builder class for convenient dialog construction
                AlertDialog.Builder builder = new AlertDialog.Builder(VideoActivity.this);
                builder.setMessage("Are you sure to delete the following feedback?\n" + feedback)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Feedback targetFeedback = mAdapter.getItem(position);
                                mAdapter.remove(targetFeedback);
                                feedbackManager.remove(targetFeedback);
                                mAdapter.notifyDataSetChanged();
                                try {
                                    JSONObject jsonObject = new JSONObject();
                                    jsonObject.accumulate("videoName", videoName);
                                    jsonObject.accumulate("userId", targetFeedback.getUserId());
                                    jsonObject.accumulate("startTime", targetFeedback.getStartTime());
                                    jsonObject.accumulate("endTime", targetFeedback.getEndTime());
                                    jsonObject.accumulate("feedback", targetFeedback.getFeedback());
                                    String result = new HttpRequestHandler("POST",
                                            MainActivity.SERVER_URL + "/delete_feedback",
                                            jsonObject.toString()).doHttpRequest();
                                    JSONObject jsonResult = new JSONObject(result);
                                    if (!jsonResult.getBoolean("success")) {
                                        // TODO: put target feedback again - mAdapter.addItem(targetFeedback);
                                        Toast.makeText(VideoActivity.this, "Deletion falied in server...", Toast.LENGTH_SHORT).show();
                                    }
                                } catch (Exception e) {
                                    Log.d("exception", e.toString());
                                }

                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // User cancelled the dialog
                            }
                        });

                builder.create().show();
            }
        });

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout,
                R.string.open_drawer, R.string.close_drawer) {
            @Override
            public void onDrawerClosed(View drawerView) {
                Log.d("drawer layout", "closed");
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                Log.d("drawer layout", "opened");
                super.onDrawerOpened(drawerView);
                pauseVideo();
            }
        };
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
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

    public void showEmojiFeedback(int[] emojiCnt) {
        emojiLIKE.setText(" " + emojiCnt[0]);
        emojiLOVE.setText(" " + emojiCnt[1]);
        emojiHAHA.setText(" " + emojiCnt[2]);
        emojiWOW.setText(" " + emojiCnt[3]);
        emojiSAD.setText(" " + emojiCnt[4]);
        emojiANGRY.setText(" " + emojiCnt[5]);
    }

    public void updateMyFeedback() {
        Iterator<Feedback> iter = feedbackManager.getFeedbacksOfPerson(userId).iterator();

        myFeedback.clear();
        while (iter.hasNext()) {
            myFeedback.add(iter.next());
        }

        mAdapter.notifyDataSetChanged();
    }

    private void pauseVideo() {
        videoView.pause();
        pauseText.setVisibility(View.VISIBLE);
    }

    private void startVideo() {
        videoView.start();
        pauseText.setVisibility(View.INVISIBLE);
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

//            showFeedback(feedbackManager.getFeedbacksAtTime(current));
            showEmojiFeedback(emojiFeedbackManager.getFeedbacksAtTime(current));
            updateMyFeedback();
            playTime.setText(milisecToMinSec(current) + " / " + milisecToMinSec(videoView.getDuration()));

            if (!videoView.isPlaying() || progressBar.getProgress() >= 100)
                isProgressRunning = false;
        }
    }

    public static String milisecToMinSec(int milisec) {
        return milisec / 60000 + ":" + (milisec % 60000) / 1000;
    }

    public void refreshEmojiFeedback() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showEmojiFeedback(emojiFeedbackManager.getFeedbacksAtTime(videoView.getCurrentPosition()));
                    }
                });
            }
        }).start();
    }

    private void moveProgressBar(MotionEvent event) {
        int width = progressBar.getWidth();
        float x = event.getX();
        progressBar.setProgress((int)((x * 100) / width));
        videoView.seekTo((int)(videoView.getDuration() * (x / width)));
        playTime.setText(milisecToMinSec((int)(videoView.getDuration() * (x / width))) + " / " + milisecToMinSec(videoView.getDuration()));
        Log.d("width", Integer.toString(width));
        Log.d("x", Float.toString(x));
        Log.d("set progress", Integer.toString((int)((x * 100) / width)));
        Log.d("seek to", Integer.toString((int)(videoView.getDuration() * (x / width))));
    }
}