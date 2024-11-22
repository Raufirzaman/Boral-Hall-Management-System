// ComplaintAdapter.java
package com.raufir.bauethms;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;



public class complaintadapter extends RecyclerView.Adapter<complaintadapter.ComplaintViewHolder> {

    private List<complaint> complaints;
    private OnComplaintClickListener onComplaintClickListener;
    private OnComplaintDeleteListener onComplaintDeleteListener;

    public interface OnComplaintClickListener {
        void onComplaintClick(complaint complaint);
    }

    public interface OnComplaintDeleteListener {
        void onComplaintDelete(complaint complaint);
    }

    public complaintadapter(List<complaint> complaints, OnComplaintClickListener clickListener, OnComplaintDeleteListener deleteListener) {
        this.complaints = complaints;
        this.onComplaintClickListener = clickListener;
        this.onComplaintDeleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ComplaintViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_complaint, parent, false);
        return new ComplaintViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ComplaintViewHolder holder, int position) {
        complaint complaint = complaints.get(position);
        holder.idTextView.setText("ID: " + complaint.getId()); // Display complaint ID
        holder.titleTextView.setText(complaint.getTitle());
        holder.roomNoTextView.setText("Room No: " + complaint.getRoomNo()); // Display room number
        holder.itemView.setOnClickListener(v -> onComplaintClickListener.onComplaintClick(complaint));

    }

    @Override
    public int getItemCount() {
        return complaints.size();
    }

    public static class ComplaintViewHolder extends RecyclerView.ViewHolder {
        TextView idTextView; // New TextView for complaint ID
        TextView titleTextView;
        TextView roomNoTextView; // TextView for room number


        public ComplaintViewHolder(@NonNull View itemView) {
            super(itemView);
            idTextView = itemView.findViewById(R.id.idTextView); // ID TextView
            titleTextView = itemView.findViewById(R.id.text1); // Title TextView
            roomNoTextView = itemView.findViewById(R.id.roomNoTextView); // Room number TextView
          // Delete button
        }
    }
}