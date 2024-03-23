package com.example.project1.activites;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.project1.R;
import com.example.project1.api.FastApiService;
import com.example.project1.model.Updateuser;
import com.example.project1.model.User;
import com.example.project1.sessionmanagement.UserSharedPreference;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Profile extends AppCompatActivity {
    private EditText Fname;
    private EditText Lname;
    private EditText Phone;
    private EditText Email;
    private EditText Password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Fname = findViewById(R.id.editTextFirstName);
        Lname = findViewById(R.id.editTextLastName);
        Phone = findViewById(R.id.editTextPhoneNumber);
        Email = findViewById(R.id.editTextEmail);
        Password = findViewById(R.id.editTextPassword);

        // Load user details
        loadUserDetails();

        // Set OnClickListener for the Update button
        findViewById(R.id.buttonUpdate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateProfile();
            }
        });

        // Set OnClickListener for the Reset button
        findViewById(R.id.buttonReset).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetFields();
            }
        });
    }

    private void resetFields() {
        Fname.setText("");
        Lname.setText("");
        Password.setText("");
    }

    private void loadUserDetails() {
        UserSharedPreference sh = new UserSharedPreference(Profile.this);
        int ownerId = sh.getUserDetails();

        // Create Retrofit instance
        String BASE_URL = "http://" + sh.getIP();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        // Create ApiService instance
        FastApiService apiService = retrofit.create(FastApiService.class);

        // Make network request to fetch user details
        Call<User> call = apiService.getUser(ownerId);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body();
                    // Update UI elements here
                    Fname.setText(user.getFirstname());
                    Lname.setText(user.getLastName());
                    Email.setText(user.getEmail());
                    Phone.setText(user.getPhoneNumber());
                    Password.setText(sh.getPassword());
                    Email.setEnabled(false);
                    Phone.setEnabled(false);
                } else {
                    Toast.makeText(Profile.this, "Failed to load user details", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(Profile.this, "Failed to load user details: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateProfile() {
        // Create Retrofit instance
        UserSharedPreference sh = new UserSharedPreference(Profile.this);
        String BASE_URL = "http://" + sh.getIP();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        // Create ApiService instance
        FastApiService apiService = retrofit.create(FastApiService.class);

        // Create UpdateUser object with updated data from EditText fields
        Updateuser updatedUser = new Updateuser(
                Fname.getText().toString(),
                Lname.getText().toString(),
                Password.getText().toString()
        );

        // Make network request to update profile
        Call<Void> call = apiService.updateProfile(Phone.getText().toString(), updatedUser);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(Profile.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(Profile.this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                }
            }


            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(Profile.this, "Failed to update profile: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
