package com.example.project1.activites;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.project1.R;
import com.example.project1.api.FastApiService;
import com.example.project1.sessionmanagement.UserSharedPreference;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class PhoneValidationActivity extends AppCompatActivity {

    private EditText editTextPhoneNumber;
    private Button buttonCheckPhoneNumber;
    private static String BASE_URL = "http://"; // Replace with your FastAPI base URL
    private UserSharedPreference sh;

    private FirebaseAuth firebaseAuth;
    private String verificationId;

    private FastApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_phone_verification);
        Spinner spinnerCountryCode = findViewById(R.id.spinnerCountryCode);

// Define the array of country codes with "+91" as the default value
        String[] countryCodes = {"+91", "+1", "+44", "+61", "+81", "+86"};

// Create an ArrayAdapter using the countryCodes array
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, countryCodes);

// Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

// Apply the adapter to the spinner
        spinnerCountryCode.setAdapter(adapter);

// Set the default selection to "+91"
        spinnerCountryCode.setSelection(adapter.getPosition("+91"));



        sh = new UserSharedPreference(this);

        editTextPhoneNumber = findViewById(R.id.editTextPhoneNumber);
        buttonCheckPhoneNumber = findViewById(R.id.buttonCheckPhoneNumber);

        firebaseAuth = FirebaseAuth.getInstance();

        // Initialize Retrofit
        BASE_URL=BASE_URL+sh.getIP()+"/";
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();

        // Create an instance of your Retrofit interface
        apiService = retrofit.create(FastApiService.class);

        buttonCheckPhoneNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Retrieve data from EditText
                String phoneNumber = editTextPhoneNumber.getText().toString().trim();

                Spinner spinnerCountryCode = findViewById(R.id.spinnerCountryCode);
                String countryCode = spinnerCountryCode.getSelectedItem().toString();

                // Combine the country code and phone number
                String fullPhoneNumber = countryCode + phoneNumber;
                // Perform input validation
                if (TextUtils.isEmpty(phoneNumber)) {
                    // If the phone number is empty, show a "required" Toast
                    Toast.makeText(PhoneValidationActivity.this, "Phone number is required", Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(PhoneValidationActivity.this, "Your phone Number is Found....! wait for OTP", Toast.LENGTH_SHORT).show();
                    // If the phone number is perfect, send OTP and check if it exists
                    sh.addPhone(fullPhoneNumber);
                    sendOtpAndCheckExistence(fullPhoneNumber);
                }
            }
        });
    }

    private void sendOtpAndCheckExistence(final String phoneNumber) {
        // Make the API call using Retrofit to check if the phone number exists
        Call<String> call = apiService.resetPassword(phoneNumber);
        Log.d("", "sendOtpAndCheckExistence: "+call.toString());
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) {
                    String result = response.body();
                    if (result != null && result.equals("Phone No not found")) {
                        // Phone does not exist, show an appropriate message
                        Toast.makeText(PhoneValidationActivity.this, "Phone number not found", Toast.LENGTH_SHORT).show();
                    } else {
                        // Phone exists, proceed with sending OTP
                        sendOtp(phoneNumber);
                        Toast.makeText(PhoneValidationActivity.this, "Firebase : Please Wait For Captcha Confirmation  ", Toast.LENGTH_LONG).show();
                    }
                } else {
                    // Handle unsuccessful response
                    Toast.makeText(PhoneValidationActivity.this, "API call failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                // Handle network failure
                Toast.makeText(PhoneValidationActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendOtp(String phoneNumber) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,
                60,  // Timeout duration
                java.util.concurrent.TimeUnit.SECONDS,
                this,
                new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                        // Automatically verify the phone number (without user input) if the verification is successful
                        Toast.makeText(PhoneValidationActivity.this, "Captcha Confirmation Process Done...!", Toast.LENGTH_SHORT).show();

                        signInWithPhoneAuthCredential(credential);
                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        Toast.makeText(PhoneValidationActivity.this, "Failed to send OTP. Please try again.", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCodeSent(@NonNull String verificationId,
                                           @NonNull PhoneAuthProvider.ForceResendingToken token) {
                        // Save the verification ID and resend token for later use
                        PhoneValidationActivity.this.verificationId = verificationId;
                        // Navigate to the OTP verification screen
                        Intent intent = new Intent(PhoneValidationActivity.this, OtpVerificationActivity.class);
                        intent.putExtra("verificationId", verificationId);
                        startActivity(intent);
                        finish();

                    }
                });
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<com.google.firebase.auth.AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<com.google.firebase.auth.AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Phone number verification successful
                            Toast.makeText(PhoneValidationActivity.this, "Phone number verified successfully", Toast.LENGTH_SHORT).show();
                            // You can also navigate to a different activity if needed
                            Intent intent = new Intent(PhoneValidationActivity.this, MainActivity.class);
                            startActivity(intent);
                        } else {
                            // Phone number verification failed
                            Toast.makeText(PhoneValidationActivity.this, "Failed to verify phone number. Please try again.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
