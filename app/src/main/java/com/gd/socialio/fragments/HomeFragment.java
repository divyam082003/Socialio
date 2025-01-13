package com.gd.socialio.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gd.socialio.R;
import com.gd.socialio.adapters.PostAdapter;
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

public class HomeFragment extends Fragment {

    private RecyclerView recyclerViewPosts;
    private SearchView searchBarPost;
    private PostAdapter postAdapter;
    private List<PostModel> postModelList,filteredList;
    private DatabaseReference databaseReference;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        postModelList = new ArrayList<>();
        filteredList = new ArrayList<>();
        databaseReference = FirebaseDatabase.getInstance().getReference(FirebaseStrings.Users);
        loadPostsFromFirebase();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerViewPosts = view.findViewById(R.id.recyclerViewPosts);
        searchBarPost = view.findViewById(R.id.searchBarPost);
        recyclerViewPosts.setLayoutManager(new LinearLayoutManager(getContext()));

        postAdapter = new PostAdapter(getContext(), postModelList);
        recyclerViewPosts.setAdapter(postAdapter);

        searchBarPost.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterPosts(query);
                return true;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                filterPosts(newText);
                return true;
            }
        });
        return view;
    }

    private void filterPosts(String query) {
        filteredList = new ArrayList<>();
        if (TextUtils.isEmpty(query)) {
            filteredList.addAll(postModelList);
        } else {
            for (PostModel post : postModelList) {
                if (post.getPostText() != null && post.getPostText().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(post);
                } else if (post.getUserName() != null && post.getUserName().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(post);
                }
            }
        }
        postAdapter.updateList(filteredList);
    }

    private void loadPostsFromFirebase() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postModelList.clear();

                SimpleDateFormat fullFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
                SimpleDateFormat dateOnlyFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());

                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    String userName = userSnapshot.child(FirebaseStrings.name).getValue(String.class);
                    String userId = userSnapshot.getKey();
                    DataSnapshot postsSnapshot = userSnapshot.child(FirebaseStrings.posts);

                    boolean isCurrentUser = userId.equals(currentUserId);
                    boolean isFollower = userSnapshot.child(FirebaseStrings.followers).hasChild(currentUserId);
                    boolean isBlockedCurrentUser = userSnapshot.child(FirebaseStrings.blockedUsers).hasChild(currentUserId);

                    // Skip posts of the author who has blocked the current user
                    if (isBlockedCurrentUser) {
                        continue;
                    }

                    for (DataSnapshot postSnapshot : postsSnapshot.getChildren()) {
                        String postContent = postSnapshot.child(FirebaseStrings.text).getValue(String.class);
                        String visibilityType = postSnapshot.child(FirebaseStrings.visibilityType).getValue(String.class);
                        Boolean isPublic = postSnapshot.child(FirebaseStrings.isPublic).getValue(Boolean.class);
                        String publishDate = postSnapshot.child(FirebaseStrings.publishDate).getValue(String.class);
                        Boolean isEdited = postSnapshot.child(FirebaseStrings.isEdited).getValue(Boolean.class);
                        String postId = postSnapshot.getKey();
                        boolean isLikedByCurrentUser = postSnapshot.child(FirebaseStrings.likes).hasChild(currentUserId);
                        boolean isDislikedByCurrentUser = postSnapshot.child(FirebaseStrings.dislikes).hasChild(currentUserId);

                        int likesCount = (int) postSnapshot.child(FirebaseStrings.likes).getChildrenCount();
                        int dislikesCount = (int) postSnapshot.child(FirebaseStrings.dislikes).getChildrenCount();
                        int commentCount = (int) postSnapshot.child(FirebaseStrings.comments).getChildrenCount();

                        String formattedDate = publishDate;
                        if (Boolean.TRUE.equals(isEdited)) {
                            formattedDate += " (edited)";
                        }

                        if ("public".equals(visibilityType) ||
                                (visibilityType == null && Boolean.TRUE.equals(isPublic)) ||
                                (isCurrentUser) ||
                                ("follower".equals(visibilityType) && isFollower) ||
                                (visibilityType == null && Boolean.FALSE.equals(isPublic) && isCurrentUser)
                        ) {

                            postModelList.add(new PostModel(
                                    userName,
                                    postContent,
                                    likesCount,
                                    dislikesCount,
                                    commentCount,
                                    postId,
                                    userId,
                                    formattedDate,
                                    Boolean.TRUE.equals(isPublic),
                                    isLikedByCurrentUser,
                                    isDislikedByCurrentUser
                            ));
                        }
                    }
                }

                Collections.sort(postModelList, (post1, post2) -> {
                    try {
                        Date date1 = parseDateWithFallback(post1.getPublishDate(), fullFormat, dateOnlyFormat);
                        Date date2 = parseDateWithFallback(post2.getPublishDate(), fullFormat, dateOnlyFormat);
                        return date2.compareTo(date1); // Descending order
                    } catch (Exception e) {
                        e.printStackTrace();
                        return 0; // If parsing fails, leave order unchanged
                    }
                });
                
                postAdapter.notifyDataSetChanged();
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("HomeFragment", "Database error: " + databaseError.getMessage());
            }
        });
    }

    private Date parseDateWithFallback(String dateString, SimpleDateFormat fullFormat, SimpleDateFormat dateOnlyFormat) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return new Date(0);
        }
        try {
            return fullFormat.parse(dateString);
        } catch (ParseException e) {
            try {
                return dateOnlyFormat.parse(dateString);
            } catch (ParseException ex) {
                ex.printStackTrace();
                return new Date(0);
            }
        }
    }

}
