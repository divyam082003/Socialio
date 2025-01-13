package com.gd.socialio.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.gd.socialio.R;
import com.gd.socialio.adapters.ProfilePostAdapter;
import com.gd.socialio.models.FirebaseStrings;
import com.gd.socialio.models.PostModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ProfileFragment extends Fragment {

    private RecyclerView profilePostRV;
    private TextView userName,postCountTV,followersCountTV, followingCountTV;
    private LinearLayout followersCountTVLL,followingsCountTVLL;
    private ImageView addFriendIV,followReqIV;
    private ProfilePostAdapter postAdapter;
    private List<PostModel> postList;

    private DatabaseReference userPostsRef,userRef;
    private FirebaseAuth mAuth;
    private static final String ARG_USER_ID = "userId";

    private String userId;

    public static ProfileFragment newInstance(String userId) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_USER_ID, userId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        mAuth = FirebaseAuth.getInstance();
        String currentUserId = mAuth.getCurrentUser().getUid();
        userId = getArguments() != null ? getArguments().getString(ARG_USER_ID, currentUserId) : currentUserId;
        userRef = FirebaseDatabase.getInstance().getReference(FirebaseStrings.Users).child(userId);
        userPostsRef = userRef.child(FirebaseStrings.posts);
        profilePostRV = view.findViewById(R.id.profilePostRV);
        userName = view.findViewById(R.id.profileUserNameTV);
        addFriendIV = view.findViewById(R.id.addFriendIV);
        postCountTV = view.findViewById(R.id.postCountTV);
        followersCountTV = view.findViewById(R.id.followersCountTV);
        followingCountTV = view.findViewById(R.id.followingsCountTV);
        followingsCountTVLL = view.findViewById(R.id.followingsCountTVLL);
        followersCountTVLL = view.findViewById(R.id.followersCountTVLL);
        followReqIV = view.findViewById(R.id.followReqIV);

        if (userId.equals(currentUserId)) {
            addFriendIV.setVisibility(View.VISIBLE);
            setupFollowersAndFollowingClickListeners();
        }
        else {
            addFriendIV.setVisibility(View.GONE);
        }

        profilePostRV.setLayoutManager(new GridLayoutManager(getContext(), 3));
        postList = new ArrayList<>();
        postAdapter = new ProfilePostAdapter(postList, postId -> {
            if (userId.equals(currentUserId)) {
                openPostDetailFragment(postId);
            }
        });

        fetchPostsFromFirebase();
        fetchFollowersAndFollowingCounts();
        profilePostRV.setAdapter(postAdapter);
        addFriendIV.setOnClickListener(v -> openAddFriendFragment(null));
        return view;
    }

    private void setupFollowersAndFollowingClickListeners() {
        followersCountTVLL.setOnClickListener(v -> openAddFriendFragment("followers"));
        followingsCountTVLL.setOnClickListener(v -> openAddFriendFragment("following"));
    }

    private void openAddFriendFragment(String listType) {
        Fragment addFriendFragment = new AddFriendFragment();
        // Pass the list type to the AddFriendFragment (followers or following)
        Bundle args = new Bundle();
        args.putString("listType", listType);
        addFriendFragment.setArguments(args);

        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, addFriendFragment)
                .addToBackStack(null)
                .commit();
    }

    private void fetchPostsFromFirebase() {
       userRef.addValueEventListener(new ValueEventListener() {
           @Override
           public void onDataChange(@NonNull DataSnapshot snapshot) {
               String name = snapshot.child(FirebaseStrings.name).getValue(String.class);
               userName.setText(name);
           }
           @Override
           public void onCancelled(@NonNull DatabaseError error) {

           }
       });

       userPostsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();

                SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
                SimpleDateFormat dateFormatShort = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());

                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    String postId = postSnapshot.getKey();
                    String postText = postSnapshot.child(FirebaseStrings.text).getValue(String.class);
                    int likesCount = 0;
                    if (postSnapshot.child(FirebaseStrings.likes).exists()) {
                        likesCount = (int) postSnapshot.child(FirebaseStrings.likes).getChildrenCount();
                    }
                    int dislikesCount = 0;
                    if (postSnapshot.child(FirebaseStrings.dislikes).exists()) {
                        dislikesCount = (int) postSnapshot.child(FirebaseStrings.dislikes).getChildrenCount();
                    }

                    String publishDateStr = postSnapshot.child(FirebaseStrings.publishDate).getValue(String.class);

                    PostModel postModel = new PostModel(postText,likesCount,dislikesCount,postId);
                    postList.add(postModel);
                }

                Collections.sort(postList, (post1, post2) -> {
                    String publishDateStr1 = post1.getPublishDate();
                    String publishDateStr2 = post2.getPublishDate();

                    Date date1 = null;
                    Date date2 = null;

                    // Try to parse publishDateStr1
                    if (publishDateStr1 != null) {
                        try {
                            date1 = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).parse(publishDateStr1);
                        } catch (ParseException e) {
                            try {
                                date1 = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).parse(publishDateStr1);
                            } catch (ParseException ex) {
                                date1 = null;
                            }
                        }
                    }
                    if (publishDateStr2 != null) {
                        try {
                            date2 = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).parse(publishDateStr2);
                        } catch (ParseException e) {
                            try {
                                date2 = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).parse(publishDateStr2);
                            } catch (ParseException ex) {
                                date2 = null;
                            }
                        }
                    }
                    if (date1 == null && date2 == null) {
                        return 0;
                    } else if (date1 == null) {
                        return 1;
                    } else if (date2 == null) {
                        return -1;
                    } else {
                        return date2.compareTo(date1);
                    }
                });
                
                postCountTV.setText(String.valueOf(postList.size()));
                postAdapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void fetchFollowersAndFollowingCounts() {
        userRef.child(FirebaseStrings.followers).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int followersCount = (int) snapshot.getChildrenCount();
                followersCountTV.setText(String.valueOf(followersCount));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
        userRef.child(FirebaseStrings.following).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int followingCount = (int) snapshot.getChildrenCount();
                followingCountTV.setText(String.valueOf(followingCount));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
        userRef.child(FirebaseStrings.followRequests).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int followReqCount = (int) snapshot.getChildrenCount();
                if (followReqCount >0){
                    followReqIV.setVisibility(View.VISIBLE);
                }
                else followReqIV.setVisibility(View.GONE);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });

    }

    private void openPostDetailFragment(String postId) {
        PostDetailFragment postDetailFragment = PostDetailFragment.newInstance(postId);
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, postDetailFragment)
                .addToBackStack(null)
                .commit();
    }
}

