package com.gd.socialio.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.gd.socialio.R;
import com.gd.socialio.databinding.ActivityHomeBinding;
import com.gd.socialio.fragments.AddPostFragment;
import com.gd.socialio.fragments.HomeFragment;
import com.gd.socialio.fragments.ProfileFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class HomeActivity extends AppCompatActivity {

    ActivityHomeBinding homeBinding;
    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    private Fragment currentFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        homeBinding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(homeBinding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        // Initialize HomeFragment as the default fragment
        if (savedInstanceState == null) {
            currentFragment = new HomeFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, currentFragment)
                    .commit();
        }

        homeBinding.logOutIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Create and show a confirmation dialog
                new AlertDialog.Builder(HomeActivity.this)
                        .setTitle("Sign Out")
                        .setMessage("Are you sure you want to sign out?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            // Perform sign-out action
                            mAuth.signOut();
                            Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        })
                        .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                        .show();
            }
        });

        homeBinding.bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                selectedFragment = new HomeFragment();
            }
            else if (itemId == R.id.nav_add) {
                selectedFragment = new AddPostFragment();
            }
            else if (itemId == R.id.nav_profile) {
                selectedFragment = new ProfileFragment();
            }
            else {
                return false;
            }

            // Avoid reloading the same fragment
            if (!selectedFragment.getClass().getSimpleName().equals(currentFragment.getClass().getSimpleName())) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainer, selectedFragment)
                        .addToBackStack(selectedFragment.getClass().getSimpleName())
                        .commit();
                currentFragment = selectedFragment;
            }
            return true;
        });

        getSupportFragmentManager().addOnBackStackChangedListener(this::updateBottomNavigation);

    }

    private void updateBottomNavigation() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
        if (currentFragment instanceof HomeFragment) {
            homeBinding.bottomNavigation.setSelectedItemId(R.id.nav_home);
        } else if (currentFragment instanceof AddPostFragment) {
            homeBinding.bottomNavigation.setSelectedItemId(R.id.nav_add);
        } else if (currentFragment instanceof ProfileFragment) {
            homeBinding.bottomNavigation.setSelectedItemId(R.id.nav_profile);
        }
    }


    @Override
    public void onBackPressed() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
        if (currentFragment instanceof HomeFragment) {
            finish();
        } else {
            // Otherwise, pop the fragment from the back stack
            if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
                getSupportFragmentManager().popBackStack();
            } else {
                super.onBackPressed();
            }
        }
    }
}