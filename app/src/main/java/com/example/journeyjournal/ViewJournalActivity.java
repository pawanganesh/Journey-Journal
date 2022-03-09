package com.example.journeyjournal;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.bumptech.glide.Glide;

import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ViewJournalActivity extends BaseActivity {

    TextView title, description, posted_on, place_name;
    int id;
    ImageView photo;
    String journal_url;
    String delete_journal_url;
    SharedPreferences sharedPreferences;
    String latitude, longitude, name_place_;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_journal);

        title = findViewById(R.id.title);
        posted_on = findViewById(R.id.posted_on);
        description = findViewById(R.id.description);
        photo = findViewById(R.id.photo);
        place_name = findViewById(R.id.place_name);

        sharedPreferences = getSharedPreferences("JourneyJournal", Context.MODE_PRIVATE);
        id = getIntent().getIntExtra("id", 0);

        Log.i("ID", String.valueOf(id));

        journal_url = "https://journeyjournal.pythonanywhere.com/journal/" + id + "/";
        delete_journal_url = "https://journeyjournal.pythonanywhere.com/journal/delete/" + id;

        Log.i("journal_url", journal_url);

        new GetJournal().execute();

    }

    @Override
    protected void onResume() {
        super.onResume();
        new GetJournal().execute();
    }

    public void deleteButtonClick(View view) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Delete");
        dialog.setMessage("Are you sure?");
        dialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // delete logic
                new DeleteJournal().execute();
            }
        });
        dialog.setNegativeButton("NO", null);
        dialog.setCancelable(false);
        dialog.show();
    }

    public void placeNameClick(View view) {
        Intent intent = new Intent(ViewJournalActivity.this, ViewJournalLocationActivity.class);
        intent.putExtra("latitude", latitude);
        intent.putExtra("longitude", longitude);
        intent.putExtra("place_name", name_place_);
        startActivity(intent);
    }

    public void editButtonClick(View view) {
        Intent intent = new Intent(ViewJournalActivity.this, EditJournalActivity.class);
        intent.putExtra("id", id);
        startActivity(intent);
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
                    String name_place = resultJson.getString("place_name");
                    latitude = resultJson.getString("lat");
                    longitude = resultJson.getString("long");
                    name_place_ = name_place;

                    ViewJournalActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            title.setText(title_);
                            posted_on.setText(created_at_);
                            description.setText(description_);
                            place_name.setText(name_place);
                            Glide.with(getApplicationContext())
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

    public class DeleteJournal extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            String access_token = sharedPreferences.getString("access_token", "");
            OkHttpClient okHttpClient = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(delete_journal_url)
                    .addHeader("Authorization", "Bearer " + access_token)
                    .delete()
                    .build();
            Response response = null;

            try {
                response = okHttpClient.newCall(request).execute();
                Log.i("RESPONSE DELETE", String.valueOf(response));
                if (response.code() == 204) {
                    ViewJournalActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(ViewJournalActivity.this, "Journal deleted", Toast.LENGTH_LONG).show();
                            startActivity(new Intent(ViewJournalActivity.this, LoginActivity.class));
                            finish();
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