package com.se.bpgc.bookshare;

import org.json.JSONObject;

public class BookMetadataModel{
    String isbn ="";
    String title ="";
    String author ="";
    String thumbnail ="";
    String category ="";
    String description="";
    double averageRating=0.0;
    JSONObject metadata;

    public BookMetadataModel(){

    }


    public BookMetadataModel(String isbn, String title,String author, String thumbnail, String category, String description, double averageRating) {
        this.isbn = isbn;
        this.author = author;
        this.thumbnail = thumbnail;
        this.category = category;
        this.description = description;
        this.averageRating = averageRating;
    }

    public BookMetadataModel(String isbn, String title,String author, String thumbnail, String category, String description, double averageRating, JSONObject metadata) {
        this.isbn = isbn;
        this.author = author;
        this.thumbnail = thumbnail;
        this.category = category;
        this.description = description;
        this.averageRating = averageRating;
        this.metadata = metadata;
    }

    public String getIsbn() {
        return isbn;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public String getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public double getAverageRating() {
        return averageRating;
    }

}