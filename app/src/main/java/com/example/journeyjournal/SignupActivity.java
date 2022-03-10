package com.example.journeyjournal;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Patterns;
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

public class SignupActivity extends AppCompatActivity {

    TextInputLayout fullname, email, password;
    Button login_button;

    SharedPreferences sharedPreferences;

    final String signup_url = "http://journeyjournal.pythonanywhere.com/user/register";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        fullname = findViewById(R.id.signup_fullname);
        email = findViewById(R.id.signup_email);
        password = findViewById(R.id.signup_password);
        login_button = findViewById(R.id.login_button);

        String blackString = "<font color='#000000'>Already have an account? </font>";
        String blueString = "<font color='#0000FF'>Login</font>";
        login_button.setText(Html.fromHtml(blackString + blueString));

        sharedPreferences = getSharedPreferences("JourneyJournal", Context.MODE_PRIVATE);
    }

    public void loginButtonClick(View view) {
        Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    public void registerButtonClick(View view) {
        String fullname_ = fullname.getEditText().getText().toString();
        String email_ = email.getEditText().getText().toString();
        String password_ = password.getEditText().getText().toString();

        if (!fullname_.isEmpty()) {
            fullname.setError(null);
            fullname.setErrorEnabled(false);
            if (!email_.isEmpty()) {
                email.setError(null);
                email.setErrorEnabled(false);
                if (!isValidEmail(email_)) {
                    email.setError("Please enter valid email");
                }
                if (!password_.isEmpty()) {
                    password.setError(null);
                    password.setErrorEnabled(false);

                    // Register logic here
                    
                    new RegisterUser().execute(fullname_, email_, password_);

                } else {
                    password.setError("Please enter password");
                }
            } else {
                email.setError("Please enter email");
            }
        } else {
            fullname.setError("Please enter full name");
        }
    }

    public boolean isValidEmail(String email_) {
        if (Patterns.EMAIL_ADDRESS.matcher(email_).matches()) {
            return true;
        } else {
            return false;
        }
    }

    public void socialRegisterClick(View view) {
        Toast.makeText(this, "Thanks for your patience. Coming soon.", Toast.LENGTH_SHORT).show();
    }

    public class RegisterUser extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            String fullname = strings[0];
            String email = strings[1];
            String password = strings[2];

            OkHttpClient okHttpClient = new OkHttpClient();
            RequestBody formBody = new FormBody.Builder()
                    .add("fullname", fullname)
                    .add("email", email)
                    .add("password", password)
                    .build();

            Request request = new Request.Builder()
                    .url(signup_url)
                    .post(formBody)
                    .build();
            Response response = null;

            try{
                response = okHttpClient.newCall(request).execute();
                if (response.code() == 200){
                    // parse response to json
                    String result = response.body().string();
                    JSONObject resultJson = new JSONObject(result);
                    String access_token = resultJson.getString("access_token");

                    // save access_token
                    sharedPreferences.edit().putString("access_token", access_token).commit();

                    SignupActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(SignupActivity.this, "Your account have been successfully registered", Toast.LENGTH_LONG).show();
                        }
                    });
                    Intent intent = new Intent(SignupActivity.this, HomeActivity.class);
                    startActivity(intent);
                    finish();
                }else{
                    SignupActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(SignupActivity.this, "A user with that email already exists", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }catch (Exception e){
                // Display error message maybe Internal server error or Something went wrong
                SignupActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(SignupActivity.this, "Oops! Something went wrong", Toast.LENGTH_LONG).show();
                    }
                });
                e.printStackTrace();
            }
            return null;
        }
    }
}
