package com.example.journeyjournal;

import androidx.annotation.Nullable;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONObject;

import java.io.File;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class EditJournalActivity extends BaseActivity {
    Button submit_button;
    TextView screen_title;
    ImageView image;
    TextInputLayout title, description, location;
    Button select_location;
    String lat, lon, place_name;
    Uri uri;
    int id;
    private static final int REQUEST_CODE_MAP = 101;

    private static final int ERROR_DIALOG_REQUEST = 9001;

    SharedPreferences sharedPreferences;

    String edit_journal_url, get_journal_url, journal_photo_url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_journal);

        id = getIntent().getIntExtra("id", 0);
        get_journal_url = "https://journeyjournal.pythonanywhere.com/journal/" + id + "/";
        edit_journal_url = "https://journeyjournal.pythonanywhere.com/journal/" + id;
        journal_photo_url = "https://journeyjournal.pythonanywhere.com/journal/photo/" + id;

        sharedPreferences = getSharedPreferences("JourneyJournal", Context.MODE_PRIVATE);

        submit_button = findViewById(R.id.submit_button);
        submit_button.setText("Edit Journal");
        screen_title = findViewById(R.id.screen_title);
        screen_title.setText("Edit Journal");
        title = findViewById(R.id.title);
        description = findViewById(R.id.description);
        image = findViewById(R.id.image);
        location = findViewById(R.id.location);
        location.setEnabled(false);

        new GetJournal().execute();

        if (isServiceOk()) {
            init();
        }

    }

    private void init() {
        select_location = findViewById(R.id.select_location);

        select_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(EditJournalActivity.this, MapActivity.class);
                startActivityForResult(intent, REQUEST_CODE_MAP);
            }
        });
    }

    public void JournalSubmitClick(View view) {
        String title_ = title.getEditText().getText().toString();
        String description_ = description.getEditText().getText().toString();
        String place_name_ = location.getEditText().getText().toString();

        // EditJournal call
        new EditJournal().execute(title_, description_, lat, lon, place_name_);

    }

    public class EditJournal extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            String title = strings[0];
            String description = strings[1];
            String lat_ = strings[2];
            String lon_ = strings[3];
            String name_place = strings[4];
            sharedPreferences = getSharedPreferences("JourneyJournal", Context.MODE_PRIVATE);
            String access_token = sharedPreferences.getString("access_token", "");

            OkHttpClient okHttpClient = new OkHttpClient();
            RequestBody formBody = new FormBody.Builder()
                    .add("title", title)
                    .add("description", description)
                    .add("lat", lat_)
                    .add("long", lon_)
                    .add("place_name", name_place)
                    .build();

            Request request = new Request.Builder()
                    .url(edit_journal_url)
                    .addHeader("Authorization", "Bearer " + access_token)
                    .patch(formBody)
                    .build();
            Response response = null;

            try {
                response = okHttpClient.newCall(request).execute();
                Log.i("ERRORRRR", String.valueOf(response));
                Log.i("ERRORRRR MSG", response.message());
                if (response.code() == 200) {
                    // parse response to json
                    String result = response.body().string();
                    JSONObject resultJson = new JSONObject(result);
                    EditJournalActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(EditJournalActivity.this, "Journal updated", Toast.LENGTH_LONG).show();
                        }
                    });
                    Intent intent = new Intent(EditJournalActivity.this, ViewJournalActivity.class);
                    intent.putExtra("id", id);
                    startActivity(intent);
                    finish();
                } else {
                    EditJournalActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(EditJournalActivity.this, "Something went wrong", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            } catch (Exception e) {
                // Display error message maybe Internal server error or Something went wrong
                EditJournalActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(EditJournalActivity.this, "Oops! Something went wrong", Toast.LENGTH_LONG).show();
                    }
                });
                e.printStackTrace();
            }
            return null;
        }
    }

    public class UpdateJournalImage extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            String photo = strings[0];
            sharedPreferences = getSharedPreferences("JourneyJournal", Context.MODE_PRIVATE);
            String access_token = sharedPreferences.getString("access_token", "");

            File file = new File(uri.getPath());
            final MediaType MEDIA_TYPE = MediaType.parse("image/*");

            OkHttpClient okHttpClient = new OkHttpClient();
            MultipartBody formBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("photo", file.getName(), RequestBody.create(MEDIA_TYPE, file))
                    .build();

            Request request = new Request.Builder()
                    .url(journal_photo_url)
                    .addHeader("Authorization", "Bearer " + access_token)
                    .patch(formBody)
                    .build();
            Response response = null;

            try {
                response = okHttpClient.newCall(request).execute();
                Log.i("ERRORRRR", String.valueOf(response));
                if (response.code() == 200) {
                    EditJournalActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(EditJournalActivity.this, "Photo updated", Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    EditJournalActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(EditJournalActivity.this, "Something went wrong", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            } catch (Exception e) {
                // Display error message maybe Internal server error or Something went wrong
                EditJournalActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(EditJournalActivity.this, "Oops! Something went wrong", Toast.LENGTH_LONG).show();
                    }
                });
                e.printStackTrace();
            }
            return null;
        }
    }

    public class GetJournal extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            String access_token = sharedPreferences.getString("access_token", "");
            OkHttpClient okHttpClient = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(get_journal_url)
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
                    String name_place = resultJson.getString("place_name");

                    lat = resultJson.getString("lat");
                    lon = resultJson.getString("long");
                    place_name = name_place;

                    EditJournalActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            title.getEditText().setText(title_);
                            description.getEditText().setText(description_);
                            location.getEditText().setText(name_place);

                            Glide.with(getApplicationContext())
                                    .load(photo_)
                                    .into(image);
                        }
                    });
                } else if (response.code() == 403) {
                    // token invalid/expired or malformed token
                    startActivity(new Intent(EditJournalActivity.this, LoginActivity.class));
                    finish();
                } else {
                    Log.i("ERROR", "ERROR");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public boolean isServiceOk() {
        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(EditJournalActivity.this);

        if (available == ConnectionResult.SUCCESS) {
            // everything is fine and user can make map requests
            return true;

        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            // an error occurred but we can resolve it
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(EditJournalActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();

        } else {
            Toast.makeText(this, "You cannot make request", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    public void selectImageClick(View view) {
        ImagePicker.with(EditJournalActivity.this)
                .crop()
                .compress(1024)
                .start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_MAP) {
            if (resultCode == Activity.RESULT_OK) {
                lat = (String) data.getStringExtra("lat");
                lon = (String) data.getStringExtra("lon");
                place_name = data.getStringExtra("place_name");
                location.getEditText().setText(place_name);
            }
        } else if (resultCode == Activity.RESULT_OK) {
            uri = data.getData();
            image.setImageURI(uri);
            String photo_ = image.toString();
            new UpdateJournalImage().execute(photo_);
        }
    }
}