package com.example.futsalmate.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.futsalmate.R;
import com.example.futsalmate.api.models.Booking;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PastBookingsTableAdapter extends RecyclerView.Adapter<PastBookingsTableAdapter.PastBookingViewHolder> {

    private final List<Booking> bookings = new ArrayList<>();

    public void setBookings(List<Booking> items) {
        bookings.clear();
        if (items != null) {
            bookings.addAll(items);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PastBookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_past_booking_row, parent, false);
        return new PastBookingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PastBookingViewHolder holder, int position) {
        Booking booking = bookings.get(position);
        String courtName = booking != null && booking.getCourt() != null ? booking.getCourt().getCourtName() : "-";
        String date = formatDate(booking != null ? booking.getDate() : null);
        String time = formatTimeRange(booking != null ? booking.getStartTime() : null, booking != null ? booking.getEndTime() : null);
        String status = booking != null && booking.getPaymentStatus() != null ? booking.getPaymentStatus() : "-";
        String method = booking != null && booking.getPayment() != null ? booking.getPayment() : "-";

        holder.tvSn.setText(String.valueOf(position + 1));
        holder.tvCourt.setText(courtName);
        holder.tvDate.setText(date);
        holder.tvTime.setText(time);
        holder.tvStatus.setText(status);
        holder.tvMethod.setText(method);
    }

    @Override
    public int getItemCount() {
        return bookings.size();
    }

    static class PastBookingViewHolder extends RecyclerView.ViewHolder {
        TextView tvSn;
        TextView tvCourt;
        TextView tvDate;
        TextView tvTime;
        TextView tvStatus;
        TextView tvMethod;

        PastBookingViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSn = itemView.findViewById(R.id.tvPastSn);
            tvCourt = itemView.findViewById(R.id.tvPastCourt);
            tvDate = itemView.findViewById(R.id.tvPastDate);
            tvTime = itemView.findViewById(R.id.tvPastTime);
            tvStatus = itemView.findViewById(R.id.tvPastStatus);
            tvMethod = itemView.findViewById(R.id.tvPastMethod);
        }
    }

    private String formatDate(String date) {
        if (date == null || date.trim().isEmpty()) return "-";
        try {
            LocalDate parsed = LocalDate.parse(date.trim());
            return parsed.format(DateTimeFormatter.ofPattern("MMM dd", Locale.getDefault()));
        } catch (Exception ignored) {
            return date;
        }
    }

    private String formatTimeRange(String start, String end) {
        String s = formatLocalTime(start);
        String e = formatLocalTime(end);
        if (s.isEmpty() && e.isEmpty()) return "-";
        return s + "-" + e;
    }

    private String formatLocalTime(String time) {
        if (time == null || time.trim().isEmpty()) return "";
        String trimmed = time.trim();
        int dotIndex = trimmed.indexOf('.');
        if (dotIndex > 0) {
            trimmed = trimmed.substring(0, dotIndex);
        }
        DateTimeFormatter[] formatters = new DateTimeFormatter[] {
            DateTimeFormatter.ofPattern("HH:mm:ss"),
            DateTimeFormatter.ofPattern("HH:mm"),
            DateTimeFormatter.ofPattern("H:mm:ss"),
            DateTimeFormatter.ofPattern("H:mm"),
            DateTimeFormatter.ofPattern("hh:mm a", Locale.getDefault()),
            DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())
        };
        for (DateTimeFormatter formatter : formatters) {
            try {
                LocalTime parsed = LocalTime.parse(trimmed, formatter);
            return parsed.format(DateTimeFormatter.ofPattern("hh:mm a", Locale.getDefault()));
            } catch (Exception ignored) {
            }
        }
        return trimmed;
    }
}
