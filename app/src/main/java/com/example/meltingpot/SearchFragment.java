package com.example.meltingpot;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchFragment extends Fragment implements ExploreAdapter.OnRecipeClickListener {

    private RecyclerView recyclerView;
    private ExploreAdapter exploreAdapter;
    private List<Recipe> recipeList;
    private EditText searchEditText;
    private List<String> selectedCategories = new ArrayList<>();
    private Button btnGreek, btnVegan, btnFrench, btnItalian, btnFusion, btnAsian, btnMexican, btnAmerican, btnIndian, btnMediterranean, btnMiddleEastern, btnDesserts ,searchButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));

        recipeList = new ArrayList<>();
        exploreAdapter = new ExploreAdapter(recipeList, this);
        recyclerView.setAdapter(exploreAdapter);


        searchButton = view.findViewById(R.id.search_button);
        searchEditText = view.findViewById(R.id.etSearch);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performSearch();
            }
        });

        btnGreek = view.findViewById(R.id.btnGreek);
        btnVegan = view.findViewById(R.id.btnVegan);
        btnFrench = view.findViewById(R.id.btnFrench);
        btnItalian = view.findViewById(R.id.btnItalian);
        btnFusion = view.findViewById(R.id.btnFusion);
        btnAsian = view.findViewById(R.id.btnAsian);
        btnMexican = view.findViewById(R.id.btnMexican);
        btnAmerican = view.findViewById(R.id.btnAmerican);
        btnIndian = view.findViewById(R.id.btnIndian);
        btnMediterranean = view.findViewById(R.id.btnMediterranean);
        btnMiddleEastern = view.findViewById(R.id.btnMiddleEastern);
        btnDesserts = view.findViewById(R.id.btnDesserts);


        setCategoryButtonListener(btnGreek, "Greek");
        setCategoryButtonListener(btnVegan, "Vegan");
        setCategoryButtonListener(btnFrench, "French");
        setCategoryButtonListener(btnItalian, "Italian");
        setCategoryButtonListener(btnFusion, "Fusion");
        setCategoryButtonListener(btnAsian, "Asian");
        setCategoryButtonListener(btnMexican, "Mexican");
        setCategoryButtonListener(btnAmerican, "American");
        setCategoryButtonListener(btnIndian, "Indian");
        setCategoryButtonListener(btnMediterranean, "Mediterranean");
        setCategoryButtonListener(btnMiddleEastern, "Middle Eastern");
        setCategoryButtonListener(btnDesserts, "Desserts");



        fetchRecipesFromServer("", new String[0]);

        return view;
    }

    private void setCategoryButtonListener(Button button, String categoryName) {
        button.setOnClickListener(v -> {
            if (selectedCategories.contains(categoryName)) {
                selectedCategories.remove(categoryName);
                button.setBackgroundResource(R.drawable.unselected_category_btn);
                button.setTextColor(ContextCompat.getColor(this.getContext(), R.color.approx_gray));
            } else {
                selectedCategories.add(categoryName);
                button.setBackgroundResource(R.drawable.button_add_recipe);
                button.setTextColor(ContextCompat.getColor(this.getContext(), R.color.white));
            }
            logSelectedCategories();
        });
    }

    private void logSelectedCategories() {
        Log.d("SelectedCategories", selectedCategories.toString());
    }

    private void fetchRecipesFromServer() {
        String url = getResources().getString(R.string.server_uri)+"/api/recipes";
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
                        try {
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject recipeObject = response.getJSONObject(i);
                                String id = recipeObject.getString("_id");
                                String userId = recipeObject.getString("userId");
                                String username=recipeObject.getString("userName");
                                String name = recipeObject.getString("name");
                                String userName = recipeObject.has("userName") ? recipeObject.getString("userName") : "Unknown";
                                String description =recipeObject.getString("description");
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

                                Recipe recipe = new Recipe(id, userId, name, username, imageUrl, ingredientsBuilder.toString().trim(), instructions, categoriesBuilder.toString().trim() ,description);
                                recipeList.add(recipe);
                            }
                            exploreAdapter.notifyDataSetChanged();
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

        queue.add(jsonArrayRequest);
    }

    private void fetchRecipesFromServer(String searchTerm, String[] categories) {
        String url  = getResources().getString(R.string.server_uri) + "/api/recipes/search";
        RequestQueue queue = Volley.newRequestQueue(getContext());

        JSONArray query = constructQuery(searchTerm, categories);
        Log.d("Query:",query.toString());

        SharedPreferences prefs = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String token = prefs.getString("token", null);

        if (token == null) {
            return;
        }

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.POST, url, query,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject recipeObject = response.getJSONObject(i);
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

                                Recipe recipe = new Recipe(id, userId, name, username, imageUrl, ingredientsBuilder.toString().trim(), instructions, categoriesBuilder.toString().trim() ,description);
                                recipeList.add(recipe);
                            }
                            exploreAdapter.notifyDataSetChanged();
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

        queue.add(jsonArrayRequest);
    }

    private void performSearch() {
        String searchTerm = searchEditText.getText().toString().trim();
        recipeList.clear();
        fetchRecipesFromServer(searchTerm, selectedCategories.toArray(new String[0]));
    }

    @Override
    public void onRecipeClick(Recipe recipe) {
        ShowRecipeFragment fragment = ShowRecipeFragment.newInstance(recipe);
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.nav_host_fragment, fragment)
                .addToBackStack(null)
                .commit();
    }

    private JSONArray constructQuery(String searchTerm, String[] categories) {
        JSONArray query = new JSONArray();

        if (searchTerm != null && !searchTerm.isEmpty()) {
            JSONObject nameRegex = new JSONObject();
            try {
                nameRegex.put("$regex", searchTerm);
                nameRegex.put("$options", "i");

                JSONObject nameCriterion = new JSONObject();
                nameCriterion.put("name", nameRegex);
                query.put(nameCriterion);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        if (categories != null && categories.length > 0) {
            try {
                JSONObject categoriesCriterion = new JSONObject();
                categoriesCriterion.put("categories", new JSONArray(categories));
                query.put(categoriesCriterion);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return query;
    }
}





