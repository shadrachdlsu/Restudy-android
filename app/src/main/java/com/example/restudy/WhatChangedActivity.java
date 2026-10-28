package com.example.restudy;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

/** Before vs after averages, weekdays / weekends. Values are hard-coded for now. */
public class WhatChangedActivity extends AppCompatActivity {

    // TODO: compute from saved check-ins. Order: stress before, stress after, mood before,
    // mood after, burnout before, burnout after (scale 1-5)
    private static final float[] WEEKDAYS = {3.7f, 3.0f, 2.5f, 3.22f, 3.6f, 3.22f};
    private static final float[] WEEKENDS = {3.1f, 2.6f, 3.0f, 3.5f, 3.2f, 2.9f};

    private static final float MAX_BAR_DP = 140f; // bar height for a value of 5.0

    private static final int[] BAR_IDS = {
            R.id.barStressBefore, R.id.barStressAfter,
            R.id.barMoodBefore, R.id.barMoodAfter,
            R.id.barBurnoutBefore, R.id.barBurnoutAfter};
    private static final int[] VALUE_IDS = {
            R.id.valStressBefore, R.id.valStressAfter,
            R.id.valMoodBefore, R.id.valMoodAfter,
            R.id.valBurnoutBefore, R.id.valBurnoutAfter};

    private View tabWeekdays, tabWeekends;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_what_changed);

        tabWeekdays = findViewById(R.id.tabWeekdays);
        tabWeekends = findViewById(R.id.tabWeekends);
        tabWeekdays.setOnClickListener(v -> showTab(true));
        tabWeekends.setOnClickListener(v -> showTab(false));
        showTab(true);

        // TODO: timeline screen ("when these changes happened")
        findViewById(R.id.btnTimeline).setOnClickListener(v ->
                Toast.makeText(this, "Timeline coming soon.", Toast.LENGTH_SHORT).show());

        // Back to home; CLEAR_TOP closes the reflection screens so the cycle starts fresh
        findViewById(R.id.btnBackToStart).setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }

    // bg_segment / segment_text handle the selected-tab colors
    private void showTab(boolean weekdays) {
        tabWeekdays.setSelected(weekdays);
        tabWeekends.setSelected(!weekdays);

        float[] values = weekdays ? WEEKDAYS : WEEKENDS;
        float density = getResources().getDisplayMetrics().density;
        for (int i = 0; i < values.length; i++) {
            View bar = findViewById(BAR_IDS[i]);
            ViewGroup.LayoutParams lp = bar.getLayoutParams();
            lp.height = Math.round(values[i] / 5f * MAX_BAR_DP * density);
            bar.setLayoutParams(lp);

            ((TextView) findViewById(VALUE_IDS[i]))
                    .setText(String.format(Locale.getDefault(), "%.2f", values[i]));
        }
    }
}
