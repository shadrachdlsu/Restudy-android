package com.example.restudy;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Check-in screen, used before AND after the lockdown.
 * Pass EXTRA_AFTER = true to show the "after" version.
 * "Start"/"Done" saves the picked answers to Firestore (see FirebaseRepo); "Answer later" skips saving.
 */
public class CheckInActivity extends AppCompatActivity {

    public static final String EXTRA_AFTER = "extra_after";
    /** Id of the current session, created in SessionActivity and passed screen to screen. */
    public static final String EXTRA_SESSION_ID = "extra_session_id";

    private boolean isAfter;
    private String sessionId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_in);

        isAfter = getIntent().getBooleanExtra(EXTRA_AFTER, false);
        sessionId = getIntent().getStringExtra(EXTRA_SESSION_ID);
        if (sessionId == null) sessionId = FirebaseRepo.newSessionId(); // safety net
        if (isAfter) {
            ((TextView) findViewById(R.id.txtPhase)).setText("After lockdown");
            ((TextView) findViewById(R.id.txtTitle)).setText("How are you after that?");
            ((TextView) findViewById(R.id.txtSubtitle))
                    .setText("A quick check-in after your 30-minute recovery block.");
            ((TextView) findViewById(R.id.btnStart)).setText("Done");
        }

        setupScale(R.id.scaleStress);
        setupScale(R.id.scaleMood);
        setupScale(R.id.scaleEnergy);

        // Start/Done -> save the answers, then move on. Firestore saves in the background
        // (and queues it if offline), so we don't wait for it to finish.
        findViewById(R.id.btnStart).setOnClickListener(v -> {
            FirebaseRepo.saveCheckIn(sessionId, isAfter,
                    selectedValue(R.id.scaleStress),
                    selectedValue(R.id.scaleMood),
                    selectedValue(R.id.scaleEnergy));
            goNext();
        });
        // Answer later -> nothing saved
        findViewById(R.id.btnAnswerLater).setOnClickListener(v -> goNext());
    }

    // Before -> lockdown, After -> reflection
    private void goNext() {
        Class<?> next = isAfter ? ReflectionActivity.class : LockdownActivity.class;
        Intent intent = new Intent(this, next);
        intent.putExtra(EXTRA_SESSION_ID, sessionId); // lockdown hands it to the "after" check-in
        startActivity(intent);
        finish();
    }

    /**
     * The scale row layout is: dot, line, dot, line, dot, line, dot, line, dot
     * so the dots are the children at even indexes (0, 2, 4, 6, 8).
     * Tapping a dot selects it and unselects the others; bg_scale_dot handles the colors.
     */
    private void setupScale(int rowId) {
        LinearLayout row = findViewById(rowId);
        for (int i = 0; i < row.getChildCount(); i += 2) {
            row.getChildAt(i).setOnClickListener((View picked) -> {
                for (int j = 0; j < row.getChildCount(); j += 2) {
                    row.getChildAt(j).setSelected(row.getChildAt(j) == picked);
                }
            });
        }
    }

    /**
     * Returns the picked value 1-5 for a scale row, or 0 if no dot is selected.
     * Dots are at child index 0, 2, 4, 6, 8 -> value = index / 2 + 1.
     */
    private int selectedValue(int rowId) {
        LinearLayout row = findViewById(rowId);
        for (int i = 0; i < row.getChildCount(); i += 2) {
            if (row.getChildAt(i).isSelected()) return i / 2 + 1;
        }
        return 0;
    }
}
