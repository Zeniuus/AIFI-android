package com.zeniuus.www.reactiontagging.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.zeniuus.www.reactiontagging.objects.Feedback;

import java.util.ArrayList;
import java.util.List;

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
        textView.setText(getItem(pos).toString());

        return v;
    }
}
