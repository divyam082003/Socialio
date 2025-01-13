package com.gd.socialio.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.gd.socialio.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AddPostFragment extends Fragment {

    private EditText postDescription;
    private Button postButton;
    private ImageView postVisibility;
    private FirebaseAuth mAuth;
    private DatabaseReference userPostsRef;
    private boolean isPostPublic = true;
    private String visibilityType = "public";


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add, container, false);

        postDescription = view.findViewById(R.id.postDescription);
        postButton = view.findViewById(R.id.postBtn);
        postVisibility = view.findViewById(R.id.postVisibility);

        mAuth = FirebaseAuth.getInstance();
        String currentUserId = mAuth.getCurrentUser().getUid();
        userPostsRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUserId).child("posts");

        postDescription.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                postButton.setEnabled(s.toString().trim().length() > 0);
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        postButton.setOnClickListener(v -> addPost());
        postVisibility.setOnClickListener(v -> showVisibilityMenu());

        return view;
    }


    private void showVisibilityMenu() {
        PopupMenu popupMenu = new PopupMenu(getContext(), postVisibility);
        popupMenu.getMenuInflater().inflate(R.menu.post_visisbility, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.post_public) {
                visibilityType = "public";
                postVisibility.setImageResource(R.drawable.public_post); // Update to public icon
            }
            else if (item.getItemId() == R.id.post_followers) {
                visibilityType = "follower";
                postVisibility.setImageResource(R.drawable.ic_followers); // Update to follower icon
            }
            else if (item.getItemId() == R.id.post_private) {
                visibilityType = "private";
                postVisibility.setImageResource(R.drawable.private_post); // Update to private icon
            }
            return true;
        });

        popupMenu.show();
    }

    private void addPost() {
        String postText = postDescription.getText().toString().trim();

        if (!postText.isEmpty()) {
            // Generate a unique ID for each post
            String postId = userPostsRef.push().getKey();
            String publishDate = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(new Date());

            // Create a post object with visibility
            Map<String, Object> postMap = new HashMap<>();
            postMap.put("text", postText);
            postMap.put("visibilityType", visibilityType); // Include visibility status
            postMap.put("publishDate", publishDate);

            // Save the post text and visibility under the generated post ID
            userPostsRef.child(postId).setValue(postMap)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            postDescription.setText("");  // Clear the input
                        } else {
                            Toast.makeText(getActivity(), "Failed to add post", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(getActivity(), "Please enter some text", Toast.LENGTH_SHORT).show();
        }
    }
}


