package com.example.futsalmate;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.button.MaterialButton;

import com.example.futsalmate.api.RetrofitClient;
import com.example.futsalmate.api.models.CourtDetail;
import com.example.futsalmate.api.models.CourtDetailResponse;
import com.example.futsalmate.adapters.CourtImagesPagerAdapter;
import com.example.futsalmate.adapters.FacilitiesAdapter;
import com.example.futsalmate.utils.TokenManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CourtDetailsActivity extends AppCompatActivity {

    private TokenManager tokenManager;
    private TextView tvCourtTitle;
    private TextView tvCourtLocation;
    private TextView tvCourtDescription;
    private TextView tvPriceValue;
    private TextView tvManagerName;
    private ViewPager2 vpCourtImages;
    private TextView tvPhotoCount;
    private String vendorPhone;
    private int selectedCourtId = -1;
    private String selectedCourtName;
    private String selectedCourtPrice;
    private String openingTime;
    private String closingTime;
    private RecyclerView rvFacilities;
    private TextView tvFacilitiesEmpty;
    private FacilitiesAdapter facilitiesAdapter;
    private CourtImagesPagerAdapter imagesPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_court_details);

        tokenManager = new TokenManager(this);
        tvCourtTitle = findViewById(R.id.tvCourtTitle);
        tvCourtLocation = findViewById(R.id.tvCourtLocation);
        tvCourtDescription = findViewById(R.id.tvCourtDescription);
        tvPriceValue = findViewById(R.id.tvPriceValue);
        tvManagerName = findViewById(R.id.tvManagerName);
        vpCourtImages = findViewById(R.id.vpCourtImages);
        tvPhotoCount = findViewById(R.id.tvPhotoCount);
        rvFacilities = findViewById(R.id.rvFacilities);
        tvFacilitiesEmpty = findViewById(R.id.tvFacilitiesEmpty);

        if (vpCourtImages != null) {
            imagesPagerAdapter = new CourtImagesPagerAdapter();
            vpCourtImages.setAdapter(imagesPagerAdapter);
            vpCourtImages.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    super.onPageSelected(position);
                    updatePhotoCount(position);
                }
            });
        }

        if (rvFacilities != null) {
            rvFacilities.setLayoutManager(new GridLayoutManager(this, 4));
            facilitiesAdapter = new FacilitiesAdapter();
            rvFacilities.setAdapter(facilitiesAdapter);
            rvFacilities.setNestedScrollingEnabled(false);
        }

        // Header Back button
        ImageButton btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // Book Now button logic
        MaterialButton btnBookNow = findViewById(R.id.btnBookNow);
        if (btnBookNow != null) {
            btnBookNow.setOnClickListener(v -> {
                // Navigate to SelectTimeslotActivity when Book Now is clicked
                Intent intent = new Intent(CourtDetailsActivity.this, SelectTimeslotActivity.class);
                if (selectedCourtId > 0) {
                    intent.putExtra("court_id", selectedCourtId);
                }
                if (selectedCourtName != null) {
                    intent.putExtra("court_name", selectedCourtName);
                }
                if (selectedCourtPrice != null) {
                    intent.putExtra("court_price", selectedCourtPrice);
                }
                if (openingTime != null) {
                    intent.putExtra("opening_time", openingTime);
                }
                if (closingTime != null) {
                    intent.putExtra("closing_time", closingTime);
                }
                startActivity(intent);
            });
        }

        MaterialButton btnContactVendor = findViewById(R.id.btnContactVendor);
        if (btnContactVendor != null) {
            btnContactVendor.setOnClickListener(v -> showVendorPhone());
        }

        int courtId = getIntent().getIntExtra("court_id", -1);
        String courtName = getIntent().getStringExtra("court_name");
        String courtLocation = getIntent().getStringExtra("court_location");
        String courtPrice = getIntent().getStringExtra("court_price");
        String courtImage = getIntent().getStringExtra("court_image");

        selectedCourtId = courtId;
        selectedCourtName = courtName;
        selectedCourtPrice = courtPrice;

        if (courtName != null && tvCourtTitle != null) {
            tvCourtTitle.setText(courtName);
        }
        if (courtLocation != null && tvCourtLocation != null) {
            tvCourtLocation.setText(courtLocation);
        }
        if (courtPrice != null && tvPriceValue != null) {
            tvPriceValue.setText("Rs." + courtPrice);
        }
        if (vpCourtImages != null && imagesPagerAdapter != null && courtImage != null) {
            imagesPagerAdapter.setImagesFromRaw(courtImage);
            updatePhotoCount(0);
        }

        if (courtId > 0) {
            loadCourtDetail(courtId);
        }
    }

    private void loadCourtDetail(int courtId) {
        String token = tokenManager.getAuthHeader();
        if (token == null) {
            Toast.makeText(this, "Please login again.", Toast.LENGTH_SHORT).show();
            return;
        }

        RetrofitClient.getInstance().getApiService().showCourtDetail(token, courtId)
                .enqueue(new Callback<CourtDetailResponse>() {
                    @Override
                    public void onResponse(Call<CourtDetailResponse> call, Response<CourtDetailResponse> response) {
                        if (!response.isSuccessful()) {
                            if (response.code() >= 500) {
                                loadCourtDetailPublic(courtId);
                                return;
                            }
                            Toast.makeText(CourtDetailsActivity.this, "Failed to load court (" + response.code() + ")", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (response.body() == null || response.body().getData() == null) {
                            String message = response.body() != null ? response.body().getMessage() : null;
                            Toast.makeText(CourtDetailsActivity.this, message != null ? message : "Failed to load court", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        bindCourtDetail(response.body().getData());
                    }

                    @Override
                    public void onFailure(Call<CourtDetailResponse> call, Throwable t) {
                        loadCourtDetailPublic(courtId);
                    }
                });
    }

    private void loadCourtDetailPublic(int courtId) {
        RetrofitClient.getInstance().getApiService().showCourtDetailPublic(courtId)
                .enqueue(new Callback<CourtDetailResponse>() {
                    @Override
                    public void onResponse(Call<CourtDetailResponse> call, Response<CourtDetailResponse> response) {
                        if (!response.isSuccessful() || response.body() == null || response.body().getData() == null) {
                            Toast.makeText(CourtDetailsActivity.this, "Failed to load court (" + response.code() + ")", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        bindCourtDetail(response.body().getData());
                    }

                    @Override
                    public void onFailure(Call<CourtDetailResponse> call, Throwable t) {
                        Toast.makeText(CourtDetailsActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void bindCourtDetail(CourtDetail detail) {
        if (detail == null) return;
        if (tvCourtTitle != null) {
            tvCourtTitle.setText(detail.getCourtName() != null ? detail.getCourtName() : "-");
        }
        if (tvCourtLocation != null) {
            tvCourtLocation.setText(detail.getLocation() != null ? detail.getLocation() : "-");
        }
        if (tvCourtDescription != null) {
            tvCourtDescription.setText(detail.getDescription() != null ? detail.getDescription() : "-");
        }
        String priceText = detail.getPrice() != null ? detail.getPrice() : "0";
        if (tvPriceValue != null) {
            tvPriceValue.setText("Rs." + priceText);
        }
        if (tvManagerName != null && detail.getVendor() != null && detail.getVendor().getName() != null) {
            tvManagerName.setText(detail.getVendor().getName());
        }
        if (detail.getVendor() != null) {
            vendorPhone = detail.getVendor().getPhone();
        }
        if (vpCourtImages != null && imagesPagerAdapter != null && detail.getImage() != null) {
            imagesPagerAdapter.setImagesFromRaw(detail.getImage());
            updatePhotoCount(0);
        }
        openingTime = detail.getOpeningTime();
        closingTime = detail.getClosingTime();
        updateFacilities(detail.getFacilities());
    }

    private void updateFacilities(List<String> facilities) {
        List<String> normalized = normalizeFacilities(facilities);
        if (facilitiesAdapter != null) {
            facilitiesAdapter.setFacilities(normalized);
        }
        boolean isEmpty = normalized == null || normalized.isEmpty();
        if (rvFacilities != null) {
            rvFacilities.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        }
        if (tvFacilitiesEmpty != null) {
            tvFacilitiesEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
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

    private void updatePhotoCount(int position) {
        if (tvPhotoCount == null || imagesPagerAdapter == null) return;
        int total = imagesPagerAdapter.getImageCount();
        if (total <= 0) {
            tvPhotoCount.setVisibility(View.GONE);
            return;
        }
        tvPhotoCount.setVisibility(View.VISIBLE);
        int current = position + 1;
        tvPhotoCount.setText(current + "/" + total + " PHOTOS");
    }

    private void showVendorPhone() {
        if (vendorPhone == null || vendorPhone.trim().isEmpty()) {
            Toast.makeText(this, "Phone number not available", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Clean phone number (remove spaces, dashes, etc.)
        String phoneNumber = vendorPhone.trim().replaceAll("[\\s\\-\\(\\)]", "");
        
        // Ensure phone number starts with tel: protocol
        if (!phoneNumber.startsWith("tel:")) {
            phoneNumber = "tel:" + phoneNumber;
        }
        
        // Open phone dialer
        Intent dialIntent = new Intent(Intent.ACTION_DIAL);
        dialIntent.setData(android.net.Uri.parse(phoneNumber));
        try {
            startActivity(dialIntent);
        } catch (android.content.ActivityNotFoundException e) {
            Toast.makeText(this, "Phone dialer not available", Toast.LENGTH_SHORT).show();
        }
    }

}
