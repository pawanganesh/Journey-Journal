package com.example.journeyjournal;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HomeActivity extends BaseActivity {

    TextView fullname;
    ImageView empty;
    ListView listView;
    JournalListAdapter adapter;
    SharedPreferences sharedPreferences;

    final String current_user_url = "http://journeyjournal.pythonanywhere.com/user/current-user";
    final String journal_url = "https://journeyjournal.pythonanywhere.com/journal/";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        fullname = findViewById(R.id.fullname);
        empty = findViewById(R.id.empty);
        listView = findViewById(R.id.listview);
        listView.setEmptyView(empty);


        new GetAlljournal().execute();
        new GetCurrentUser().execute();

    }

    @Override
    protected void onResume() {
        super.onResume();
        new GetAlljournal().execute();
    }

    public void addNewJournalButtonClick(View view) {
        Intent intent = new Intent(HomeActivity.this, NewJournalActivity.class);
        startActivity(intent);
    }

    public class GetAlljournal extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            ArrayList<JournalInfo> list = new ArrayList<>();
            sharedPreferences = getSharedPreferences("JourneyJournal", Context.MODE_PRIVATE);
            String access_token = sharedPreferences.getString("access_token", "");
            OkHttpClient okHttpClient = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(journal_url)
                    .addHeader("Authorization", "Bearer " + access_token)
                    .build();
            okHttpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String myResponse = response.body().string();
                        HomeActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    JSONObject resultJson = new JSONObject(myResponse);
                                    JSONArray journals = resultJson.getJSONArray("results");

                                    for (int i = 0; i < journals.length(); i++) {
                                        JournalInfo info = new JournalInfo();
                                        JSONObject journal = journals.getJSONObject(i);

                                        info.id = journal.getInt("id");
                                        info.title = journal.getString("title");
                                        info.description = journal.getString("description");
                                        info.photo = journal.getString("photo");

                                        String date_ = journal.getString("created_at");
                                        try {
                                            Date date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'").parse(date_);
                                            SimpleDateFormat formatter = new SimpleDateFormat("dd MMMM yyyy hh:mm");
                                            info.created_at = formatter.format(date);

                                        } catch (ParseException e) {
                                            e.printStackTrace();
                                        }

                                        list.add(info);

                                    }
                                    adapter = new JournalListAdapter(HomeActivity.this, list);
                                    listView.setAdapter(adapter);

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                }
            });
            return null;
        }
    }

    public class GetCurrentUser extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            String access_token = sharedPreferences.getString("access_token", "");
            OkHttpClient okHttpClient = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(current_user_url)
                    .addHeader("Authorization", "Bearer " + access_token)
                    .build();
            Response response = null;

            try {
                response = okHttpClient.newCall(request).execute();
                if (response.code() == 200) {
                    String result = response.body().string();
                    JSONObject resultJson = new JSONObject(result);
                    String fullname_ = resultJson.getString("fullname");
                    HomeActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.i("CURRENT_USER", fullname_);
                            String welcome_message = "Welcome " + fullname_;
                            fullname.setText(welcome_message);
                            Log.i("welcome_message", welcome_message);
                        }
                    });
                } else if (response.code() == 403) {
                    // token invalid/expired or malformed token
                    startActivity(new Intent(HomeActivity.this, LoginActivity.class));
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
