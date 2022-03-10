package com.example.journeyjournal;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class JournalListAdapter extends ArrayAdapter<JournalInfo> {
    Context context;

    public JournalListAdapter(@NonNull Context context, ArrayList<JournalInfo> list) {
        super(context, 0, list);
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.journal_item_layout, null);
        TextView title = view.findViewById(R.id.title);
        TextView posted_on = view.findViewById(R.id.posted_on);
        ImageView photo = view.findViewById(R.id.photo);

        JournalInfo info = getItem(position);

        title.setText(info.title);
        posted_on.setText((CharSequence) info.created_at);
        Glide.with(context).load(info.photo).into(photo);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ViewJournalActivity.class);
                intent.putExtra("id", info.id);
                context.startActivity(intent);
            }
        });
        return view;
    }
}
