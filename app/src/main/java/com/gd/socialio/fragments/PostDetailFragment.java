package com.gd.socialio.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.gd.socialio.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PostDetailFragment extends Fragment {

    private static final String ARG_POST_ID = "postId";
    private boolean isEditModeActive = false;

    private DatabaseReference postRef;
    private FirebaseAuth auth;
    private String postId,currentUserId;

    private EditText postTextView;
    private TextView userName,publishDateView,likeCountTV,dislikeCountTV;
    private ImageView editButton,likeIcon,dislikeIcon;

    LinearLayout postLikeLL,postDisLikeLL;
    private Button postButton;

    public static PostDetailFragment newInstance(String postId) {
        PostDetailFragment fragment = new PostDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_POST_ID, postId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_postdetailview, container, false);

        userName = view.findViewById(R.id.DPVuserName);
        postTextView = view.findViewById(R.id.DPVpostText);
        publishDateView = view.findViewById(R.id.DPVdateTV);
        editButton = view.findViewById(R.id.DPVeditBT);
        postButton = view.findViewById(R.id.DPVpostBtn);
        likeIcon = view.findViewById(R.id.DPVlikeIcon);
        dislikeIcon = view.findViewById(R.id.DPVdislikeIcon);
        likeCountTV =  view.findViewById(R.id.DPVlikeCount);
        dislikeCountTV =  view.findViewById(R.id.DPVdislikeCount);
        postDisLikeLL = view.findViewById(R.id.postDisLikeLL);
        postLikeLL = view.findViewById(R.id.postLikeLL);
        postButton.setVisibility(View.GONE);

        if (getArguments() != null) {
            postId = getArguments().getString(ARG_POST_ID);
        }

        auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser().getUid();

        postRef = FirebaseDatabase.getInstance().getReference("Users")
                .child(currentUserId)
                .child("posts")
                .child(postId);

        fetchPostDetails();
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setupEditButton();
            }
        });
        setupLikeDislikeListeners();
        return view;
    }

    private void fetchPostDetails() {
        // Fetch post details from Firebase
        postRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String postText = snapshot.child("text").getValue(String.class);
                    String publishDate = snapshot.child("publishDate").getValue(String.class);
                    boolean hasLiked = snapshot.child("likes").hasChild(currentUserId);
                    boolean hasDisliked = snapshot.child("dislikes").hasChild(currentUserId);

                    updateLikeDislikeIcons(hasLiked, hasDisliked);

                    if (hasLiked) {
                        likeIcon.setColorFilter(Color.BLUE);
                        dislikeIcon.setColorFilter(Color.GRAY);
                    }
                    else if (hasDisliked) {
                        likeIcon.setColorFilter(Color.GRAY);
                        dislikeIcon.setColorFilter(Color.RED);
                    }
                    else {
                        likeIcon.setColorFilter(Color.GRAY);
                        dislikeIcon.setColorFilter(Color.GRAY);
                    }

                    postRef.child("likes").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            int likesCount = (int) dataSnapshot.getChildrenCount(); // Count the number of users who liked the post
                            likeCountTV.setText(String.valueOf(likesCount)); // Update UI
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            // Handle database error
                        }
                    });

                    postRef.child("dislikes").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            int dislikesCount = (int) dataSnapshot.getChildrenCount(); // Count the number of users who disliked the post
                            dislikeCountTV.setText(String.valueOf(dislikesCount)); // Update UI
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            // Handle database error
                        }
                    });

                    DatabaseReference currentUserRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUserId);
                    currentUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                            if (userSnapshot.exists()) {
                                String currentUserName = userSnapshot.child("name").getValue(String.class);
                                userName.setText(currentUserName);
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.d("PostDetailFragment", "Error fetching current user details: " + error.getMessage());
                        }
                    });

                    postTextView.setText(postText);
                    publishDateView.setText(publishDate);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("PostDetailFragment", "Error: " + error.getMessage());
            }
        });
    }

    private void setupEditButton() {

        if (isEditModeActive) {
            // If already in edit mode, disable it
            isEditModeActive = false;
            postTextView.setFocusableInTouchMode(false);  // Disable editing mode
            postButton.setVisibility(View.GONE);  // Hide postButton
        } else {
            // If not in edit mode, enable it
            isEditModeActive = true;
            postTextView.setFocusableInTouchMode(true);  // Enable editing mode
            postButton.setVisibility(View.VISIBLE);  // Show postButton
            postButton.setOnClickListener(v -> updatePostDetails());
        }

    }

    private void updatePostDetails() {
        String updatedText = postTextView.getText().toString();
        String currentDate = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(new Date());

        // Update the post in Firebase
        postRef.child("text").setValue(updatedText);
        postRef.child("publishDate").setValue(currentDate);
        postRef.child("isEdited").setValue(true);

        // Update the UI
        publishDateView.setText(currentDate);
        postButton.setVisibility(View.GONE); // Hide post button after saving
        postTextView.setFocusable(false);
    }


    private void setupLikeDislikeListeners() {
        postLikeLL.setOnClickListener(v -> toggleLike(true));
        postDisLikeLL.setOnClickListener(v -> toggleLike(false));
    }

    private void toggleLike(boolean isLike) {
        postRef.child(isLike ? "likes" : "dislikes").child(currentUserId).setValue(true);
        postRef.child(isLike ? "dislikes" : "likes").child(currentUserId).removeValue();

        fetchPostDetails(); // Refresh UI
    }

    private void updateLikeDislikeIcons(boolean hasLiked, boolean hasDisliked) {
        likeIcon.setColorFilter(hasLiked ? Color.BLUE : Color.GRAY);
        dislikeIcon.setColorFilter(hasDisliked ? Color.RED : Color.GRAY);
    }
}
