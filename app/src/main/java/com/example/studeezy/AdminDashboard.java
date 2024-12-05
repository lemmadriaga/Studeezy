package com.example.studeezy;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import com.example.studeezy.userAuth.Login;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class AdminDashboard extends AppCompatActivity {

    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private FirebaseFirestore firestore;
    private ArrayList<User> userList;
    private EditText searchBar;
    private ImageView signOutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        searchBar = findViewById(R.id.searchBar);
        signOutButton = findViewById(R.id.signOutButton);

        firestore = FirebaseFirestore.getInstance();
        userList = new ArrayList<>();

        loadUsers();

        
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                filterUsers(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        
        signOutButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(AdminDashboard.this, Login.class));
            finish();
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadUsers() {
        firestore.collection("users").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        userList.clear();
                        for (var doc : task.getResult()) {
                            String id = doc.getId();
                            String name = doc.getString("name");
                            String email = doc.getString("email");
                            Boolean hasPremium = doc.getBoolean("hasPremium");
                            Long expirationDate = doc.getLong("expirationDate");

                            userList.add(new User(id, name, email, hasPremium, expirationDate));
                        }

                        userAdapter = new UserAdapter(userList, this);
                        recyclerView.setAdapter(userAdapter);
                    } else {
                        Toast.makeText(this, "Failed to load users", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void filterUsers(String query) {
        ArrayList<User> filteredList = new ArrayList<>();
        for (User user : userList) {
            if (user.getName().toLowerCase().contains(query.toLowerCase()) ||
                    user.getEmail().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(user);
            }
        }
        userAdapter.updateList(filteredList);
    }
}
