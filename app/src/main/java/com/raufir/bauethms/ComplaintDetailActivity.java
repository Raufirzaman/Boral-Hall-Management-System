package com.raufir.bauethms;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ComplaintDetailActivity extends AppCompatActivity {

    private TextView titleTextView;
    private TextView descriptionTextView;
    private TextView roomNoTextView;
    private Button deleteButton;
    private FirebaseFirestore db;
    private String complaintId;
    private complaint currentComplaint; // Add this to store the current complaint
    private complaintadapter complaintAdapter;
    private List<complaint> complaintsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complaint_detail);

        titleTextView = findViewById(R.id.titleTextView);
        descriptionTextView = findViewById(R.id.descriptionTextView);
        roomNoTextView = findViewById(R.id.roomNoTextView);
        deleteButton = findViewById(R.id.deleteButton);
        db = FirebaseFirestore.getInstance();

        // Get the complaint data from the intent
        complaintId = getIntent().getStringExtra("COMPLAINT_ID");

        // Fetch the complaint data from Firestore
        db.collection("Complaints").document(complaintId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            String title = document.getString("subject");
                            String description = document.getString("description");
                            String roomNo = document.getString("roomNumber");
                            String uId=document.getString("userId");

                            // Create a complaint object from the retrieved data
                            currentComplaint = new complaint(complaintId, title, description, roomNo,uId); // Create the complaint object

                            // Set the complaint data to the TextViews
                            titleTextView.setText(title);
                            descriptionTextView.setText("Description: "+description);
                            roomNoTextView.setText("Room No: " + roomNo);

                            // Set up the delete button
                            deleteButton.setOnClickListener(v -> deleteComplaint(currentComplaint)); // Pass the currentComplaint object
                        } else {
                            Toast.makeText(ComplaintDetailActivity.this, "Complaint not found", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(ComplaintDetailActivity.this, "Error fetching complaint", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void deleteComplaint(complaint complaint) {
        db.collection("Complaints").document(complaint.getId()).delete() // Use the complaint's ID
                .addOnSuccessListener(aVoid -> {
                    complaintsList.remove(complaint);
                    complaintAdapter.notifyDataSetChanged();
                    Toast.makeText(ComplaintDetailActivity.this, "Complaint deleted", Toast.LENGTH_SHORT).show();
                    finish(); // Close the activity
                })
                .addOnFailureListener(e -> Toast.makeText(ComplaintDetailActivity.this, "Error deleting complaint", Toast.LENGTH_SHORT).show());
    }
}