package com.example.meltingpot;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;

public class Diet implements Parcelable {

    private String dietId;
    private String userId;
    private String userName;
    private String name;
    private JSONArray recipes;
    private JSONArray visibleRecipes;
    private double price;
    private String description;
    private String info;

    public Diet(String dietId, String userId, String userName, String name, JSONArray recipes, JSONArray visibleRecipes, double price, String description, String info) {
        this.dietId = dietId;
        this.userId = userId;
        this.userName = userName;
        this.name = name;
        this.recipes = recipes != null ? recipes : new JSONArray();
        this.visibleRecipes = visibleRecipes != null ? visibleRecipes : new JSONArray();
        this.price = price;
        this.description = description;
        this.info = info;
    }

    protected Diet(Parcel in) {
        dietId = in.readString();
        userId = in.readString();
        userName = in.readString();
        name = in.readString();
        try {
            recipes = new JSONArray(in.readString());
            visibleRecipes = new JSONArray(in.readString());
        } catch (JSONException e) {
            recipes = new JSONArray();
            visibleRecipes = new JSONArray();
        }
        price = in.readDouble();
        description = in.readString();
        info = in.readString();
    }

    public static final Creator<Diet> CREATOR = new Creator<Diet>() {
        @Override
        public Diet createFromParcel(Parcel in) {
            return new Diet(in);
        }

        @Override
        public Diet[] newArray(int size) {
            return new Diet[size];
        }
    };

    public String getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public String getName() {
        return name;
    }

    public JSONArray getRecipesJSON() {
        return recipes;
    }

    public JSONArray getVisibilityJSON() {
        return visibleRecipes;
    }

    public double getPrice() {
        return price;
    }

    public String getDescription() {
        return description;
    }

    public String getInfo() {
        return info;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(userId);
        dest.writeString(userName);
        dest.writeString(name);
        dest.writeString(recipes.toString());
        dest.writeString(visibleRecipes.toString());
        dest.writeDouble(price);
        dest.writeString(description);
        dest.writeString(info);
    }

    public String getDietId() {
        return dietId;
    }

    public void setDietId(String dietId) {
        this.dietId = dietId;
    }
}
