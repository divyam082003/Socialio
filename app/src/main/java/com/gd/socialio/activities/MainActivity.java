package com.gd.socialio.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.gd.socialio.R;
import com.gd.socialio.databinding.ActivityMainBinding;
import com.gd.socialio.fragments.PhoneAuthFragment;
import com.gd.socialio.models.FirebaseStrings;
import com.gd.socialio.models.UserProfile;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class MainActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 100;
    private static final String TAG = "MainActivity";

    ActivityMainBinding mainBinding;
    private FirebaseAuth mAuth;
    private DatabaseReference userDatabaseRef;
    private GoogleSignInClient mGoogleSignInClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mainBinding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        userDatabaseRef = FirebaseDatabase.getInstance().getReference(FirebaseStrings.Users);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // Replace with your Web Client ID
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        mainBinding.loginBT.setOnClickListener(v -> loginUserWithEmail());
        mainBinding.registerNowTV.setOnClickListener(v -> registerUserWithEmail());
        mainBinding.forgotPasswordTV.setOnClickListener(v -> resetPassword());
        mainBinding.googleLoginBT.setOnClickListener(v -> signInWithGoogle());
        mainBinding.phoneLoginBT.setOnClickListener(v -> openPhoneAuthFragment());
    }

    private void registerUserWithEmail() {
        String email = mainBinding.loginEmailET.getText().toString().trim();
        String password = mainBinding.loginPasswordET.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Email and Password cannot be empty", Toast.LENGTH_SHORT).show();
            mainBinding.loginEmailET.setError("Required");
            mainBinding.loginPasswordET.setError("Required");
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        promptForAdditionalInfo(user);
                    } else {
                        Toast.makeText(this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void loginUserWithEmail() {
        String email = mainBinding.loginEmailET.getText().toString().trim();
        String password = mainBinding.loginPasswordET.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Email and Password cannot be empty", Toast.LENGTH_SHORT).show();
            mainBinding.loginEmailET.setError("Required");
            mainBinding.loginPasswordET.setError("Required");
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        redirectToHome();
                    } else {
                        Toast.makeText(this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void resetPassword() {
        String email = mainBinding.loginEmailET.getText().toString().trim();

        if (email.isEmpty()) {
            Toast.makeText(this, "Enter your registered email to reset password", Toast.LENGTH_SHORT).show();
            mainBinding.loginEmailET.setError("Required");
            return;
        }

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Password reset email sent", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            try {
                GoogleSignInAccount account = GoogleSignIn.getSignedInAccountFromIntent(data).getResult(ApiException.class);
                if (account != null) {
                    firebaseAuthWithGoogle(account);
                }
            } catch (ApiException e) {
                Log.w(TAG, "Google sign-in failed", e);
                Toast.makeText(this, "Google sign-in failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (task.getResult().getAdditionalUserInfo().isNewUser()) {
                            promptForAdditionalInfo(user);
                        } else {
                            redirectToHome();
                        }
                    } else {
                        Toast.makeText(this, "Authentication Failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void openPhoneAuthFragment() {
        mainBinding.fragmentContainer.setVisibility(View.VISIBLE);
        PhoneAuthFragment phoneAuthFragment = new PhoneAuthFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentContainer, phoneAuthFragment);
        transaction.addToBackStack(null);  // Optional, allows back navigation
        transaction.commit();
    }

    public void hideFragmentContainer() {
        mainBinding.fragmentContainer.setVisibility(View.GONE); // Hide the fragment container
    }

    private void promptForAdditionalInfo(FirebaseUser user) {
        // Collect additional info such as name, phone number, etc.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Profile Details");

        // Inflate a custom view with EditTexts for name and phone
        View customView = getLayoutInflater().inflate(R.layout.dialog_user_info, null);
        EditText nameET = customView.findViewById(R.id.nameET);
        EditText phoneET = customView.findViewById(R.id.phoneET);
        builder.setView(customView);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String name = nameET.getText().toString().trim();
            String phone = phoneET.getText().toString().trim();

            if (!name.isEmpty() && !phone.isEmpty()) {
                saveUserToDatabase(user, name, "+91"+phone);
            } else {
                Toast.makeText(MainActivity.this, "Please enter all details", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    public void saveUserToDatabase(FirebaseUser user, String name, String phone) {
        UserProfile userProfile = new UserProfile(name, phone, user.getEmail(), user.getUid());
        userDatabaseRef.child(user.getUid()).setValue(userProfile)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        redirectToHome();
                    } else {
                        Toast.makeText(MainActivity.this, "Failed to save user info", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onBackPressed() {
        // If fragment is visible, close it instead of exiting the activity
        if (mainBinding.fragmentContainer.getVisibility() == View.VISIBLE) {
            // Pop the fragment from the back stack
            getSupportFragmentManager().popBackStack(); // Removes fragment from back stack
            mainBinding.fragmentContainer.setVisibility(View.GONE);
        } else {
            super.onBackPressed(); // Default behavior if fragment is not open
        }
    }

    private void redirectToHome() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // Check if user data (name and phone) exists
            userDatabaseRef.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        UserProfile userProfile = snapshot.getValue(UserProfile.class);
                        if (userProfile != null && userProfile.getName() != null && userProfile.getPhone() != null) {
                            // If data is complete, proceed to home
                            startActivity(new Intent(MainActivity.this, HomeActivity.class));
                            finish();
                        } else {
                            // Prompt for missing details
                            promptForAdditionalInfo(currentUser);
                        }
                    } else {
                        // Prompt for additional details if no data exists
                        promptForAdditionalInfo(currentUser);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(MainActivity.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

}