package com.shariaty.translation.net.response;

import com.google.gson.annotations.SerializedName;

public class TranslateResponse {
    @SerializedName("response")
    public StatusCodeResponse statusCodeResponse;
    @SerializedName("data")
    public TranslateData translateData;
}
