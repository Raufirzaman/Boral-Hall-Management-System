package com.raufir.bauethms;
public class complaint {
    private String id; // Add an ID field to identify the complaint in Firestore
    private String subject;
    private String description;
    private String roomNumber; // New field for room number
private String userId;
    public complaint() {
        // Default constructor required for calls to DataSnapshot.getValue(Complaint.class)
    }

    public complaint(String id, String title, String description, String roomNo,String userId) {
        this.id = id;
        this.subject = title;
        this.description = description;
        this.roomNumber = roomNo;
        this.userId=userId;
        // Initialize room number
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return subject;
    }

    public String getDescription() {
        return description;
    }

    public String getRoomNo() {
        return roomNumber; // Getter for room number
    }

    public String getuId() {
       return userId;
    }
}