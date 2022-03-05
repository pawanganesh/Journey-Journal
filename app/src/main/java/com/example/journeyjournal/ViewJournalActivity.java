package com.example.journeyjournal;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ViewJournalActivity extends BaseActivity {

    TextView title, description, posted_on;
    int id;
    ImageView photo;
    String journal_url;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_journal);

        title = findViewById(R.id.title);
        posted_on = findViewById(R.id.posted_on);
        description = findViewById(R.id.description);
        photo = findViewById(R.id.photo);

        sharedPreferences = getSharedPreferences("JourneyJournal", Context.MODE_PRIVATE);
        id = getIntent().getIntExtra("id", 0);

        Log.i("ID", String.valueOf(id));

        journal_url = "https://journeyjournal.pythonanywhere.com/journal/" + id + "/";

        Log.i("journal_url", journal_url);

        new GetJournal().execute();

    }

    @Override
    protected void onResume() {
        super.onResume();
        new GetJournal().execute();
    }

    public class GetJournal extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            String access_token = sharedPreferences.getString("access_token", "");
            OkHttpClient okHttpClient = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(journal_url)
                    .addHeader("Authorization", "Bearer " + access_token)
                    .build();
            Response response = null;

            try {
                response = okHttpClient.newCall(request).execute();
                if (response.code() == 200) {
                    String result = response.body().string();
                    JSONObject resultJson = new JSONObject(result);
                    String title_ = resultJson.getString("title");
                    String photo_ = resultJson.getString("photo");
                    String description_ = resultJson.getString("description");
                    String created_at_ = resultJson.getString("created_at");
                    ViewJournalActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            title.setText(title_);
                            posted_on.setText(created_at_);
                            description.setText(description_);
                            Glide.with(ViewJournalActivity.this)
                                    .load(photo_)
                                    .into(photo);
                        }
                    });
                } else if (response.code() == 403) {
                    // token invalid/expired or malformed token
                    startActivity(new Intent(ViewJournalActivity.this, LoginActivity.class));
                    finish();
                } else {
                    Log.i("ERROR", "ERROR");
                }
            } catch (Exception e) {
                Log.i("EXCEPTION", "EXCEPTION");
                e.printStackTrace();
            }
            return null;
        }
    }
}