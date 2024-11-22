package com.raufir.bauethms;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class mainpage extends AppCompatActivity {

    Button attend;
    RelativeLayout meal, payment, complain, profile;
    LinearLayout logout;
    SharedPreferences sharedPreferences;
    private static final String LAST_CLICK_DATE = "lastClickDate";
    private static final String PREFS_NAME = "buttonPrefs";
    private static final int START_HOUR = 20;
    private static final int END_HOUR = 22;
    private static final double SPECIFIC_LATITUDE = 24.289514; // Replace with your specific latitude
    private static final double SPECIFIC_LONGITUDE = 89.009024; // Replace with your specific longitude
    private static final float RADIUS_IN_METERS = 100; // Define the radius in meters

    // Location services
    private FusedLocationProviderClient fusedLocationClient;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference; // Reference to Realtime Database

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_mainpage);

        // Initialize Firestore, Auth, and location services
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("users"); // Reference to users in Realtime Database
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
complain=findViewById(R.id.complain);
        attend = findViewById(R.id.attend);
        meal = findViewById(R.id.mealp);
        payment = findViewById(R.id.paymentp);
        logout = findViewById(R.id.lgout);
        profile = findViewById(R.id.user);

        meal.setOnClickListener(view -> {
            Intent intent = new Intent(mainpage.this, meal.class);
            startActivity(intent);
        });

        complain.setOnClickListener(view -> {
            Intent intent = new Intent(mainpage.this, complain.class);
            startActivity(intent);
        });

        logout.setOnClickListener(view -> {
            Intent intent = new Intent(mainpage.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
payment.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View view) {
        Intent intent=new Intent(mainpage.this,paymentp.class);
        startActivity(intent);
    }
});
        profile.setOnClickListener(view -> {
            Intent intent = new Intent(mainpage.this, profile.class);
            startActivity(intent);
        });

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        checkButtonState();

        attend.setOnClickListener(v -> {
            saveClickDate();
            attend.setEnabled(false);
            // Get location and save attendance data
            getLocationAndSaveAttendance();
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.register), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void checkButtonState() {
        Calendar calendar = Calendar.getInstance();
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        String lastClickDate = sharedPreferences.getString(LAST_CLICK_DATE, "");
        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        // Check if the current time is within the specified hours
        boolean isWithinTime = currentHour >= START_HOUR && currentHour < END_HOUR;

        // Check if the user is within the specific location
        getLocationAndCheckWithinLocation(isWithinTime, currentDate);
    }

    private void getLocationAndCheckWithinLocation(boolean isWithinTime, String currentDate) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Request location permission if not granted
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
            return;
        }

        fusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    // Get the location
                    Location location = task.getResult();
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();

                    // Check if the user is within the specific location
                    boolean isWithinLocation = isWithinLocation(latitude, longitude);

                    // Enable the attend button based on time and location
                    attend.setEnabled(isWithinTime && isWithinLocation );
                } else {
                    Toast.makeText(mainpage.this, "Unable to get location", Toast.LENGTH_SHORT).show();
                    attend.setEnabled(false); // Disable button if location cannot be retrieved
                }
            }
        });
    }

    private void saveClickDate() {
        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(LAST_CLICK_DATE, currentDate);
        editor .apply();
    }

    // Method to get location and save attendance
    private void getLocationAndSaveAttendance() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Request location permission if not granted
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
            return;
        }

        fusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    // Get the location
                    Location location = task.getResult();
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();

                    // Check if the user is within the specific location
                    if (isWithinLocation(latitude, longitude)) {
                        // Retrieve the user ID from Realtime Database
                        getIdFromUIDAndSaveAttendance(latitude, longitude);
                    } else {
                        Toast.makeText(mainpage.this, "You are outside the attendance area", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(mainpage.this, "Unable to get location", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private boolean isWithinLocation(double userLatitude, double userLongitude) {
        Location specificLocation = new Location("specificLocation");
        specificLocation.setLatitude(SPECIFIC_LATITUDE);
        specificLocation.setLongitude(SPECIFIC_LONGITUDE);

        Location userLocation = new Location("userLocation");
        userLocation.setLatitude(userLatitude);
        userLocation.setLongitude(userLongitude);

        // Calculate the distance between the user and the specific location
        float distanceInMeters = specificLocation.distanceTo(userLocation);
        return distanceInMeters <= RADIUS_IN_METERS; // Check if within the defined radius
    }

    private void getIdFromUIDAndSaveAttendance(double latitude, double longitude) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            // Get UID from Firebase Authenticator

            // Query the Realtime Database to get the ID based on the UID
            // Assuming 'uid' is the UID you got from Firebase Authentication
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

// Query the entire 'users' node
            databaseReference.get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    for (DataSnapshot dataSnapshot : task.getResult().getChildren()) {
                        // Check if the 'uid' inside this custom ID node matches the Firebase Auth UID
                        String retrievedUid = dataSnapshot.child("uid").getValue(String.class);
                        if (retrievedUid != null && retrievedUid.equals(uid)) {
                            // Found the correct user, now retrieve the custom ID and proceed
                            String customId = dataSnapshot.getKey();  // This is your custom ID (e.g., '0812220205101004')

                            // Now you can use this customId to save the attendance
                            saveAttendanceToFirestore(customId, latitude, longitude);
                            break; // Stop the loop as you've found the correct user
                        }
                    }
                } else {
                    // Handle the case where no users were found
                    Toast.makeText(mainpage.this, "User not found", Toast.LENGTH_SHORT).show();
                }
            });

        } else {
            Toast.makeText(mainpage.this, "User not authenticated", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveAttendanceToFirestore(String userId, double latitude, double longitude) {
        // Get the UID from Firebase Authentication
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(mainpage.this, "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get the current date for creating a unique document in the sub-collection
        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        // Create a map of attendance data
        Map<String, Object> attendanceData = new HashMap<>();
        attendanceData.put("timestamp", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));
        attendanceData.put("latitude", latitude);
        attendanceData.put("longitude", longitude);
        attendanceData.put("uid", currentUser.getUid());

        // Use the `userId` as the document, and store the attendance info under a sub-collection named "attendance" for that user
        db.collection("attendance")
                .document(userId)  // Use 'userId' from the Realtime Database
                .collection("attendance_by_date")  // Sub-collection for attendance records
                .document(currentDate)  // Document named by the current date
                .set(attendanceData)  // Save the attendance data
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(mainpage.this, "Attendance recorded", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(mainpage.this, "Failed to record attendance", Toast.LENGTH_SHORT).show();
                });
    }



    @Override
    protected void onResume() {
        super.onResume();
        checkButtonState();
    }
}