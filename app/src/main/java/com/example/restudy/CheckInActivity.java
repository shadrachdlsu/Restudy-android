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
 * Answers are only highlighted on screen (nothing is saved or passed on).
 */
public class CheckInActivity extends AppCompatActivity {

    public static final String EXTRA_AFTER = "extra_after";

    private boolean isAfter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_in);

        isAfter = getIntent().getBooleanExtra(EXTRA_AFTER, false);
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

        findViewById(R.id.btnStart).setOnClickListener(v -> goNext());
        findViewById(R.id.btnAnswerLater).setOnClickListener(v -> goNext());
    }

    // Before -> lockdown, After -> reflection
    private void goNext() {
        Class<?> next = isAfter ? ReflectionActivity.class : LockdownActivity.class;
        startActivity(new Intent(this, next));
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
}
