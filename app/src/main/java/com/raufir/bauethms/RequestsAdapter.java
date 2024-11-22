package com.raufir.bauethms;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RequestsAdapter extends RecyclerView.Adapter<RequestsAdapter.RequestViewHolder> {

    private List<Request> requestList;
    private OnApproveListener approveListener;
    private OnRejectListener rejectListener;

    @FunctionalInterface
    public interface OnApproveListener {
        void onApprove(Request request);
    }

    @FunctionalInterface
    public interface OnRejectListener {
        void onReject(Request request);
    }

    public RequestsAdapter(List<Request> requestList, OnApproveListener approveListener, OnRejectListener rejectListener) {
        this.requestList = requestList;
        this.approveListener = approveListener;
        this.rejectListener = rejectListener;
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_request, parent, false);
        return new RequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
        Request request = requestList.get(position);
        holder.bind(request, approveListener, rejectListener, this);
    }

    @Override
    public int getItemCount() {
        return requestList.size();
    }

    public void removeItem(Request request) {
        int position = requestList.indexOf(request);
        if (position >= 0) {
            requestList.remove(position);
            notifyItemRemoved(position);
        }
    }

    public static class RequestViewHolder extends RecyclerView.ViewHolder {
        private TextView idTextView;
        private TextView fieldNameTextView;
        private TextView fieldValueTextView;
        private Button approveButton;
        private Button rejectButton;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            idTextView = itemView.findViewById(R.id.id);
            fieldNameTextView = itemView.findViewById(R.id.fieldNameTextView);
            fieldValueTextView = itemView.findViewById(R.id.fieldValueTextView);
            approveButton = itemView.findViewById(R.id.approveButton);
            rejectButton = itemView.findViewById(R.id.rejectButton);
        }

        public void bind(Request request, OnApproveListener approveListener, OnRejectListener rejectListener, RequestsAdapter adapter) {
            idTextView.setText(request.getDocumentId());
            fieldNameTextView.setText(request.getFieldName());
            fieldValueTextView.setText(String.valueOf(request.getFieldValue()));

            approveButton.setOnClickListener(v -> {
                if (approveListener != null) {
                    approveListener.onApprove(request);
                    adapter.removeItem(request); // Remove item from the list using the Request object
                }
            });

            rejectButton.setOnClickListener(v -> {
                if (rejectListener != null) {
                    rejectListener.onReject(request);
                    adapter.removeItem(request); // Remove item from the list using the Request object
                }
            });
        }
    }
}