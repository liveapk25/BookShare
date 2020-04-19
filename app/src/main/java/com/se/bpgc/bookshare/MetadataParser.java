package com.se.bpgc.bookshare;

import org.json.JSONException;
import org.json.JSONObject;

public class MetadataParser {

    JSONObject metadata;

    public MetadataParser(JSONObject metadata) {
        this.metadata = metadata;
    }

    public String getTitle(){
        try {
            return metadata.getJSONArray("items").getJSONObject(0).getJSONObject("volumeInfo").getString("title");
        }
        catch (JSONException e){
            return "";
        }
    }

    public String getAuthor(){
        try {
            return metadata.getJSONArray("items").getJSONObject(0).getJSONObject("volumeInfo").getJSONArray("authors").getString(0);
        }
        catch (JSONException e){
            return "";
        }
    }

    public String getCategory(){
        try {
            return metadata.getJSONArray("items").getJSONObject(0).getJSONObject("volumeInfo").getJSONArray("categories").getString(0);
        }
        catch (JSONException e){
            return "";
        }
    }

    public double getRating(){
        try {
            return metadata.getJSONArray("items").getJSONObject(0).getJSONObject("volumeInfo").getDouble("averageRating");
        }
        catch (JSONException e){
            return 0;
        }
    }

    public String getDescription(){
        try {
            return metadata.getJSONArray("items").getJSONObject(0).getJSONObject("volumeInfo").getString("description");
        }
        catch (JSONException e){
            return "-";
        }
    }

    public String getPublisher(){
        try {
            return metadata.getJSONArray("items").getJSONObject(0).getJSONObject("volumeInfo").getString("publisher");
        }
        catch (JSONException e){
            return "-";
        }
    }

    public String getRetailPrice(){
        try {
            return metadata.getJSONArray("items").getJSONObject(0).getJSONObject("volumeInfo").getJSONObject("saleInfo").getJSONObject("retailPrice").getString("amount");
        }
        catch (JSONException e){
            return "";
        }
    }

    public String getImageLink(){
        try {
            return metadata.getJSONArray("items").getJSONObject(0).getJSONObject("volumeInfo").getJSONObject("imageLinks").getString("thumbnail");
        }
        catch (JSONException e){
            return "";
        }
    }

    public boolean isValid(){
        try {
            int val = metadata.getInt("totalItems");
            if(val == 0)
                return false;
            else
                return true;
        }
        catch (JSONException e){
            return false;
        }
    }
}
