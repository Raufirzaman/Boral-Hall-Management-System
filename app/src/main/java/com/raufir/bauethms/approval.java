package com.raufir.bauethms;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class approval extends AppCompatActivity {

    private RecyclerView requestsRecyclerView;
    private RequestsAdapter adapter;
    private FirebaseFirestore db;
    private List<Request> requestList;
    private DatabaseReference realTimeDbRef;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_approval);

        requestsRecyclerView = findViewById(R.id.requestsRecyclerView);
        progressBar = findViewById(R.id.progressBar);
        findViewById(R.id.backButton).setOnClickListener(v -> finish());
        realTimeDbRef = FirebaseDatabase.getInstance().getReference("users");
        db = FirebaseFirestore.getInstance();
        requestList = new ArrayList<>();

        adapter = new RequestsAdapter(requestList,
                request -> onApprove(request),
                request -> onReject(request));

        requestsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        requestsRecyclerView.setAdapter(adapter);

        fetchRequests();
    }

    private void fetchRequests() {
        progressBar.setVisibility(View.VISIBLE); // Show the loading spinner
        requestsRecyclerView.setVisibility(View.GONE); // Hide the RecyclerView initially

        db.collection("requests")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        requestList.clear(); // Clear the list before adding new data
                        for (DocumentSnapshot document : task.getResult()) {
                            String docId = document.getId();
                            Map<String, Object> fields = document.getData(); // Get all fields as a Map

                            if (fields != null) {
                                for (Map.Entry<String, Object> entry : fields.entrySet()) {
                                    String fieldName = entry.getKey();
                                    Object fieldValue = entry.getValue();

                                    // Create a new Request object for each field
                                    requestList.add(new Request(docId, fieldName, fieldValue));
                                }
                            }
                        }
                        adapter.notifyDataSetChanged(); // Notify the adapter of data changes

                        // Check if the request list is empty
                        if (requestList.isEmpty()) {
                            requestsRecyclerView.setVisibility(View.GONE); // Hide RecyclerView if no requests
                        } else {
                            requestsRecyclerView.setVisibility(View.VISIBLE); // Show RecyclerView if there are requests
                        }
                    } else {
                        Toast.makeText(this, "Error fetching requests", Toast.LENGTH_SHORT).show();
                    }
                    progressBar.setVisibility(View.GONE); // Hide the loading spinner
                });
    }

    private void onApprove(Request request) {
        request.setApproved(true);
        updateRequestInDatabase(request);
    }

    private void onReject(Request request) {
        request.setApproved(false);
        updateRequestInDatabase(request);
    }

    private void updateRequestInDatabase(Request request) {
        String documentId = request.getDocumentId();
        String fieldName = request.getFieldName();
        Object fieldValue = request.getFieldValue(); // Assuming you have a method to get the field value

        if (request.isApproved()) {
            // Create a map to update in the Realtime Database
            Map<String, Object> updateMap = getFieldUpdateMap(fieldName, fieldValue);
            String userId = documentId.split("_")[0];

            // Update the field in the Realtime Database
            realTimeDbRef.child(userId).updateChildren(updateMap)
                    .addOnSuccessListener(aVoid -> {
                        // Delete the field from Firestore
                        db.collection("requests").document(documentId)
                                .update(fieldName, FieldValue.delete())
                                .addOnSuccessListener(aVoid1 -> {
                                    Toast.makeText(this, "Request approved and updated in Realtime Database", Toast.LENGTH_SHORT).show();
                                    // Check if the document is empty and delete if necessary
                                    checkAndDeleteDocumentIfEmpty(documentId);
                                    // Remove the item from the adapter
                                    adapter.removeItem(request);
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Error deleting field from Firestore", Toast.LENGTH_SHORT).show();
                                });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error updating request in Realtime Database", Toast.LENGTH_SHORT).show();
                    });
        } else {
            // Delete the field from Firestore only
            db.collection("requests").document(documentId)
                    .update(fieldName, FieldValue.delete())
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Request rejected and deleted from Firestore", Toast.LENGTH_SHORT).show();
                        // Check if the document is empty and delete if necessary
                        checkAndDeleteDocumentIfEmpty(documentId);
                        // Remove the item from the adapter
                        adapter.removeItem(request);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error deleting field from Firestore", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private Map<String, Object> getFieldUpdateMap(String fieldName, Object fieldValue) {

        Map<String, Object> updateMap = new HashMap<>();
        switch (fieldName) {
            case "ID":
                updateMap.put("id", fieldValue);
                break;
            case "Address":
                updateMap.put("address", fieldValue);
                break;
            case "Date Of Birth":
                updateMap.put("dob", fieldValue);
                break;
            case "Father's Name":
                updateMap.put("father_name", fieldValue);
                break;
            case "Mother's Name":
                updateMap.put("mother_name", fieldValue);
                break;
            case "Department Name":
                updateMap.put("dept", fieldValue);
                break;
            case "Room":
                updateMap.put("room", fieldValue);
                break;
            case "Batch":
                updateMap.put("batch", fieldValue);
                break;
            case "Phone No":
                updateMap.put("phone_number", fieldValue);
                break;
            case "Email":
                updateMap.put("email", fieldValue);
                break;
            default:
                break;
        }
        return updateMap;
    }

    private void checkAndDeleteDocumentIfEmpty(String documentId) {
        db.collection("requests").document(documentId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists() && documentSnapshot.getData() != null && documentSnapshot.getData().isEmpty()) {
                        // Document is empty, delete it
                        db.collection("requests").document(documentId)
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Document deleted successfully", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Error deleting document", Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error checking document", Toast.LENGTH_SHORT).show();
                });
    }
}