package com.example.journeyjournal;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HomeActivity extends BaseActivity {

    TextView fullname;
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
        listView = findViewById(R.id.listview);


//        GetJournals();
        new GetAlljournal().execute();
        new GetCurrentUser().execute();

    }

    @Override
    protected void onResume() {
        super.onResume();
//        GetJournals();
        new GetAlljournal().execute();
    }


    public void GetJournals(){
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
                                String count = resultJson.getString("count");
                                Log.i("COUNT", count);

                                int countOfResult = journals.length();
                                Log.i("countOfResult", String.valueOf(countOfResult));
                                for (int i = 0; i < journals.length(); i++) {
                                    JournalInfo info = new JournalInfo();
                                    JSONObject journal = journals.getJSONObject(i);

                                    Log.i("HELLO", String.valueOf(journal));


                                    info.id = journal.getInt("id");
                                    info.title = journal.getString("title");
                                    info.description = journal.getString("description");
                                    info.created_at = journal.getString("created_at");
                                    info.photo = journal.getString("photo");

                                    list.add(info);

                                }
                                Log.i("LIST", String.valueOf(list));
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
    }

    public void addNewJournalButtonClick(View view) {
        Log.i("AAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAa");
        Intent intent = new Intent(HomeActivity.this, NewJournalActivity.class);
        startActivity(intent);
    }

    public class GetAlljournal extends AsyncTask<String, Void, String>{

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
                                    String count = resultJson.getString("count");
                                    Log.i("COUNT", count);

                                    int countOfResult = journals.length();
                                    Log.i("countOfResult", String.valueOf(countOfResult));
                                    for (int i = 0; i < journals.length(); i++) {
                                        JournalInfo info = new JournalInfo();
                                        JSONObject journal = journals.getJSONObject(i);

                                        Log.i("HELLO", String.valueOf(journal));


                                        info.id = journal.getInt("id");
                                        info.title = journal.getString("title");
                                        info.description = journal.getString("description");
                                        info.created_at = journal.getString("created_at");
                                        info.photo = journal.getString("photo");

                                        list.add(info);

                                    }
                                    Log.i("LIST", String.valueOf(list));
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
