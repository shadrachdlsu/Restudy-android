package com.example.restudy;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class SessionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session);

        // TODO: set chipDay / chipTime / txtSessionLabel from the current date and time

        // Ready -> check-in before the lockdown
        findViewById(R.id.btnReady).setOnClickListener(v -> {
            startActivity(new Intent(this, CheckInActivity.class));
            finish();
        });

        // Not yet -> back to home
        findViewById(R.id.btnNotYet).setOnClickListener(v -> finish());
    }
}
