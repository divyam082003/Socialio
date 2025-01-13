package com.gd.socialio.fragments;

import android.os.Bundle;
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
import com.gd.socialio.adapters.FriendAdapter;
import com.gd.socialio.models.FirebaseStrings;
import com.gd.socialio.models.UserProfile;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AddFriendFragment extends Fragment {
    private RecyclerView recyclerView;
    private FriendAdapter friendAdapter;
    private List<UserProfile> userList;
    private List<UserProfile> displayedList;
    private Set<String> addedUserIds;
    private DatabaseReference usersRef,currentUserRef;
    private String currentUserId;
    private String listType;

    private SearchView searchBarUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_addfriend, container, false);
        searchBarUser = view.findViewById(R.id.searchBarUser);
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference(FirebaseStrings.Users);
        currentUserRef = FirebaseDatabase.getInstance().getReference(FirebaseStrings.Users).child(currentUserId);
        if (getArguments() != null) {
            listType = getArguments().getString("listType", "all");
        }
        recyclerView = view.findViewById(R.id.usersRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        userList = new ArrayList<>();
        displayedList = new ArrayList<>();
        addedUserIds = new HashSet<>();
        friendAdapter = new FriendAdapter(getContext(),userList,getFragmentManager());
        recyclerView.setAdapter(friendAdapter);
        loadUsers();

        searchBarUser.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterUsers(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterUsers(newText);
                return true;
            }
        });
        return view;
    }

    private void loadUsers() {
        userList.clear();
        displayedList.clear();
        addedUserIds.clear();
        if (FirebaseStrings.followers.equals(listType)) {
            currentUserRef.child(FirebaseStrings.followers).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot followerSnapshot : snapshot.getChildren()) {
                        String followerId = followerSnapshot.getKey();
                        loadUserProfile(followerId);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        }
        else if (FirebaseStrings.following.equals(listType)) {
            currentUserRef.child(FirebaseStrings.following).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot followingSnapshot : snapshot.getChildren()) {
                        String followingId = followingSnapshot.getKey();
                        loadUserProfile(followingId);
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        }
        else {
            usersRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        String userId = userSnapshot.getKey();
                        if (!currentUserId.equals(userId)){
                            loadUserProfile(userId);
                        }
                    }

                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        }
    }

    private void filterUsers(String query) {
        displayedList.clear();
        if (query.isEmpty()) {
            displayedList.addAll(userList);
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (UserProfile user : userList) {
                if (user.getName().toLowerCase().contains(lowerCaseQuery)) {
                    displayedList.add(user);
                }
            }
        }
        friendAdapter.updateData(displayedList);
    }

    private void loadUserProfile(String userId) {
        if (!addedUserIds.contains(userId)) {
            usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    UserProfile user = snapshot.getValue(UserProfile.class);
                    if (user != null) {
                        userList.add(user);
                        addedUserIds.add(userId);
                        friendAdapter.notifyDataSetChanged();
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        }
    }

}
