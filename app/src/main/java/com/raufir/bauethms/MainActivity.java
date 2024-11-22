package com.raufir.bauethms;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {
    EditText id, password;
    TextView register, forgot;
    Button submit;
    FirebaseAuth mAuth;  // Firebase Authentication instance

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Make the activity fullscreen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // UI components
        id = findViewById(R.id.editid);
        password = findViewById(R.id.editpass);
        submit = findViewById(R.id.lgbtn);
        register = findViewById(R.id.newuser);
        forgot = findViewById(R.id.forgot);

        // Firebase Realtime Database reference to the "users" node
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

        // Redirect to registration page
        register.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, Register.class);
            startActivity(intent);
        });

        // Redirect to forgot password page
        forgot.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, newpass.class);
            startActivity(intent);
        });

        // Login button click listener
        submit.setOnClickListener(view -> {

            // Get ID number and password from the input fields
            String idNumber = id.getText().toString().trim();
            String pass = password.getText().toString().trim();

            if (idNumber.isEmpty() || pass.isEmpty()) {
                Toast.makeText(MainActivity.this, "Please enter both ID and password", Toast.LENGTH_SHORT).show();
                return;
            }

            // Query Firebase Realtime Database to get email by ID number
            usersRef.child(idNumber).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DataSnapshot dataSnapshot = task.getResult();
                    if (dataSnapshot.exists()) {
                        // Retrieve the email from the user node
                        String email = dataSnapshot.child("email").getValue(String.class);
                        if (email != null) {
                            Log.d("Login", "Retrieved email: " + email); // Debug log
                            // Call the login method with email and password
                            loginUserWithEmail(email, pass);
                        } else {
                            // Handle case where email is not found in the user data
                            Toast.makeText(MainActivity.this, "Email not found", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // Handle case where the ID is not found
                        Toast.makeText(MainActivity.this, "ID not found", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Handle task failure (e.g., network error)
                    Log.d("RealtimeDB", "Error: " + task.getException());
                }
            });
        });

        // Set window insets listener to handle padding

    }

    // Method to login user with email and password
    private void loginUserWithEmail(String email, String password) {
        Log.d("Login", "Attempting login with email: " + email + " and password: " + password); // Debug log
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, get the current user's ID
                        Log.d("Login", "signInWithEmail:success");
                        String userId =id.getText().toString().trim();

                        // Reference to the user's data in the Realtime Database
                        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);

                        // Get the user's role from the database
                        userRef.child("role").get().addOnCompleteListener(roleTask -> {
                            if (roleTask.isSuccessful()) {
                                DataSnapshot roleSnapshot = roleTask.getResult();
                                if (roleSnapshot.exists()) {
                                    String role = roleSnapshot.getValue(String.class);
                                    Log.d("Login", "User role: " + role);

                                    // Redirect based on role
                                    if ("admin".equals(role)) {
                                        // Redirect to Admin Activity
                                        Intent intent = new Intent(MainActivity.this, adminhome.class);
                                        startActivity(intent);
                                        finish();
                                    } else if ("member".equals(role)) {
                                        // Redirect to Member Activity
                                        Intent intent = new Intent(MainActivity.this, mainpage.class);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        Toast.makeText(MainActivity.this, "Unknown role", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Toast.makeText(MainActivity.this, "No role assigned", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Log.w("Login", "Error getting role: ", roleTask.getException());
                                Toast.makeText(MainActivity.this, "Failed to retrieve role", Toast.LENGTH_SHORT).show();
                            }
                        });

                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("Login", "signInWithEmail:failure", task.getException());
                        Toast.makeText(MainActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
