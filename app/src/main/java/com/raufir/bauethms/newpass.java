package com.raufir.bauethms;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class newpass extends AppCompatActivity {

    private LinearLayout emailSection, passwordSection;
    private EditText emailInput;
    private Button emailSubmitButton;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newpass);  // Update with your correct XML file name

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize the UI components
        emailSection = findViewById(R.id.email_section);

        passwordSection = findViewById(R.id.password_section);

        emailInput = findViewById(R.id.email_input);

        emailSubmitButton = findViewById(R.id.emailsubmitf);



        // Set up onClick listeners
        emailSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleEmailSubmit();
            }
        });


    }

    // Method to handle the email submission for password reset
    private void handleEmailSubmit() {
        String email = emailInput.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show();
            return;
        }

        // Send password reset email
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(newpass.this, "Password reset email sent", Toast.LENGTH_SHORT).show();
                        // Transition to the password section (assuming user accessed email and clicked the link)
                        emailSection.setVisibility(View.GONE);
                        passwordSection.setVisibility(View.VISIBLE);
                    } else {
                        Toast.makeText(newpass.this, "Failed to send reset email", Toast.LENGTH_SHORT).show();
                    }
                });
    }


}
