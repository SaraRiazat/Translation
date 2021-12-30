package com.shariaty.translation.net;

import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import com.shariaty.translation.net.WebService;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitBuilder {
    public static WebService webService = null;

    private RetrofitBuilder() {
    }

    public static WebService getInstance() {
        if (webService == null) {
            webService = new Retrofit.Builder()
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl("http://api.vajehyab.com/v3/")
                    .build()
                    .create(WebService.class);
        }
        return webService;
    }
}
