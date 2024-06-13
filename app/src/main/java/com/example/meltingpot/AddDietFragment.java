package com.example.meltingpot;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageButton;
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
import java.util.Map;

public class AddDietFragment extends Fragment implements RecipePickerFragment.OnRecipeSelectedListener {

    private EditText etTitle, etPrice, etDescription, etAddInfo;
    private String dietTitle, description, addInfo;
    private float price;
    private boolean isEditing = false;
    private boolean isTitleSet = false;
    private ArrayList<Boolean>[][] visible; // Visibility array
    private ArrayList<TextView>[][] recipes; // Meals array
    private static final String TAG = "AddDietFragment";
    private String serverUrl;

    // Variables to handle meal selection
    private LinearLayout currentMealLayout;
    private int currentDayIndex;
    private int currentMealIndex;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_diet, container, false);

        etTitle = view.findViewById(R.id.tv_title);
        etPrice = view.findViewById(R.id.et_price);
        etDescription = view.findViewById(R.id.et_description);
        etAddInfo = view.findViewById(R.id.et_add_info);

        AppCompatImageButton btnBack = view.findViewById(R.id.btn_back);
        AppCompatImageButton btnEdit = view.findViewById(R.id.btn_edit);
        AppCompatButton btnSharePlan = view.findViewById(R.id.btn_share_plan);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStack();
            }
        });

        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleEditTitle();
            }
        });

        btnSharePlan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareDietPlan();
            }
        });

        if (!isTitleSet) {

            showAddDietTitleDialog();

            visible = new ArrayList[7][3];
            recipes = new ArrayList[7][3];
            for (int i = 0; i < 7; i++) {
                for (int j = 0; j < 3; j++) {
                    recipes[i][j] = new ArrayList<>();
                    visible[i][j] = new ArrayList<>();
                }
            }
        }


        setupExpandableFields(view);

        serverUrl = getResources().getString(R.string.server_uri) + "/api/diets";

        return view;
    }

    private void toggleEditTitle() {
        if (isEditing) {
            etTitle.setEnabled(false);
            isEditing = false;
        } else {
            // Enter edit mode
            etTitle.setEnabled(true);
            etTitle.requestFocus();
            isEditing = true;
        }
    }

    private void showAddDietTitleDialog() {
        if (!isTitleSet) {
            LayoutInflater inflater = getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.dialog_add_diet_title, null);

            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(requireContext());
            dialogBuilder.setView(dialogView);
            AlertDialog dialog = dialogBuilder.create();
            dialog.setCancelable(false);

            EditText editTextDietTitle = dialogView.findViewById(R.id.editTextDietTitle);
            AppCompatButton buttonStartPlanning = dialogView.findViewById(R.id.buttonStartPlanning);

            buttonStartPlanning.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dietTitle = editTextDietTitle.getText().toString().trim();
                    if (!dietTitle.isEmpty()) {
                        etTitle.setText(dietTitle);
                        isTitleSet = true;
                        dialog.dismiss();
                    } else {
                        editTextDietTitle.setError("Title is required");
                    }
                }
            });
            dialog.show();
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

            setupMealExpandableFields(dayView, R.id.tv_breakfast, R.id.expandable_breakfast, R.id.btn_add_breakfast, i, 0);
            setupMealExpandableFields(dayView, R.id.tv_lunch, R.id.expandable_lunch, R.id.btn_add_lunch, i, 1);
            setupMealExpandableFields(dayView, R.id.tv_dinner, R.id.expandable_dinner, R.id.btn_add_dinner, i, 2);
        }
    }

    private void setupMealExpandableFields(View dayView, int mealTextViewId, int mealExpandableLayoutId, int mealAddButtonId, int dayIndex, int mealIndex) {
        TextView tvMeal = dayView.findViewById(mealTextViewId);
        LinearLayout expandableMealLayout = dayView.findViewById(mealExpandableLayoutId);
        AppCompatButton btnAddMeal = dayView.findViewById(mealAddButtonId);

        tvMeal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (expandableMealLayout.getVisibility() == View.VISIBLE) {
                    expandableMealLayout.setVisibility(View.GONE);
                } else {
                    expandableMealLayout.setVisibility(View.VISIBLE);
                }
            }
        });

        btnAddMeal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addMealField(expandableMealLayout, dayIndex, mealIndex);
            }
        });

        expandableMealLayout.removeAllViews();
        expandableMealLayout.addView(btnAddMeal);

        for (TextView mealTextView : recipes[dayIndex][mealIndex]) {
            addExistingMealField(expandableMealLayout, dayIndex, mealIndex, mealTextView);
        }
    }

    private void addExistingMealField(LinearLayout mealLayout, int dayIndex, int mealIndex, TextView mealTextView) {
        View mealItemView = LayoutInflater.from(requireContext()).inflate(R.layout.item_meal, mealLayout, false);
        TextView tvMeal = mealItemView.findViewById(R.id.tv_meal);
        AppCompatImageButton btnRemoveMeal = mealItemView.findViewById(R.id.btn_remove_meal);
        AppCompatImageButton btnToggleVisibility = mealItemView.findViewById(R.id.btn_toggle_visibility);

        tvMeal.setText(mealTextView.getText());
        tvMeal.setTag(mealTextView.getTag());

        mealLayout.addView(mealItemView, mealLayout.getChildCount() - 1);

        btnRemoveMeal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mealLayout.removeView(mealItemView);
                int index = recipes[dayIndex][mealIndex].indexOf(mealTextView);
                recipes[dayIndex][mealIndex].remove(mealTextView);
                visible[dayIndex][mealIndex].remove(index);
            }
        });

        btnToggleVisibility.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = mealLayout.indexOfChild(mealItemView);
                boolean isVisible = visible[dayIndex][mealIndex].get(position);
                visible[dayIndex][mealIndex].set(position, !isVisible);

                if (isVisible) {
                    btnToggleVisibility.setImageResource(R.drawable.ic_visibility_false);
                } else {
                    btnToggleVisibility.setImageResource(R.drawable.ic_visibility);
                }
            }
        });

        int position = mealLayout.indexOfChild(mealItemView);
        boolean isVisible = visible[dayIndex][mealIndex].get(position);
        if (isVisible) {
            btnToggleVisibility.setImageResource(R.drawable.ic_visibility);
        } else {
            btnToggleVisibility.setImageResource(R.drawable.ic_visibility_false);
        }
    }


    private void addMealField(LinearLayout mealLayout, int dayIndex, int mealIndex) {
        currentMealLayout = mealLayout;
        currentDayIndex = dayIndex;
        currentMealIndex = mealIndex;

        RecipePickerFragment recipePickerFragment = new RecipePickerFragment();
        recipePickerFragment.setTargetFragment(AddDietFragment.this, 0);
        getFragmentManager().beginTransaction()
                .replace(R.id.nav_host_fragment, recipePickerFragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onRecipeSelected(String recipeId, String recipeName) {
        Log.d(TAG, "RECIPE ID : " + recipeId);
        if (currentMealLayout != null) {
            addSelectedRecipe(currentMealLayout, currentDayIndex, currentMealIndex, recipeId, recipeName);
        }
    }

    private void addSelectedRecipe(LinearLayout mealLayout, int dayIndex, int mealIndex, String recipeId, String recipeName) {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View mealItemView = inflater.inflate(R.layout.item_meal, mealLayout, false);

        TextView tvMeal = mealItemView.findViewById(R.id.tv_meal);
        AppCompatImageButton btnRemoveMeal = mealItemView.findViewById(R.id.btn_remove_meal);
        AppCompatImageButton btnToggleVisibility = mealItemView.findViewById(R.id.btn_toggle_visibility);

        tvMeal.setText(recipeName);
        tvMeal.setTag(recipeId);  // Store the recipeId in the tag

        recipes[dayIndex][mealIndex].add(tvMeal);
        visible[dayIndex][mealIndex].add(false);
        btnRemoveMeal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = mealLayout.indexOfChild(mealItemView);
                mealLayout.removeView(mealItemView);
                recipes[dayIndex][mealIndex].remove(position - 1);
                visible[dayIndex][mealIndex].remove(position - 1);
            }
        });

        btnToggleVisibility.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = mealLayout.indexOfChild(mealItemView);
                boolean isVisible = visible[dayIndex][mealIndex].get(position);
                visible[dayIndex][mealIndex].set(position, !isVisible);

                if (isVisible) {
                    btnToggleVisibility.setImageResource(R.drawable.ic_visibility_false);
                } else {
                    btnToggleVisibility.setImageResource(R.drawable.ic_visibility);
                }
            }
        });

        mealLayout.addView(mealItemView, mealLayout.getChildCount() - 1);
    }

    private void shareDietPlan() {
        try {
            JSONObject dietPlanJson = new JSONObject();
            dietPlanJson.put("name", etTitle.getText().toString().trim());

            JSONArray daysArray = new JSONArray();
            for (int i = 0; i < 7; i++) {
                JSONArray mealsArray = new JSONArray();
                for (int j = 0; j < 3; j++) {
                    JSONArray mealList = new JSONArray();
                    for (TextView tvMeal : recipes[i][j]) {
                        String recipeId = (String) tvMeal.getTag();
                        mealList.put(recipeId);
                    }
                    mealsArray.put(mealList);
                }
                daysArray.put(mealsArray);
            }
            dietPlanJson.put("recipes", daysArray);

            JSONArray visibleArray = new JSONArray();
            for (int i = 0; i < 7; i++) {
                JSONArray mealsVisibleArray = new JSONArray();
                for (int j = 0; j < 3; j++) {
                    JSONArray visibleList = new JSONArray();
                    for (Boolean isVisible : visible[i][j]) {
                        visibleList.put(isVisible);
                    }
                    mealsVisibleArray.put(visibleList);
                }
                visibleArray.put(mealsVisibleArray);
            }
            dietPlanJson.put("visible_recipes", visibleArray);

            float parsedFloat = Float.parseFloat(etPrice.getText().toString().trim());
            float truncatedFloat = (int) (parsedFloat * 100) / 100.0f;

            dietPlanJson.put("price", truncatedFloat);
            dietPlanJson.put("description", etDescription.getText().toString().trim());
            dietPlanJson.put("info", etAddInfo.getText().toString().trim());

            sendDietPlanToServer(dietPlanJson);
        } catch (JSONException e) {
            Log.e(TAG, "Error creating JSON object for diet plan", e);
            Toast.makeText(requireContext(), "Failed to create diet plan JSON", Toast.LENGTH_SHORT).show();
        }
        isTitleSet = false;
    }

    private void sendDietPlanToServer(JSONObject dietPlanJson) {
        String url = getResources().getString(R.string.server_uri) + "/api/diets";

        RequestQueue requestQueue = Volley.newRequestQueue(requireContext());
        SharedPreferences prefs = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String token = prefs.getString("token", null);

        if (token == null) {
            return;
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST, url, dietPlanJson, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Toast.makeText(getActivity(), "Diet plan shared!", Toast.LENGTH_SHORT).show();
                        getFragmentManager().popBackStack();
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        String errorMessage = error.getMessage() != null ? error.getMessage() : "Unknown error";
                        Toast.makeText(getActivity(), "Error while sharing the plan:" + errorMessage, Toast.LENGTH_SHORT).show();
                    }

                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("x-auth-token", token);
                return headers;
            }
        };

        requestQueue.add(jsonObjectRequest);
        isTitleSet = false;
    }
}