package com.example.foundlah;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.sql.Time;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.firebase.Timestamp;

public class ChatActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private EditText messageInput;
    private Button sendButton;
    private RecyclerView recyclerView;
    private MessageAdapter messageAdapter;
    private List<Message> messageList = new ArrayList<>();
    private String chatroomID;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);
        recyclerView = findViewById(R.id.recyclerView);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        messageAdapter = new MessageAdapter(messageList);
        recyclerView.setAdapter(messageAdapter);

        chatroomID = getIntent().getStringExtra("chatroomID");
        if(chatroomID == null){
            Log.e("ChatActivity", "No chatroomID provided!");
            finish();
            return;
        }

        sendButton.setOnClickListener(v -> sendMessage());
        loadMessages(chatroomID);
    }


    private void sendMessage(){
        String text = messageInput.getText().toString();

        if (!text.isEmpty()) {

            Long currentTime = System.currentTimeMillis();
            Message message = new Message(text, auth.getCurrentUser().getEmail(), currentTime);

            Log.d("ChatActivity", "Current time: " + currentTime);

            db.collection("chatrooms").document(chatroomID)
                        .collection("messages")
                        .add(message)
                        .addOnSuccessListener(documentReference -> Log.d("ChatActivity", "Message sent"))
                        .addOnFailureListener((e -> Log.e("ChatActivity", "Error sending message", e)));

            messageInput.setText("");
        }
    }

    private void loadMessages(String chatroomID){

        db.collection("chatrooms").document(chatroomID)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshots, e) -> {
                if (e != null) {
                    Log.e("ChatActivity", "Listen failed", e);
                    return;
                }

                messageList.clear();
                for (DocumentSnapshot doc: snapshots.getDocuments()){
                    Message message = doc.toObject(Message.class);
                    messageList.add(message);
                }

                messageAdapter.notifyDataSetChanged();
                recyclerView.scrollToPosition(messageList.size() - 1);
            });
    }
}