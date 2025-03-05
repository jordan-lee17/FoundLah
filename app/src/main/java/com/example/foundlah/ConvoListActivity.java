package com.example.foundlah;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.ComponentActivity;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConvoListActivity extends ComponentActivity {

    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private List<User> userList = new ArrayList<>();
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_convo_list);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        auth = FirebaseAuth.getInstance();

        userAdapter = new UserAdapter(userList, user -> {
            openChat(user.getEmail()); // Open chat on user click
        });

        recyclerView.setAdapter(userAdapter);

        loadUsers();
    }

    private void loadUsers() {
        String currentUserEmail = auth.getCurrentUser().getEmail();

        // Get users from Firestore
        FirebaseFirestore.getInstance().collection("users").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    userList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        User user = doc.toObject(User.class);
                        if (user != null && !user.getEmail().equals(currentUserEmail)) {
                            userList.add(user); // Exclude current user
                        }
                    }
                    userAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Log.e("UserList", "Error loading users", e));
    }

    private String generateChatroomID(String user1, String user2){
        if(user1.compareTo(user2) < 0){
            return user1 + "_" + user2;
        }
        else{
            return user2 + "_" + user1;
        }
    }

    private void openChatActivity(String chatroomID){
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("chatroomID", chatroomID);
        startActivity(intent);
    }

    private void openChat(String otherUserEmail) {

        String currentUserEmail = auth.getCurrentUser().getEmail();
        String chatroomID = generateChatroomID(currentUserEmail, otherUserEmail);

        DocumentReference chatroomRef = db.collection("chatrooms").document(chatroomID);

        chatroomRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()){
                Log.d("Chat", "Chatroom exists: " + chatroomID);
                openChatActivity(chatroomID);
            }
            else{
                Map<String, Object> chatroom = new HashMap<>();
                chatroom.put("users", Arrays.asList(currentUserEmail, otherUserEmail));
                chatroomRef.set(chatroom).addOnSuccessListener(aVoid ->{
                    Log.d("Chat", "New room created: " + chatroomID);
                    openChatActivity(chatroomID);
                });
            }
        });
        /*Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("otherUserEmail", otherUserEmail);
        startActivity(intent);*/
    }


}