package com.gd.socialio.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.gd.socialio.R;
import com.gd.socialio.databinding.ActivitySplashBinding;
import com.gd.socialio.models.UserProfile;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SplashActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference userDatabaseRef;

    ActivitySplashBinding splashBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        splashBinding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(splashBinding.getRoot());

        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        splashBinding.appLogo.startAnimation(fadeIn);
        splashBinding.splashTV.startAnimation(fadeIn);

        mAuth = FirebaseAuth.getInstance();
        userDatabaseRef = FirebaseDatabase.getInstance().getReference("Users");
        new Handler().postDelayed(this::checkUserStatus, 2000);

    }

    private void checkUserStatus() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // If the user is logged in, check if profile info is complete
            userDatabaseRef.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        UserProfile userProfile = snapshot.getValue(UserProfile.class);
                        if (userProfile != null && userProfile.getName() != null && userProfile.getPhone() != null) {
                            // User info is complete, redirect to HomeActivity
                            redirectToHome();
                        } else {
                            // Prompt for additional info by going to MainActivity (login/registration)
                            redirectToLogin();
                        }
                    } else {
                        // No user data exists, redirect to MainActivity
                        redirectToLogin();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(SplashActivity.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    redirectToLogin(); // Fallback to login on error
                }
            });
        } else {
            // User not logged in, redirect to MainActivity
            redirectToLogin();
        }
    }


    private void redirectToLogin() {
        Intent loginIntent = new Intent(SplashActivity.this, MainActivity.class);
        startActivity(loginIntent);
        finish();
    }

    private void redirectToHome() {
        Intent homeIntent = new Intent(SplashActivity.this, HomeActivity.class);
        startActivity(homeIntent);
        finish();
    }
}