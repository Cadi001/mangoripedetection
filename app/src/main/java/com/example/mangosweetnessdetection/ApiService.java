package com.example.mangosweetnessdetection;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {
    @Headers("Content-Type: application/x-www-form-urlencoded")
    @POST("/mangoripenessdetection/2")
    Call<ResponseBody> detectMangoRipeness(
            @Query("api_key") String apiKey,
            @Body RequestBody image
    );
}
