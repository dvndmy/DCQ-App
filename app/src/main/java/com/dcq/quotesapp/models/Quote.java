package com.dcq.quotesapp.models;

import com.google.firebase.database.Exclude;

import java.io.Serializable;

public class Quote implements Serializable {

    // Use meaningful variable names and reorder for better readability
    public String id;
    public String quote;
    public String author;
    public String userAdded;
    public String quote_category;

    // Exclude the 'id' field from Firebase database operations
    @Exclude
    public boolean isFavourite = false;

    // Default constructor required for Firebase
    public Quote() {
    }

    // Constructor using String parameters
    public Quote(String author, String id, String quote, String userAdded, String quote_category) {
        this.id = id;
        this.quote = quote;
        this.author = author;
        this.userAdded = userAdded + "";
        this.quote_category = quote_category;
    }

    // Constructor using Long parameters, converting to String where needed
    public Quote(String author, Long id, String quote, Long userAdded, Long quote_category) {
        this.id = id.toString();
        this.quote = quote;
        this.author = author;
        this.userAdded = userAdded + "";
        this.quote_category = quote_category.toString();
    }

    // Getter and Setter methods for each field

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getQuote() {
        return quote;
    }

    public void setQuote(String quote) {
        this.quote = quote;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getUserAdded() {
        return userAdded;
    }

    public void setUserAdded(String userAdded) {
        this.userAdded = userAdded;
    }

    public String getQuote_category() {
        return quote_category;
    }

    public void setQuote_category(String quote_category) {
        this.quote_category = quote_category;
    }
}
