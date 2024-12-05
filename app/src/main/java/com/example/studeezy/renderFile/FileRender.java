package com.example.studeezy.renderFile;

import android.os.Bundle;
import android.os.Environment;
import android.util.Base64;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.studeezy.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileRender extends AppCompatActivity implements FileAdapter.OnFileClickListener {

    private FirebaseFirestore firestore;
    private String chosenCampus, chosenCourse, chosenYear, chosenSemester, chosenSubject;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private FileAdapter fileAdapter;
    private List<FileModel> fileList;

    private boolean isPremium = false; // Tracks user subscription status
    private int downloadCount = 0; // Tracks downloads for non-premium users
    private final int MAX_DOWNLOADS = 3; // Max downloads for non-premium users
    private String userId = "USER_ID_HERE"; // Replace with logic to get the current user's ID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_render);

        firestore = FirebaseFirestore.getInstance();
        recyclerView = findViewById(R.id.recyclerViewFiles);
        progressBar = findViewById(R.id.progressBar);
        fileList = new ArrayList<>();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        fileAdapter = new FileAdapter(this, fileList, this);
        recyclerView.setAdapter(fileAdapter);

        // Retrieve the user-selected filters
        chosenCampus = getIntent().getStringExtra("campus");
        chosenCourse = getIntent().getStringExtra("course");
        chosenYear = getIntent().getStringExtra("year");
        chosenSemester = getIntent().getStringExtra("semester");
        chosenSubject = getIntent().getStringExtra("subject");

        fetchUserSubscriptionStatus(); // Fetch user's premium status and download count
        fetchDataFromFirestore(); // Load files from Firestore
    }

    private void fetchDataFromFirestore() {
        progressBar.setVisibility(View.VISIBLE);

        firestore.collection("uploads")
                .whereEqualTo("campus", chosenCampus)
                .whereEqualTo("course", chosenCourse)
                .whereEqualTo("year", chosenYear)
                .whereEqualTo("subject", chosenSubject)
                .get()
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful() && task.getResult() != null) {
                        for (DocumentSnapshot document : task.getResult()) {
                            String fileBase64 = document.getString("fileBase64");
                            String fileName = document.getString("fileName");
                            fileList.add(new FileModel(fileName, fileBase64));
                        }
                        fileAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(FileRender.this, "No files found or failed to fetch data.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fetchUserSubscriptionStatus() {
        firestore.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        isPremium = documentSnapshot.getBoolean("hasPremium") != null ? documentSnapshot.getBoolean("hasPremium") : false;
                        downloadCount = documentSnapshot.getLong("downloadCount") != null ? documentSnapshot.getLong("downloadCount").intValue() : 0;

                        long expirationDate = documentSnapshot.getLong("expirationDate") != null ? documentSnapshot.getLong("expirationDate") : 0;
                        long startingDate = documentSnapshot.getLong("startingDate") != null ? documentSnapshot.getLong("startingDate") : 0;
                        long currentTime = System.currentTimeMillis();

                        // Reset premium status if expired
                        if (isPremium && currentTime > expirationDate) {
                            isPremium = false;
                            downloadCount = 0; // Reset count for expired premium users
                            saveDownloadCount();
                            Toast.makeText(this, "Your premium subscription has expired.", Toast.LENGTH_SHORT).show();
                        }

                        // Reset download count if 30 days have passed for non-premium users
                        if (!isPremium && shouldResetDownloadCount(startingDate)) {
                            downloadCount = 0;
                            saveDownloadCount();
                            Toast.makeText(this, "Your download count has been reset.", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to fetch user subscription status.", Toast.LENGTH_SHORT).show();
                });
    }

    private boolean shouldResetDownloadCount(long startingDateMillis) {
        long currentTimeMillis = System.currentTimeMillis();
        long daysElapsed = (currentTimeMillis - startingDateMillis) / (1000 * 60 * 60 * 24);
        return daysElapsed >= 30;
    }

    private void saveDownloadCount() {
        firestore.collection("users").document(userId)
                .update("downloadCount", downloadCount, "hasPremium", isPremium, "startingDate", System.currentTimeMillis())
                .addOnSuccessListener(aVoid -> {
                    // Data updated successfully
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to update user data.", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onFileClick(FileModel fileModel) {
        if (isPremium) {
            downloadFile(fileModel.getFileBase64(), fileModel.getFileName());
        } else {
            if (downloadCount < MAX_DOWNLOADS) {
                downloadFile(fileModel.getFileBase64(), fileModel.getFileName());
                downloadCount++;
                saveDownloadCount(); // Save updated count in Firestore
            } else {
                Toast.makeText(this, "You have reached the maximum downloads. Upgrade to premium for unlimited downloads.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void downloadFile(String fileBase64, String fileName) {
        if (fileBase64 != null && !fileBase64.isEmpty()) {
            try {
                byte[] decodedBytes = Base64.decode(fileBase64, Base64.DEFAULT);

                File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

                if (!fileName.toLowerCase().endsWith(".pdf")) {
                    fileName = fileName + ".pdf";
                }

                // Avoid overwriting files
                String uniqueFileName = fileName.replace(".pdf", "") + "_" + System.currentTimeMillis() + ".pdf";
                File file = new File(downloadsDir, uniqueFileName);

                if (!downloadsDir.exists()) {
                    downloadsDir.mkdirs();
                }

                FileOutputStream fos = new FileOutputStream(file);
                fos.write(decodedBytes);
                fos.close();

                Toast.makeText(this, "File saved to: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();

            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error saving file.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "No file content found.", Toast.LENGTH_SHORT).show();
        }
    }
}
