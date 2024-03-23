package com.example.project1.activites;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import com.example.project1.R;
import com.example.project1.api.FastApiService;
import com.example.project1.model.User;
import com.example.project1.sessionmanagement.UserSharedPreference;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.gson.JsonObject;
import java.io.IOException;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
public class RegisterActivity extends AppCompatActivity {

    private EditText editTextFirstName, editTextLastName, editTextEmail, editTextPhoneNumber, editTextPassword, editTextCPassword;
    private Button buttonLogin;
    UserSharedPreference sh = new UserSharedPreference(this);
    private FirebaseAuth mAuth;
    private static  String BASE_URL = "http://";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_new);
        mAuth = FirebaseAuth.getInstance();

        editTextFirstName = findViewById(R.id.editTextFirstName);
        editTextLastName = findViewById(R.id.editTextLastName);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPhoneNumber = findViewById(R.id.editTextPhoneNumber);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextCPassword = findViewById(R.id.editTextConfirmPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        AppCompatImageButton buttonRegister = findViewById(R.id.buttonRegister);

        buttonRegister.setOnClickListener(view -> {
            if (validateFields()) {
                sendRegistrationData();

            }
        });

        buttonLogin.setOnClickListener(view -> navigateToLoginActivity());
    }

    private void registerUserWithEmailAndPassword() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();



        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign up success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
//                            Toast.makeText(LoginActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();
                            // Proceed to the next activity or perform other actions
                        } else {
                            // If sign up fails, display a message to the user.
//                            Toast.makeText(LoginActivity.this, "Registration failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    private void sendRegistrationData() {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(loggingInterceptor);

        BASE_URL=BASE_URL+sh.getIP()+"/";
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build())
                .build();

        FastApiService service = retrofit.create(FastApiService.class);
        User user = new User(
                editTextFirstName.getText().toString().trim(),
                editTextLastName.getText().toString().trim(),
                editTextEmail.getText().toString().trim(),
                editTextPhoneNumber.getText().toString().trim(),
                editTextPassword.getText().toString().trim()
        );
        sh.setfname(editTextFirstName.getText().toString().trim());
        service.registerUser(user).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String message = response.body().get("message").getAsString();
                    showToast(message);
                    if ("User registered successfully".equals(message)) {

                        registerUserWithEmailAndPassword();
                        navigateToLoginActivity();
                    }
                } else {
                    showToast("Registration failed. Please try again.");
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                showToast("Network error. Please try again.");
            }
        });
    }

    private boolean validateFields() {
        String firstName = editTextFirstName.getText().toString().trim();
        String lastName = editTextLastName.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String phoneNumber = editTextPhoneNumber.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
//        String confirmPassword = editTextConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(firstName)) {
            editTextFirstName.setError("First name is required");
            return false;
        }

        if (TextUtils.isEmpty(lastName)) {
            editTextLastName.setError("Last name is required");
            return false;
        }

        if (TextUtils.isEmpty(email)) {
            editTextEmail.setError("Email is required");
            return false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setError("Enter a valid email address");
            return false;
        }

        if (TextUtils.isEmpty(phoneNumber)) {
            editTextPhoneNumber.setError("Phone number is required");
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            editTextPassword.setError("Password is required");
            return false;
        }


        if (password.equals(editTextCPassword)) {
            editTextPassword.setError("Passwords do not match");
            editTextCPassword.setError("Passwords do not match");
            return false;
        }


        return true; // All validations passed
    }





    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void navigateToLoginActivity() {
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
