package com.example.restudy;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/** 14-day summary. All values are hard-coded in activity_reflection.xml for now. */
public class ReflectionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reflection);

        // TODO: compute averages from saved check-ins

        findViewById(R.id.btnExplore).setOnClickListener(v ->
                startActivity(new Intent(this, WhatChangedActivity.class)));

        // TODO: entries history screen
        findViewById(R.id.btnReviewEntries).setOnClickListener(v ->
                Toast.makeText(this, "Entries coming soon.", Toast.LENGTH_SHORT).show());
    }
}
