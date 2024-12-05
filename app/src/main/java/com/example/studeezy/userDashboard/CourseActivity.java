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

public class CourseActivity extends AppCompatActivity {
    private FirebaseDatabase mDatabase;
    private DatabaseReference mCoursesRef;
    private ListView courseListView;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> courseList;
    private String selectedCampus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course);

        selectedCampus = getIntent().getStringExtra("campus");

        mDatabase = FirebaseDatabase.getInstance();
        mCoursesRef = mDatabase.getReference().child("campuses").child(selectedCampus.replace(" ", "_")).child("courses");  // Use campus name with underscores for DB path

        courseListView = findViewById(R.id.courseListView);
        courseList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, courseList);
        courseListView.setAdapter(adapter);

        loadCourses();

        courseListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedCourse = courseList.get(position);

                Intent intent = new Intent(CourseActivity.this, SubjectActivity.class);
                intent.putExtra("campus", selectedCampus);  // Pass campus
                intent.putExtra("course", selectedCourse);  // Pass selected course
                startActivity(intent);  // Start the activity
            }
        });
    }

    private void loadCourses() {
        mCoursesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                courseList.clear();
                for (DataSnapshot courseSnapshot : dataSnapshot.getChildren()) {
                    String courseName = courseSnapshot.getKey();
                    courseList.add(courseName);
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