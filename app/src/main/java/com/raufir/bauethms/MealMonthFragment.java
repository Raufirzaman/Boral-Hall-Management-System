package com.raufir.bauethms;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MealMonthFragment extends Fragment {

    private int monthOffset;
    private TableLayout tableLayout;
    private TextView tvMonthHeader;
    private FirebaseFirestore db;
    private HashMap<String, Boolean> mealPreferences = new HashMap<>();
    private Button btnSavePreferences;
    private String customId;
    private DatabaseReference databaseReference;
    private FirebaseAuth auth;
    private FirebaseDatabase realtimeDb;
    public static MealMonthFragment newInstance(int monthOffset) {
        MealMonthFragment fragment = new MealMonthFragment();
        Bundle args = new Bundle();
        args.putInt("monthOffset", monthOffset);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            monthOffset = getArguments().getInt("monthOffset");
        }
        db = FirebaseFirestore.getInstance();
        databaseReference = FirebaseDatabase.getInstance("https://bauet-hms-63f5b-default-rtdb.firebaseio.com/").getReference("users");
        auth = FirebaseAuth.getInstance();

        String uid = auth.getCurrentUser().getEmail();
        // Retrieve customId based on UID from Firebase Auth
        databaseReference.orderByChild("email").equalTo(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                customId = snapshot.child("id").getValue(String.class);
                                // Populate table only after customId is retrieved
                                if(isAdded()) { // Check if fragment is still attached
                                    populateMealTable();
                                }
                            }
                        } else {
                            // Handle case where no data is found
                            Toast.makeText(getContext(), "User data not found", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle database error
                        Toast.makeText(getContext(), "Database error: " + databaseError.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_meal_month, container, false);

        tableLayout = view.findViewById(R.id.tableLayout);
        tvMonthHeader = view.findViewById(R.id.tvMonthHeader);
        btnSavePreferences = view.findViewById(R.id.btnSavePreferences);

        // Don't populate here anymore as it's moved to after customId is retrieved
        // populateMealTable();

        btnSavePreferences.setOnClickListener(v -> savePreferences());

        return view;
    }

    private void populateMealTable() {
        Calendar baseCalendar = Calendar.getInstance();
        baseCalendar.add(Calendar.MONTH, monthOffset);
        baseCalendar.set(Calendar.DAY_OF_MONTH, 1);

        SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        tvMonthHeader.setText(monthFormat.format(baseCalendar.getTime()));

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        int daysInMonth = baseCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        tableLayout.removeAllViews();

        TableRow headerRow = new TableRow(getContext());
        TextView headerDate = new TextView(getContext());
        TextView headerCost = new TextView(getContext());
        TextView headerSwitch = new TextView(getContext());

        headerDate.setText("Date");
        headerCost.setText("Cost");
        headerSwitch.setText("OFF/ON");

        headerDate.setGravity(android.view.Gravity.CENTER);
        headerCost.setGravity(android.view.Gravity.CENTER);
        headerSwitch.setGravity(android.view.Gravity.CENTER);

        headerDate.setTextColor(getResources().getColor(android.R.color.white));
        headerCost.setTextColor(getResources().getColor(android.R.color.white));
        headerSwitch.setTextColor(getResources().getColor(android.R.color.white));

        headerRow.addView(headerDate);
        headerRow.addView(headerCost);
        headerRow.addView(headerSwitch);
        tableLayout.addView(headerRow);

        for (int i = 1; i <= daysInMonth; i++) {
            Calendar dayCalendar = (Calendar) baseCalendar.clone();
            dayCalendar.set(Calendar.DAY_OF_MONTH, i);

            TableRow row = new TableRow(getContext());
            TextView dateView = new TextView(getContext());
            TextView costView = new TextView(getContext());
            Switch mealSwitch = new Switch(getContext());

            String date = dateFormat.format(dayCalendar.getTime());
            dateView.setText(date);
            dateView.setGravity(android.view.Gravity.CENTER);
            dateView.setTextColor(getResources().getColor(android.R.color.white));

            costView.setText("0/-");
            costView.setGravity(android.view.Gravity.CENTER);
            costView.setTextColor(getResources().getColor(android.R.color.white));

            mealSwitch.setEnabled(!dayCalendar.before(today));

            mealSwitch.setThumbTintList(ContextCompat.getColorStateList(getContext(), R.color.switch_thumb_color));
            mealSwitch.setTrackTintList(ContextCompat.getColorStateList(getContext(), R.color.switch_track_color));

            final Switch finalMealSwitch = mealSwitch;
            final String finalDate = date;

            // Check Firestore for existing meal preference
            DocumentReference mealRef = db.collection("users").document(customId)
                    .collection("meals").document(date);

            mealRef.get().addOnSuccessListener(document -> {
                if (document != null && document.exists()) {
                    Boolean on = document.getBoolean("on");
                    if (on != null) {
                        finalMealSwitch.setChecked(on);
                        mealPreferences.put(finalDate, on);
                    }
                }
            }).addOnFailureListener(e -> {
                Log.e("Firestore", "Error accessing document: " + e.getMessage());
            });

            mealSwitch.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
                mealPreferences.put(date, isChecked);
            });

            row.addView(dateView);
            row.addView(costView);
            row.addView(mealSwitch);
            tableLayout.addView(row);
        }
    }

    private void savePreferences() {
        if (customId == null) {
            // Show error message if customId is not available
            Toast.makeText(getContext(), "User ID not available. Please try again.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show progress dialog
        ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Saving preferences...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // Counter for tracking completed operations
        final int[] completedOperations = {0};
        final int totalOperations = mealPreferences.size();
        final boolean[] hasError = {false};

        for (Map.Entry<String, Boolean> entry : mealPreferences.entrySet()) {
            String date = entry.getKey();
            Boolean on = entry.getValue();

            DocumentReference mealRef = db.collection("users").document(customId)
                    .collection("meals").document(date);

            Map<String, Object> data = new HashMap<>();
            data.put("on", on);

            mealRef.set(data)
                    .addOnSuccessListener(aVoid -> {
                        completedOperations[0]++;
                        // Check if all operations are complete
                        if (completedOperations[0] == totalOperations) {
                            progressDialog.dismiss();
                            if (!hasError[0]) {
                                Toast.makeText(getContext(), "Preferences saved successfully!", Toast.LENGTH_SHORT).show();
                                // Navigate back to main page
                                Intent intent = new Intent(requireActivity(), mainpage.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                requireActivity().startActivity(intent);
                                requireActivity().finish();
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        hasError[0] = true;
                        completedOperations[0]++;
                        if (completedOperations[0] == totalOperations) {
                            progressDialog.dismiss();
                            Toast.makeText(getContext(), "Error saving preferences: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
        }

        // Handle empty preferences case
        if (mealPreferences.isEmpty()) {
            progressDialog.dismiss();
            Toast.makeText(getContext(), "No preferences to save", Toast.LENGTH_SHORT).show();
        }
    }
}
