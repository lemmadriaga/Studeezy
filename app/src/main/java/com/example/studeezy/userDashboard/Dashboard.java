package com.example.studeezy.userDashboard;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.net.Uri;

import com.example.studeezy.calculator.Calculator;
import com.example.studeezy.library.Library;
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
    private MaterialButton buttonSignOut, buttonUpload, buttonPayment, buttonUpgradeToPremium, buttonCalculator, buttonLibrary;
    private FirebaseAuth mAuth;
    private DatabaseReference userRef;
    private ListView campusListView;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> campusList;
    private TextInputEditText editTextSearch;
    private CircleImageView imageViewProfile;
    private androidx.cardview.widget.CardView cardPremiumStatus;
    private static final int PICK_IMAGE_REQUEST = 1;
    private TextView textPremiumBenefits;
    private TextView textViewPoints;
    private Button buttonRedeem;

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

        syncNameFromFirestoreToRealtimeDatabase();

        userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid());

        initializeViews();
        setupListView();
        setupButtons();
        setupSearch();
        loadUserData();
        loadCampuses();
        checkPremiumStatus();
        loadProfilePicture();
    }


    private void initializeViews() {

        textPremiumStatus = findViewById(R.id.textPremiumStatus);
        textViewPoints = findViewById(R.id.textViewPoints);
        textPremiumBenefits = findViewById(R.id.textPremiumBenefits);
        buttonUpgradeToPremium = findViewById(R.id.buttonUpgradeToPremium);
        buttonCalculator = findViewById(R.id.btn_calculator);
        buttonLibrary = findViewById(R.id.btn_free_books);
        buttonSignOut = findViewById(R.id.btn_signout);
        buttonUpload = findViewById(R.id.btn_upload);
        buttonRedeem = findViewById(R.id.btn_redeem);
        buttonPayment = findViewById(R.id.btn_payment);
        textViewGreeting = findViewById(R.id.textViewGreeting);
        campusListView = findViewById(R.id.campusListView);
        editTextSearch = findViewById(R.id.editTextSearch);
        imageViewProfile = findViewById(R.id.imageView);
        cardPremiumStatus = findViewById(R.id.cardPremiumStatus);
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

        buttonCalculator.setOnClickListener(v -> {
            Intent intent = new Intent(Dashboard.this, Calculator.class);
            startActivity(intent);
        });

        buttonLibrary.setOnClickListener(v -> {
            Intent intent = new Intent(Dashboard.this, Library.class);
            startActivity(intent);
        });

        buttonUpgradeToPremium.setOnClickListener(v -> {
            startActivity(new Intent(Dashboard.this, PaymentActivity.class));
        });

        buttonRedeem.setOnClickListener(v -> {
            redeemPremium();
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
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        String userId = FirebaseAuth.getInstance().getUid();

        if (userId != null) {
            DocumentReference userRef = firestore.collection("users").document(userId);

            userRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    DocumentSnapshot document = task.getResult();

                    if (document.exists()) {
                        // Fetch and display name
                        String name = document.getString("name");
                        textViewGreeting.setText(name != null ? "Hi, " + name + "!" : "Hi, User!");

                        // Fetch and display points
                        Long points = document.getLong("points");
                        if (points != null) {
                            textViewPoints.setText("Points: " + points);
                        } else {
                            textViewPoints.setText("Points: 0");
                        }
                    } else {
                        Log.e("Dashboard", "User document does not exist");
                        textViewGreeting.setText("Hi, User!");
                        textViewPoints.setText("Points: 0");
                    }
                } else {
                    Log.e("Dashboard", "Error fetching user data: " + task.getException());
                }
            });
        }
    }

    private void loadCampuses() {
        DatabaseReference campusRef = FirebaseDatabase.getInstance().getReference("campuses");

        campusRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    campusList.clear();
                    for (DataSnapshot campusSnapshot : snapshot.getChildren()) {
                        String campusName = campusSnapshot.getKey();
                        campusList.add(campusName.replace('_', ' '));
                    }
                    adapter.notifyDataSetChanged();
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


        DocumentReference firestoreUserRef = firestore.collection("users").document(userId);


        DatabaseReference realtimeUserRef = realtimeDatabase.getReference("users").child(userId);


        firestoreUserRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                DocumentSnapshot document = task.getResult();

                if (document.exists()) {
                    String name = document.getString("name");

                    if (name != null) {

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

                    cardPremiumStatus.setVisibility(View.VISIBLE);

                    if (hasPremium != null && hasPremium) {
                        if (expirationDate != null) {
                            long currentTime = System.currentTimeMillis();
                            if (expirationDate > currentTime) {
                                long daysLeft = (expirationDate - currentTime) / (1000 * 60 * 60 * 24);
                                textPremiumStatus.setText("Premium expires in " + daysLeft + " days");
                                textPremiumStatus.setVisibility(View.VISIBLE);
                                buttonUpgradeToPremium.setVisibility(View.GONE);
                                textPremiumBenefits.setVisibility(View.GONE);
                            } else {
                                textPremiumStatus.setText("Your premium has expired.");
                                textPremiumStatus.setVisibility(View.VISIBLE);
                                buttonUpgradeToPremium.setVisibility(View.VISIBLE);
                                textPremiumBenefits.setVisibility(View.VISIBLE);
                            }
                        } else {
                            textPremiumStatus.setText("Your premium status could not be verified.");
                            textPremiumStatus.setVisibility(View.VISIBLE);
                            buttonUpgradeToPremium.setVisibility(View.VISIBLE);
                            textPremiumBenefits.setVisibility(View.VISIBLE);
                        }
                    } else {
                        textPremiumStatus.setVisibility(View.GONE);
                        buttonUpgradeToPremium.setVisibility(View.VISIBLE);
                        textPremiumBenefits.setVisibility(View.VISIBLE);
                    }
                } else {
                    cardPremiumStatus.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("Dashboard", "Error checking premium status: " + error.getMessage());
                cardPremiumStatus.setVisibility(View.GONE);
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

                            byte[] decodedBytes = Base64.decode(base64Image, Base64.DEFAULT);
                            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                            imageViewProfile.setImageBitmap(bitmap);
                        } else {

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

                InputStream inputStream = getContentResolver().openInputStream(imageUri);
                byte[] bytes = IOUtils.toByteArray(inputStream);
                String base64Image = Base64.encodeToString(bytes, Base64.DEFAULT);


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

    private void redeemPremium() {
        // Step 1: Check if user has premium status in Realtime Database
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Boolean hasPremium = dataSnapshot.child("hasPremium").getValue(Boolean.class);

                    if (hasPremium != null && hasPremium) {
                        // Step 2: If already a premium user, show a toast
                        Toast.makeText(Dashboard.this, "There is an existing subscription", Toast.LENGTH_SHORT).show();
                    } else {
                        // Step 3: If not premium, check points in Firestore
                        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
                        String userId = FirebaseAuth.getInstance().getUid();

                        if (userId != null) {
                            DocumentReference userDocRef = firestore.collection("users").document(userId);

                            userDocRef.get().addOnCompleteListener(task -> {
                                if (task.isSuccessful() && task.getResult() != null) {
                                    DocumentSnapshot document = task.getResult();

                                    if (document.exists()) {
                                        Long points = document.getLong("points");

                                        if (points != null && points >= 15) {
                                            // Step 4: Update Realtime Database with premium status and expiration date
                                            long expirationDate = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000); // 30 days from now
                                            userRef.child("hasPremium").setValue(true);
                                            userRef.child("expirationDate").setValue(expirationDate)
                                                    .addOnCompleteListener(updateTask -> {
                                                        if (updateTask.isSuccessful()) {
                                                            Toast.makeText(Dashboard.this, "Premium activated!", Toast.LENGTH_SHORT).show();
                                                            // Refresh the activity
                                                            userDocRef.update("points", 0);
                                                            restartActivity();
                                                        } else {
                                                            Toast.makeText(Dashboard.this, "Failed to update premium status.", Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                        } else {
                                            // Step 5: Set points to 0 and refresh the activity
                                            Toast.makeText(Dashboard.this, "Not enough points.", Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        Toast.makeText(Dashboard.this, "User data not found in Firestore.", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Toast.makeText(Dashboard.this, "Failed to fetch user data from Firestore.", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                } else {
                    Toast.makeText(Dashboard.this, "User data not found in Realtime Database.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("Dashboard", "Error checking premium status: " + error.getMessage());
            }
        });
    }

    private void restartActivity() {
        new Handler().postDelayed(() -> {
            Intent intent = getIntent();
            finish();  // Close the current activity
            startActivity(intent);  // Restart the current activity
        }, 2000);  // Delay for 2000 milliseconds (2 seconds)
    }
}