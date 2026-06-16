package com.example.farmbiddingsystem.network;

import com.example.farmbiddingsystem.models.AuctionModel;
import com.example.farmbiddingsystem.wrapperClasses.BidsResponse;

import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface ApiService {

    // Example 1: Login API sending a Map of data or a custom object
    @FormUrlEncoded
    @POST("login.php")
    Call<Map<String, Object>> loginUser(@Field("email") String email,@Field("password") String password);

    // Example 2: Fetching live auctions list for your home page
    @GET("homePage.php")
    Call<List<Map<String, Object>>> getAuctions();

    @GET("myBid.php")
    Call<BidsResponse> getMyBids(@Header("Authorization") String token);

    @GET("homepage.php")
    Call<List<AuctionModel>> getHomePageAuctions();
}