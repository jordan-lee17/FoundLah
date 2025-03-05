package com.example.foundlah;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.ComponentActivity;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class AdminHomeActivity extends ComponentActivity {

    private Button btnTrack, btnChat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnTrack = findViewById(R.id.btnTrackConvos);
        btnTrack.setOnClickListener(v -> trackConvos());

        btnChat = findViewById(R.id.btnChat);
        btnChat.setOnClickListener(v -> trackChats());
    }

    private void trackConvos(){
        Intent intent = new Intent(this, AdminConvoListActivity.class);
        startActivity(intent);
    }

    private void trackChats(){
        Intent intent = new Intent(this, ConvoListActivity.class);
        startActivity(intent);
    }
}