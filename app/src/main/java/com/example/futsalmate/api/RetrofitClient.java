package com.example.futsalmate.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.concurrent.TimeUnit;

public class RetrofitClient {
    private static final String BASE_URL = "https://futsalmateapp.sameem.in.net/api/";

    private static RetrofitClient instance;
    private ApiService apiService;

    private RetrofitClient() {
        try {
            // Create logging interceptor
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            // Create OkHttpClient with timeout and logging
                OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(chain -> chain.proceed(
                        chain.request().newBuilder()
                            .header("Accept", "application/json")
                            .build()
                    ))
                    .addInterceptor(loggingInterceptor)
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build();

                Gson gson = new GsonBuilder()
                    .setLenient()
                    .create();

            // Create Retrofit instance
                Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();

            apiService = retrofit.create(ApiService.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static synchronized RetrofitClient getInstance() {
        if (instance == null) {
            instance = new RetrofitClient();
        }
        return instance;
    }

    public ApiService getApiService() {
        if (apiService == null) {
            throw new IllegalStateException("ApiService not initialized. Check RetrofitClient initialization.");
        }
        return apiService;
    }
}
