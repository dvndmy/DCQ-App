package com.dcq.quotesapp;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface UnsplashApi {
    @GET("/search/photos")
    Call<ApiResponse> searchPhotos(@Query("query") String query, @Query("client_id") String clientId);
}

