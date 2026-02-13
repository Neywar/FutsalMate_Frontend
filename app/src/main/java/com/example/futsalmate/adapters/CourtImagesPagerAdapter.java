package com.example.futsalmate.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.futsalmate.R;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class CourtImagesPagerAdapter extends RecyclerView.Adapter<CourtImagesPagerAdapter.ImageViewHolder> {

    private final List<String> imageUrls = new ArrayList<>();

    public void setImagesFromRaw(String imageRaw) {
        imageUrls.clear();
        if (imageRaw == null || imageRaw.trim().isEmpty()) {
            notifyDataSetChanged();
            return;
        }
        // Try JSON array first
        String trimmed = imageRaw.trim();
        if (trimmed.startsWith("[")) {
            try {
                JSONArray array = new JSONArray(trimmed);
                for (int i = 0; i < array.length(); i++) {
                    String path = array.optString(i, null);
                    String url = resolveImageUrl(path);
                    if (url != null) {
                        imageUrls.add(url);
                    }
                }
            } catch (JSONException e) {
                // Fallback to single path
                String url = resolveImageUrl(imageRaw);
                if (url != null) imageUrls.add(url);
            }
        } else {
            String url = resolveImageUrl(imageRaw);
            if (url != null) imageUrls.add(url);
        }
        if (imageUrls.isEmpty()) {
            // Ensure at least one placeholder entry
            imageUrls.add(null);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_court_image_pager, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        String url = imageUrls.get(position);
        if (url == null || url.trim().isEmpty()) {
            // No image URL – clear any previous image so nothing static is shown
            holder.ivPagerImage.setImageDrawable(null);
        } else {
            Glide.with(holder.itemView.getContext())
                    .load(url)
                    .centerCrop()
                    .into(holder.ivPagerImage);
        }
    }

    @Override
    public int getItemCount() {
        return imageUrls.size();
    }

    public int getImageCount() {
        return imageUrls.size();
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPagerImage;

        ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPagerImage = itemView.findViewById(R.id.ivPagerImage);
        }
    }

    private String resolveImageUrl(String image) {
        if (image == null || image.trim().isEmpty()) {
            return null;
        }
        String trimmed = image.trim();
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            return trimmed;
        }
        return "https://futsalmateapp.sameem.in.net/" + trimmed.replaceFirst("^/+", "");
    }
}

