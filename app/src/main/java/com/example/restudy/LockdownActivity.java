package com.example.restudy;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class LockdownActivity extends AppCompatActivity {

    private static final long TOTAL_MS = 30 * 60 * 1000L; // 30 minutes

    private TextView txtTimer;
    private CountDownTimer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lockdown);

        txtTimer = findViewById(R.id.txtTimer);
        updateTimerText(TOTAL_MS);
        startTimer();

        // TODO: real quick-reset actions (guided breathing, water reminder, open music app)
        findViewById(R.id.btnBreathe).setOnClickListener(v -> toast("Breathe in… and out."));
        findViewById(R.id.btnWater).setOnClickListener(v -> toast("Grab a glass of water."));
        findViewById(R.id.btnMusic).setOnClickListener(v -> toast("Music coming soon."));

        findViewById(R.id.btnEndEarly).setOnClickListener(v -> endLockdown());
    }

    private void startTimer() {
        timer = new CountDownTimer(TOTAL_MS, 1000) {
            @Override
            public void onTick(long msLeft) {
                updateTimerText(msLeft);
            }

            @Override
            public void onFinish() {
                updateTimerText(0);
                endLockdown();
            }
        }.start();
    }

    private void updateTimerText(long ms) {
        long totalSec = (ms + 999) / 1000;
        txtTimer.setText(String.format(Locale.getDefault(), "%02d:%02d",
                totalSec / 60, totalSec % 60));
    }

    // Lockdown over -> check-in after
    private void endLockdown() {
        if (timer != null) timer.cancel();
        Intent intent = new Intent(this, CheckInActivity.class);
        intent.putExtra(CheckInActivity.EXTRA_AFTER, true);
        // Pass the session id along so the "after" answers go into the same session document
        intent.putExtra(CheckInActivity.EXTRA_SESSION_ID,
                getIntent().getStringExtra(CheckInActivity.EXTRA_SESSION_ID));
        startActivity(intent);
        finish();
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) timer.cancel();
    }
}
