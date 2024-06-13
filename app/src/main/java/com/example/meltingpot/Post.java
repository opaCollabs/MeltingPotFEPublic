package com.example.meltingpot;

public class Post {
    private int recipeImage;
    private String userName;
    private int userImage;

    public Post(int recipeImage, String userName, int userImage) {
        this.recipeImage = recipeImage;
        this.userName = userName;
        this.userImage = userImage;
    }

    public int getRecipeImage() {
        return recipeImage;
    }

    public String getUserName() {
        return userName;
    }

    public int getUserImage() {
        return userImage;
    }
}
