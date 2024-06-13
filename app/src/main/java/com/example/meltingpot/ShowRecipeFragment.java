package com.example.meltingpot;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ShowRecipeFragment extends Fragment {
    private static final String ARG_RECIPE = "recipe";

    private Recipe recipe;
    private boolean isLiked = false;
    private boolean isSaved = false;

    int heartChange;

    View view;

    Button deliciousButton;
    Button saveButton;

    public ShowRecipeFragment() {

    }

    public static ShowRecipeFragment newInstance(Recipe recipe) {
        ShowRecipeFragment fragment = new ShowRecipeFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_RECIPE, recipe);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            recipe = getArguments().getParcelable(ARG_RECIPE);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_show_recipe, container, false);

        ImageView recipeImageView = view.findViewById(R.id.recipeImageView);
        TextView recipeNameTextView = view.findViewById(R.id.recipeNameTextView);
        TextView userNameTextView = view.findViewById(R.id.userNameTextView);
        TextView ingredientsLabelTextView = view.findViewById(R.id.ingredientsLabelTextView);
        TextView ingredientsTextView = view.findViewById(R.id.ingredientsTextView);
        TextView instructionsLabelTextView = view.findViewById(R.id.instructionsLabelTextView);
        TextView instructionsTextView = view.findViewById(R.id.instructionsTextView);
        TextView categoriesTextView = view.findViewById(R.id.categoriesTextView);
        TextView descriptionTextView = view.findViewById(R.id.descriptionTextView);
        deliciousButton = view.findViewById(R.id.deliciousButton);
        saveButton = view.findViewById(R.id.saveButton);

        if (recipe != null) {
            Glide.with(this).load(recipe.getImageUrl()).into(recipeImageView);
            recipeNameTextView.setText(recipe.getName());
            userNameTextView.setText(recipe.getUserName());
            ingredientsTextView.setText(recipe.getIngredients());
            instructionsTextView.setText(recipe.getInstructions());
            categoriesTextView.setText(recipe.getCategories());
            descriptionTextView.setText(recipe.getDescription());
        }
        checkIfUserLikes(recipe.getId(), view);
        checkIfUserSaved(recipe.getId(), view);

        deliciousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                heartChange = isLiked ? -1 : 1;
                toggleButtonState(deliciousButton, heartChange);
                patchRecipeHearts(heartChange);
                isLiked = !isLiked;
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleSaveState();
            }
        });

        return view;
    }

    private void toggleButtonState(Button button, int heartChange) {
        if (heartChange == 1) {
            button.setBackgroundResource(R.drawable.button_add_recipe);
            button.setTextColor(ContextCompat.getColor(this.getContext(), R.color.white));
        } else {
            button.setTextColor(ContextCompat.getColor(this.getContext(), R.color.approx_gray));
            button.setBackgroundResource(R.drawable.unselected_category_btn);
            button.setPadding(
                    dpToPx(16),
                    button.getPaddingTop(),
                    dpToPx(16),
                    button.getPaddingBottom()
            );
        }
    }

    private void initiateDeliciousBtn(boolean userLikesThis) {
        if (userLikesThis) {
            isLiked = true;
            deliciousButton.setBackgroundResource(R.drawable.button_add_recipe);
            deliciousButton.setTextColor(ContextCompat.getColor(view.getContext(), R.color.white));
        } else {
            isLiked = false;
            deliciousButton.setTextColor(ContextCompat.getColor(this.getContext(), R.color.approx_gray));
            deliciousButton.setBackgroundResource(R.drawable.unselected_category_btn);
            deliciousButton.setPadding(
                    dpToPx(16),
                    deliciousButton.getPaddingTop(),
                    dpToPx(16),
                    deliciousButton.getPaddingBottom()
            );
        }
    }

    private void toggleSaveState() {
        isSaved = !isSaved;
        updateSaveButton();
        patchRecipeSaveState(isSaved ? 1 : -1);
    }

    private void updateSaveButton() {
        if (isSaved) {
            saveButton.setBackgroundResource(R.drawable.button_add_recipe);
            saveButton.setTextColor(ContextCompat.getColor(this.getContext(), R.color.white));
        } else {
            saveButton.setTextColor(ContextCompat.getColor(this.getContext(), R.color.approx_gray));
            saveButton.setBackgroundResource(R.drawable.unselected_category_btn);
            saveButton.setPadding(
                    dpToPx(16),
                    saveButton.getPaddingTop(),
                    dpToPx(16),
                    saveButton.getPaddingBottom()
            );
        }
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    private void patchRecipeHearts(int heartChange) {
        Log.d("Recipe id:", recipe.getId());
        RequestQueue queue = Volley.newRequestQueue(getContext());

        SharedPreferences prefs = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String token = prefs.getString("token", null);

        if (token == null) {

            return;
        }

        String url = getResources().getString(R.string.server_uri) + "/api/recipes/" + recipe.getId();
        JSONObject patchData = new JSONObject();
        try {
            patchData.put("hearts", heartChange);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.PATCH,
                url,
                patchData,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
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

    private void patchRecipeSaveState(int saveChange) {
        Log.d("Recipe id:", recipe.getId());
        RequestQueue queue = Volley.newRequestQueue(getContext());

        SharedPreferences prefs = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String token = prefs.getString("token", null);

        if (token == null) {


            return;
        }

        String url = getResources().getString(R.string.server_uri) + "/api/recipes/save/" + recipe.getId();
        JSONObject patchData = new JSONObject();
        try {
            patchData.put("saved", saveChange);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.PATCH,
                url,
                patchData,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

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

    private void checkIfUserLikes(String recipeId, View view) {
        String url = getResources().getString(R.string.server_uri) + "/api/recipes/doesUserLike/" + recipeId;
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
                            boolean userLikesThis = Boolean.parseBoolean(response.getString("userLikes"));
                            initiateDeliciousBtn(userLikesThis);
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

    private void checkIfUserSaved(String recipeId, View view) {
        String url = getResources().getString(R.string.server_uri) + "/api/recipes/doesUserSave/" + recipeId;
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
                            boolean userSavedThis = Boolean.parseBoolean(response.getString("userSaved"));
                            isSaved = userSavedThis;
                            updateSaveButton();
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


