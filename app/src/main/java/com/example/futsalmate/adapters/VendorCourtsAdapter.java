package com.example.futsalmate.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.futsalmate.R;
import com.example.futsalmate.api.models.Court;
import com.google.android.material.button.MaterialButton;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class VendorCourtsAdapter extends RecyclerView.Adapter<VendorCourtsAdapter.CourtViewHolder> {

    private Context context;
    private List<Court> courts;
    private OnCourtActionListener listener;

    public interface OnCourtActionListener {
        void onEditCourt(Court court);
        void onDeleteCourt(Court court);
    }

    public VendorCourtsAdapter(Context context, OnCourtActionListener listener) {
        this.context = context;
        this.courts = new ArrayList<>();
        this.listener = listener;
    }

    public void setCourts(List<Court> courts) {
        android.util.Log.d("VendorCourtsAdapter", "setCourts called with " + (courts != null ? courts.size() : "null") + " courts");
        this.courts = courts != null ? courts : new ArrayList<>();
        notifyDataSetChanged();
        android.util.Log.d("VendorCourtsAdapter", "notifyDataSetChanged() called");
    }

    @NonNull
    @Override
    public CourtViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_vendor_court, parent, false);
        return new CourtViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CourtViewHolder holder, int position) {
        Court court = courts.get(position);
        android.util.Log.d("VendorCourtsAdapter", "Binding court at position " + position + ": " + court.getCourtName());
        
        holder.tvCourtName.setText(court.getCourtName());
        holder.tvLocation.setText(court.getLocation());
        holder.tvPrice.setText("Rs. " + court.getPrice());
        
        // Set status
        String status = court.getStatus();
        if ("active".equalsIgnoreCase(status)) {
            holder.tvStatus.setText("ACTIVE");
            holder.tvStatus.setTextColor(context.getResources().getColor(R.color.action_yellow));
            holder.tvStatus.setBackgroundTintList(context.getResources().getColorStateList(android.R.color.transparent));
        } else {
            holder.tvStatus.setText("INACTIVE");
            holder.tvStatus.setTextColor(context.getResources().getColor(R.color.gray_text));
            holder.tvStatus.setBackgroundTintList(context.getResources().getColorStateList(android.R.color.transparent));
        }
        
        // Load image
        String imageData = court.getImage();
        if (imageData != null && !imageData.isEmpty()) {
            String imageUrl = extractFirstImageUrl(imageData);
            if (imageUrl != null) {
                loadImageFromUrl(imageUrl, holder.ivCourtImage);
            }
        }
        
        // Set click listeners
        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditCourt(court);
            }
        });
        
        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteCourt(court);
            }
        });
    }

    @Override
    public int getItemCount() {
        int count = courts != null ? courts.size() : 0;
        android.util.Log.d("VendorCourtsAdapter", "getItemCount: " + count);
        return count;
    }

    private String extractFirstImageUrl(String imageJson) {
        try {
            // Remove outer quotes and parse JSON array
            String cleaned = imageJson.replace("\\\\", "\\").replace("\\/", "/");
            if (cleaned.startsWith("[\"")) {
                cleaned = cleaned.substring(2, cleaned.length() - 2);
                String[] images = cleaned.split("\",\"");
                if (images.length > 0) {
                    String imagePath = images[0].replace("\\", "").replace("\"", "");
                    return "https://futsalmateapp.sameem.in.net" + imagePath;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void loadImageFromUrl(String urlString, ImageView imageView) {
        new Thread(() -> {
            try {
                URL url = new URL(urlString);
                InputStream inputStream = url.openStream();
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                imageView.post(() -> {
                    if (bitmap != null) {
                        imageView.setImageBitmap(bitmap);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    static class CourtViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCourtImage;
        TextView tvStatus, tvCourtName, tvLocation, tvPrice;
        MaterialButton btnEdit, btnDelete;

        public CourtViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCourtImage = itemView.findViewById(R.id.ivCourtImage);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvCourtName = itemView.findViewById(R.id.tvCourtName);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
