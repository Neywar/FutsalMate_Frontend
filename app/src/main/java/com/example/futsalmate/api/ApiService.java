package com.example.futsalmate.api;

import com.example.futsalmate.api.models.AddCourtRequest;
import com.example.futsalmate.api.models.AvailableCourtsResponse;
import com.example.futsalmate.api.models.ApiResponse;
import com.example.futsalmate.api.models.BookCourtRequest;
import com.example.futsalmate.api.models.BookCourtResponse;
import com.example.futsalmate.api.models.BookedTimesResponse;
import com.example.futsalmate.api.models.CommunityTeam;
import com.example.futsalmate.api.models.ManualBookingRequest;
import com.example.futsalmate.api.models.RegisterTeamRequest;
import com.example.futsalmate.api.models.ShowTeamsResponse;
import com.example.futsalmate.api.models.VendorBookingsData;
import com.example.futsalmate.api.models.Vendor;
import com.example.futsalmate.api.models.Court;
import com.example.futsalmate.api.models.CourtsResponse;
import com.example.futsalmate.api.models.CourtDetail;
import com.example.futsalmate.api.models.CourtDetailResponse;
import com.example.futsalmate.api.models.EditBookingRequest;
import com.example.futsalmate.api.models.EditProfileRequest;
import com.example.futsalmate.api.models.PastBookingsResponse;
import com.example.futsalmate.api.models.ShowCourtsResponse;
import com.example.futsalmate.api.models.UpcomingBookingsResponse;
import com.example.futsalmate.api.models.UserDashboardResponse;
import com.example.futsalmate.api.models.VendorCustomersResponse;
import com.example.futsalmate.api.models.LoginRequest;
import com.example.futsalmate.api.models.OtpVerifyRequest;
import com.example.futsalmate.api.models.ResendOtpRequest;
import com.example.futsalmate.api.models.SignupRequest;
import com.example.futsalmate.api.models.User;
import com.example.futsalmate.api.models.VendorDashboardResponse;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Query;
import com.example.futsalmate.api.models.ViewBookingResponse;

public interface ApiService {
    
    @POST("signup")
    Call<ApiResponse<User>> signup(@Body SignupRequest request);
    
    @POST("login")
    Call<ApiResponse<User>> login(@Body LoginRequest request);

    @POST("vendor/login")
        Call<ApiResponse<Vendor>> vendorLogin(@Body LoginRequest request);
    
    @POST("logout")
    Call<ApiResponse<Void>> logout(@Header("Authorization") String token);

    @GET("user-dashboard")
    Call<UserDashboardResponse> userDashboard(
            @Header("Authorization") String token
    );

    @PUT("profile")
    Call<ApiResponse<User>> editProfile(
            @Header("Authorization") String token,
            @Body EditProfileRequest request
    );
    
    @POST("email/verify/otp")
    Call<ApiResponse<Void>> verifyOtp(@Body OtpVerifyRequest request);
    
    @POST("email/verify/resend-otp")
    Call<ApiResponse<Void>> resendOtp(@Body ResendOtpRequest request);
    
    @Multipart
    @POST("vendor/add-courts")
    Call<ApiResponse<Court>> addCourt(
            @Header("Authorization") String token,
            @Part("court_name") RequestBody courtName,
            @Part("location") RequestBody location,
            @Part("price") RequestBody price,
            @Part("description") RequestBody description,
            @Part("status") RequestBody status,
            @Part("facilities[]") RequestBody facilities,
            @Part("opening_time") RequestBody openingTime,
            @Part("closing_time") RequestBody closingTime,
            @Part("latitude") RequestBody latitude,
            @Part("longitude") RequestBody longitude,
            @Part MultipartBody.Part[] images
    );
    
    @GET("vendor/view-courts")
    Call<ApiResponse<CourtsResponse>> viewVendorCourts(
            @Header("Authorization") String token
    );

        @GET("vendor/vendor-dashboard")
        Call<ApiResponse<VendorDashboardResponse>> vendorDashboard(
            @Header("Authorization") String token
        );

    @Multipart
    @POST("vendor/edit-courts/{id}")
    Call<ApiResponse<Court>> updateCourt(
            @Header("Authorization") String token,
            @retrofit2.http.Path("id") int courtId,
            @Part("court_name") RequestBody courtName,
            @Part("location") RequestBody location,
            @Part("price") RequestBody price,
            @Part("description") RequestBody description,
            @Part("status") RequestBody status,
            @Part("facilities[]") RequestBody facilities,
            @Part("opening_time") RequestBody openingTime,
            @Part("closing_time") RequestBody closingTime,
            @Part("latitude") RequestBody latitude,
            @Part("longitude") RequestBody longitude,
            @Part MultipartBody.Part[] images
    );
    
    @retrofit2.http.DELETE("vendor/delete-courts/{id}")
    Call<ApiResponse<Void>> deleteCourt(
            @Header("Authorization") String token,
            @retrofit2.http.Path("id") int courtId
    );
    
    @POST("vendor/manual-booking")
    Call<ApiResponse<Void>> manualBookCourt(
            @Header("Authorization") String token,
            @Body ManualBookingRequest request
    );

    @POST("book")
    Call<BookCourtResponse> bookCourt(
            @Header("Authorization") String token,
            @Body BookCourtRequest request
    );

    @PUT("edit-booking/{id}")
    Call<ViewBookingResponse> editBooking(
            @Header("Authorization") String token,
            @retrofit2.http.Path("id") int bookingId,
            @Body EditBookingRequest request
    );

    @retrofit2.http.DELETE("cancel-booking/{id}")
    Call<ViewBookingResponse> cancelBooking(
            @Header("Authorization") String token,
            @retrofit2.http.Path("id") int bookingId
    );

    @GET("book/booked-times")
    Call<BookedTimesResponse> getBookedTimes(
            @Header("Authorization") String token,
            @Query("court_id") int courtId,
            @Query("date") String date
    );

    @GET("vendor/bookings")
    Call<ApiResponse<VendorBookingsData>> vendorCourtBookings(
            @Header("Authorization") String token
    );

    @GET("vendor/view-customers")
    Call<ApiResponse<VendorCustomersResponse>> viewVendorCustomers(
            @Header("Authorization") String token
    );

    @GET("show-court")
    Call<ShowCourtsResponse> showBookCourt(
            @Header("Authorization") String token
    );

    @GET("show-court")
    Call<ShowCourtsResponse> showBookCourtPublic();

    @GET("court-detail/{courtId}")
        Call<CourtDetailResponse> showCourtDetail(
            @Header("Authorization") String token,
            @retrofit2.http.Path("courtId") int courtId
    );

    @GET("court-detail/{courtId}")
    Call<CourtDetailResponse> showCourtDetailPublic(
            @retrofit2.http.Path("courtId") int courtId
    );

    @GET("book/user-bookings/{id}")
    Call<ViewBookingResponse> viewBooking(
            @Header("Authorization") String token,
            @retrofit2.http.Path("id") int bookingId
    );

    @GET("book/booking-detail/{id}")
    Call<ViewBookingResponse> viewBookingDetail(
            @Header("Authorization") String token,
            @retrofit2.http.Path("id") int bookingId
    );

    @GET("book/upcoming-bookings")
    Call<UpcomingBookingsResponse> upcomingBookings(
            @Header("Authorization") String token
    );

    @GET("book/past-bookings")
    Call<PastBookingsResponse> pastBookings(
            @Header("Authorization") String token
    );

    @POST("community/register-team")
    Call<ApiResponse<CommunityTeam>> registerTeam(
            @Header("Authorization") String token,
            @Body RegisterTeamRequest request
    );

    @GET("community/user-communities")
    Call<ShowTeamsResponse> showTeams(
            @Header("Authorization") String token
    );

    @GET("community/available-courts")
    Call<AvailableCourtsResponse> availableCourts(
            @Header("Authorization") String token
    );
}
