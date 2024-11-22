package com.raufir.bauethms;

public class Request {
    private String documentId; // ID of the student
    private String fieldName;   // Name of the field being requested (e.g., "fatherName", "dob")
    private Object fieldValue;   // Value of the field (can be of any type)
    private boolean isApproved;   // Approval status

    public Request() {
        // Default constructor required for calls to DataSnapshot.getValue(Request.class)
    }

    public Request(String documentId, String fieldName, Object fieldValue) {
        this.documentId = documentId;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
        this.isApproved = false; // Default to not approved
    }

    public String getDocumentId() {
        return documentId;
    }

    public String getFieldName() {
        return fieldName;
    }

    public Object getFieldValue() {
        return fieldValue;
    }

    public boolean isApproved() {
        return isApproved;
    }

    public void setApproved(boolean approved) {
        isApproved = approved;
    }
}