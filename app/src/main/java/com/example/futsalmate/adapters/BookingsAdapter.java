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
import com.example.futsalmate.api.models.Booking;

import org.json.JSONArray;
import org.json.JSONException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BookingsAdapter extends RecyclerView.Adapter<BookingsAdapter.BookingViewHolder> {

    public interface OnBookingClickListener {
        void onDetailsClick(Booking booking);
    }

    private final List<Booking> bookings = new ArrayList<>();
    private final OnBookingClickListener listener;

    public BookingsAdapter(OnBookingClickListener listener) {
        this.listener = listener;
    }

    public void setBookings(List<Booking> items) {
        bookings.clear();
        if (items != null) {
            bookings.addAll(items);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_booking, parent, false);
        return new BookingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        Booking booking = bookings.get(position);
        String courtName = booking.getCourt() != null ? booking.getCourt().getCourtName() : "Court";
        String location = booking.getCourt() != null ? booking.getCourt().getLocation() : "";
        String price = booking.getCourt() != null && booking.getCourt().getPrice() != null ? booking.getCourt().getPrice() : "0";

        holder.tvCourtName.setText(courtName);
        holder.tvLocation.setText(location);
        holder.tvPrice.setText("Rs. " + price);
        holder.tvTime.setText(formatTime(booking.getDate(), booking.getStartTime(), booking.getEndTime()));

        bindStatus(holder.tvStatus, booking.getStatus(), booking.getPaymentStatus());
        loadImage(holder.ivCourt, booking.getCourt() != null ? booking.getCourt().getImage() : null);

        holder.btnDetails.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDetailsClick(booking);
            }
        });
    }

    @Override
    public int getItemCount() {
        return bookings.size();
    }

    static class BookingViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCourt;
        TextView tvCourtName;
        TextView tvLocation;
        TextView tvStatus;
        TextView tvTime;
        TextView tvPrice;
        View btnDetails;

        BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCourt = itemView.findViewById(R.id.ivBookingCourt);
            tvCourtName = itemView.findViewById(R.id.tvBookingCourtName);
            tvLocation = itemView.findViewById(R.id.tvBookingLocation);
            tvStatus = itemView.findViewById(R.id.tvBookingStatus);
            tvTime = itemView.findViewById(R.id.tvBookingTime);
            tvPrice = itemView.findViewById(R.id.tvBookingPrice);
            btnDetails = itemView.findViewById(R.id.btnBookingDetails);
        }
    }

    private void bindStatus(TextView tvStatus, String status, String paymentStatus) {
        if (tvStatus == null) return;
        String label = status != null ? status : "Pending";
        int bg = R.color.status_pending;
        int fg = R.color.action_yellow;
        if ("Confirmed".equalsIgnoreCase(status) || "Paid".equalsIgnoreCase(paymentStatus)) {
            label = "PAID";
            bg = R.color.status_paid;
            fg = R.color.bright_green;
        } else if ("Cancelled".equalsIgnoreCase(status) || "Rejected".equalsIgnoreCase(status)) {
            label = status.toUpperCase(Locale.getDefault());
            bg = R.color.status_cancelled;
            fg = R.color.text_grey;
        } else if ("Pending".equalsIgnoreCase(status)) {
            label = "PENDING";
            bg = R.color.status_pending;
            fg = R.color.action_yellow;
        }
        tvStatus.setText(label);
        tvStatus.setBackgroundResource(R.drawable.bg_otp_box_inactive);
        tvStatus.setBackgroundTintList(tvStatus.getContext().getColorStateList(bg));
        tvStatus.setTextColor(tvStatus.getContext().getColor(fg));
    }

    private String formatTime(String date, String startTime, String endTime) {
        String dateLabel = date;
        try {
            if (date != null) {
                LocalDate parsed = LocalDate.parse(date.trim());
                dateLabel = parsed.format(DateTimeFormatter.ofPattern("EEE, MMM dd", Locale.getDefault()));
            }
        } catch (Exception ignored) {
        }
        String start = formatLocalTime(startTime);
        String end = formatLocalTime(endTime);
        return dateLabel + " • " + start + " - " + end;
    }

    private String formatLocalTime(String time) {
        if (time == null || time.trim().isEmpty()) {
            return "";
        }
        String trimmed = time.trim();
        int dotIndex = trimmed.indexOf('.');
        if (dotIndex > 0) {
            trimmed = trimmed.substring(0, dotIndex);
        }
        DateTimeFormatter[] formatters = new DateTimeFormatter[] {
                DateTimeFormatter.ofPattern("HH:mm:ss"),
                DateTimeFormatter.ofPattern("HH:mm"),
                DateTimeFormatter.ofPattern("H:mm:ss"),
                DateTimeFormatter.ofPattern("H:mm")
        };
        for (DateTimeFormatter formatter : formatters) {
            try {
                LocalTime parsed = LocalTime.parse(trimmed, formatter);
                return parsed.format(DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault()));
            } catch (Exception ignored) {
            }
        }
        return trimmed;
    }

    private void loadImage(ImageView imageView, String image) {
        if (imageView == null) return;
        String url = resolveImageUrl(image);
        Glide.with(imageView.getContext())
                .load(url)
                .placeholder(R.drawable.ic_court_one)
                .error(R.drawable.ic_court_one)
                .centerCrop()
                .into(imageView);
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
