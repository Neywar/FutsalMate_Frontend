package com.example.futsalmate;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.futsalmate.adapters.CourtsAdapter;
import com.example.futsalmate.api.RetrofitClient;
import com.example.futsalmate.api.models.Court;
import com.example.futsalmate.api.models.ShowCourtsResponse;
import com.example.futsalmate.utils.TokenManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CourtsActivity extends AppCompatActivity {

    private TokenManager tokenManager;
    private final List<Court> courts = new ArrayList<>();
    private CourtsAdapter adapter;
    private RecyclerView recyclerViewCourts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_courts);

        tokenManager = new TokenManager(this);

        recyclerViewCourts = findViewById(R.id.recyclerViewCourts);
        recyclerViewCourts.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CourtsAdapter(this::openCourtDetails);
        recyclerViewCourts.setAdapter(adapter);

        // Header Back button
        ImageButton btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        loadCourts();
    }

    private void loadCourts() {
        String token = tokenManager.getAuthHeader();
        if (token == null) {
            Toast.makeText(this, "Please login again.", Toast.LENGTH_SHORT).show();
            return;
        }

        RetrofitClient.getInstance().getApiService().showBookCourt(token)
                .enqueue(new Callback<ShowCourtsResponse>() {
                    @Override
                    public void onResponse(Call<ShowCourtsResponse> call, Response<ShowCourtsResponse> response) {
                        if (!response.isSuccessful() || response.body() == null || response.body().getCourts() == null) {
                            if (response.code() >= 500) {
                                loadCourtsPublic();
                                return;
                            }
                            Toast.makeText(CourtsActivity.this, "Failed to load courts (" + response.code() + ")", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        courts.clear();
                        courts.addAll(response.body().getCourts());
                        adapter.setCourts(courts);
                    }

                    @Override
                    public void onFailure(Call<ShowCourtsResponse> call, Throwable t) {
                        loadCourtsPublic();
                    }
                });
    }

    private void loadCourtsPublic() {
        RetrofitClient.getInstance().getApiService().showBookCourtPublic()
                .enqueue(new Callback<ShowCourtsResponse>() {
                    @Override
                    public void onResponse(Call<ShowCourtsResponse> call, Response<ShowCourtsResponse> response) {
                        if (!response.isSuccessful() || response.body() == null || response.body().getCourts() == null) {
                            Toast.makeText(CourtsActivity.this, "Failed to load courts (" + response.code() + ")", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        courts.clear();
                        courts.addAll(response.body().getCourts());
                        adapter.setCourts(courts);
                    }

                    @Override
                    public void onFailure(Call<ShowCourtsResponse> call, Throwable t) {
                        Toast.makeText(CourtsActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void openCourtDetails(Court court) {
        Intent intent = new Intent(CourtsActivity.this, CourtDetailsActivity.class);
        intent.putExtra("court_id", court.getId());
        intent.putExtra("court_name", court.getCourtName());
        intent.putExtra("court_location", court.getLocation());
        intent.putExtra("court_price", court.getPrice());
        intent.putExtra("court_image", court.getImage());
        startActivity(intent);
    }
}
