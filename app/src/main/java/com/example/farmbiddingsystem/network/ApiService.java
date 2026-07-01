package com.example.farmbiddingsystem.network;

import com.example.farmbiddingsystem.models.AuctionDetailsResponse;
import com.example.farmbiddingsystem.models.AuctionModel;
import com.example.farmbiddingsystem.wrapperClasses.BidsResponse;
import com.example.farmbiddingsystem.wrapperClasses.GenericResponse;
import com.example.farmbiddingsystem.wrapperClasses.SignupResponse;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
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

    @FormUrlEncoded
    @POST("signup.php")
    Call<SignupResponse> registerUser(
            @Field("userType") String userType,
            @Field("fullName") String fullName,
            @Field("cnic") String cnic,
            @Field("email") String email,
            @Field("phone") String phone,
            @Field("password") String password,

            // Buyer Optional Fields
            @Field("buyerType") String buyerType,
            @Field("companyName") String companyName,
            @Field("companyAddress") String companyAddress,
            @Field("companyType") String companyType,

            // Farmer Optional Fields
            @Field("farmLocation") String farmLocation,
            @Field("farmSize") String farmSize,
            @Field("city") String city
    );


    @FormUrlEncoded
    @POST("createauction.php")
    Call<GenericResponse> createAuction(
            @Header("Authorization") String token,
            @Field("auc_title") String title,
            @Field("base_price") String price,
            @Field("auc_qty") String qty,
            @Field("auc_desc") String desc,
            @Field("end_time") String endTime,
            @Field("item_image") String base64Image
    );

    @GET("viewBid.php")
    Call<AuctionDetailsResponse> getAuctionDetails(
            @Query("id") int auctionId
    );

    @FormUrlEncoded
    @POST("auction_actions.php") // Ensure this matches your filename
    Call<GenericResponse> placeBid(
            @Header("Authorization") String token,
            @Field("action") String action,
            @Field("auc_id") int auctionId,
            @Field("bid_amount") String bidAmount
    );
}