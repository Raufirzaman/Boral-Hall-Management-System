package com.raufir.bauethms;

import android.Manifest; import android.content.Intent; import android.content.pm.PackageManager; import android.net.Uri; import android.os.Build; import android.os.Bundle; import android.os.Environment; import android.provider.Settings; import android.widget.LinearLayout; import android.widget.RelativeLayout; import android.widget.Toast;

import androidx.activity.EdgeToEdge; import androidx.annotation.NonNull; import androidx.appcompat.app.AlertDialog; import androidx.appcompat.app.AppCompatActivity; import androidx.core.app.ActivityCompat; import androidx.core.content.ContextCompat; import androidx.core.graphics.Insets; import androidx.core.view.ViewCompat; import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DataSnapshot; import com.google.firebase.database.DatabaseError; import com.google.firebase.database.DatabaseReference; import com.google.firebase.database.FirebaseDatabase; import com.google.firebase.database.ValueEventListener; import com.google.firebase.firestore.FirebaseFirestore; import com.itextpdf.text.BaseColor; import com.itextpdf.text.Document; import com.itextpdf.text.DocumentException; import com.itextpdf.text.Element; import com.itextpdf.text.Font; import com.itextpdf.text.PageSize; import com.itextpdf.text.Paragraph; import com.itextpdf.text.Phrase; import com.itextpdf.text.pdf.PdfPCell; import com.itextpdf.text.pdf.PdfPTable; import com.itextpdf.text.pdf.PdfWriter;

import java.io.File; import java.io.FileOutputStream; import java.text.SimpleDateFormat; import java.util.ArrayList; import java.util.Date; import java.util.List; import java.util.Locale;

public class adminhome extends AppCompatActivity { RelativeLayout attend, search, mealReport,approve,complaints; LinearLayout logout; private FirebaseFirestore db; private DatabaseReference realtimeDb; private static final int STORAGE_PERMISSION_CODE = 101;
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    EdgeToEdge.enable(this);
    setContentView(R.layout.adminhome);

    // Initialize Firebase instances
    db = FirebaseFirestore.getInstance();
    realtimeDb = FirebaseDatabase.getInstance("https://bauet-hms-63f5b-default-rtdb.firebaseio.com/")
            .getReference("users");

    // Initialize buttons
    attend = findViewById(R.id.adattend);
    search = findViewById(R.id.search);
    logout = findViewById(R.id.lgout);
    mealReport = findViewById(R.id.mealReport);
    approve=findViewById(R.id.approve);
    complaints=findViewById(R.id.complaints);
    complaints.setOnClickListener(view -> {
        Intent intent = new Intent(adminhome.this,complaints.class);
        startActivity(intent);});
    // Logout button click listener
    logout.setOnClickListener(view -> {
        Intent intent = new Intent(adminhome.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    });
    approve.setOnClickListener(view -> {Intent intent = new Intent(adminhome.this,approval.class);
        startActivity(intent);});
    // Attendance button click listener
    attend.setOnClickListener(view -> {
        Intent intent = new Intent(adminhome.this, attendance.class);
        startActivity(intent);
    });

    // Search button click listener
    search.setOnClickListener(view -> {
        Intent intent = new Intent(adminhome.this, search.class);
        startActivity(intent);
    });

    // Meal report button click listener
    mealReport.setOnClickListener(view -> {
        checkAndRequestPermission();
    });

    // Window insets
    ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
        Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
        v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
        return insets;
    });
}

private void checkAndRequestPermission() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        // For Android 11 and above
        if (Environment.isExternalStorageManager()) {
            // Permission granted
            generateTodayMealReport();
        } else {
            // Request permission
            new AlertDialog.Builder(this)
                    .setTitle("Permission Needed")
                    .setMessage("Storage permission is needed to save the PDF report. Please allow access in settings.")
                    .setPositiveButton("Open Settings", (dialog, which) -> {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                        intent.setData(Uri.parse("package:" + getPackageName()));
                        startActivity(intent);
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> {
                        dialog.dismiss();
                    })
                    .create().show();
        }
    } else {
        // For Android 10 and below
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, request it
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                // Show an explanation to the user
                new AlertDialog.Builder(this)
                        .setTitle("Permission Needed")
                        .setMessage("Storage permission is needed to save the PDF report.")
                        .setPositiveButton("OK", (dialog, which) -> {
                            requestStoragePermission();
                        })
                        .setNegativeButton("Cancel", (dialog, which) -> {
                            dialog.dismiss();
                        })
                        .create().show();
            } else {
                // No explanation needed; request the permission
                requestStoragePermission();
            }
        } else {
            // Permission has already been granted
            generateTodayMealReport();
        }
    }
}

private void requestStoragePermission() {
    ActivityCompat.requestPermissions(this,
            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
            STORAGE_PERMISSION_CODE);
}

@Override
public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                       @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    if (requestCode == STORAGE_PERMISSION_CODE) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Permission granted
            generateTodayMealReport();
        } else {
            // Permission denied
            Toast.makeText(this, "Permission denied. Cannot generate PDF without storage access.",
                    Toast.LENGTH_LONG).show();

            // If permission is denied, show a message to the user and provide an option to open app settings
            new AlertDialog.Builder(this)
                    .setTitle("Permission Denied")
                    .setMessage("Storage permission is needed to save the PDF report. Please grant permission in app settings.")
                    .setPositiveButton("Open Settings", (dialog, which) -> {
                        Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        intent.setData(Uri.parse("package:" + getPackageName()));
                        startActivity(intent);
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> {
                        dialog.dismiss();
                    })
                    .create().show();
        }
    }
}

private void generateTodayMealReport() {
    String today = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new Date());

    try {
        // Create directory for PDF
        File pdfDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS), "MealReports");
        if (!pdfDir.exists()) {
            pdfDir.mkdirs();
        }

        // Create PDF file
        String fileName = "meal_report_" + today.replace(" ", "_") + ".pdf";
        File file = new File(pdfDir, fileName);

        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, new FileOutputStream(file));
        document.open();

        // Add title
        Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
        Paragraph title = new Paragraph("Meal Status Report - " + today, titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        document.add(new Paragraph("\n"));

        // Create table
        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(100);

        // Add headers
        String[] headers = {"Student ID", "Name", "Meal Status"};
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header,
                    new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD)));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
            table.addCell(cell);
        }

        final Document finalDocument = document;
        final PdfPTable finalTable = table;

        // Fetch data from Realtime Database
        realtimeDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<UserMealData> mealDataList = new ArrayList<>();
                final long totalUsers = dataSnapshot.getChildrenCount();
                final long[] processedUsers = {0};

                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    String userId = userSnapshot.child("id").getValue(String.class);
                    String userName = userSnapshot.child("name").getValue(String.class);

                    // Fetch meal status from Firestore
                    if (userId != null) {
                        db.collection("users").document(userId)
                                .collection("meals").document(today)
                                .get()
                                .addOnSuccessListener(mealDoc -> {
                                    Boolean mealStatus = null;
                                    if (mealDoc.exists()) {
                                        mealStatus = mealDoc.getBoolean("on");
                                    }
                                    mealDataList.add(new UserMealData(userId, userName, mealStatus));

                                    processedUsers[0]++;

                                    // Check if all users are processed
                                    if (processedUsers[0] == totalUsers) {
                                        try {
                                            // Sort the list by ID (optional)
                                            mealDataList.sort((a, b) -> a.id.compareTo(b.id));

                                            // Add all data to table
                                            for (UserMealData data : mealDataList) {
                                                finalTable.addCell(data.id != null ? data.id : "");
                                                finalTable.addCell(data.name != null ? data.name : "");
                                                finalTable.addCell(data.mealStatus != null ?
                                                        (data.mealStatus ? "ON" : "OFF") : "N/A");
                                            }

                                            finalDocument.add(finalTable);
                                            finalDocument.close();

                                            runOnUiThread(() -> {
                                                Toast.makeText(adminhome.this,
                                                        "PDF generated successfully in Downloads/MealReports",
                                                        Toast.LENGTH_LONG).show();
                                            });
                                        } catch (DocumentException e) {
                                            e.printStackTrace();
                                            runOnUiThread(() -> {
                                                Toast.makeText(adminhome.this,
                                                        "Error generating PDF: " + e.getMessage(),
                                                        Toast.LENGTH_SHORT).show();
                                            });
                                        }
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    processedUsers[0]++;
                                    // Still add the user even if meal status fetch fails
                                    mealDataList.add(new UserMealData(userId, userName, null));
                                });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                finalDocument.close();
                Toast.makeText(adminhome.this,
                        "Error accessing database: " + databaseError.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });

    } catch (DocumentException | java.io.IOException e) {
        Toast.makeText(this,
                "Error creating PDF: " + e.getMessage(),
                Toast.LENGTH_SHORT).show();
    }
}

private class UserMealData {
    String id;
    String name;
    Boolean mealStatus;

    UserMealData(String id, String name, Boolean mealStatus) {
        this.id = id;
        this.name = name;
        this.mealStatus = mealStatus;
    }
}}