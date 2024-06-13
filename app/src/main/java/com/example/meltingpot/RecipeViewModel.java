package com.example.meltingpot;

import androidx.lifecycle.ViewModel;
import java.util.ArrayList;
import java.util.List;

public class RecipeViewModel extends ViewModel {
    private List<Recipe> recipeList = new ArrayList<>();
    private int currentPage = 0;

    public List<Recipe> getRecipeList() {
        return recipeList;
    }

    public void setRecipeList(List<Recipe> recipeList) {
        this.recipeList = recipeList;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }
}

