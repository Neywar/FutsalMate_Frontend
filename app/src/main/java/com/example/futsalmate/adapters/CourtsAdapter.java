package com.example.futsalmate.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.futsalmate.R;
import com.example.futsalmate.api.models.Court;
import com.google.android.material.button.MaterialButton;
import com.bumptech.glide.Glide;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;

public class CourtsAdapter extends RecyclerView.Adapter<CourtsAdapter.CourtViewHolder> {

    public interface OnCourtClickListener {
        void onOpenCourt(Court court);
    }

    private final List<Court> courts = new ArrayList<>();
    private final OnCourtClickListener listener;

    public CourtsAdapter(OnCourtClickListener listener) {
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
    public CourtViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_court_card, parent, false);
        return new CourtViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CourtViewHolder holder, int position) {
        Court court = courts.get(position);
        holder.tvCourtName.setText(court.getCourtName() != null ? court.getCourtName() : "-");
        holder.tvCourtLocation.setText(court.getLocation() != null ? court.getLocation() : "-");
        holder.tvCourtPrice.setText("Rs. " + (court.getPrice() != null ? court.getPrice() : "0"));

        bindFacilities(holder.facilitiesContainer, normalizeFacilities(court.getFacilities()));

        String imageUrl = resolveImageUrl(court.getImage());
        Glide.with(holder.itemView.getContext())
            .load(imageUrl)
            .placeholder(R.drawable.ic_court_one)
            .error(R.drawable.ic_court_one)
            .centerCrop()
            .into(holder.ivCourtPhoto);

        View.OnClickListener open = v -> {
            if (listener != null) {
                listener.onOpenCourt(court);
            }
        };
        holder.itemView.setOnClickListener(open);
        holder.btnBookNow.setOnClickListener(open);
        holder.ivCourtPhoto.setOnClickListener(open);
    }

    @Override
    public int getItemCount() {
        return courts.size();
    }

    static class CourtViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCourtPhoto;
        TextView tvCourtName;
        TextView tvCourtLocation;
        TextView tvCourtPrice;
        MaterialButton btnBookNow;
        LinearLayout facilitiesContainer;

        CourtViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCourtPhoto = itemView.findViewById(R.id.ivCourtPhoto);
            tvCourtName = itemView.findViewById(R.id.tvCourtName);
            tvCourtLocation = itemView.findViewById(R.id.tvCourtLocation);
            tvCourtPrice = itemView.findViewById(R.id.tvCourtPrice);
            btnBookNow = itemView.findViewById(R.id.btnBookNow);
            facilitiesContainer = itemView.findViewById(R.id.facilitiesContainer);
        }
    }

    private void bindFacilities(LinearLayout container, List<String> facilities) {
        if (container == null) return;
        container.removeAllViews();
        if (facilities == null || facilities.isEmpty()) {
            return;
        }

        int max = Math.min(facilities.size(), 4);
        for (int i = 0; i < max; i++) {
            String facility = cleanFacility(facilities.get(i));
            TextView chip = new TextView(container.getContext());
            chip.setText(facility);
            chip.setTextSize(10);
            chip.setTextColor(container.getContext().getColor(R.color.text_grey));
            chip.setBackgroundResource(R.drawable.bg_facility_chip);
            int paddingH = dpToPx(container, 10);
            int paddingV = dpToPx(container, 4);
            chip.setPadding(paddingH, paddingV, paddingH, paddingV);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            if (i > 0) {
                params.setMarginStart(dpToPx(container, 8));
            }
            chip.setLayoutParams(params);
            container.addView(chip);
        }
    }

    private List<String> normalizeFacilities(List<String> facilities) {
        if (facilities == null || facilities.isEmpty()) {
            return facilities;
        }
        if (facilities.size() == 1) {
            String raw = facilities.get(0);
            if (raw != null && (raw.contains(",") || (raw.startsWith("[") && raw.endsWith("]")))) {
                String cleaned = raw.trim();
                if (cleaned.startsWith("[") && cleaned.endsWith("]")) {
                    cleaned = cleaned.substring(1, cleaned.length() - 1);
                }
                String[] parts = cleaned.split(",");
                List<String> result = new ArrayList<>();
                for (String part : parts) {
                    String value = cleanFacility(part);
                    if (!value.isEmpty()) {
                        result.add(value);
                    }
                }
                return result;
            }
        }
        return facilities;
    }

    private int dpToPx(LinearLayout container, int dp) {
        float density = container.getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
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
