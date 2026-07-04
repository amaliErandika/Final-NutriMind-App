package com.example.nutrimind;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class DiaryAdapter extends BaseAdapter {

    private List<DiaryModel> diaryList;
    private LayoutInflater inflater;

    public DiaryAdapter(Context context, List<DiaryModel> diaryList) {
        this.diaryList = diaryList;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return diaryList.size();
    }

    @Override
    public Object getItem(int position) {
        return diaryList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.activity_diary_item, parent, false);
        }

        // Bind the diary data to the view
        DiaryModel entry = diaryList.get(position);

        TextView feelingText = convertView.findViewById(R.id.textFeeling);
        TextView dateText = convertView.findViewById(R.id.textDate);

        feelingText.setText(entry.getFeeling());

        // Convert timestamp to readable date
        if (entry.getTimestamp() > 0) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            String formattedDate = dateFormat.format(entry.getTimestamp());
            dateText.setText(formattedDate);
        } else {
            dateText.setText("Unknown Date");
        }

        return convertView;
    }
}
