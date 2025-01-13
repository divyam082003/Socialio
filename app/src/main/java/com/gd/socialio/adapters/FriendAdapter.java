package com.gd.socialio.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.gd.socialio.R;
import com.gd.socialio.fragments.ProfileFragment;
import com.gd.socialio.models.FirebaseStrings;
import com.gd.socialio.models.UserProfile;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.FriendViewHolder> {

    private Context context;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference usersRef;
    private FragmentManager fragmentManager;
    private String currentUserId;
    private  UserProfile currentUser;

    private List<UserProfile> filteredList;

    public FriendAdapter(Context context, List<UserProfile> userList, FragmentManager fragmentManager) {
        this.context = context;
        this.filteredList = userList;
        this.firebaseAuth = FirebaseAuth.getInstance();
        this.usersRef = FirebaseDatabase.getInstance().getReference(FirebaseStrings.Users);
        this.fragmentManager = fragmentManager;
        currentUserId = firebaseAuth.getCurrentUser().getUid();
        usersRef.child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                currentUser = snapshot.getValue(UserProfile.class);

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }


    @NonNull
    @Override
    public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user, parent, false);
        return new FriendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {

        UserProfile userProfile = filteredList.get(position);
        holder.userName.setText(userProfile.getName());
        boolean isFollowing = userProfile.followers != null && userProfile.followers.containsKey(currentUserId);
        boolean isFollowingCurrentUser = userProfile.following != null && userProfile.following.containsKey(currentUserId);
        boolean isBlocked = currentUser.blockedUsers != null && currentUser.blockedUsers.containsKey(userProfile.uid);
        boolean isBlockedCurrentUser = userProfile.blockedUsers != null && userProfile.blockedUsers.containsKey(currentUserId);
        boolean hasRequest = currentUser.followRequests != null && currentUser.followRequests.containsKey(userProfile.uid);
        boolean hasSentRequest = userProfile.followRequests != null && userProfile.followRequests.containsKey(currentUserId);

        holder.sentFollowRequestTV.setVisibility(hasSentRequest ? View.VISIBLE : View.GONE);
        holder.followButton.setText(isFollowing ? "Unfollow" : "Follow");
        holder.followButton.setBackgroundColor(isFollowing ?
                context.getResources().getColor(R.color.colorGray) :
                context.getResources().getColor(R.color.light_green));

        if (isFollowingCurrentUser) {
            holder.blockIV.setVisibility(View.VISIBLE);
            holder.blockIV.setColorFilter(isBlocked ? context.getResources().getColor(R.color.colorGray) : context.getResources().getColor(R.color.red));
        }
        else {
            holder.blockIV.setVisibility(View.GONE);
        }

        holder.requestLL.setVisibility(hasRequest ? View.VISIBLE : View.GONE);

        holder.followButton.setOnClickListener(v -> {
            if (isFollowing) {
                // Unfollow and remove user from the follower list
                usersRef.child(userProfile.uid).child("followers").child(currentUserId).removeValue()
                        .addOnSuccessListener(aVoid -> {
                            usersRef.child(currentUserId).child("following").child(userProfile.uid).removeValue()
                                    .addOnSuccessListener(aVoid2 -> {
                                        userProfile.followers.remove(currentUserId);
                                        currentUser.following.remove(userProfile.uid);
                                        holder.followButton.setText("Follow");
                                        holder.followButton.setBackgroundColor(v.getContext().getResources().getColor(R.color.light_green));
                                        notifyItemChanged(position);
                                    })
                                    .addOnFailureListener(e -> Toast.makeText(v.getContext(), "Failed to update following", Toast.LENGTH_SHORT).show());
                        })
                        .addOnFailureListener(e -> Toast.makeText(v.getContext(), "Failed to unfollow", Toast.LENGTH_SHORT).show());
            } else {
                if (userProfile.followRequests.containsKey(currentUserId)){
                    Toast.makeText(context, "Follow request already sent", Toast.LENGTH_SHORT).show();
                }
                else {
                    usersRef.child(userProfile.uid).child("followRequests").child(currentUserId).setValue(true)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(context, "Follow request sent", Toast.LENGTH_SHORT).show();
                                userProfile.followRequests.put(currentUserId,true);
                                notifyItemChanged(position);
                            })
                            .addOnFailureListener(e -> Toast.makeText(context, "Failed to send request", Toast.LENGTH_SHORT).show());
                }
            }
        });

        holder.acceptReqTVLL.setOnClickListener(v -> {
            // Add to followers and remove the follow request
            usersRef.child(currentUserId).child("followers").child(userProfile.uid).setValue(true)
                    .addOnSuccessListener(aVoid -> {
                        usersRef.child(userProfile.uid).child("following").child(currentUserId).setValue(true);
                        usersRef.child(currentUserId).child("followRequests").child(userProfile.uid).removeValue()
                                .addOnSuccessListener(aVoid2 -> {
                                    currentUser.followRequests.remove(userProfile.uid);
                                    holder.requestLL.setVisibility(View.GONE);
                                    notifyItemChanged(position);
                                })
                                .addOnFailureListener(e -> Toast.makeText(v.getContext(), "Failed to remove request", Toast.LENGTH_SHORT).show());
                    })
                    .addOnFailureListener(e -> Toast.makeText(v.getContext(), "Failed to accept request", Toast.LENGTH_SHORT).show());
        });

        holder.rejectReqTVLL.setOnClickListener(v -> {
            // Remove the follow request without adding to followers
            usersRef.child(currentUserId).child("followRequests").child(userProfile.uid).removeValue()
                    .addOnSuccessListener(aVoid -> {
                                currentUser.followRequests.remove(userProfile.uid);
                                holder.requestLL.setVisibility(View.GONE);
                                notifyItemChanged(position);
                            })
                    .addOnFailureListener(e -> Toast.makeText(v.getContext(), "Failed to reject request", Toast.LENGTH_SHORT).show());
        });

        holder.blockIV.setOnClickListener(v -> {
            if (isBlocked) {
                // Unblock the user
                usersRef.child(currentUserId).child("blockedUsers").child(userProfile.uid).removeValue()
                        .addOnSuccessListener(aVoid ->
                                {
                                    currentUser.blockedUsers.remove(userProfile.uid);
                                    holder.blockIV.setColorFilter(context.getResources().getColor(R.color.red)) ;
                                    notifyItemChanged(position);

                                }
                        )
                        .addOnFailureListener(e -> Toast.makeText(context, "Failed to unblock", Toast.LENGTH_SHORT).show());
            }
            else {
                // Block the user
                usersRef.child(currentUserId).child("blockedUsers").child(userProfile.uid).setValue(true)
                        .addOnSuccessListener(aVoid ->
                                {
                                    currentUser.blockedUsers.put(userProfile.uid, true);
                                    holder.blockIV.setColorFilter(context.getResources().getColor(R.color.colorGray));
                                    notifyItemChanged(position);
                                }
                        )
                        .addOnFailureListener(e -> Toast.makeText(context, "Failed to block", Toast.LENGTH_SHORT).show());
            }
        });

        holder.userLL.setOnClickListener(v -> {
            if (isFollowing) {
                if (!isBlockedCurrentUser){
                    ProfileFragment profileFragment = ProfileFragment.newInstance(userProfile.uid);
                    FragmentTransaction transaction = fragmentManager.beginTransaction();
                    transaction.replace(R.id.fragmentContainer, profileFragment);
                    transaction.addToBackStack(null);
                    transaction.commit();
                }
                else {
                    Toast.makeText(context, userProfile.name+" Blocked You", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(context, "Follow to view profile", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }
    public void updateData(List<UserProfile> newList) {
        this.filteredList = newList;
        notifyDataSetChanged();
    }
    public static class FriendViewHolder extends RecyclerView.ViewHolder {
        TextView userName,acceptTV,rejectTV,sentFollowRequestTV;
        Button followButton;
        ImageView blockIV;
        LinearLayout requestLL,acceptReqTVLL,rejectReqTVLL,userLL;
        public FriendViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.addFriendusernameTV);
            followButton = itemView.findViewById(R.id.followButton);
            requestLL = itemView.findViewById(R.id.requestLL);
            acceptTV = itemView.findViewById(R.id.acceptReqTV);
            rejectTV = itemView.findViewById(R.id.rejectReqTV);
            blockIV = itemView.findViewById(R.id.blockUserIV);
            sentFollowRequestTV =  itemView.findViewById(R.id.sentFollowRequestTV);
            acceptReqTVLL =  itemView.findViewById(R.id.acceptReqTVLL);
            rejectReqTVLL =  itemView.findViewById(R.id.rejectReqTVLL);
            userLL =  itemView.findViewById(R.id.userLL);
        }
    }
}
