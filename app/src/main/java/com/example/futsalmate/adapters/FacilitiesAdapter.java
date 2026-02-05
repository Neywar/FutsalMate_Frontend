package com.example.futsalmate.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.futsalmate.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FacilitiesAdapter extends RecyclerView.Adapter<FacilitiesAdapter.FacilityViewHolder> {

    private final List<String> facilities = new ArrayList<>();

    public void setFacilities(List<String> items) {
        facilities.clear();
        if (items != null) {
            for (String item : items) {
                String cleaned = cleanFacility(item);
                if (!cleaned.isEmpty()) {
                    facilities.add(cleaned);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FacilityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_facility, parent, false);
        return new FacilityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FacilityViewHolder holder, int position) {
        String facility = facilities.get(position);
        holder.tvFacilityName.setText(facility.toUpperCase(Locale.US));
        holder.ivFacilityIcon.setImageResource(R.drawable.ic_bookings);
    }

    @Override
    public int getItemCount() {
        return facilities.size();
    }

    static class FacilityViewHolder extends RecyclerView.ViewHolder {
        ImageView ivFacilityIcon;
        TextView tvFacilityName;

        FacilityViewHolder(@NonNull View itemView) {
            super(itemView);
            ivFacilityIcon = itemView.findViewById(R.id.ivFacilityIcon);
            tvFacilityName = itemView.findViewById(R.id.tvFacilityName);
        }
    }

    private String cleanFacility(String facility) {
        if (facility == null) {
            return "";
        }
        String cleaned = facility.trim();
        if (cleaned.startsWith("[") && cleaned.endsWith("]")) {
            cleaned = cleaned.substring(1, cleaned.length() - 1);
        }
        cleaned = cleaned.replace("\"", "");
        return cleaned.trim();
    }
}
