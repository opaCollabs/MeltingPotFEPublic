package com.example.meltingpot;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import android.provider.DocumentsContract;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.documentfile.provider.DocumentFile;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class ComplexRegisterActivity extends AppCompatActivity {

    private EditText nameEditText, emailEditText, passwordEditText, confirmPasswordEditText;
    private TextView fileNameTextView;
    private ImageView imageView;
    private String selectedRole = "";

    private String profileImageName, certificateName;
    private String profileImageBase64, certificateBase64;

    private List<String> selectedCategories = new ArrayList<>();
    private Button btnUserRole, btnNutritionistRole, btnGreek, btnVegan, btnFrench, btnItalian, btnFusion, btnAsian,
            btnMexican, btnAmerican, btnIndian, btnMediterranean, btnMiddleEastern, btnDesserts,
            uploadImageButton, uploadFileButton, registerButton;

    private ActivityResultLauncher<String> pickDocuments =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    String fileName = "";
                    if (DocumentsContract.isDocumentUri(this, uri)) {
                        String documentId = DocumentsContract.getDocumentId(uri);
                        if (documentId != null) {
                            String[] split = documentId.split(":");
                            if (split.length == 2) {
                                fileName = split[1];
                            }
                        }
                    }

                    if (!fileName.toLowerCase().endsWith(".pdf")) {
                        fileName += ".pdf";
                    }

                    fileNameTextView.setText(fileName);
                    String emailValue = emailEditText.getText().toString();

                    if (!emailValue.isEmpty()) {
                        fileName = emailValue + "_" + fileName;
                    }

                    String base64Pdf = convertFileToBase64(uri);

                    certificateName = fileName;
                    certificateBase64 = base64Pdf;
                }
            });



    private ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) {
                    String imageName = null;
                    String imageExtension = null;

                    DocumentFile documentFile = DocumentFile.fromSingleUri(this, uri);
                    if (documentFile != null && documentFile.getName() != null) {
                        imageName = documentFile.getName();
                        int dotIndex = imageName.lastIndexOf('.');
                        if (dotIndex >= 0) {
                            imageExtension = imageName.substring(dotIndex + 1);
                        } else {
                            imageExtension = "jpg";
                        }
                    }

                    if (imageName == null) {
                        imageName = "unknown_image";
                    }
                    if (imageExtension == null) {
                        imageExtension = "jpg";
                    }

                    String emailValue = emailEditText.getText().toString();
                    if (!emailValue.isEmpty()) {
                        imageName = emailValue + "." + imageExtension;
                    }

                    Log.d("Image name: ", imageName);
                    Log.d("Image Extension: ", imageExtension);

                    String base64Image = convertFileToBase64(uri);
                    Log.d("stuff", base64Image);

                    ImageView imageView = findViewById(R.id.imageView);
                    imageView.setImageURI(uri);


                    profileImageName = imageName;
                    profileImageBase64 = base64Image;

                } else {
                    Log.d("PhotoPicker", "No media selected");
                }
            });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complex_register);

        nameEditText = findViewById(R.id.nameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        fileNameTextView = findViewById(R.id.fileNameTextView);
        imageView = findViewById(R.id.imageView);

        btnUserRole = findViewById(R.id.btnUserRole);
        btnNutritionistRole = findViewById(R.id.btnNutritionistRole);
        btnGreek = findViewById(R.id.btnGreek);
        btnVegan = findViewById(R.id.btnVegan);
        btnFrench = findViewById(R.id.btnFrench);
        btnItalian = findViewById(R.id.btnItalian);
        btnFusion = findViewById(R.id.btnFusion);
        btnAsian = findViewById(R.id.btnAsian);
        btnMexican = findViewById(R.id.btnMexican);
        btnAmerican = findViewById(R.id.btnAmerican);
        btnIndian = findViewById(R.id.btnIndian);
        btnMediterranean = findViewById(R.id.btnMediterranean);
        btnMiddleEastern = findViewById(R.id.btnMiddleEastern);
        btnDesserts = findViewById(R.id.btnDesserts);
        uploadImageButton = findViewById(R.id.uploadImageButton);
        uploadFileButton = findViewById(R.id.uploadFileButton);
        registerButton = findViewById(R.id.registerButton);

        setRoleButtonListener(btnUserRole, "user");
        setRoleButtonListener(btnNutritionistRole, "nutritionist");

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

        uploadImageButton.setOnClickListener(v -> openImagePicker());

        uploadFileButton.setOnClickListener(v -> openPdfPicker());

        registerButton.setOnClickListener(v -> {
            if (validateRegisterForm(nameEditText, emailEditText, passwordEditText, confirmPasswordEditText)) {
                postUser(nameEditText.getText().toString().trim()
                        ,emailEditText.getText().toString().trim()
                        , passwordEditText.getText().toString().trim());
            }
        });
    }

    private boolean validateRegisterForm(EditText nameEditText, EditText emailEditText, EditText passwordEditText, EditText confirmPasswordEditText) {
        String name = nameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            nameEditText.setError("Name is required");
            nameEditText.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("Email is required");
            emailEditText.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("Password is required");
            passwordEditText.requestFocus();
            return false;
        }

        if (!password.equals(confirmPassword)) {
            confirmPasswordEditText.setError("Passwords do not match");
            confirmPasswordEditText.requestFocus();
            return false;
        }

        return true;
    }



    private boolean isPasswordValid(String password, String confirmPassword) {
        return !TextUtils.isEmpty(password) && password.equals(confirmPassword);
    }

    private void openPdfPicker(){
        pickDocuments.launch("application/pdf");
    }

    private void openImagePicker() {
            pickMedia.launch(new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                    .build());
    }

    private void setCategoryButtonListener(Button button, String categoryName) {
        button.setOnClickListener(v -> {
            if (selectedCategories.contains(categoryName)) {
                selectedCategories.remove(categoryName);
                button.setBackgroundResource(R.drawable.unselected_category_btn);
                button.setTextColor(ContextCompat.getColor(this, R.color.approx_gray));
            } else {
                selectedCategories.add(categoryName);
                button.setBackgroundResource(R.drawable.selected_category_btn);
                button.setTextColor(ContextCompat.getColor(this, R.color.white));
            }
            logSelectedCategories();
        });
    }

    private void setRoleButtonListener(Button button, String roleName) {
        button.setOnClickListener(v -> {
            if (selectedRole.equals(roleName)) {
                selectedRole = "";
                button.setBackgroundResource(R.drawable.unselected_category_btn);
                button.setTextColor(ContextCompat.getColor(this, R.color.approx_gray));
                uploadFileButton.setEnabled(false);
            } else {
                selectedRole = roleName;
                button.setBackgroundResource(R.drawable.selected_category_btn);
                button.setTextColor(ContextCompat.getColor(this, android.R.color.white));
                if (roleName.equals("nutritionist")) {
                    uploadFileButton.setEnabled(true);
                } else {
                    uploadFileButton.setEnabled(false);
                }

                Button otherButton = button.getId() == R.id.btnUserRole ?
                        findViewById(R.id.btnNutritionistRole) :
                        findViewById(R.id.btnUserRole);
                otherButton.setBackgroundResource(R.drawable.unselected_category_btn);
                otherButton.setTextColor(ContextCompat.getColor(this, R.color.approx_gray));
            }
        });
    }


    private void logSelectedCategories() {
        Log.d("SelectedCategories", selectedCategories.toString());
    }

    private String convertFileToBase64(Uri fileUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(fileUri);
            if (inputStream == null) {
                return null;
            }

            byte[] buffer = new byte[inputStream.available()];
            inputStream.read(buffer);

            inputStream.close();

            String base64File = Base64.encodeToString(buffer, Base64.DEFAULT);

            return base64File;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    private void postUser(String name, String email, String password){
        String serverUri = getResources().getString(R.string.server_uri);
        String apiUrl = serverUri + "/api/users/register";

        JSONObject userJson = new JSONObject();
        try {
            userJson.put("name", name);
            userJson.put("email", email);
            userJson.put("password", password);
            userJson.put("role", selectedRole);
            userJson.put("categories", new JSONArray(selectedCategories));
            userJson.put("imageData", "");
            userJson.put("fileData", "");

            if (profileImageName != null && profileImageBase64 != null) {
                JSONObject imageData = new JSONObject();
                imageData.put("fileName", profileImageName);
                imageData.put("payloadBase64", profileImageBase64);
                userJson.put("imageData", imageData);
            }

            if (certificateName != null && certificateBase64 != null) {
                JSONObject fileData = new JSONObject();
                fileData.put("fileName", certificateName);
                fileData.put("payloadBase64", certificateBase64);
                userJson.put("fileData", fileData);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST, apiUrl, userJson, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Log.d("RESPONSE : ", response.toString());
                        String token = response.getString("token");
                        String role = response.getString("role");

                        SharedPreferences.Editor editor = getSharedPreferences("MyPrefs", MODE_PRIVATE).edit();
                        editor.putString("token", token);
                        editor.putString("role", role);
                        editor.apply();
                        Log.e("Token: ",token);
                        Log.e("ROLE: ",role);

                        if (!token.isEmpty()) {
                            startActivity(new Intent(ComplexRegisterActivity.this, MainActivity.class));
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

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonObjectRequest);
    }
}



