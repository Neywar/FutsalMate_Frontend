package com.example.futsalmate.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.futsalmate.R;
import com.example.futsalmate.api.models.Court;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class FeaturedCourtsAdapter extends RecyclerView.Adapter<FeaturedCourtsAdapter.FeaturedCourtViewHolder> {

    public interface OnCourtClickListener {
        void onCourtClick(Court court);
    }

    private final List<Court> courts = new ArrayList<>();
    private final OnCourtClickListener listener;

    public FeaturedCourtsAdapter(OnCourtClickListener listener) {
        this.listener = listener;
    }

    public void setCourts(List<Court> items) {
        courts.clear();
        if (items != null) {
            courts.addAll(items);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FeaturedCourtViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_featured_court, parent, false);
        return new FeaturedCourtViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FeaturedCourtViewHolder holder, int position) {
        Court court = courts.get(position);
        holder.tvCourtName.setText(court.getCourtName() != null ? court.getCourtName() : "-");
        holder.tvCourtPrice.setText("Rs. " + (court.getPrice() != null ? court.getPrice() : "0"));

        String imageUrl = resolveImageUrl(court.getImage());
        Glide.with(holder.itemView.getContext())
                .load(imageUrl)
                .placeholder(R.drawable.ic_court_one)
                .error(R.drawable.ic_court_one)
                .centerCrop()
                .into(holder.ivCourtPhoto);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCourtClick(court);
            }
        });
    }

    @Override
    public int getItemCount() {
        return courts.size();
    }

    static class FeaturedCourtViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCourtPhoto;
        TextView tvCourtName;
        TextView tvCourtPrice;

        FeaturedCourtViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCourtPhoto = itemView.findViewById(R.id.ivFeaturedCourt);
            tvCourtName = itemView.findViewById(R.id.tvFeaturedCourtName);
            tvCourtPrice = itemView.findViewById(R.id.tvFeaturedCourtPrice);
        }
    }

    private String resolveImageUrl(String image) {
        if (image == null || image.isEmpty()) {
            return null;
        }
        String cleaned = extractFirstImage(image);
        if (cleaned == null || cleaned.isEmpty()) {
            return null;
        }
        if (cleaned.startsWith("http://") || cleaned.startsWith("https://")) {
            return cleaned;
        }
        return "https://futsalmateapp.sameem.in.net/" + cleaned.replaceFirst("^/+", "");
    }

    private String extractFirstImage(String image) {
        String trimmed = image.trim();
        if (trimmed.startsWith("[")) {
            try {
                JSONArray array = new JSONArray(trimmed);
                if (array.length() > 0) {
                    return array.optString(0, null);
                }
                return null;
            } catch (JSONException e) {
                return null;
            }
        }
        return trimmed;
    }
}
