package com.raufir.bauethms;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class Register extends AppCompatActivity {
    Button rgbtn, btnUploadImage;
    EditText editname, editid, editpass, editemail, editaddress, editfather, editmother, editphone, editbatch, editroom, editDOB, editdept;
    FirebaseAuth mAuth;
    FirebaseStorage storage;
    Uri imageUri;
    String profileImageUrl = ""; // Will store the uploaded profile image URL
    private static final int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.register);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Initialize FirebaseAuth and FirebaseStorage
        mAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance("gs://bauet-hms-63f5b.appspot.com");

        rgbtn = findViewById(R.id.btnsubmit);
        btnUploadImage = findViewById(R.id.btnUploadImage);
        editname = findViewById(R.id.editname);
        editid = findViewById(R.id.editTextID);
        editpass = findViewById(R.id.editTextPassword);
        editemail = findViewById(R.id.editTextEmail);
        editaddress = findViewById(R.id.editTextAddress);
        editfather = findViewById(R.id.editTextFatherName);
        editmother = findViewById(R.id.editTextMotherName);
        editphone = findViewById(R.id.editTextPhoneNumber);
        editbatch = findViewById(R.id.editTextBatch);
        editroom = findViewById(R.id.editTextRoom);
        editDOB = findViewById(R.id.editTextDate);
        editdept = findViewById(R.id.editdept);

        // Handle Date Picker for Date of Birth
        editDOB.setOnClickListener(view -> {
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    Register.this,
                    (view1, year1, monthOfYear, dayOfMonth) -> {
                        String selectedDate = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year1;
                        editDOB.setText(selectedDate);
                    }, year, month, day);
            datePickerDialog.show();
        });

        // Handle Image Upload Button Click
        btnUploadImage.setOnClickListener(view -> openImagePicker());

        // Handle Submit Button Click
        rgbtn.setOnClickListener(view -> {
            if (imageUri != null) {
                // Wait until the image is uploaded before registering the user
                uploadImageToFirebase();

            } else {
                Toast.makeText(Register.this, "Please upload a profile image first", Toast.LENGTH_SHORT).show();
            }
        });
    }


    // Open image picker to select a profile image
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    // Handle image picker result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();  // Get the Uri of the selected image
        }
    }

    // Upload image to Firebase Storage
    private void uploadImageToFirebase() {
        if (imageUri != null) {
            StorageReference storageReference = storage.getReference().child("profile_images/" + System.currentTimeMillis() + ".jpg");

            // Upload image to Firebase Storage
            storageReference.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        // Get the download URL of the uploaded image
                        storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                            profileImageUrl = uri.toString();
                            // Now that the image is uploaded, proceed to register the user
                            registerUser();
                        });
                    })
                    .addOnFailureListener(e -> Toast.makeText(Register.this, "Image upload failed", Toast.LENGTH_SHORT).show());
             }
    }

    // Register the user in Firebase Authentication and store additional data
    // Register the user in Firebase Authentication and store additional data
    private void registerUser() {
        String idNumber = editid.getText().toString();
        String name = editname.getText().toString();
        String email = editemail.getText().toString();
        String password = editpass.getText().toString();
        String address = editaddress.getText().toString();
        String fatherName = editfather.getText().toString();
        String motherName = editmother.getText().toString();
        String dob = editDOB.getText().toString();
        String phoneNumber = editphone.getText().toString();
        String batch = editbatch.getText().toString();
        String room = editroom.getText().toString();
        String deptName = editdept.getText().toString();

        // Create user with Firebase Authentication
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Registration success, store additional user data in Realtime Database
                        FirebaseUser user = mAuth.getCurrentUser();
                        DatabaseReference usersRef = FirebaseDatabase.getInstance("https://bauet-hms-63f5b-default-rtdb.firebaseio.com/").getReference("users");

                        if (user != null) {
                            // Create a Map to store all the user details including UID
                            Map<String, Object> userDetails2 = new HashMap<>();
                            userDetails2.put("id", idNumber);  // Custom ID
                            userDetails2.put("name", name);
                            userDetails2.put("email", email);
                            userDetails2.put("address", address);
                            userDetails2.put("father_name", fatherName);
                            userDetails2.put("mother_name", motherName);
                            userDetails2.put("dob", dob);
                            userDetails2.put("phone_number", phoneNumber);
                            userDetails2.put("batch", batch);
                            userDetails2.put("room", room);
                            userDetails2.put("dept_name", deptName);
                            userDetails2.put("profile_image_url", profileImageUrl);
                            userDetails2.put("role", "member");
                            userDetails2.put("uid", user.getUid());  // Store the UID generated by Firebase

                            // Use the custom ID as the key in Realtime Database
                            usersRef.child(idNumber).setValue(userDetails2)  // Now using userDetails2 instead of userDetails
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d("RealtimeDB", "Data successfully written!");
                                        Toast.makeText(Register.this, "Registration successful", Toast.LENGTH_SHORT).show();
                                        // Redirect to MainActivity or home screen
                                        Intent intent = new Intent(Register.this, MainActivity.class);
                                        startActivity(intent);
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.w("RealtimeDB", "Error writing data", e);
                                        Toast.makeText(Register.this, "Failed to save data", Toast.LENGTH_SHORT).show();
                                    });
                        }
                    } else {
                        // Registration failed
                        Log.w("Auth", "createUser WithEmail:failure", task.getException());
                        Toast.makeText(Register.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                    }
                });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.register), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

    }

}

