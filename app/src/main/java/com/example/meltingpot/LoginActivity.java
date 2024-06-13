package com.example.meltingpot;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);


        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = emailEditText.getText().toString();
                String password = passwordEditText.getText().toString();


                JSONObject credentials = new JSONObject();
                try {
                    credentials.put("email", email);
                    credentials.put("password", password);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                String serverUri = getResources().getString(R.string.server_uri);
                String url = serverUri + "/api/users/login";


                JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, credentials,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {

                                    String token = response.getString("token");
                                    String role = response.getString("role");


                                    SharedPreferences.Editor editor = getSharedPreferences("MyPrefs", MODE_PRIVATE).edit();


                                    editor.putString("token", token);
                                    editor.putString("role", role);
                                    editor.apply();


                                    Log.e("Token: ", token);
                                    Log.e("Role: ", role);


                                    if (!token.isEmpty()) {
                                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                        finish();
                                    } else {
                                        Log.e("Login Error", response.getString("message"));
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
                        });


                Volley.newRequestQueue(LoginActivity.this).add(request);
            }
        });

    }
}
