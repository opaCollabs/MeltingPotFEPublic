package com.example.meltingpot;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ViewDietFragment extends Fragment {
    private static final String TAG = "ViewDietFragment";
    private static final String ARG_DIET = "diet_arg";

    private String dietTitle;
    private double price;
    private String description;
    private String addInfo;
    private JSONArray recipesJSON;
    private JSONArray visibilityJSON;
    private ArrayList<Boolean>[][] visibles; // Visibility array
    private ArrayList<TextView>[][] recipes; // Meals array, text contains the title and tag contains the object id
    private List<Recipe> recipeObjesctList;
    private View rootView;
    private boolean isPurchased = false;
    private RequestQueue requestQueue;
    private Diet diet;

    public static ViewDietFragment newInstance(Diet diet) {
        ViewDietFragment fragment = new ViewDietFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_DIET, diet);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_diet, container, false);
        rootView = view;
        recipeObjesctList = new ArrayList<>();

        AppCompatButton fab = getActivity().findViewById(R.id.fab);
        fab.setVisibility(View.GONE);

        AppCompatImageButton btnBack = view.findViewById(R.id.btn_back);
        AppCompatImageButton btnEdit = view.findViewById(R.id.btn_edit);
        btnEdit.setVisibility(View.INVISIBLE);

        TextView tvTitle = view.findViewById(R.id.tv_title);
        TextView tvDescription = view.findViewById(R.id.tv_diet_description);
        TextView tvAddInfo = view.findViewById(R.id.tv_diet_add_info);

        AppCompatButton btnUnlock = view.findViewById(R.id.btn_unlock_plan);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStack();
            }
        });

        if (getArguments() != null) {
            diet = getArguments().getParcelable(ARG_DIET);
            if (diet != null) {
                dietTitle = diet.getName();
                price = (long) (diet.getPrice() * 100) / 100.0;
                description = diet.getDescription();
                addInfo = diet.getInfo();
                recipesJSON = diet.getRecipesJSON();
                visibilityJSON = diet.getVisibilityJSON();

                btnUnlock.setText("Unlock for $" + price);

                visibles = new ArrayList[7][3];
                recipes = new ArrayList[7][3];
                for (int i = 0; i < 7; i++) {
                    for (int j = 0; j < 3; j++) {
                        recipes[i][j] = new ArrayList<>();
                        visibles[i][j] = new ArrayList<>();
                    }
                }

                try {
                    for (int i = 0; i < recipesJSON.length(); i++) {
                        JSONArray dayRecipes = recipesJSON.getJSONArray(i);
                        JSONArray dayVisible = visibilityJSON.getJSONArray(i);

                        for (int j = 0; j < dayRecipes.length(); j++) {
                            JSONArray mealRecipes = dayRecipes.getJSONArray(j);
                            JSONArray mealVisible = dayVisible.getJSONArray(j);

                            for (int k = 0; k < mealRecipes.length(); k++) {
                                String recipe = mealRecipes.getString(k);
                                boolean visible = mealVisible.getBoolean(k);

                                TextView recipeTextView = new TextView(getContext());
                                recipeTextView.setText(recipe);

                                recipes[i][j].add(recipeTextView);

                                visibles[i][j].add(visible);
                            }
                        }
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Error formatting recipes", e);
                }

                tvTitle.setText(dietTitle);
                tvDescription.setText(description);

                checkIfUserPurchased(diet.getDietId(), view);

                requestQueue = Volley.newRequestQueue(requireContext());

                setupExpandableFields(view);
            }
        }

        btnUnlock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openPayPal();
                isPurchased = true;
                patchDietPurchase(view);
                updateVisibility(true);
                refreshUI();
            }
        });

        return view;
    }

    private void openPayPal() {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.paypal.com"));
        startActivity(browserIntent);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isPurchased) {
            updateVisibility(true);
            refreshUI();
        }
    }

    private void updateVisibility(boolean isVisible) {
        if (visibles != null) {
            for (int i = 0; i < 7; i++) {
                for (int j = 0; j < 3; j++) {
                    for (int k = 0; k < visibles[i][j].size(); k++) {
                        visibles[i][j].set(k, isVisible);
                    }
                }
            }
        }
    }

    private void refreshUI() {
        if (rootView != null) {
            setupExpandableFields(rootView);
        }
    }

    private void setupExpandableFields(View view) {
        int[] dayIds = new int[]{
                R.id.day1, R.id.day2, R.id.day3,
                R.id.day4, R.id.day5, R.id.day6, R.id.day7
        };
        String[] days = new String[]{
                "Monday", "Tuesday", "Wednesday",
                "Thursday", "Friday", "Saturday", "Sunday"
        };

        for (int i = 0; i < dayIds.length; i++) {
            View dayView = view.findViewById(dayIds[i]);
            TextView tvDay = dayView.findViewById(R.id.tv_day);
            LinearLayout expandableLayout = dayView.findViewById(R.id.expandable_layout);

            tvDay.setText(days[i]);

            tvDay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (expandableLayout.getVisibility() == View.VISIBLE) {
                        expandableLayout.setVisibility(View.GONE);
                    } else {
                        expandableLayout.setVisibility(View.VISIBLE);
                    }
                }
            });

            setupMealExpandableFields(dayView, R.id.tv_breakfast, R.id.expandable_breakfast, i, 0);
            setupMealExpandableFields(dayView, R.id.tv_lunch, R.id.expandable_lunch, i, 1);
            setupMealExpandableFields(dayView, R.id.tv_dinner, R.id.expandable_dinner, i, 2);
        }

    }

    private void setupMealExpandableFields(View dayView, int mealTextViewId, int mealExpandableLayoutId, int dayIndex, int mealIndex) {
        TextView tvMeal = dayView.findViewById(mealTextViewId);
        LinearLayout expandableMealLayout = dayView.findViewById(mealExpandableLayoutId);
        tvMeal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (expandableMealLayout.getVisibility() == View.VISIBLE) {
                    expandableMealLayout.setVisibility(View.GONE);
                } else {
                    expandableMealLayout.setVisibility(View.VISIBLE);
                    expandableMealLayout.removeAllViews();

                    int lockedRecipes = 0;
                    for (int k = 0; k < recipes[dayIndex][mealIndex].size(); k++) {
                        if (visibles[dayIndex][mealIndex].get(k)) {
                            TextView mealTextView = recipes[dayIndex][mealIndex].get(k);
                            getRecipeInField(expandableMealLayout, dayIndex, mealIndex, mealTextView);
                        } else {
                            lockedRecipes = lockedRecipes + 1;
                        }
                    }
                    if (lockedRecipes>0) {addLockedMealField(expandableMealLayout, lockedRecipes);}
                }
            }
        });
    }

    private void getRecipeInField(LinearLayout mealLayout, int dayIndex, int mealIndex, TextView mealTextView) {
        SharedPreferences prefs = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String token = prefs.getString("token", null);

        if (token == null) {
            return;
        }

        String dietId = mealTextView.getText().toString().trim();
        String url = getResources().getString(R.string.server_uri) + "/api/recipes/" + dietId;

        String name = mealTextView.getText().toString().trim();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String id = response.getString("_id");
                            String userId = response.getString("userId");
                            String username=response.getString("userName");
                            String name = response.getString("name");
                            String userName = response.has("userName") ? response.getString("userName") : "Unknown";
                            String description =response.getString("description");
                            String instructions = response.getString("instructions");
                            String imageUrl = response.getString("imageUrl");

                            JSONArray categoriesArray = response.getJSONArray("categories");
                            StringBuilder categoriesBuilder = new StringBuilder();
                            for (int j = 0; j < categoriesArray.length(); j++) {
                                String category = categoriesArray.getString(j);
                                categoriesBuilder.append(category);
                                if (j < categoriesArray.length() - 1) {
                                    categoriesBuilder.append(", ");
                                }
                            }


                            JSONArray ingredientsArray = response.getJSONArray("ingredients");
                            StringBuilder ingredientsBuilder = new StringBuilder();
                            for (int j = 0; j < ingredientsArray.length(); j++) {
                                JSONObject ingredientObject = ingredientsArray.getJSONObject(j);
                                String ingredientName = ingredientObject.getString("name");
                                String quantity = ingredientObject.getString("quantity");
                                ingredientsBuilder.append(ingredientName).append(": ").append(quantity).append("\n");
                            }

                            Recipe recipe = new Recipe(id, userId, name, username, imageUrl, ingredientsBuilder.toString().trim(), instructions, categoriesBuilder.toString().trim() ,description);
                            recipeObjesctList.add(recipe);
                            addExistingMealField(mealLayout, dayIndex, mealIndex, recipe);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                    }
                }
        ) {
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("x-auth-token", token);
                return headers;
            }
        };

        requestQueue.add(jsonObjectRequest);
    }

    private void addExistingMealField(LinearLayout mealLayout, int dayIndex, int mealIndex, Recipe recipe) {
        View mealItemView = LayoutInflater.from(requireContext()).inflate(R.layout.item_meal, mealLayout, false);
        TextView tvMeal = mealItemView.findViewById(R.id.tv_meal);
        AppCompatImageButton btnRemoveMeal = mealItemView.findViewById(R.id.btn_remove_meal);
        AppCompatImageButton btnToggleVisibility = mealItemView.findViewById(R.id.btn_toggle_visibility);
        btnRemoveMeal.setVisibility(View.INVISIBLE);
        btnToggleVisibility.setVisibility(View.GONE);

        tvMeal.setText(recipe.getName());
        tvMeal.setTag(recipe.getId());

        tvMeal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ShowRecipeFragment fragment = ShowRecipeFragment.newInstance(recipe);
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.nav_host_fragment, fragment)
                        .addToBackStack(null)
                        .commit();
            }
        });

        mealLayout.addView(mealItemView, mealLayout.getChildCount() - 1);
    }

    private void addLockedMealField(LinearLayout mealLayout, int lockedRecipes) {

        View mealItemView = LayoutInflater.from(requireContext()).inflate(R.layout.item_meal, mealLayout, false);
        TextView tvMeal = mealItemView.findViewById(R.id.tv_meal);
        AppCompatImageButton btnRemoveMeal = mealItemView.findViewById(R.id.btn_remove_meal);
        AppCompatImageButton btnToggleVisibility = mealItemView.findViewById(R.id.btn_toggle_visibility);
        btnRemoveMeal.setVisibility(View.INVISIBLE);
        btnToggleVisibility.setVisibility(View.GONE);

        tvMeal.setText("Unlock for " + lockedRecipes + " more...");

        mealLayout.addView(mealItemView, mealLayout.getChildCount() - 1);
    }

    private void patchDietPurchase(View view) {
        Log.d("Diet id:", diet.getDietId());
        RequestQueue queue = Volley.newRequestQueue(getContext());

        SharedPreferences prefs = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String token = prefs.getString("token", null);

        if (token == null) {
            return;
        }

        String url = getResources().getString(R.string.server_uri) + "/api/users/purchaseDiet/" + diet.getDietId();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.PATCH,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        view.findViewById(R.id.btn_unlock_plan).setVisibility(View.GONE);
                        TextView textView = view.findViewById(R.id.tv_diet_add_info);
                        textView.setText(addInfo);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                }
        ) {
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("x-auth-token", token);
                return headers;
            }
        };

        queue.add(jsonObjectRequest);
    }

    private void checkIfUserPurchased(String dietId, View view) {
        RequestQueue queue = Volley.newRequestQueue(getContext());

        SharedPreferences prefs = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String token = prefs.getString("token", null);

        if (token == null) {
            return;
        }

        String url = getResources().getString(R.string.server_uri) + "/api/users/isDietPurchased/" + dietId;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            boolean userPurchasedThis = Boolean.parseBoolean(response.getString("userPurchased"));
                            isPurchased = userPurchasedThis;
                            if (isPurchased) {
                                view.findViewById(R.id.btn_unlock_plan).setVisibility(View.GONE);
                                TextView textView = view.findViewById(R.id.tv_diet_add_info);
                                textView.setText(addInfo);
                                updateVisibility(true);
                            }
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
}
