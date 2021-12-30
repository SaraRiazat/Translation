package com.shariaty.translation.net.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class TranslateData {
    @SerializedName("results")
    public List<TranslateDataResult> translateDataResults;
}
