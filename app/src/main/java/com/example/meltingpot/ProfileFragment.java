package com.example.meltingpot;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.flexbox.FlexboxLayout;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProfileFragment extends Fragment {

    private ImageView imageProfile;
    private TextView textProfileName, textProfileEmail, textCookScore;
    private FlexboxLayout categoriesContainer;
    private RequestQueue requestQueue;
    private String imageUrl;

    public ProfileFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        imageProfile = view.findViewById(R.id.image_profile);
        textProfileName = view.findViewById(R.id.text_profile_name);
        textProfileEmail = view.findViewById(R.id.text_profile_email);
        textCookScore = view.findViewById(R.id.text_cook_score_number);
        categoriesContainer = view.findViewById(R.id.categories_container);
        imageUrl="https://dummylittleblob.blob.core.windows.net/potblob/TestRecipe-9542784076877964.jpg?sv=2023-11-03&st=2024-06-06T00%3A06%3A51Z&se=2025-06-06T00%3A06%3A51Z&sr=b&sp=r&sig=pxGXkECluriGis3txlO3dTmfnMrAMdOm0kHx5QaPGXo%3D";


        requestQueue = Volley.newRequestQueue(requireContext());


        fetchProfileData();
        fetchProfileData2();

        return view;
    }

    private void fetchProfileData() {
        String serverUri = getResources().getString(R.string.server_uri);
        String url = serverUri + "/api/users/profile";


        SharedPreferences prefs = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String token = prefs.getString("token", null);

        if (token == null) {

            return;
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            imageUrl = response.getString("imageUrl");




                                Glide.with(ProfileFragment.this).load(imageUrl).into(imageProfile);







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

    private void fetchProfileData2() {
        String serverUri = getResources().getString(R.string.server_uri);
        String url = serverUri + "/api/users/profile";


        SharedPreferences prefs = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String token = prefs.getString("token", null);

        if (token == null) {

            return;
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {

                            String name = response.getString("name");
                            String email = response.getString("email");
                            String cookScore = response.getString("cookScore").toString();
                            JSONArray categories = response.getJSONArray("categories");



                            Glide.with(ProfileFragment.this).load(imageUrl).into(imageProfile);



                            textProfileName.setText(name);
                            textProfileEmail.setText(email);
                            textCookScore.setText(cookScore);


                            addCategoriesToFlexbox(categories);
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

    private void addCategoriesToFlexbox(JSONArray categories) {
        categoriesContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        for (int i = 0; i < categories.length(); i++) {
            try {
                String category = categories.getString(i);
                TextView categoryView = (TextView) inflater.inflate(R.layout.category_tag_layout, categoriesContainer, false);
                categoryView.setText(category);
                FlexboxLayout.LayoutParams params = new FlexboxLayout.LayoutParams(
                        FlexboxLayout.LayoutParams.WRAP_CONTENT,
                        FlexboxLayout.LayoutParams.WRAP_CONTENT
                );
                params.setMargins(8, 8, 8, 8);
                categoryView.setLayoutParams(params);
                categoriesContainer.addView(categoryView);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
