// ComplaintsActivity.java
package com.raufir.bauethms;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class complaints extends AppCompatActivity {

    private RecyclerView recyclerView;
    private complaintadapter complaintAdapter;
    private List<complaint> complaintsList = new ArrayList<>();
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complaints);

        recyclerView = findViewById(R.id.complaintsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        complaintAdapter = new complaintadapter(complaintsList, this::showComplaintDescription, this::deleteComplaint);
        recyclerView.setAdapter(complaintAdapter);

        db = FirebaseFirestore.getInstance();
        fetchComplaints();
    }

    private void fetchComplaints() {
        CollectionReference complaintsRef = db.collection("Complaints");
        complaintsRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                complaintsList.clear(); // Clear the list before adding new items
                for (QueryDocumentSnapshot document : task.getResult()) {
                    complaint complaint = document.toObject(complaint.class);
                    String roomNo = document.getString("roomNumber");
                  // Ensure this matches your Firestore field name
                    complaintsList.add(new complaint(document.getId(), complaint.getTitle(), complaint.getDescription(), roomNo,complaint.getuId()));
                }
                complaintAdapter.notifyDataSetChanged();
            } else {
                Toast.makeText(complaints.this, "Error getting documents.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showComplaintDescription(complaint complaint) {
        Intent intent = new Intent(this, ComplaintDetailActivity.class);
        intent.putExtra("COMPLAINT_ID", complaint.getId());
        intent.putExtra("COMPLAINT_TITLE", complaint.getTitle());
        intent.putExtra("COMPLAINT_DESCRIPTION", complaint.getDescription());
        intent.putExtra("COMPLAINT_ROOM_NO", complaint.getRoomNo());
        startActivity(intent);
    }

    private void deleteComplaint(complaint complaint) {
        db.collection("Complaints").document(complaint.getId()).delete()
                .addOnSuccessListener(aVoid -> {
                    complaintsList.remove(complaint);
                    complaintAdapter.notifyDataSetChanged();
                    Toast.makeText(complaints.this, "Complaint deleted", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(complaints.this, "Error deleting complaint", Toast.LENGTH_SHORT).show());
    }
}