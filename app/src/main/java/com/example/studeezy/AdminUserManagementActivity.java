package com.example.studeezy;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studeezy.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class AdminUserManagementActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private FirebaseFirestore firestore;
    private DatabaseReference realtimeDatabaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_user_management);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        firestore = FirebaseFirestore.getInstance();
        realtimeDatabaseRef = FirebaseDatabase.getInstance().getReference("users");

        loadUsers();
    }

    private void loadUsers() {
        
        firestore.collection("users")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        ArrayList<User> userList = new ArrayList<>();
                        for (var doc : task.getResult()) {
                            String id = doc.getId();
                            String name = doc.getString("name");
                            String email = doc.getString("email");
                            Boolean hasPremium = doc.getBoolean("hasPremium");
                            long expirationDate = doc.getLong("expirationDate");

                            
                            realtimeDatabaseRef.child(id)
                                    .get()
                                    .addOnSuccessListener(snapshot -> {
                                        if (snapshot.exists()) {
                                            Boolean premium = snapshot.child("hasPremium").getValue(Boolean.class);
                                            Long expiry = snapshot.child("expirationDate").getValue(Long.class);

                                            userList.add(new User(id, name, email, premium, expiry));
                                            userAdapter.notifyDataSetChanged();
                                        }
                                    });
                        }
                        userAdapter = new UserAdapter(userList, this);
                        recyclerView.setAdapter(userAdapter);
                    } else {
                        Toast.makeText(this, "Failed to load users", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
