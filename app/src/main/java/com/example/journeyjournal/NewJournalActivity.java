package com.example.journeyjournal;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.github.dhaval2404.imagepicker.ImagePicker;

public class NewJournalActivity extends AppCompatActivity {

    ImageView image;
    String title, description;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_journal);

        image = findViewById(R.id.image);

    }

    public void selectImageClick(View view) {
        ImagePicker.with(NewJournalActivity.this)
                .crop()
                .compress(1024)
                .start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Uri uri = data.getData();
        image.setImageURI(uri);
    }
}