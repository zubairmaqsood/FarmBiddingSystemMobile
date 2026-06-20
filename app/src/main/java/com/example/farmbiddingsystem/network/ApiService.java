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
import retrofit2.http.Query;

public interface ApiService {

    // Example 1: Login API sending a Map of data or a custom object
    @FormUrlEncoded
    @POST("login.php")
    Call<Map<String, Object>> loginUser(@Field("email") String email,@Field("password") String password);

    @GET("myBid.php")
    Call<BidsResponse> getMyBids(@Header("Authorization") String token);

    @GET("homePage.php")
    Call<List<AuctionModel>> getHomePageAuctions();

}