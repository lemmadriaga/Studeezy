package com.example.studeezy.library;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studeezy.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Library extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore firestoreDb;
    private RecyclerView recyclerView;
    private LibraryAdapter adapter;
    private List<LibraryFile> fileList;
    private EditText fileNameInput;
    private Button submitButton;
    private ProgressBar progressBar;
    private ActivityResultLauncher<String> filePicker;
    private String fileBase64;
    private String fileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_library);

        // Initialize Firebase and Firestore
        auth = FirebaseAuth.getInstance();
        firestoreDb = FirebaseFirestore.getInstance();

        // Initialize UI elements
        Button uploadButton = findViewById(R.id.upload_button);
        fileNameInput = findViewById(R.id.file_name_input);
        submitButton = findViewById(R.id.submit_button);
        progressBar = findViewById(R.id.progressBar);
        recyclerView = findViewById(R.id.filesRecyclerView);

        // Set RecyclerView properties
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        fileList = new ArrayList<>();

        // Initialize the RecyclerView adapter
        adapter = new LibraryAdapter(this, fileList);  // Pass 'this' context to the adapter
        recyclerView.setAdapter(adapter);

        // Hide the file name input and submit button initially
        fileNameInput.setVisibility(View.GONE);
        submitButton.setVisibility(View.GONE);

        // Initialize the file picker to choose a file
        filePicker = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                try {
                    fileBase64 = encodeFileToBase64(uri);  // Encode file to Base64
                    Toast.makeText(this, "File selected successfully!", Toast.LENGTH_SHORT).show();

                    // Show the file name input and submit button after file selection
                    fileNameInput.setVisibility(View.VISIBLE);
                    submitButton.setVisibility(View.VISIBLE);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show();
            }
        });

        // Upload button click listener
        uploadButton.setOnClickListener(v -> filePicker.launch("application/pdf"));

        // Submit button click listener to upload file
        submitButton.setOnClickListener(v -> {
            fileName = fileNameInput.getText().toString();
            if (fileName != null && !fileName.isEmpty() && fileBase64 != null) {
                uploadToFirestore(fileBase64, fileName);
            } else {
                Toast.makeText(this, "Please select a file and enter a file name.", Toast.LENGTH_SHORT).show();
            }
        });

        // Load files from Firestore when activity starts
        loadFilesFromFirestore();
    }

    private String encodeFileToBase64(Uri fileUri) throws IOException {
        InputStream inputStream = getContentResolver().openInputStream(fileUri);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            byteArrayOutputStream.write(buffer, 0, bytesRead);
        }
        byte[] fileBytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(fileBytes, Base64.NO_WRAP);  // Return Base64-encoded file
    }

    private void uploadToFirestore(String base64, String fileName) {
        progressBar.setVisibility(View.VISIBLE);

        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            // Handle the case where the user is not logged in
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = user.getUid();

        // Query Firestore to get the user's name from the "users" collection
        firestoreDb.collection("users").document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String uploaderName = documentSnapshot.getString("name");
                        if (uploaderName == null || uploaderName.isEmpty()) {
                            uploaderName = user.getEmail(); // Fallback to email if name is not set
                        }

                        // Generate a unique file ID
                        String uniqueFileId = UUID.randomUUID().toString();

                        // Upload the file metadata to the "library" collection
                        firestoreDb.collection("library").document(uniqueFileId)
                                .set(new LibraryFile(base64, uid, uploaderName, fileName))  // Upload file metadata to Firestore
                                .addOnSuccessListener(aVoid -> {
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(this, "File uploaded successfully!", Toast.LENGTH_SHORT).show();

                                    // Increment user points after successful upload
                                    incrementUserPoints(uid);

                                    // Reload the files to reflect the new upload
                                    loadFilesFromFirestore();

                                    // Reset UI for further uploads
                                    fileNameInput.setText("");
                                    fileNameInput.setVisibility(View.GONE);
                                    submitButton.setVisibility(View.GONE);
                                })
                                .addOnFailureListener(e -> {
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(this, "Failed to upload file.", Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Failed to retrieve user data", Toast.LENGTH_SHORT).show();
                });
    }

    private void incrementUserPoints(String userId) {
        firestoreDb.collection("users").document(userId)
                .update("points", FieldValue.increment(1))
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Points updated.", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to update points.", Toast.LENGTH_SHORT).show());
    }

    private void loadFilesFromFirestore() {
        firestoreDb.collection("library")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<LibraryFile> files = new ArrayList<>();
                        for (DocumentSnapshot document : task.getResult()) {
                            LibraryFile file = document.toObject(LibraryFile.class);
                            files.add(file);
                        }
                        fileList.clear();
                        fileList.addAll(files);
                        adapter.notifyDataSetChanged(); // Notify the adapter to refresh the RecyclerView
                    } else {
                        Toast.makeText(this, "Failed to load files.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void showPdfPreviewDialog(byte[] decodedBytes) {
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
            int pageCount = pdfRenderer.getPageCount(); // Get the total number of pages in the PDF

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
}