package com.shariaty.translation.net;

import com.shariaty.translation.net.response.TranslateResponse;

import io.reactivex.Single;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WebService {
    @GET("search")
    Single<TranslateResponse> translate(
            @Query("token") String token,
            @Query("q") String query,
            @Query("num") Integer number,
            @Query("type") String type,
            @Query("filter") String filter
    );
}
