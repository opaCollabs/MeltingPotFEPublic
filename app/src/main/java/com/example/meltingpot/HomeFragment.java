package com.example.meltingpot;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeFragment extends Fragment implements FeedAdapter.OnRecipeClickListener {

    private RecyclerView recyclerView;
    private FeedAdapter feedAdapter;
    private RecipeViewModel recipeViewModel;
    private boolean isLoading = false;
    private final int PAGE_SIZE = 5;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        recipeViewModel = new ViewModelProvider(this).get(RecipeViewModel.class);
        feedAdapter = new FeedAdapter(recipeViewModel.getRecipeList(), this);
        recyclerView.setAdapter(feedAdapter);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (!isLoading && layoutManager != null && layoutManager.findLastCompletelyVisibleItemPosition() == recipeViewModel.getRecipeList().size() - 1) {
                    // Bottom of list
                    recipeViewModel.setCurrentPage(recipeViewModel.getCurrentPage() + 1);
                    fetchRecipesFromServer(recipeViewModel.getCurrentPage(), PAGE_SIZE);
                }
            }
        });

        if (recipeViewModel.getRecipeList().isEmpty()) {
            fetchRecipesFromServer(recipeViewModel.getCurrentPage(), PAGE_SIZE);
        }

        return view;
    }

    private void fetchRecipesFromServer(int page, int pageSize) {
        isLoading = true;
        String url = getResources().getString(R.string.server_uri) + "/api/recipes/feed?page=" + page + "&pageSize=" + pageSize;
        Log.d("Request URL", url);
        RequestQueue queue = Volley.newRequestQueue(getContext());

        SharedPreferences prefs = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String token = prefs.getString("token", null);

        if (token == null) {
            return;
        }

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.d("Server Response", response.toString());
                        try {
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject recipeObject = response.getJSONObject(i);
                                String id = recipeObject.getString("_id");
                                String userId = recipeObject.getString("userId");
                                String username = recipeObject.getString("userName");
                                String name = recipeObject.getString("name");
                                String userName = recipeObject.has("userName") ? recipeObject.getString("userName") : "Unknown";
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
                                recipeViewModel.getRecipeList().add(recipe);
                            }
                            feedAdapter.notifyDataSetChanged();
                            isLoading = false;
                        } catch (JSONException e) {
                            e.printStackTrace();
                            isLoading = false;
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        isLoading = false;
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

        queue.add(jsonArrayRequest);
    }

    @Override
    public void onRecipeClick(Recipe recipe) {
        ShowRecipeFragment fragment = ShowRecipeFragment.newInstance(recipe);
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.nav_host_fragment, fragment)
                .addToBackStack(null)
                .commit();
    }

    public boolean onBackPressed() {
        if (getActivity() != null) {
            getActivity().getSupportFragmentManager().popBackStack();
            return true;
        }
        return false;
    }
}




