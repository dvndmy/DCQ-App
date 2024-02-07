package com.dcq.quotesapp;

import com.google.gson.annotations.SerializedName;

public class Result {
    @SerializedName("urls")
    private Urls urls;

    public Urls getUrls() {
        return urls;
    }
}
