package com.zeniuus.www.reactiontagging.activities;

import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.zeniuus.www.reactiontagging.R;
import com.zeniuus.www.reactiontagging.helpers.SoftKeyboard;
import com.zeniuus.www.reactiontagging.managers.EmojiFeedbackManager;
import com.zeniuus.www.reactiontagging.managers.FeedbackManager;
import com.zeniuus.www.reactiontagging.networks.HttpRequestHandler;
import com.zeniuus.www.reactiontagging.objects.Feedback;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static android.view.MotionEvent.ACTION_UP;
import static java.lang.Thread.sleep;

public class VideoActivity extends AppCompatActivity {
    SoftKeyboard softKeyboard;
    VideoView videoView;
    TextView pauseText;
    ProgressBar progressBar;
    EditText feedbackInput;
    Button button;
    TextView title;
    TextView playTime;
//    TextView feedbackView;
//    TextView emojiLIKE;
//    TextView emojiLOVE;
//    TextView emojiHAHA;
//    TextView emojiWOW;
//    TextView emojiSAD;
//    TextView emojiANGRY;
    TextView givingThreadTo;
    LinearLayout suggestionFeedbackLayout;
    ListView suggestionFeedbackList;
    SuggestionFeedbackAdapter suggestionFeedbackAdapter;
    LinearLayout threadLayout;
    Button gobackBtn;
    TextView selectedFeedback;
    ListView threadFeedbackList;
    ThreadFeedbackAdapter threadFeedbackAdapter;
    ListView feedbackHistoryList;
    DrawerLayout drawerLayout;
    ActionBarDrawerToggle actionBarDrawerToggle;
    FeedbackHistoryAdapter feedbackHistoryAdapter;

    FeedbackManager feedbackManager;
    public EmojiFeedbackManager emojiFeedbackManager;
    String videoName;
    String userId;
    ArrayList<Feedback> suggestionFeedback;
    ArrayList<Feedback> myFeedback;
    ArrayList<JSONObject> threadFeedback;
    boolean isShowingSuggestionFeedback = true;
    Feedback currThreadFeedback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        softKeyboard = new SoftKeyboard
                ((ViewGroup) findViewById(android.R.id.content), (InputMethodManager) getSystemService(Service.INPUT_METHOD_SERVICE));
        softKeyboard.setSoftKeyboardCallback(new SoftKeyboard.SoftKeyboardChanged()
        {

            @Override
            public void onSoftKeyboardHide()
            {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (isShowingSuggestionFeedback)
                            suggestionFeedbackLayout.setVisibility(View.VISIBLE);
                        else
                            threadLayout.setVisibility(View.VISIBLE);
                        givingThreadTo.setVisibility(View.GONE);
                    }
                });
            }

            @Override
            public void onSoftKeyboardShow()
            {
                runOnUiThread(new Runnable() {
                    public void run() {
                        if (isShowingSuggestionFeedback)
                            suggestionFeedbackLayout.setVisibility(View.GONE);
                        else {
                            threadLayout.setVisibility(View.GONE);
                            givingThreadTo.setVisibility(View.VISIBLE);
                        }
                    }
                });
            }
        });

        final View contentView = this.findViewById(android.R.id.content);

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
                if (isShowingSuggestionFeedback) {
                    Log.d("event", "give a new feedback");
                    if (feedbackManager.addItem(
                            Integer.toString(startTime),
                            Integer.toString(startTime + 5000),
                            feedbackInput.getText().toString()) == -2)
                        Toast.makeText(VideoActivity.this, "Please give a richer feedback", Toast.LENGTH_SHORT).show();
                    else {
                        feedbackInput.setText("");
                        softKeyboard.closeSoftKeyboard();
//                        new ProgressController().execute();
                    }
                } else {
                    Log.d("event", "give a new thread feedback");
                    feedbackManager.giveThreadFeedback(currThreadFeedback, feedbackInput.getText().toString());
                    feedbackInput.setText("");
                    softKeyboard.closeSoftKeyboard();
//                    new ProgressController().execute();
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

//        emojiLIKE = (TextView) findViewById(R.id.emoji_like);
//        emojiLOVE = (TextView) findViewById(R.id.emoji_love);
//        emojiHAHA = (TextView) findViewById(R.id.emoji_haha);
//        emojiWOW = (TextView) findViewById(R.id.emoji_wow);
//        emojiSAD = (TextView) findViewById(R.id.emoji_sad);
//        emojiANGRY = (TextView) findViewById(R.id.emoji_angry);
//
//        emojiLIKE.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                emojiFeedbackManager.addItem(videoView.getCurrentPosition(), Emoji.LIKE);
//            }
//        });
//        emojiLOVE.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                emojiFeedbackManager.addItem(videoView.getCurrentPosition(), Emoji.LOVE);
//            }
//        });
//        emojiHAHA.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                emojiFeedbackManager.addItem(videoView.getCurrentPosition(), Emoji.HAHA);
//            }
//        });
//        emojiWOW.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                emojiFeedbackManager.addItem(videoView.getCurrentPosition(), Emoji.WOW);
//            }
//        });
//        emojiSAD.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                emojiFeedbackManager.addItem(videoView.getCurrentPosition(), Emoji.SAD);
//            }
//        });
//        emojiANGRY.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                emojiFeedbackManager.addItem(videoView.getCurrentPosition(), Emoji.ANGRY);
//            }
//        });

        givingThreadTo = (TextView) findViewById(R.id.feedback_giving_thread_to);

        suggestionFeedbackLayout = (LinearLayout) findViewById(R.id.suggestion_feedback_layout);
        suggestionFeedbackList = (ListView) findViewById(R.id.suggestion_feedback_list);
        suggestionFeedback = new ArrayList<>();
        suggestionFeedbackAdapter = new SuggestionFeedbackAdapter
                (this, android.R.layout.simple_list_item_1, suggestionFeedback);
        suggestionFeedbackList.setAdapter(suggestionFeedbackAdapter);

        threadLayout = (LinearLayout) findViewById(R.id.thread_layout);
        gobackBtn = (Button) findViewById(R.id.goback_btn);
        gobackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                suggestionFeedbackLayout.setVisibility(View.VISIBLE);
                threadLayout.setVisibility(View.GONE);
                isShowingSuggestionFeedback = true;
            }
        });
        selectedFeedback = (TextView) findViewById(R.id.selected_feedback);
        threadFeedbackList = (ListView) findViewById(R.id.thread_feedback_list);
        threadFeedback = new ArrayList<>();
        threadFeedbackAdapter = new ThreadFeedbackAdapter(this, android.R.layout.simple_list_item_1, threadFeedback);
        threadFeedbackList.setAdapter(threadFeedbackAdapter);

        feedbackHistoryList = (ListView) findViewById(R.id.feedback_history_list);
        myFeedback = new ArrayList<>();
        feedbackHistoryAdapter = new FeedbackHistoryAdapter(this, android.R.layout.simple_list_item_1, myFeedback);
        feedbackHistoryList.setAdapter(feedbackHistoryAdapter);
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
                                Feedback targetFeedback = feedbackHistoryAdapter.getItem(position);
                                feedbackHistoryAdapter.remove(targetFeedback);
                                feedbackManager.remove(targetFeedback);
                                feedbackHistoryAdapter.notifyDataSetChanged();
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
                                        // TODO: put target feedback again - feedbackHistoryAdapter.addItem(targetFeedback);
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
                updateMyFeedback();
                pauseVideo();
            }
        };
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
    }

    private void updateFeedback(ArrayList<Feedback> feedbacks) {
        suggestionFeedback.clear();

        Iterator<Feedback> iter = feedbacks.iterator();
        while (iter.hasNext()) {
            suggestionFeedback.add(iter.next());
        }

        suggestionFeedbackAdapter.notifyDataSetChanged();
    }

    public void addFeedback(Feedback feedback) {
        suggestionFeedback.add(feedback);
        Collections.sort(suggestionFeedback, FeedbackManager.mComparator);
        suggestionFeedbackAdapter.notifyDataSetChanged();
    }

//    public void showEmojiFeedback(int[] emojiCnt) {
//        emojiLIKE.setText(" " + emojiCnt[0]);
//        emojiLOVE.setText(" " + emojiCnt[1]);
//        emojiHAHA.setText(" " + emojiCnt[2]);
//        emojiWOW.setText(" " + emojiCnt[3]);
//        emojiSAD.setText(" " + emojiCnt[4]);
//        emojiANGRY.setText(" " + emojiCnt[5]);
//    }

    public void updateMyFeedback() {
        Iterator<Feedback> iter = feedbackManager.getFeedbacksOfPerson(userId).iterator();

        myFeedback.clear();
        while (iter.hasNext()) {
            myFeedback.add(iter.next());
        }

        feedbackHistoryAdapter.notifyDataSetChanged();
    }

    public void updateThreadFeedback(Feedback feedback) {
        threadFeedback.clear();
        try {
            JSONArray jsonArray = feedback.getThread();
            for (int i = 0; i < jsonArray.length(); i++) {
                threadFeedback.add(jsonArray.getJSONObject(i));
            }

            threadFeedbackAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            Log.d("exception", e.toString());
        }
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

            updateFeedback(feedbackManager.getFeedbacksAtTime(current));
//            showEmojiFeedback(emojiFeedbackManager.getFeedbacksAtTime(current));
            updateMyFeedback();
            playTime.setText(milisecToMinSec(current) + " / " + milisecToMinSec(videoView.getDuration()));

            if (!videoView.isPlaying() || progressBar.getProgress() >= 100)
                isProgressRunning = false;
        }
    }

    public static String milisecToMinSec(int milisec) {
        return milisec / 60000 + ":" + (milisec % 60000) / 1000;
    }

    public void updateEmoticons() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                        showEmojiFeedback(emojiFeedbackManager.getFeedbacksAtTime(videoView.getCurrentPosition()));
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
        updateFeedback(feedbackManager.getFeedbacksAtTime((int)(videoView.getDuration() * (x / width))));
        playTime.setText(milisecToMinSec((int)(videoView.getDuration() * (x / width))) + " / " + milisecToMinSec(videoView.getDuration()));
        Log.d("width", Integer.toString(width));
        Log.d("x", Float.toString(x));
        Log.d("set progress", Integer.toString((int)((x * 100) / width)));
        Log.d("seek to", Integer.toString((int)(videoView.getDuration() * (x / width))));
    }

    /**
     * Created by zeniuus on 2017. 7. 13..
     */

    public class FeedbackHistoryAdapter extends ArrayAdapter<Feedback> {
        ArrayList<Feedback> mList;
        public FeedbackHistoryAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
        }

        public FeedbackHistoryAdapter(Context context, int resource, List<Feedback> items) {
            super(context, resource, items);
            mList = new ArrayList<>(items);
        }

        @Override
        public View getView(int pos, View convertView, ViewGroup parent) {
            View v = convertView;

            if (v == null) {
                LayoutInflater inflater;
                inflater = LayoutInflater.from(getContext());
                v = inflater.inflate(android.R.layout.simple_list_item_1, null);
            }

            TextView textView = (TextView) v.findViewById(android.R.id.text1);
            textView.setText(getItem(pos).getFeedbackText());

            return v;
        }
    }

    /**
     * Created by zeniuus on 2017. 7. 16..
     */

    public class SuggestionFeedbackAdapter extends ArrayAdapter<Feedback> {
        ArrayList<Feedback> mList;

        public SuggestionFeedbackAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
        }

        public SuggestionFeedbackAdapter(Context context, int resource, List<Feedback> items) {
            super(context, resource, items);
            mList = new ArrayList<>(items);
        }

        @Override
        public View getView(final int pos, View convertView, ViewGroup parent) {
            View v = convertView;

            if (v == null) {
                LayoutInflater inflater;
                inflater = LayoutInflater.from(getContext());
                v = inflater.inflate(R.layout.suggestion_feedback_list_item, null);
            }

            final TextView textView = (TextView) v.findViewById(R.id.suggestion_feedback);
            textView.setText(getItem(pos).getFeedbackText());

//            textView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    Log.d("event", "click");
//
//                    pauseVideo();
//                    givingThreadTo.setText(textView.getText());
//                    InputMethodManager keyboard = (InputMethodManager)
//                            getSystemService(Context.INPUT_METHOD_SERVICE);
//                    keyboard.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
//                    feedbackInput.requestFocus();
//                }
//            });
//            textView.setOnLongClickListener(new View.OnLongClickListener() {
//                @Override
//                public boolean onLongClick(View view) {
//                    Log.d("event", "long click");
//                    return true;
//                }
//            });

            Button feedbackLikeBtn = (Button) v.findViewById(R.id.feedback_like_btn);
            feedbackLikeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    feedbackManager.giveLikeToFeedback(getItem(pos));
                }
            });

            Button expandBtn = (Button) v.findViewById(R.id.expand_btn);
            expandBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    currThreadFeedback = getItem(pos);

                    selectedFeedback.setText("Thread: " + getItem(pos).getFeedbackText());

                    updateThreadFeedback(getItem(pos));

                    suggestionFeedbackLayout.setVisibility(View.GONE);
                    threadLayout.setVisibility(View.VISIBLE);
                    isShowingSuggestionFeedback = false;
                    givingThreadTo.setText(textView.getText());
                }
            });

            return v;
        }
    }

    public class ThreadFeedbackAdapter extends ArrayAdapter<JSONObject> {
        ArrayList<JSONObject> mList;

        public ThreadFeedbackAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
        }

        public ThreadFeedbackAdapter(Context context, int resource, List<JSONObject> items) {
            super(context, resource, items);
            mList = new ArrayList<>(items);
        }

        private String toString(int pos) {
            try {
                return getItem(pos).getString("feedback");
            } catch (Exception e) {
                Log.d("exception", e.toString());
                return "";
            }
        }

        @Override
        public View getView(final int pos, View convertView, ViewGroup parent) {
            View v = convertView;

            if (v == null) {
                LayoutInflater inflater;
                inflater = LayoutInflater.from(getContext());
                v = inflater.inflate(R.layout.thread_feedback_list_item, null);
            }

            TextView textView = (TextView) v.findViewById(R.id.thread_feedback);
            textView.setText(toString(pos));

            Button threadFeedbackLikeBtn = (Button) v.findViewById(R.id.thread_feedback_like_btn);
            threadFeedbackLikeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    feedbackManager.giveLikeToThreadFeedback(currThreadFeedback, pos);
                }
            });


            return v;
        }
    }
}