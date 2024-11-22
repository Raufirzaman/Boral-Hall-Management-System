package com.raufir.bauethms;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser ;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;

public class complain extends AppCompatActivity {

    private EditText subjectEditText;
    private EditText descriptionEditText;
    private Button submitButton;

    private FirebaseFirestore firestore;
    private DatabaseReference realtimeDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complain); // Ensure your layout name is correct

        subjectEditText = findViewById(R.id.subject);
        descriptionEditText = findViewById(R.id.description);
        submitButton = findViewById(R.id.button);

        firestore = FirebaseFirestore.getInstance();
        realtimeDatabase = FirebaseDatabase.getInstance().getReference("users");

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitComplaint();
                Intent intent = new Intent(complain.this, mainpage.class);
                startActivity(intent);
            }
        });
    }

    private void submitComplaint() {
        String subject = subjectEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();

        if (subject.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get the currently logged-in user's email
        FirebaseUser  user = FirebaseAuth.getInstance().getCurrentUser ();
        if (user == null) {
            Toast.makeText(this, "User  not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userEmail = user.getEmail();
        realtimeDatabase.orderByChild("email").equalTo(userEmail).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        String userId = userSnapshot.child("id").getValue(String.class);
                        String roomNumber = userSnapshot.child("room").getValue(String.class); // Retrieve Room Number

                        // Create a new complaint document
                        Complaint complaint = new Complaint(subject, description, userId, roomNumber);
                        String documentName = userId + "_" + subject;
                        // Save the complaint to Firestore under the "Complaints" collection
                        firestore.collection("Complaints").document(documentName) // Using userId as the document ID
                                .set(complaint)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(complain.this, "Complaint submitted successfully", Toast.LENGTH_SHORT).show();
                                    // Optionally clear the input fields
                                    subjectEditText.setText("");
                                    descriptionEditText.setText("");
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(complain.this, "Error submitting complaint: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                        break; // Exit the loop after the first match
                    }
                } else {
                    Toast.makeText(complain.this, "User  not found in database", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
                Toast.makeText(complain.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Complaint model class
    public static class Complaint {
        private String subject;
        private String description;
        private String userId; // Add userId field
        private String roomNumber; // Add roomNumber field

        // Default constructor required for calls to DataSnapshot.getValue(Complaint.class)
        public Complaint() {
        }

        public Complaint(String subject, String description, String userId, String roomNumber) {
            this.subject = subject;
            this.description = description;
            this.userId = userId;
            this.roomNumber = roomNumber;
        }

        public String getSubject() {
            return subject;
        }

        public String getDescription() {
            return description;
        }

        public String getUserId() {
            return userId;
        }

        public String getRoomNumber() {
            return roomNumber;
        }
    }
}