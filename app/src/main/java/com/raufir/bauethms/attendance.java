package com.raufir.bauethms;

import android.app.DatePickerDialog;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class attendance extends AppCompatActivity {

    private Spinner personSpinner;
    private EditText dateInput;
    private TextView attendanceIdText, attendanceDateText, attendanceTimeText, attendanceLocationText;
    private Button fetchButton;
    private DatabaseReference databaseReference;
    private FirebaseFirestore firestoreDb;
    private ArrayList<String> personIds = new ArrayList<>();
    private String selectedDate = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.attendance);

        // Initialize Firebase Database and Firestore
        databaseReference = FirebaseDatabase.getInstance().getReference("users");
        firestoreDb = FirebaseFirestore.getInstance();

        // Find views by ID
        personSpinner = findViewById(R.id.person_spinner);
        dateInput = findViewById(R.id.date_input);
        attendanceIdText = findViewById(R.id.attendance_id_text);
        attendanceDateText = findViewById(R.id.attendance_date_text);
        attendanceTimeText = findViewById(R.id.attendance_time_text);
        attendanceLocationText = findViewById(R.id.attendance_location_text);
        fetchButton = findViewById(R.id.fetch_button);

        loadPersonSpinner();
        setupDatePicker();

        fetchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String selectedPersonId = personSpinner.getSelectedItem() != null ? personSpinner.getSelectedItem().toString() : null;
                if (selectedPersonId != null && !selectedDate.isEmpty()) {
                    fetchAttendanceData(selectedPersonId, selectedDate);
                } else {
                    Toast.makeText(attendance.this, "Please select both person and date", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loadPersonSpinner() {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                personIds.clear(); // Clear previous entries
                for (DataSnapshot personSnapshot : dataSnapshot.getChildren()) {
                    String personId = personSnapshot.getKey(); // Assuming person ID is the key
                    personIds.add(personId);
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(attendance.this, android.R.layout.simple_spinner_item, personIds);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                personSpinner.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("FirebaseError", "Error loading persons: ", databaseError.toException());
                Toast.makeText(attendance.this, "Error loading persons", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupDatePicker() {
        dateInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        attendance.this,
                        (view, selectedYear, selectedMonth, selectedDay) -> {
                            selectedDate = selectedYear + "-" + (selectedMonth + 1) + "-" + selectedDay;
                            dateInput.setText(selectedDate);
                        }, year, month, day);
                datePickerDialog.show();
            }
        });
    }

    private void fetchAttendanceData(String personId, String date) {
        firestoreDb.collection("attendance")
                .document(personId)
                .collection("attendance_by_date")
                .document(date)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult ();
                        if (document != null && document.exists()) {
                            String time = document.getString("timestamp");
                            Double latitude = document.getDouble("latitude");
                            Double longitude = document.getDouble("longitude");

                            String location = "Location not available";
                            if (latitude != null && longitude != null) {
                                location = getAddressFromLatLong(latitude, longitude);
                            }

                            attendanceIdText.setText("ID: " + personId);
                            attendanceDateText.setText("Date: " + date);
                            attendanceTimeText.setText("Time: " + (time != null ? time : "Not available"));
                            attendanceLocationText.setText("Location: " + location);

                            findViewById(R.id.attendance_result_section).setVisibility(View.VISIBLE);
                        } else {
                            Toast.makeText(this, "The Student is not Present", Toast.LENGTH_SHORT).show();
                            findViewById(R.id.attendance_result_section).setVisibility(View.GONE);
                        }
                    } else {
                        Log.e("FirestoreError", "Error getting documents: ", task.getException());
                        Toast.makeText(this, "Error fetching data", Toast.LENGTH_SHORT).show();
                        findViewById(R.id.attendance_result_section).setVisibility(View.GONE);
                    }
                });
    }

    private String getAddressFromLatLong(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                return addresses.get(0).getAddressLine(0);
            } else {
                return "Location not found";
            }
        } catch (IOException e) {
            Log.e("GeocoderError", "Geocoder failed", e);
            return "Location not available";
        }
    }
}