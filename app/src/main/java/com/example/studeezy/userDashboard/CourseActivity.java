package com.example.studeezy.userDashboard;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.studeezy.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CourseActivity extends AppCompatActivity {

    private FirebaseDatabase mDatabase;
    private DatabaseReference mCoursesRef;
    private ListView courseListView;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> courseList;
    private Map<String, String> courseMap;
    private String selectedCampus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course);

        selectedCampus = getIntent().getStringExtra("campus");

        mDatabase = FirebaseDatabase.getInstance();
        mCoursesRef = mDatabase.getReference().child("campuses").child(selectedCampus.replace(" ", "_")).child("courses");

        courseListView = findViewById(R.id.courseListView);
        courseList = new ArrayList<>();
        courseMap = new HashMap<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, courseList);
        courseListView.setAdapter(adapter);

        loadCourses();

        courseListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedCourseName = courseList.get(position); // Get full course name
                String selectedCourseCode = null;

                for (Map.Entry<String, String> entry : courseMap.entrySet()) {
                    if (entry.getValue().equals(selectedCourseName)) {
                        selectedCourseCode = entry.getKey();
                        break;
                    }
                }

                Intent intent = new Intent(CourseActivity.this, SubjectActivity.class);
                intent.putExtra("campus", selectedCampus);
                intent.putExtra("course", selectedCourseCode);
                intent.putExtra("courseFullName", selectedCourseName);
                startActivity(intent);
            }
        });
    }

    private void loadCourses() {
        mCoursesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                courseList.clear();
                courseMap.clear();
                for (DataSnapshot courseSnapshot : dataSnapshot.getChildren()) {
                    String courseCode = courseSnapshot.getKey(); // e.g., "BSCS"
                    String courseName = courseSnapshot.child("name").getValue(String.class);

                    courseList.add(courseName);

                    courseMap.put(courseCode, courseName);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                String errorMessage = "Error loading courses. Please try again later.";
                Toast.makeText(CourseActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                Log.e("CourseActivity", "Database Error: " + databaseError.getMessage());
            }
        });
    }

}