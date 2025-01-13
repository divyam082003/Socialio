package com.gd.socialio.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.gd.socialio.R;
import com.gd.socialio.activities.HomeActivity;
import com.gd.socialio.activities.MainActivity;
import com.gd.socialio.models.UserProfile;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.concurrent.TimeUnit;

public class PhoneAuthFragment extends Fragment {

    private EditText phoneEditText, otpEditText;
    private Button getOtpButton, verifyOtpButton;
    private FirebaseAuth mAuth;
    private DatabaseReference userDatabaseRef;
    private String verificationId;
    private View view;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_phone_auth, container, false);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        userDatabaseRef = FirebaseDatabase.getInstance().getReference("Users");

        // Initialize Views
        phoneEditText = view.findViewById(R.id.phoneEditText);
        otpEditText = view.findViewById(R.id.otpEditText);
        getOtpButton = view.findViewById(R.id.getOtpButton);
        verifyOtpButton = view.findViewById(R.id.verifyOtpButton);

        // Get OTP Button Listener
        getOtpButton.setOnClickListener(v -> sendOtpCode());

        // Verify OTP Button Listener
        verifyOtpButton.setOnClickListener(v -> verifyOtpCode());

        return view;
    }

    private void sendOtpCode() {
        String phoneNumber = phoneEditText.getText().toString().trim();

        if (TextUtils.isEmpty(phoneNumber)) {
            phoneEditText.setError("Phone number is required");
            return;
        }
        else {
            phoneNumber = "+91"+phoneNumber;
        }

        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(getActivity())
                .setCallbacks(mCallbacks)
                .build();
        PhoneAuthProvider.verifyPhoneNumber(options);

        otpEditText.setVisibility(View.VISIBLE);
        verifyOtpButton.setVisibility(View.VISIBLE);
    }

    private final PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks =
            new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                @Override
                public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                    // Auto-retrieval of OTP, directly use the credential to sign in
                    signInWithPhoneAuthCredential(credential);
                }

                @Override
                public void onVerificationFailed(@NonNull FirebaseException e) {
                    Toast.makeText(getActivity(), "Verification Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    removeFragment();
                }

                @Override
                public void onCodeSent(@NonNull String verificationId,
                                       @NonNull PhoneAuthProvider.ForceResendingToken token) {
                    PhoneAuthFragment.this.verificationId = verificationId;
                    Toast.makeText(getActivity(), "Code Sent", Toast.LENGTH_SHORT).show();
                }
            };

    private void verifyOtpCode() {
        String code = otpEditText.getText().toString().trim();
        if (TextUtils.isEmpty(code)) {
            otpEditText.setError("Enter OTP");
            return;
        }

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential){
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        FirebaseUser user = mAuth.getCurrentUser();

                        // Check if this is a new user, then prompt for additional info
                        if (task.getResult().getAdditionalUserInfo().isNewUser()) {
                            showUserInfoDialog(user);
                        } else {
                            redirectToHome();
                        }
                    } else {
                        Toast.makeText(getActivity(), "Authentication Failed", Toast.LENGTH_SHORT).show();
                        removeFragment();
                    }
                });
    }

    private void showUserInfoDialog(FirebaseUser user) {
        // Show a dialog to prompt for the user's name
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Enter Profile Details");

        // Inflate the custom view for user information
        View customView = getLayoutInflater().inflate(R.layout.dialog_user_info, null);
        EditText nameET = customView.findViewById(R.id.nameET);
        EditText emailET = customView.findViewById(R.id.phoneET);
        emailET.setHint("Enter Email");
        emailET.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        builder.setView(customView);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String name = nameET.getText().toString().trim();
            String email = emailET.getText().toString().trim();

            if (!name.isEmpty() && !email.isEmpty()) {
                saveUserToDatabase(user, name, email);
            } else {
                Toast.makeText(getActivity(), "Please enter all details", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }


    private void saveUserToDatabase(FirebaseUser user, String name,String email) {
        // Save user information to Firebase
        UserProfile userProfile = new UserProfile(name, user.getPhoneNumber(),email, user.getUid());
        userDatabaseRef.child(user.getUid()).setValue(userProfile)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        redirectToHome();
                    } else {
                        Toast.makeText(getActivity(), "Failed to save user info", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void redirectToHome() {
        Intent intent = new Intent(getActivity(), HomeActivity.class);
        startActivity(intent);
        getActivity().finish();
    }

    private void removeFragment() {
            // Close the fragment and hide the container in the MainActivity
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().popBackStack();
                ((MainActivity) getActivity()).hideFragmentContainer();
            }
        }
}
