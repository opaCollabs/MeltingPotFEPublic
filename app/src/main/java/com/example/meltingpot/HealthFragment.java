package com.example.meltingpot;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HealthFragment extends Fragment implements DietFeedAdapter.OnDietClickListener {

    private AppCompatButton fab;
    private static final String TAG = "HealthFragment";
    private RecyclerView recyclerView;
    private DietFeedAdapter dietFeedAdapter;
    private List<Diet> dietList;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_health, container, false);

        SharedPreferences prefs = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String role = prefs.getString("role", null);

        fab = getActivity().findViewById(R.id.fab);
        if (role.equals("admin")||role.equals("nutritionist")) {
            fab.setVisibility(View.VISIBLE);
        } else {
            fab.setVisibility(View.GONE);
        }
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                fab.setVisibility(View.GONE);
                getFragmentManager().beginTransaction()
                        .replace(R.id.nav_host_fragment, new AddDietFragment())
                        .addToBackStack(null)
                        .commit();
            }
        });

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        dietList = new ArrayList<>();
        dietFeedAdapter = new DietFeedAdapter(dietList, this);
        recyclerView.setAdapter(dietFeedAdapter);


        fetchDietsFromServer("", new String[0]);

        return view;
    }


    private void fetchDietsFromServer(String searchTerm, String[] categories) {
        String url  = getResources().getString(R.string.server_uri) + "/api/diets/search";
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
                                JSONObject dietObject = response.getJSONObject(i);
                                String id = dietObject.getString("_id");
                                String userId = dietObject.getString("userId");
                                String username = dietObject.getString("userName");
                                String name = dietObject.getString("name");
                                String description = dietObject.getString("description");
                                double price = dietObject.getDouble("price");
                                String info = dietObject.getString("info");



                                JSONArray recipesJSON = dietObject.getJSONArray("recipes");

                                JSONArray visibilityJSON = dietObject.getJSONArray("visible_recipes");

                                Diet diet = new Diet(id, userId, username, name, recipesJSON, visibilityJSON, price, description, info);
                                dietList.add(diet);
                            }
                            dietFeedAdapter.notifyDataSetChanged();
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

    @Override
    public void onDietClick(Diet diet) {
        ViewDietFragment viewDietFragment = ViewDietFragment.newInstance(diet);;
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.nav_host_fragment, viewDietFragment)
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

