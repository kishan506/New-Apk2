package com.example.project1.activites;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.project1.R;
import com.example.project1.api.FastApiService;
import com.example.project1.model.NewPassword;
import com.example.project1.model.ResetPasswordResponse;
import com.example.project1.sessionmanagement.UserSharedPreference;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ResetPasswordActivity extends AppCompatActivity {

    private EditText editTextNewPassword;
    private EditText editTextConfirmPassword;
    private Button buttonResetPassword;
    private static Retrofit retrofit;
    private static String BASE_URL = "http://";
    private UserSharedPreference sh;
    public String phone;
    public Retrofit getClient() {
        if (retrofit == null) {
            OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

            // Add logging interceptor for debugging purposes
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            httpClient.addInterceptor(logging);

            BASE_URL = BASE_URL + sh.getIP() + "/";
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(httpClient.build())
                    .build();
        }
        return retrofit;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_reset_password);
        sh = new UserSharedPreference(this);
        phone =sh.getPhone();
        editTextNewPassword = findViewById(R.id.editTextNewPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        buttonResetPassword = findViewById(R.id.buttonResetPassword);

        buttonResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Retrieve data from EditText
                String newPassword = editTextNewPassword.getText().toString().trim();
                String confirmPassword = editTextConfirmPassword.getText().toString().trim();

                // Perform input validation
                if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
                    // If the passwords are empty, show a "required" Toast
                    Toast.makeText(ResetPasswordActivity.this, "New Password and Confirm Password are required", Toast.LENGTH_SHORT).show();
                } else if (!newPassword.equals(confirmPassword)) {
                    // If passwords do not match, show a Toast
                    Toast.makeText(ResetPasswordActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                } else {
                    // Handle the password reset logic
                    sh.setpassword(newPassword);
                    new ResetPasswordTask().execute(newPassword);
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//                    String newPassword = "newPassword123";

                    user.updatePassword(newPassword)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task <Void> task) {
                                    if (task.isSuccessful()) {
                                        Log.d("TAG", "User password updated successfully");
                                        // Password updated successfully
                                    } else {
                                        Log.d("TAG", "Failed to update user password");
                                        // Handle failure to update password
                                    }
                                }
                            });

                }
            }
        });
    }

    private void resetPassword(String newPassword) {
        // Implement your password reset logic here
        // You can use newPassword for further processing
        Toast.makeText(ResetPasswordActivity.this, "Password reset logic goes here", Toast.LENGTH_SHORT).show();
    }

    private class ResetPasswordTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String newPassword = params[0];

            // Make Retrofit API call here
            FastApiService apiService = getClient().create(FastApiService.class);
            Log.d("new Password", "doInBackground: "+newPassword);
            ResetPasswordResponse ResetPasswordResponse = new ResetPasswordResponse(phone,newPassword);
            // Replace YourResponseModel with the actual class representing the response
            Call<ResetPasswordResponse> call = apiService.reset_password(ResetPasswordResponse);

            try {
                Response<ResetPasswordResponse> response = call.execute();
                if (response.isSuccessful()) {

                    finish();

                    return "Password reset successful";

                } else {
                    return "Failed to reset password";
                }
            } catch (IOException e) {
                e.printStackTrace();
                return "Failed to reset password";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            // Update UI based on the result
            Toast.makeText(ResetPasswordActivity.this, result, Toast.LENGTH_LONG).show();
        }
    }
}
