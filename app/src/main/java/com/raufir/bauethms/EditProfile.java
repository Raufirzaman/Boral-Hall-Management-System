package com.raufir.bauethms;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class EditProfile extends AppCompatActivity {

    private Spinner spinnerFields;
    private Button buttonAdd, buttonUpdate;
    private LinearLayout fieldsContainer;
    private DatabaseReference databaseReference;
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;
    private String[] fields = {"ID", "Name", "Father's Name", "Mother's Name", "Address", "Email", "Phone No", "Room", "Batch", "Date Of Birth", "Department Name"};
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.editprofile);

        // Initialize Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference("users");
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        spinnerFields = findViewById(R.id.spinner_fields);
        buttonAdd = findViewById(R.id.button_add);
        buttonUpdate = findViewById(R.id.button_update);
        fieldsContainer = findViewById(R.id.fields_container);

        // Populate spinner with field names
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, fields);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFields.setAdapter(adapter);

        // Set up spinner selection listener
        spinnerFields.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Fetch current value from Firebase for the selected field
                if (currentUserId != null) {
                    String selectedField = fields[position];
                    fetchCurrentValue(selectedField);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        // Set up add button click listener
        buttonAdd.setOnClickListener(v -> {
            String selectedField = (String) spinnerFields.getSelectedItem();
            addField(selectedField);
        });

        // Set up update button click listener
        buttonUpdate.setOnClickListener(v -> {
            sendFieldsToFirestore();
            Intent intent = new Intent(EditProfile.this, profile.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        // Get current user ID
        getCurrentUserId(userId -> {
            currentUserId = userId;
            // Optionally, you can fetch initial values for the first field here
        });
    }

    private void fetchCurrentValue(String field) {
        // Logic to fetch current value from Firebase for the selected field
        databaseReference.child(currentUserId).child(field).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String currentValue = dataSnapshot.getValue(String.class);
                // Update the field with the current value
                updateField(field, currentValue);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
            }
        });
    }

    private void addField(String field) {
        // Create a new field layout (vertical orientation)
        LinearLayout fieldLayout = new LinearLayout(this);
        fieldLayout.setOrientation(LinearLayout.VERTICAL);

        // Set layout parameters for the field layout with a bottom margin for spacing
        LinearLayout.LayoutParams fieldLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        fieldLayoutParams.setMargins(0, 0, 0, 20); // 20dp gap at the bottom
        fieldLayout.setLayoutParams(fieldLayoutParams);

        // Create and set up the label TextView
        TextView fieldNameTextView = new TextView(this);
        fieldNameTextView.setText(field);
        fieldNameTextView.setTextColor(android.graphics.Color.WHITE); // Set label text color to white
        fieldNameTextView.setTextSize(18); // Set label text size larger than hint
        fieldLayout.addView(fieldNameTextView);

        // Create a horizontal layout for EditText and remove button
        LinearLayout editTextLayout = new LinearLayout(this);
        editTextLayout.setOrientation(LinearLayout.HORIZONTAL);
        editTextLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        // Create and set up the EditText
        EditText fieldValueEditText = new EditText(this);
        fieldValueEditText.setHint("Enter new value");
        fieldValueEditText.setTextColor(android.graphics.Color.BLACK); // Set input text color to black
        fieldValueEditText.setHintTextColor(android.graphics.Color.LTGRAY); // Set hint text color
        fieldValueEditText.setTextSize(16); // Set input text size smaller than label
        fieldValueEditText.setBackgroundColor(android.graphics.Color.WHITE); // Set background color to white

        // Set layout parameters for EditText to fill available space
        LinearLayout.LayoutParams editTextLayoutParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f); // Use weight to fill available space
        editTextLayoutParams.setMargins(0, 0, 10, 0); // Add a margin to the right for spacing
        fieldValueEditText.setLayoutParams(editTextLayoutParams);

        editTextLayout.addView(fieldValueEditText);

        // Create and set up the remove button
        Button removeButton = new Button(this);
        removeButton.setText("-");
        removeButton.setBackgroundColor(android.graphics.Color.RED); // Set button background color to red
        removeButton.setTextColor(android.graphics.Color.WHITE); // Set button text color to white

        // Set button size to be small and square
        LinearLayout.LayoutParams buttonLayoutParams = new LinearLayout.LayoutParams(100, 100); // Width and height of 100 pixels
        buttonLayoutParams.setMargins(10, 0, 0, 0); // Add a margin to the left for spacing
        removeButton.setLayoutParams(buttonLayoutParams);

        removeButton.setOnClickListener(v -> {
            fieldsContainer.removeView(fieldLayout);
        });
        editTextLayout.addView(removeButton);

        // Add the horizontal layout (EditText + remove button) to the vertical field layout
        fieldLayout.addView(editTextLayout);

        // Add the field layout to the container
        fieldsContainer.addView(fieldLayout);
    }
    private void updateField(String field, String currentValue) {
        // Find the field layout and update the edit text with the current value
        for (int i = 0; i < fieldsContainer.getChildCount(); i++) {
            View child = fieldsContainer.getChildAt(i);
            if (child instanceof LinearLayout) {
                LinearLayout fieldLayout = (LinearLayout) child;
                TextView fieldNameTextView = (TextView) fieldLayout.getChildAt(0);
                if (fieldNameTextView.getText().toString().equals(field)) {
                    // Get the horizontal layout that contains the EditText and remove button
                    LinearLayout editTextLayout = (LinearLayout) fieldLayout.getChildAt(1);
                    EditText fieldValueEditText = (EditText) editTextLayout.getChildAt(0); // Get the EditText from the horizontal layout
                    fieldValueEditText.setText(currentValue);
                    break;
                }
            }
        }
    }

    private void sendFieldsToFirestore() {
        // Create a new document in Firestore with the updated fields
        Map<String, String> fieldsMap = new HashMap<>();
        for (int i = 0; i < fieldsContainer.getChildCount(); i++) {
            View child = fieldsContainer.getChildAt(i);
            if (child instanceof LinearLayout) {
                LinearLayout fieldLayout = (LinearLayout) child;
                TextView fieldNameTextView = (TextView) fieldLayout.getChildAt(0);

                // Get the horizontal layout that contains the EditText and remove button
                LinearLayout editTextLayout = (LinearLayout) fieldLayout.getChildAt(1);
                EditText fieldValueEditText = (EditText) editTextLayout.getChildAt(0); // Get the EditText from the horizontal layout

                fieldsMap.put(fieldNameTextView.getText().toString(), fieldValueEditText.getText().toString());
            }
        }

        // Get the current date
        String currentDate = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());

        // Create a document reference with the current date
        DocumentReference documentReference = firestore.collection("requests").document(currentUserId + "_" + currentDate);
        documentReference.set(fieldsMap).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(EditProfile.this, "Request sent successfully!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(EditProfile.this, "Error sending fields to Firestore: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private interface UserIdCallback {
        void onUserIdRetrieved(String userId);
    }

    private void getCurrentUserId(UserIdCallback callback) {
        // Get the current user's email
        FirebaseUser currentUser = auth.getCurrentUser();
        String email = currentUser.getEmail();

        if (email != null) {
            DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
            usersRef.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {

                                String userId = userSnapshot.child("id").getValue(String.class);

                                callback.onUserIdRetrieved(userId);
                                break;

                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle error
                    Toast.makeText(EditProfile.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Handle case where user is not logged in
            Toast.makeText(EditProfile.this, "No user is logged in", Toast.LENGTH_SHORT).show();
        }
    }
}