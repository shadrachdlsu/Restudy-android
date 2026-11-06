package com.example.restudy;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Log in anonymously as soon as the app opens, so we have a user id ready
        // by the time the user saves their first check-in.
        FirebaseRepo.ensureSignedIn();

        // Add the system bar insets on top of the padding set in the XML (keeps the 28dp side margins)
        View root = findViewById(R.id.main);
        int padStart = root.getPaddingStart();
        int padTop = root.getPaddingTop();
        int padEnd = root.getPaddingEnd();
        int padBottom = root.getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPaddingRelative(padStart + systemBars.left, padTop + systemBars.top,
                    padEnd + systemBars.right, padBottom + systemBars.bottom);
            return insets;
        });

        // Start -> session setup screen
        findViewById(R.id.btnStart).setOnClickListener(v ->
                startActivity(new Intent(this, SessionActivity.class)));
    }
}
