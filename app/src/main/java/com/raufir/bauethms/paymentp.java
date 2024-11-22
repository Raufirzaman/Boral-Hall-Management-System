package  com.raufir.bauethms;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class paymentp extends AppCompatActivity {

    private static final String TAG = "PaymentHistoryActivity";

    private FirebaseFirestore db;
    private FirebaseDatabase realtimeDb;
    private FirebaseAuth auth;

    private RecyclerView paymentHistoryList;
    private PaymentHistoryAdapter adapter;
    private TextView totalDuesValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paymentp);

        db = FirebaseFirestore.getInstance();
        realtimeDb = FirebaseDatabase.getInstance("https://bauet-hms-63f5b-default-rtdb.firebaseio.com/");
        auth = FirebaseAuth.getInstance();

        paymentHistoryList = findViewById(R.id.payment_history_list);
        totalDuesValue = findViewById(R.id.total_dues_value);

        // Set up RecyclerView
        paymentHistoryList.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PaymentHistoryAdapter(new ArrayList<>());
        paymentHistoryList.setAdapter(adapter);

        // First, retrieve the custom ID from Realtime Database
        loadCustomId();
    }

    private void loadCustomId() {
        // Get the current user's UID
        String uid = auth.getCurrentUser ().getEmail();

        // Reference to the root where IDs are stored in the Realtime Database
        DatabaseReference idsRef =  FirebaseDatabase.getInstance("https://bauet-hms-63f5b-default-rtdb.firebaseio.com/").getReference("users");

        // Get all users and filter locally
        idsRef.orderByChild("email").equalTo(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                // Retrieve all the relevant fields
                                String id = snapshot.child("id").getValue(String.class);
                     loadPaymentHistory(id);
                     loadDuesSummary(id);

                            }
                        } else {
                            // Handle case where no data is found
                            Toast.makeText(paymentp.this, "No user data found", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle database error
                        Toast.makeText(paymentp.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void loadPaymentHistory(String customId) {
        // Query the payment history document using the custom ID
        db.collection("payment history")
                .document(customId) // Custom ID used to reference the document
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                List<Payment> payments = new ArrayList<>();

                                // Iterate over the fields in the document
                                for (String date : document.getData().keySet()) {
                                    // Retrieve the amount as a String
                                    String money = document.get(date).toString();

                                    // Create a Payment object with the date and amount
                                    Payment payment = new Payment(date, money);
                                    payments.add(payment);
                                }
                                // Update the RecyclerView with the payments
                                adapter.updatePayments(payments);
                            } else {
                                Log.w(TAG, "No payment history found for this custom ID.");
                            }
                        } else {
                            Log.w(TAG, "Error getting payment history.", task.getException());
                        }
                    }
                });
    }



    private void loadDuesSummary(String customId) {
        // Query the DUES document using the custom ID
        db.collection("payment history")
                .document("DUES") // "DUES" document
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                // Log all field names in the document
                                for (String key : document.getData().keySet()) {
                                    Log.d(TAG, "Field key: " + key);
                                }

                                // Handle special characters in customId and fetch the dues safely
                                try {
                                    // Use FieldPath.of() to handle any special characters in the customId
                                    FieldPath customFieldPath = FieldPath.of(customId);

                                    // Fetch dues value using FieldPath
                                    Object duesValueObj = document.get(customFieldPath);

                                    if (duesValueObj != null) {
                                        // Assuming duesValueObj is either a String or Double, format it accordingly
                                        String duesValue = String.valueOf(duesValueObj);
                                        // Set the dues value to the TextView
                                        totalDuesValue.setText("৳" + duesValue);
                                    } else {
                                        // Handle the case where dues value is null
                                        Log.e(TAG, "Dues value is null for custom ID: " + customId);
                                        totalDuesValue.setText("৳0"); // Default to 0
                                    }
                                } catch (Exception e) {
                                    // Handle any exceptions during FieldPath access
                                    Log.e(TAG, "Error retrieving dues value for custom ID: " + customId + ". " + e.getMessage());
                                    totalDuesValue.setText("৳0"); // Default to 0
                                }
                            } else {
                                Log.d(TAG, "No such document in Firestore.");
                            }
                        } else {
                            Log.w(TAG, "Error fetching dues summary: ", task.getException());
                        }
                    }
                });
    }




}
