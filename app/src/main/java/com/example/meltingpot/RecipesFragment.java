package com.example.meltingpot;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecipesFragment extends Fragment implements RecipeAdapter.OnRecipeClickListener {

    private RecipeAdapter adapter;
    private List<Recipe> recipeList;
    private List<Recipe> filteredRecipeList;
    private AppCompatButton fab;
    private SearchView searchView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recipes, container, false);

        GridView gridView = view.findViewById(R.id.gvRecipes);
        searchView = view.findViewById(R.id.searchView);

        recipeList = new ArrayList<>();
        filteredRecipeList = new ArrayList<>();
        adapter = new RecipeAdapter(getContext(), filteredRecipeList, this);
        gridView.setAdapter(adapter);

        fetchUserRecipesFromServer();

        fab = getActivity().findViewById(R.id.fab);
        fab.setVisibility(View.VISIBLE);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fab.setVisibility(View.GONE);
                getFragmentManager().beginTransaction()
                        .replace(R.id.nav_host_fragment, new AddRecipeFragment())
                        .addToBackStack(null)
                        .commit();
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterRecipes(newText);
                return true;
            }
        });

        return view;
    }

    private void fetchUserRecipesFromServer() {
        String url = getResources().getString(R.string.server_uri) + "/api/recipes/user/recipes";
        RequestQueue queue = Volley.newRequestQueue(getContext());

        SharedPreferences prefs = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String token = prefs.getString("token", null);

        if (token == null) {
            return;
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray uploadedRecipes = response.getJSONArray("uploadedRecipes");
                            JSONArray savedRecipes = response.getJSONArray("savedRecipes");

                            for (int i = 0; i < uploadedRecipes.length(); i++) {
                                JSONObject recipeObject = uploadedRecipes.getJSONObject(i);
                                addRecipeToList(recipeObject);
                            }

                            for (int i = 0; i < savedRecipes.length(); i++) {
                                JSONObject recipeObject = savedRecipes.getJSONObject(i);
                                addRecipeToList(recipeObject);
                            }

                            adapter.notifyDataSetChanged();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("x-auth-token", token);
                return headers;
            }
        };

        queue.add(jsonObjectRequest);
    }

    private void addRecipeToList(JSONObject recipeObject) throws JSONException {
        String id = recipeObject.getString("_id");
        String userId = recipeObject.getString("userId");
        String username = recipeObject.getString("userName");
        String name = recipeObject.getString("name");
        String description = recipeObject.getString("description");
        String instructions = recipeObject.getString("instructions");
        String imageUrl = recipeObject.getString("imageUrl");

        JSONArray categoriesArray = recipeObject.getJSONArray("categories");
        StringBuilder categoriesBuilder = new StringBuilder();
        for (int j = 0; j < categoriesArray.length(); j++) {
            String category = categoriesArray.getString(j);
            categoriesBuilder.append(category);
            if (j < categoriesArray.length() - 1) {
                categoriesBuilder.append(", ");
            }
        }

        JSONArray ingredientsArray = recipeObject.getJSONArray("ingredients");
        StringBuilder ingredientsBuilder = new StringBuilder();
        for (int j = 0; j < ingredientsArray.length(); j++) {
            JSONObject ingredientObject = ingredientsArray.getJSONObject(j);
            String ingredientName = ingredientObject.getString("name");
            String quantity = ingredientObject.getString("quantity");
            ingredientsBuilder.append(ingredientName).append(": ").append(quantity).append("\n");
        }

        Recipe recipe = new Recipe(id, userId, name, username, imageUrl, ingredientsBuilder.toString().trim(), instructions, categoriesBuilder.toString().trim(), description);
        recipeList.add(recipe);
        filteredRecipeList.add(recipe);
    }

    private void filterRecipes(String query) {
        filteredRecipeList.clear();
        for (Recipe recipe : recipeList) {
            if (recipe.getName().toLowerCase().contains(query.toLowerCase())) {
                filteredRecipeList.add(recipe);
            }
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onRecipeClick(Recipe recipe) {
        ShowRecipeFragment fragment = ShowRecipeFragment.newInstance(recipe);
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.nav_host_fragment, fragment)
                .addToBackStack(null)
                .commit();
    }
}



