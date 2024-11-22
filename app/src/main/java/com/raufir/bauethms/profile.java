package com.raufir.bauethms;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
public class profile extends AppCompatActivity {

    private TextView tvId, tvName, tvAddress, tvFatherName, tvMotherName, tvEmail, tvDob, tvPhoneNumber, tvBatch, tvRoom, tvDeptName, urtv;
    private ImageView profileImageView;
    private DatabaseReference databaseReference;
    private FirebaseAuth auth;
    private ProgressBar progressBar; // Declare ProgressBar

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_profile);

        // Initialize TextViews and ImageView
        tvId = findViewById(R.id.tv_id);
        tvName = findViewById(R.id.tv_name);
        tvAddress = findViewById(R.id.tv_address);
        tvFatherName = findViewById(R.id.tv_father_name);
        tvMotherName = findViewById(R.id.tv_mother_name);
        tvEmail = findViewById(R.id.tv_email);
        tvDob = findViewById(R.id.tv_dob);
        tvPhoneNumber = findViewById(R.id.tv_phone_number);
        tvBatch = findViewById(R.id.tv_batch);
        tvRoom = findViewById(R.id.tv_room);
        tvDeptName = findViewById(R.id.tv_dept_name);
        profileImageView = findViewById(R.id.profile_image);
        urtv = findViewById(R.id.textView4);
        progressBar = findViewById(R.id.progressBar); // Initialize ProgressBar

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance();
        urtv.setOnClickListener(view -> {
            Intent intent = new Intent(profile.this, EditProfile.class);
            startActivity(intent);
        });

        FirebaseUser  currentUser  = auth.getCurrentUser ();

        if (currentUser  != null) {
            // Show the ProgressBar while loading data
            progressBar.setVisibility(View.VISIBLE);
            String email = currentUser .getEmail();
            if (email != null) {
                databaseReference = FirebaseDatabase.getInstance().getReference("users");

                databaseReference.orderByChild("email").equalTo(email)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                // Hide the ProgressBar once data is loaded
                                progressBar.setVisibility(View.GONE);
                                if (dataSnapshot.exists()) {
                                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                        String id = snapshot.child("id").getValue(String.class);
                                        String name = snapshot.child("name").getValue(String.class);
                                        String address = snapshot.child("address").getValue(String.class);
                                        String fatherName = snapshot.child("father_name").getValue(String.class);
                                        String motherName = snapshot.child("mother_name").getValue(String.class);
                                        String dob = snapshot.child("dob").getValue(String.class);
                                        String phoneNumber = snapshot.child("phone_number").getValue(String.class);
                                        String batch = snapshot.child("batch").getValue(String.class);
                                        String room = snapshot.child("room").getValue(String.class);
                                        String deptName = snapshot.child("dept_name").getValue(String.class);
                                        String profileImageUrl = snapshot.child("profile_image_url").getValue(String.class);

                                        tvId.setText("ID: " + id);
                                        tvName.setText("Name: " + name);
                                        tvAddress.setText("Address: " + address);
                                        tvFatherName.setText("Father's Name: " + fatherName);
                                        tvMotherName.setText("Mother's Name: " + motherName);
                                        tvEmail.setText("Email: " + email);
                                        tvDob.setText("Date of Birth: " + dob);
                                        tvPhoneNumber.setText("Phone Number: " + phoneNumber);
                                        tvBatch.setText("Batch: " + batch);
                                        tvRoom.setText("Room: " + room);
                                        tvDeptName.setText("Department Name: " + deptName);

                                        if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                                            Glide.with(profile.this)
                                                    .load(profileImageUrl)
                                                    .placeholder(R.drawable.nullimage)
                                                    .into(profileImageView);
                                        }
                                    }
                                } else {
                                    Toast.makeText(profile.this, "No user data found", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                progressBar.setVisibility(View.GONE); // Hide ProgressBar on error
                                Toast.makeText(profile.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                Toast.makeText(profile.this, "User  email not available", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "User  not logged in", Toast.LENGTH_SHORT).show();
        }
    }
}