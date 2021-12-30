package com.shariaty.translation.net.response;

import com.google.gson.annotations.SerializedName;

public class StatusCodeResponse {
    @SerializedName("status")
    public boolean status;
    @SerializedName("code")
    public int statusCode;
}
