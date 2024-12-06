package com.example.studeezy.renderFile;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.util.Base64;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.studeezy.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileRender extends AppCompatActivity implements FileAdapter.OnFileClickListener {

    private FirebaseFirestore firestore;
    private DatabaseReference databaseReference;
    private String chosenCampus, chosenCourse, chosenYear, chosenSemester, chosenSubject;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private FileAdapter fileAdapter;
    private List<FileModel> fileList;

    private boolean isPremium = false; // Tracks user subscription status
    private int downloadCount = 0; // Tracks downloads for non-premium users
    private final int MAX_DOWNLOADS = 3; // Max downloads for non-premium users
    private String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_render);

        firestore = FirebaseFirestore.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("users").child(userId);
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
        databaseReference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                DataSnapshot snapshot = task.getResult();

                isPremium = snapshot.child("hasPremium").getValue(Boolean.class) != null ? snapshot.child("hasPremium").getValue(Boolean.class) : false;
                downloadCount = snapshot.child("downloadCount").getValue(Integer.class) != null ? snapshot.child("downloadCount").getValue(Integer.class) : 0;

                long expirationDate = snapshot.child("expirationDate").getValue(Long.class) != null ? snapshot.child("expirationDate").getValue(Long.class) : 0;
                long startingDate = snapshot.child("startingDate").getValue(Long.class) != null ? snapshot.child("startingDate").getValue(Long.class) : 0;
                long currentTime = System.currentTimeMillis();

                // Reset premium status if expired
                if (isPremium && currentTime >= expirationDate) {
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
            } else {
                Toast.makeText(this, "Failed to fetch user subscription status.", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to fetch user subscription status.", Toast.LENGTH_SHORT).show();
        });
    }

    private boolean shouldResetDownloadCount(long startingDateMillis) {
        long currentTimeMillis = System.currentTimeMillis();
        long daysElapsed = (currentTimeMillis - startingDateMillis) / (1000 * 60 * 60 * 24);
        return daysElapsed >= 30;
    }

    private void saveDownloadCount() {
        databaseReference.child("downloadCount").setValue(downloadCount);
        databaseReference.child("hasPremium").setValue(isPremium);
        databaseReference.child("startingDate").setValue(System.currentTimeMillis())
                .addOnSuccessListener(aVoid -> {
                    // Data updated successfully
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to update user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
                saveDownloadCount(); // Save updated count in Realtime Database
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

    private void showPdfPreviewDialog(byte[] decodedBytes) {
        // Create a dialog
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_pdf_preview);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        LinearLayout pdfPreviewLayout = dialog.findViewById(R.id.pdfPreviewLayout);
        Button closeButton = dialog.findViewById(R.id.buttonClose);

        try {
            // Save decodedBytes to a temporary file
            File tempFile = new File(getCacheDir(), "temp_preview.pdf");
            FileOutputStream fos = new FileOutputStream(tempFile);
            fos.write(decodedBytes);
            fos.close();

            // Render PDF
            PdfRenderer pdfRenderer = new PdfRenderer(ParcelFileDescriptor.open(tempFile, ParcelFileDescriptor.MODE_READ_ONLY));
            int pageCount = Math.min(pdfRenderer.getPageCount(), 2); // Render 1-2 pages only

            for (int i = 0; i < pageCount; i++) {
                PdfRenderer.Page page = pdfRenderer.openPage(i);

                Bitmap bitmap = Bitmap.createBitmap(page.getWidth(), page.getHeight(), Bitmap.Config.ARGB_8888);
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

                ImageView imageView = new ImageView(this);
                imageView.setImageBitmap(bitmap);
                pdfPreviewLayout.addView(imageView);

                page.close();
            }
            pdfRenderer.close();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error rendering PDF", Toast.LENGTH_SHORT).show();
        }

        closeButton.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    @Override
    public void onFileNameClick(FileModel fileModel) {
        String fileBase64 = fileModel.getFileBase64();
        if (fileBase64 != null && !fileBase64.isEmpty()) {
            try {
                byte[] decodedBytes = Base64.decode(fileBase64, Base64.DEFAULT);

                File tempFile = new File(getCacheDir(), "temp_preview.pdf");
                FileOutputStream fos = new FileOutputStream(tempFile);
                fos.write(decodedBytes);
                fos.close();

                showPdfPreviewDialog(decodedBytes);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error preparing file for preview.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "No file content available for preview.", Toast.LENGTH_SHORT).show();
        }
    }
}