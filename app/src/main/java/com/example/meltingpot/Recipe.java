package com.example.meltingpot;


import android.os.Parcel;
import android.os.Parcelable;

public class Recipe implements Parcelable {

    private String _id;
    private String userId;
    private String objectId;
    private String name;
    private String userName;


    private String imageUrl;
    private String ingredients;
    private String instructions;

    private String categories;
    private String description;


	public Recipe(String _id, String userId, String name, String userName, String imageUrl, String ingredients, String instructions,String categories, String description) {
        this._id = _id;
        this.userId = userId;
		this.name = name;
		this.userName = userName;
		this.imageUrl = imageUrl;
		this.ingredients = ingredients;
		this.instructions = instructions;
        this.categories=categories;
        this.description=description;
    }

    public Recipe(String objectId, String name, String userName, String imageUrl, String ingredients, String instructions) {
        this.objectId = objectId;
        this.name = name;
        this.userName = userName;
        this.imageUrl = imageUrl;
        this.ingredients = ingredients;
        this.instructions = instructions;
    }






    protected Recipe(Parcel in) {
        name = in.readString();
        userName = in.readString();
        imageUrl = in.readString();
        ingredients = in.readString();
        instructions = in.readString();
    }

    public static final Creator<Recipe> CREATOR = new Creator<Recipe>() {
        @Override
        public Recipe createFromParcel(Parcel in) {
            return new Recipe(in);
        }

        @Override
        public Recipe[] newArray(int size) {
            return new Recipe[size];
        }
    };

    public String getId() {
        return _id;
    }
    public String getUserId() {
        return userId;
    }
    public String getName() {
        return name;
    }


    public String getImageUrl() {
        return imageUrl;
    }
    public String getUserName() {
        return userName;
    }


    public String getIngredients() {
        return ingredients;
    }

    public String getInstructions() {
        return instructions;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(userName);
        dest.writeString(imageUrl);
        dest.writeString(ingredients);
        dest.writeString(instructions);
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public String getCategories(){
        return categories;
    }

    public String getDescription(){
        return description;
    }
}


