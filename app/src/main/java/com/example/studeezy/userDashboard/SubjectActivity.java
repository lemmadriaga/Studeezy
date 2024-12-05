package com.example.studeezy.userDashboard;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ExpandableListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.studeezy.R;
import com.example.studeezy.renderFile.FileRender;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SubjectActivity extends AppCompatActivity {

    private FirebaseDatabase mDatabase;
    private DatabaseReference mSubjectsRef;
    private ExpandableListView expandableListView;
    private SubjectExpandableListAdapter adapter;
    private List<String> yearList;
    private HashMap<String, List<String>> semesterMap;
    private String campus, course;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subject);

        expandableListView = findViewById(R.id.expandableListView);
        yearList = new ArrayList<>();
        semesterMap = new HashMap<>();

        campus = getIntent().getStringExtra("campus");
        course = getIntent().getStringExtra("course");

        if (campus == null || course == null) {
            Toast.makeText(this, "Error: Campus or Course data not found", Toast.LENGTH_LONG).show();
            return;
        }

        mDatabase = FirebaseDatabase.getInstance();
        mSubjectsRef = mDatabase.getReference("campuses").child(campus.replace(" ", "_"))
                .child("courses").child(course).child("years");

        loadSubjects();

        expandableListView.setOnChildClickListener((parent, v, groupPosition, childPosition, id) -> {
            String selectedItem = semesterMap.get(yearList.get(groupPosition)).get(childPosition);

            String selectedSemester = null;
            String selectedSubject = null;

            if (selectedItem.contains(" - ")) {
                String[] parts = selectedItem.split(" - ");
                selectedSubject = parts[0];
            }

            Intent intent = new Intent(SubjectActivity.this, FileRender.class);
            intent.putExtra("campus", campus.replace(" ", "_"));
            intent.putExtra("course", course);
            intent.putExtra("semester", selectedSemester);
            intent.putExtra("subject", selectedSubject);
            intent.putExtra("year", yearList.get(groupPosition).replace(" ", "_"));
            startActivity(intent);

            return true;
        });
    }

    private void loadSubjects() {
        mSubjectsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                yearList.clear();
                semesterMap.clear();

                if (dataSnapshot.exists()) {
                    for (DataSnapshot yearSnapshot : dataSnapshot.getChildren()) {
                        String year = yearSnapshot.getKey();
                        year = year.replace("_", " ");
                        yearList.add(year);

                        DataSnapshot semestersSnapshot = yearSnapshot.child("semesters");
                        List<String> semesterSubjects = new ArrayList<>();

                        for (DataSnapshot semesterSnapshot : semestersSnapshot.getChildren()) {
                            String semesterName = semesterSnapshot.getKey(); // e.g. "1st_Semester"
                            semesterName = semesterName.replace("_", " "); // Replace underscores with spaces
                            semesterSubjects.add(semesterName); // Add semester name (non-clickable)

                            // Add subjects for this semester
                            DataSnapshot subjectsSnapshot = semesterSnapshot.child("subjects");
                            for (DataSnapshot subjectSnapshot : subjectsSnapshot.getChildren()) {
                                String subjectCode = subjectSnapshot.getKey();
                                String subjectName = subjectSnapshot.getValue(String.class);
                                String subject = subjectCode + " - " + subjectName;
                                semesterSubjects.add(subject); // Add subject (clickable)
                            }
                        }

                        semesterMap.put(year, semesterSubjects);
                    }

                    adapter = new SubjectExpandableListAdapter(SubjectActivity.this, yearList, semesterMap);
                    expandableListView.setAdapter(adapter);
                } else {
                    Toast.makeText(SubjectActivity.this, "No subjects found for this course.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                String errorMessage = "Error loading subjects. Please try again later.";
                Toast.makeText(SubjectActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                Log.e("SubjectActivity", "Database Error: " + databaseError.getMessage());
            }
        });
    }
}