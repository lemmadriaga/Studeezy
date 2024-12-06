package com.example.studeezy.fileUpload;

import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.studeezy.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FileUpload extends AppCompatActivity {

    private DatabaseReference dbRef;
    private FirebaseFirestore firestoreDb;
    private FirebaseAuth auth;
    private Spinner campusSpinner, courseSpinner, yearSpinner, semesterSpinner, subjectSpinner;
    private Button uploadButton, submitButton;
    private EditText fileNameEditText;
    private String selectedCampus, selectedCourse, selectedYear, selectedSemester, selectedSubject;
    private String fileBase64;
    private String fileName;

    private ActivityResultLauncher<String> filePicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_upload);

        dbRef = FirebaseDatabase.getInstance().getReference();
        firestoreDb = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        campusSpinner = findViewById(R.id.spinner_campus);
        courseSpinner = findViewById(R.id.spinner_course);
        yearSpinner = findViewById(R.id.spinner_year);
        semesterSpinner = findViewById(R.id.spinner_semester);
        subjectSpinner = findViewById(R.id.spinner_subject);

        uploadButton = findViewById(R.id.upload_button);
        submitButton = findViewById(R.id.submit_button);
        fileNameEditText = findViewById(R.id.input_filename);

        initSpinners();
        fetchCampuses();

        filePicker = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                try {
                    fileBase64 = encodeFileToBase64(uri);
                    String fileName = getFileName(uri);

                    TextView displayFileName = findViewById(R.id.display_filename);
                    displayFileName.setText("Selected File: " + fileName);

                    Log.d("FileUpload", "File Base64: " + fileBase64);
                    Toast.makeText(this, "File selected: " + fileName, Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    Log.e("FileUpload", "Error encoding file", e);
                }
            } else {
                Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show();
            }
        });

        uploadButton.setOnClickListener(v -> filePicker.launch("application/pdf"));

        submitButton.setOnClickListener(v -> {
            Log.d("FileUpload", "Campus: " + selectedCampus);
            Log.d("FileUpload", "Course: " + selectedCourse);
            Log.d("FileUpload", "Year: " + selectedYear);
            Log.d("FileUpload", "Semester: " + selectedSemester);
            Log.d("FileUpload", "Subject: " + selectedSubject);
            Log.d("FileUpload", "File Base64: " + fileBase64);

            if (selectedCampus != null && selectedCourse != null && selectedYear != null && selectedSemester != null && selectedSubject != null && fileBase64 != null && !fileNameEditText.getText().toString().isEmpty()) {
                fileName = fileNameEditText.getText().toString(); // Get file name from input box
                uploadFile(fileBase64, fileName);
            } else {
                Toast.makeText(FileUpload.this, "Please select all fields and file before submitting.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initSpinners() {
        ArrayAdapter<String> defaultAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{"Select"});
        defaultAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        campusSpinner.setAdapter(defaultAdapter);
        courseSpinner.setAdapter(defaultAdapter);
        yearSpinner.setAdapter(defaultAdapter);
        semesterSpinner.setAdapter(defaultAdapter);
        subjectSpinner.setAdapter(defaultAdapter);

        campusSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) return;
                selectedCampus = parent.getItemAtPosition(position).toString();
                fetchCourses(selectedCampus);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Optional: Handle if no selection is made
            }
        });

        courseSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) return;
                selectedCourse = parent.getItemAtPosition(position).toString();
                fetchYears(selectedCampus, selectedCourse);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Optional: Handle if no selection is made
            }
        });

        yearSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) return;
                selectedYear = parent.getItemAtPosition(position).toString();
                fetchSemesters(selectedCampus, selectedCourse, selectedYear);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Optional: Handle if no selection is made
            }
        });

        semesterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) return;
                selectedSemester = parent.getItemAtPosition(position).toString();
                fetchSubjects(selectedCampus, selectedCourse, selectedYear, selectedSemester);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Optional: Handle if no selection is made
            }
        });

        subjectSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) return;
                selectedSubject = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Optional: Handle if no selection is made
            }
        });
    }

    private void fetchCampuses() {
        dbRef.child("campuses").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<String> campuses = new ArrayList<>();
                campuses.add("Select");
                for (DataSnapshot campus : snapshot.getChildren()) {
                    campuses.add(campus.getKey());
                }
                updateSpinner(campusSpinner, campuses);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("FileUpload", "Error fetching campuses", error.toException());
            }
        });
    }

    private void fetchCourses(String campus) {
        dbRef.child("campuses").child(campus).child("courses").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<String> courses = new ArrayList<>();
                courses.add("Select");
                for (DataSnapshot course : snapshot.getChildren()) {
                    courses.add(course.getKey());
                }
                updateSpinner(courseSpinner, courses);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("FileUpload", "Error fetching courses", error.toException());
            }
        });
    }

    private void fetchYears(String campus, String course) {
        dbRef.child("campuses").child(campus).child("courses").child(course).child("years").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<String> years = new ArrayList<>();
                years.add("Select");
                for (DataSnapshot year : snapshot.getChildren()) {
                    years.add(year.getKey());
                }
                updateSpinner(yearSpinner, years);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("FileUpload", "Error fetching years", error.toException());
            }
        });
    }

    private void fetchSemesters(String campus, String course, String year) {
        dbRef.child("campuses").child(campus).child("courses").child(course).child("years").child(year).child("semesters").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<String> semesters = new ArrayList<>();
                semesters.add("Select");
                for (DataSnapshot semester : snapshot.getChildren()) {
                    semesters.add(semester.getKey());
                }
                updateSpinner(semesterSpinner, semesters);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("FileUpload", "Error fetching semesters", error.toException());
            }
        });
    }

    private void fetchSubjects(String campus, String course, String year, String semester) {
        dbRef.child("campuses").child(campus).child("courses").child(course).child("years").child(year).child("semesters").child(semester).child("subjects").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<String> subjects = new ArrayList<>();
                subjects.add("Select");
                for (DataSnapshot subject : snapshot.getChildren()) {
                    subjects.add(subject.getKey());
                }
                updateSpinner(subjectSpinner, subjects);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("FileUpload", "Error fetching subjects", error.toException());
            }
        });
    }

    private void updateSpinner(Spinner spinner, List<String> items) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
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
        return Base64.encodeToString(fileBytes, Base64.NO_WRAP);
    }

    private String getFileName(Uri uri) {
        return uri.getLastPathSegment();
    }

    private void uploadFile(String base64, String fileName) {
        String uniqueFileId = UUID.randomUUID().toString();
        String userId = auth.getCurrentUser().getUid();

        FileUploadData fileUploadData = new FileUploadData(
                userId,
                selectedCampus,
                selectedCourse,
                selectedYear,
                selectedSemester,
                selectedSubject,
                base64,
                fileName
        );

        firestoreDb.collection("uploads").document(uniqueFileId).set(fileUploadData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "File uploaded successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error uploading file", Toast.LENGTH_SHORT).show();
                });
    }
}