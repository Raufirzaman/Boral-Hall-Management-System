package com.raufir.bauethms;

import android.os.Bundle;
import android.transition.Fade;
import android.view.View;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class search extends AppCompatActivity {

    private androidx.appcompat.widget.SearchView searchStudent;
    private LinearLayout searchResultsSection;
    private TextView noResultsText;
    private LinearLayout studentInfoList;

    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search); // your layout XML name

        // Setting up transition effects
        getWindow().setEnterTransition(new Fade());
        getWindow().setExitTransition(new Fade());

        // Initialize Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("users"); // Change to your database path

        // Initialize views
        searchStudent = findViewById(R.id.search_student);
        searchResultsSection = findViewById(R.id.search_results_section);
        noResultsText = findViewById(R.id.no_results_text);
        studentInfoList = findViewById(R.id.student_info_list_container);

        // Set up the search listener
        searchStudent.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchStudentInfo(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // You can implement real-time searching here if needed
                return false;
            }
        });
    }
    private void searchStudentInfo(String query) {
        // Clear previous results
        studentInfoList.removeAllViews(); // This ensures the list is cleared before adding new results

        noResultsText.setVisibility(View.GONE);
        searchResultsSection.setVisibility(View.GONE);

        // Fetch student information from Firebase
        databaseReference.orderByKey().equalTo(query).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()) {
                    for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                        String studentId = childSnapshot.getKey();
                        String name = childSnapshot.child("name").getValue(String.class);
                        String address = childSnapshot.child("address").getValue(String.class);
                        String fatherName = childSnapshot.child("father_name").getValue(String.class);
                        String motherName = childSnapshot.child("mother_name").getValue(String.class);
                        String email = childSnapshot.child("email").getValue(String.class);
                        String dob = childSnapshot.child("dob").getValue(String.class);
                        String phoneNumber = childSnapshot.child("phone_number").getValue(String.class);
                        String batch = childSnapshot.child("batch").getValue(String.class);
                        String room = childSnapshot.child("room").getValue(String.class);
                        String deptName = childSnapshot.child("dept_name").getValue(String.class);
                        String imageUrl = childSnapshot.child("profile_image_url").getValue(String.class); // Get the image URL

                        // Create and display the student information
                        displayStudentInfo(studentId, name, address, fatherName, motherName, email, dob, phoneNumber, batch, room, deptName, imageUrl);
                        searchResultsSection.setVisibility(View.VISIBLE);
                    }
                } else {
                    // Show "No results found" text when no matching student ID is found
                    noResultsText.setVisibility(View.VISIBLE);
                    searchResultsSection.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(search.this, "Error fetching data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayStudentInfo(String studentId, String name, String address, String fatherName,
                                    String motherName, String email, String dob,
                                    String phoneNumber, String batch, String room, String deptName, String imageUrl) {
        // Create a LinearLayout to hold the student info
        LinearLayout studentInfoLayout = new LinearLayout(this);
        studentInfoLayout.setOrientation(LinearLayout.VERTICAL); // Change to vertical orientation
        studentInfoLayout.setPadding(10, 10, 10, 10);
        studentInfoLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        // Create a TextView to show student details
        TextView studentInfoText = new TextView(this);
        String studentDetails = "ID: " + studentId + "\n" +
                "Name: " + name + "\n" +
                "Address: " + address + "\n" +
                "Father's Name: " + fatherName + "\n" +
                "Mother's Name: " + motherName + "\n" +
                "Email: " + email + "\n" +
                "Date of Birth: " + dob + "\n" +
                "Phone Number: " + phoneNumber + "\n" +
                "Batch: " + batch + "\n" +
                "Room: " + room + "\n" +
                "Department: " + deptName;

        studentInfoText.setText(studentDetails);
        studentInfoText.setTextSize(18);
        studentInfoText.setPadding(10, 10, 10, 10);
        studentInfoText.setTextColor(0xFF000000); // Set text color to black

        // Set layout parameters for the TextView
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        studentInfoText.setLayoutParams(textParams);

        // Add the TextView to the student info layout
        studentInfoLayout.addView(studentInfoText); // Add the text first

        // Create a LinearLayout for the image
        LinearLayout imageLayout = new LinearLayout(this);
        imageLayout.setOrientation(LinearLayout.VERTICAL);
        imageLayout.setGravity(Gravity.CENTER); // Center the image horizontally

        // Create an ImageView for the student image
        ImageView studentImage = new ImageView(this);
        LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(500, 500); // Set size for image (500x500)
        studentImage.setLayoutParams(imageParams);
        studentImage.setScaleType(ImageView.ScaleType.CENTER_CROP);

        // Load the image using Glide
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this)
                    .load(imageUrl) // Use the image URL from Firebase
                    .placeholder(R.drawable.nullimage) // Placeholder image
                    .into(studentImage);
        } else {
            studentImage.setImageResource(R.drawable.nullimage); // Set a default image if URL is null or empty
        }

        // Add the ImageView to the image Layout
        imageLayout.addView(studentImage);

        // Add the image layout to the student info layout
        studentInfoLayout.addView(imageLayout); // Add the image layout after the text

        // Add the student info layout to the student info list
        studentInfoList.addView(studentInfoLayout);
    }
}