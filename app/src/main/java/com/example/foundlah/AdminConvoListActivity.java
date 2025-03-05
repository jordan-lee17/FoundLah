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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class AdminConvoListActivity extends ComponentActivity {
    private RecyclerView recyclerView;
    private List<Convo> convoList = new ArrayList<>();
    private ConvoAdapter convoAdapter;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_convo_list);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        auth = FirebaseAuth.getInstance();

        convoAdapter = new ConvoAdapter(convoList, convo -> {
            openChat(convo.getChatroomID());
        });

        recyclerView.setAdapter(convoAdapter);
        loadConvos();
    }

    private void loadConvos(){
        FirebaseFirestore.getInstance().collection("chatrooms").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    convoList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()){
                        Log.d("FirestoreData", "Raw Data: " + doc.getData());
                        List<String> users = (List<String>) doc.get("users");
                        Convo convo = new Convo();
                        convo.setUsers(users);
                        convoList.add(convo);
                    }
                    Log.d("AdminConvoList", "ConvoList count:" + convoList.size());
                    convoAdapter.notifyDataSetChanged();

                }).addOnFailureListener(e -> Log.e("AdminConvoList", "Error loading convos", e));
    }

    private void openChat(String chatroomID){
        Intent intent= new Intent(this, AdminChatActivity.class);
        intent.putExtra("chatroomID", chatroomID);
        startActivity(intent);
    }
}