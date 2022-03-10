package com.example.journeyjournal;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONObject;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {

    Button signupButton, loginButton;
    TextInputLayout email, password;

    SharedPreferences sharedPreferences;

    final String login_url = "http://journeyjournal.pythonanywhere.com/user/login";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        signupButton = findViewById(R.id.signup_button);
        loginButton = findViewById(R.id.login_button);
        email = findViewById(R.id.login_email);
        password = findViewById(R.id.login_password);

        String blackString = "<font color='#000000'>Don't have an account yet? </font>";
        String blueString = "<font color='#0000FF'>Create new</font>";
        signupButton.setText(Html.fromHtml(blackString + blueString));

        sharedPreferences = getSharedPreferences("JourneyJournal", Context.MODE_PRIVATE);

        if (sharedPreferences.contains("access_token")) {
            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
            finish();
        }

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email_ = email.getEditText().getText().toString();
                String password_ = password.getEditText().getText().toString();

                if (!email_.isEmpty()) {
                    email.setError(null);
                    email.setErrorEnabled(false);
                    if (!password_.isEmpty()) {
                        password.setError(null);
                        password.setErrorEnabled(false);
                        // Login logic here
                        new LoginUser().execute(email_, password_);
                    } else {
                        password.setError("Please enter password");
                    }
                } else {
                    email.setError("Please enter email");
                }
            }
        });

        // Go to register screen when clicked on signup
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(intent);
            }
        });
    }

    public void socialoginClick(View view) {
        Toast.makeText(this, "Thanks for your patience. Coming soon.", Toast.LENGTH_SHORT).show();
    }

    public class LoginUser extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            String email = strings[0];
            String password = strings[1];

            OkHttpClient okHttpClient = new OkHttpClient();
            RequestBody formBody = new FormBody.Builder()
                    .add("email", email)
                    .add("password", password)
                    .build();

            Request request = new Request.Builder()
                    .url(login_url)
                    .post(formBody)
                    .build();
            Response response = null;

            try {
                response = okHttpClient.newCall(request).execute();
                if (response.code() == 200) {
                    // parse response to json
                    String result = response.body().string();
                    JSONObject resultJson = new JSONObject(result);
                    String access_token = resultJson.getString("access_token");

                    Log.i("ACCESS_TOKEN", access_token);
                    Log.i("ACCESS_TOKEN TYPE", access_token.getClass().getName());
                    // save access_token
                    sharedPreferences.edit().putString("access_token", access_token).commit();
                    LoginActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(LoginActivity.this, "Login success", Toast.LENGTH_LONG).show();
                        }
                    });
                    Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    // Display error message maybe Invalid Credentials if status !== 200
                    LoginActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(LoginActivity.this, "Invalid Credentials", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            } catch (Exception e) {
                // Display error message maybe Internal server error or Something went wrong
                LoginActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(LoginActivity.this, "Oops! Something went wrong", Toast.LENGTH_LONG).show();
                    }
                });
                e.printStackTrace();
            }
            return null;
        }
    }
}
