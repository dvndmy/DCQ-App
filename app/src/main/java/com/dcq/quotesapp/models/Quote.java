package com.dcq.quotesapp.models;

import com.google.firebase.database.Exclude;

import java.io.Serializable;


public class Quote implements Serializable {

    @Exclude
    public String no;

    public String quote, person, url;

    @Exclude
    public String quote_category;

    @Exclude
    public boolean isFavourite = false;

    public Quote() {
    }

    public Quote(String no, String quote, String person, String url, String quote_category) {
        this.no = no;
        this.quote = quote;
        this.person = person;
        this.url = url + "";
        this.quote_category = quote_category;
    }

    public String getNo() {
        return no;
    }

    public void setNo(String no) {
        this.no = no;
    }

    public String getQuote() {
        return quote;
    }

    public void setQuote(String quote) {
        this.quote = quote;
    }

    public String getPerson() {
        return person;
    }

    public void setPerson(String person) {
        this.person = person;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getQuote_category() {
        return quote_category;
    }

    public void setQuote_category(String quote_category) {
        this.quote_category = quote_category;
    }
}
