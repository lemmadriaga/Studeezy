package com.example.studeezy.userDashboard;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.net.Uri;

import com.google.android.gms.common.util.IOUtils;




import androidx.appcompat.app.AppCompatActivity;

import com.example.studeezy.R;
import com.example.studeezy.fileUpload.FileUpload;
import com.example.studeezy.PaymentActivity;
import com.example.studeezy.userAuth.Login;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class Dashboard extends AppCompatActivity {
    private TextView textPremiumStatus, textViewGreeting;
    private MaterialButton buttonSignOut, buttonUpload, buttonPayment, buttonUpgradeToPremium;
    private FirebaseAuth mAuth;
    private DatabaseReference userRef;
    private ListView campusListView;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> campusList;
    private TextInputEditText editTextSearch;
    private CircleImageView imageViewProfile;
    private androidx.cardview.widget.CardView cardPremiumStatus;
    private static final int PICK_IMAGE_REQUEST = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        mAuth = FirebaseAuth.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(Dashboard.this, Login.class));
            finish();
            return;
        }

        syncNameFromFirestoreToRealtimeDatabase(); // Call the method here

        userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid());

        initializeViews();
        setupListView();
        setupButtons();
        setupSearch();
        loadUserData();
        loadCampuses();
        checkPremiumStatus();
        initializeViews();
        loadProfilePicture();

    }


    private void initializeViews() {
        textPremiumStatus = findViewById(R.id.textPremiumStatus);
        buttonUpgradeToPremium = findViewById(R.id.buttonUpgradeToPremium);
        buttonSignOut = findViewById(R.id.btn_signout);
        buttonUpload = findViewById(R.id.btn_upload);
        buttonPayment = findViewById(R.id.btn_payment);
        textViewGreeting = findViewById(R.id.textViewGreeting);
        campusListView = findViewById(R.id.campusListView);
        editTextSearch = findViewById(R.id.editTextSearch);
        imageViewProfile = findViewById(R.id.imageView);
        cardPremiumStatus = findViewById(R.id.cardPremiumStatus); // Add this line
    }


    private void setupListView() {
        campusList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, campusList);
        campusListView.setAdapter(adapter);

        campusListView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedCampus = campusList.get(position);
            Intent intent = new Intent(Dashboard.this, CourseActivity.class);
            intent.putExtra("campus", selectedCampus);
            startActivity(intent);
        });
    }

    private void setupButtons() {
        buttonSignOut.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(Dashboard.this, Login.class));
            finish();
        });

        buttonUpload.setOnClickListener(v -> {
            startActivity(new Intent(Dashboard.this, FileUpload.class));
        });

        buttonPayment.setOnClickListener(v -> {
            Intent intent = new Intent(Dashboard.this, PaymentActivity.class);
            startActivity(intent);
        });


        buttonUpgradeToPremium.setOnClickListener(v -> {
            startActivity(new Intent(Dashboard.this, PaymentActivity.class));
        });
    }

    private void setupSearch() {
        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadUserData() {
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String name = dataSnapshot.child("name").getValue(String.class);
                    Log.d("Dashboard", "Retrieved name: " + name);
                    if (name != null) {
                        textViewGreeting.setText("Hi, " + name + "!");
                    } else {
                        textViewGreeting.setText("Hi, User!");
                        Log.e("Dashboard", "Name is null");
                    }
                } else {
                    Log.e("Dashboard", "DataSnapshot does not exist");
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("Dashboard", "Error loading user data: " + error.getMessage());
            }
        });
    }




    private void loadCampuses() {
        DatabaseReference campusRef = FirebaseDatabase.getInstance().getReference("campuses");

        campusRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    campusList.clear(); // Clear the list before adding new data
                    for (DataSnapshot campusSnapshot : snapshot.getChildren()) {
                        String campusName = campusSnapshot.getKey(); // Get the key (campus name)
                        campusList.add(campusName.replace('_', ' ')); // Replace underscores with spaces for better display
                    }
                    adapter.notifyDataSetChanged(); // Notify the adapter to update the ListView
                } else {
                    Toast.makeText(Dashboard.this, "No campuses available.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("Dashboard", "Error loading campuses: " + error.getMessage());
                Toast.makeText(Dashboard.this, "Failed to load campuses.", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void syncNameFromFirestoreToRealtimeDatabase() {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        FirebaseDatabase realtimeDatabase = FirebaseDatabase.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            Log.e("Sync", "User not logged in");
            return;
        }

        String userId = currentUser.getUid();

        // Firestore reference
        DocumentReference firestoreUserRef = firestore.collection("users").document(userId);

        // Realtime Database reference
        DatabaseReference realtimeUserRef = realtimeDatabase.getReference("users").child(userId);

        // Fetch name from Firestore
        firestoreUserRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                DocumentSnapshot document = task.getResult();

                if (document.exists()) {
                    String name = document.getString("name"); // Ensure Firestore has a `name` field

                    if (name != null) {
                        // Update Realtime Database
                        realtimeUserRef.child("name").setValue(name).addOnCompleteListener(updateTask -> {
                            if (updateTask.isSuccessful()) {
                                Log.d("Sync", "Name successfully synced to Realtime Database");
                            } else {
                                Log.e("Sync", "Failed to update Realtime Database", updateTask.getException());
                            }
                        });
                    } else {
                        Log.e("Sync", "Name field is missing in Firestore document");
                    }
                } else {
                    Log.e("Sync", "Firestore document does not exist");
                }
            } else {
                Log.e("Sync", "Failed to fetch Firestore document", task.getException());
            }
        });
    }


    private void checkPremiumStatus() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Boolean hasPremium = dataSnapshot.child("hasPremium").getValue(Boolean.class);
                    Long expirationDate = dataSnapshot.child("expirationDate").getValue(Long.class);

                    cardPremiumStatus.setVisibility(View.VISIBLE); // Make the card visible

                    if (hasPremium != null && hasPremium) {
                        if (expirationDate != null) {
                            long currentTime = System.currentTimeMillis();
                            if (expirationDate > currentTime) {
                                long daysLeft = (expirationDate - currentTime) / (1000 * 60 * 60 * 24);
                                textPremiumStatus.setText("Premium expires in " + daysLeft + " days");
                                textPremiumStatus.setVisibility(View.VISIBLE);
                                buttonUpgradeToPremium.setVisibility(View.GONE);

                                // Disable the payment button
                                buttonPayment.setEnabled(false);
                                buttonPayment.setBackgroundTintList(getResources().getColorStateList(R.color.gray)); // Set disabled color
                            } else {
                                textPremiumStatus.setText("Your premium has expired.");
                                textPremiumStatus.setVisibility(View.VISIBLE);
                                buttonUpgradeToPremium.setVisibility(View.VISIBLE);

                                // Enable the payment button
                                buttonPayment.setEnabled(true);
                                buttonPayment.setBackgroundTintList(getResources().getColorStateList(R.color.black)); // Set enabled color
                            }
                        } else {
                            textPremiumStatus.setText("Your premium status could not be verified.");
                            textPremiumStatus.setVisibility(View.VISIBLE);
                            buttonUpgradeToPremium.setVisibility(View.VISIBLE);

                            // Enable the payment button
                            buttonPayment.setEnabled(true);
                            buttonPayment.setBackgroundTintList(getResources().getColorStateList(R.color.black)); // Set enabled color
                        }
                    } else {
                        textPremiumStatus.setVisibility(View.GONE);
                        buttonUpgradeToPremium.setVisibility(View.VISIBLE);

                        // Enable the payment button
                        buttonPayment.setEnabled(true);
                        buttonPayment.setBackgroundTintList(getResources().getColorStateList(R.color.black)); // Set enabled color
                    }
                } else {
                    cardPremiumStatus.setVisibility(View.GONE); // Hide the card if user data doesn't exist

                    // Enable the payment button by default if user data doesn't exist
                    buttonPayment.setEnabled(true);
                    buttonPayment.setBackgroundTintList(getResources().getColorStateList(R.color.black)); // Set enabled color
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("Dashboard", "Error checking premium status: " + error.getMessage());
                cardPremiumStatus.setVisibility(View.GONE); // Hide the card in case of error

                // Enable the payment button by default in case of error
                buttonPayment.setEnabled(true);
                buttonPayment.setBackgroundTintList(getResources().getColorStateList(R.color.black)); // Set enabled color
            }
        });
    }

    private void loadProfilePicture() {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        String userId = FirebaseAuth.getInstance().getUid();

        if (userId != null) {
            DocumentReference userRef = firestore.collection("users").document(userId);

            userRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    DocumentSnapshot document = task.getResult();

                    if (document.exists() && document.contains("profilePicture")) {
                        String base64Image = document.getString("profilePicture");

                        if (base64Image != null && !base64Image.isEmpty()) {
                            // Decode Base64 and set to ImageView
                            byte[] decodedBytes = Base64.decode(base64Image, Base64.DEFAULT);
                            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                            imageViewProfile.setImageBitmap(bitmap);
                        } else {
                            // Set default profile picture
                            imageViewProfile.setImageResource(R.drawable.default_profile);
                        }
                    } else {
                        imageViewProfile.setImageResource(R.drawable.default_profile);
                    }
                } else {
                    Log.e("Dashboard", "Error loading profile picture: " + task.getException());
                    imageViewProfile.setImageResource(R.drawable.default_profile);
                }
            });
        }
    }

    public void onProfilePictureClick(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();

            try {
                // Convert image to Base64
                InputStream inputStream = getContentResolver().openInputStream(imageUri);
                byte[] bytes = IOUtils.toByteArray(inputStream); // Apache Commons IO library
                String base64Image = Base64.encodeToString(bytes, Base64.DEFAULT);

                // Save to Firestore
                uploadProfilePicture(base64Image);

            } catch (IOException e) {
                Log.e("Dashboard", "Error converting image to Base64: " + e.getMessage());
                Toast.makeText(this, "Failed to process image.", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void uploadProfilePicture(String base64Image) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        String userId = FirebaseAuth.getInstance().getUid();

        if (userId != null) {
            DocumentReference userRef = firestore.collection("users").document(userId);

            userRef.update("profilePicture", base64Image).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    loadProfilePicture();
                    Toast.makeText(this, "Profile picture updated successfully.", Toast.LENGTH_SHORT).show();
                } else {
                    Log.e("Dashboard", "Error updating profile picture: " + task.getException());
                    Toast.makeText(this, "Failed to update profile picture.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }



}