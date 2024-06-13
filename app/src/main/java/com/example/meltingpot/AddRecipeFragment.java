package com.example.meltingpot;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.app.Activity.RESULT_OK;

public class AddRecipeFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;
    private EditText titleEditText, descriptionEditText;
    private Button addCategoryButton, addIngredientButton, addInstructionsButton, uploadImageButton, shareButton, ingredientCountButton;
    private TextView addRecipeText;
    private ImageView logoImageView;
    private List<String> categories, ingredients;
    private String instructions, imageBase64;

    private String recipeImageName;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_recipe, container, false);

        titleEditText = view.findViewById(R.id.titleEditText);
        descriptionEditText = view.findViewById(R.id.descriptionEditText);
        addCategoryButton = view.findViewById(R.id.addCategoryButton);
        addIngredientButton = view.findViewById(R.id.addIngredientButton);
        addInstructionsButton = view.findViewById(R.id.addInstructionsButton);
        uploadImageButton = view.findViewById(R.id.uploadImageButton);
        shareButton = view.findViewById(R.id.shareButton);
        ingredientCountButton = view.findViewById(R.id.ingredientCountButton);
        addRecipeText = view.findViewById(R.id.addRecipeText);
        logoImageView = view.findViewById(R.id.logoImageView);

        categories = new ArrayList<>();
        ingredients = new ArrayList<>();
        instructions = "";
        imageBase64 = "";
        recipeImageName = "";

        addCategoryButton.setOnClickListener(v -> showCategoryPopup());
        addIngredientButton.setOnClickListener(v -> showIngredientPopup());
        addInstructionsButton.setOnClickListener(v -> showInstructionsPopup());
        ingredientCountButton.setOnClickListener(v -> showIngredientListPopup());
        uploadImageButton.setOnClickListener(v -> chooseImage());
        shareButton.setOnClickListener(v -> shareRecipe());

        return view;
    }

    private void showCategoryPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Choose Categories");

        String[] categoryArray = {"Appetizer", "Main Course", "Dessert", "Greek", "Vegan", "French", "Italian", "Fusion", "Asian", "Mexican", "American", "Indian", "Mediterranean", "Middle Eastern"};
        boolean[] checkedItems = new boolean[categoryArray.length];

        builder.setMultiChoiceItems(categoryArray, checkedItems, (dialog, which, isChecked) -> {
            if (isChecked) {
                categories.add(categoryArray[which]);
            } else {
                categories.remove(categoryArray[which]);
            }
        });

        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showIngredientPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.popup_add_ingredient, null);
        builder.setView(dialogView);

        EditText ingredientEditText = dialogView.findViewById(R.id.ingredientEditText);
        EditText quantityEditText = dialogView.findViewById(R.id.quantityEditText);
        Button addButton = dialogView.findViewById(R.id.addButton);

        AlertDialog dialog = builder.create();

        addButton.setOnClickListener(v -> {
            String ingredient = ingredientEditText.getText().toString().trim();
            String quantity = quantityEditText.getText().toString().trim();

            if (!ingredient.isEmpty() && !quantity.isEmpty()) {
                ingredients.add(ingredient + ": " + quantity);
                ingredientEditText.setText("");
                quantityEditText.setText("");
                updateIngredientCount();
                Toast.makeText(getActivity(), "Ingredient added", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(), "Please enter both ingredient and quantity", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void showInstructionsPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.popup_add_instructions, null);
        builder.setView(dialogView);

        EditText instructionsEditText = dialogView.findViewById(R.id.instructionsEditText);
        instructionsEditText.setText(instructions);

        builder.setPositiveButton("OK", (dialog, which) -> {
            instructions = instructionsEditText.getText().toString().trim();
            dialog.dismiss();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void chooseImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireContext().getContentResolver(), uri);
                imageBase64 = encodeImage(bitmap);
                Log.d("OurImage", imageBase64);
                recipeImageName = titleEditText.getText().toString().trim() + ".jpg";
                Toast.makeText(getActivity(), "Image selected", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String encodeImage(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 25, byteArrayOutputStream);
        byte[] imageBytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }

    private void shareRecipe() {
        try {

            ArrayList<String> categoryStrings = new ArrayList<>(categories);
            ArrayList<String> ingredientStrings = new ArrayList<>(ingredients);

            JSONObject recipeJson = new JSONObject();
            recipeJson.put("name", titleEditText.getText().toString().trim());
            recipeJson.put("description", descriptionEditText.getText().toString().trim());
            recipeJson.put("ingredients", new JSONArray(ingredientStrings));
            recipeJson.put("instructions", instructions);
            recipeJson.put("categories", new JSONArray(categoryStrings));
            recipeJson.put("imageData", "");

            if (recipeImageName != null && imageBase64 != null) {
                JSONObject imageData = new JSONObject();
                imageData.put("fileName", recipeImageName);
                imageData.put("payloadBase64", imageBase64);
                recipeJson.put("imageData", imageData);
            }

            sendRecipeToServer(recipeJson);
        } catch (JSONException e) {
            Log.e("AddRecipeFragment", "Error creating JSON object for recipe", e);
            Toast.makeText(requireContext(), "Failed to create recipe JSON", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendRecipeToServer(JSONObject recipeJson) {
        String url = getResources().getString(R.string.server_uri) + "/api/recipes";

        RequestQueue requestQueue = Volley.newRequestQueue(requireContext());
        SharedPreferences prefs = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String token = prefs.getString("token", null);

        if (token == null) {
            return;
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST, url, recipeJson, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Toast.makeText(getActivity(), "Recipe Saved", Toast.LENGTH_SHORT).show();
                        getFragmentManager().popBackStack();
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();

                        Toast.makeText(getActivity(), "Save failed" , Toast.LENGTH_SHORT).show();
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
    }

    private void resetFields() {
        titleEditText.setText("");
        descriptionEditText.setText("");
        categories.clear();
        ingredients.clear();
        instructions = "";
        imageBase64 = "";
        recipeImageName = "";
        ingredientCountButton.setText("Ingredients: 0");
        logoImageView.setImageDrawable(null);
    }

    private void updateIngredientCount() {
        ingredientCountButton.setText("Ingredients: " + ingredients.size());
    }

    private void showIngredientListPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Ingredients List");

        String[] ingredientArray = ingredients.toArray(new String[0]);
        boolean[] checkedItems = new boolean[ingredientArray.length];

        builder.setMultiChoiceItems(ingredientArray, checkedItems, (dialog, which, isChecked) -> {

        });

        builder.setPositiveButton("Remove Selected", (dialog, which) -> {
            for (int i = checkedItems.length - 1; i >= 0; i--) {
                if (checkedItems[i]) {
                    ingredients.remove(i);
                }
            }
            updateIngredientCount();
            dialog.dismiss();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}


