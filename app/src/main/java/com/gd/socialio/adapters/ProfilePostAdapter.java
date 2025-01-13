package com.gd.socialio.adapters;

// PostAdapter.java

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gd.socialio.R;
import com.gd.socialio.models.PostModel;

import java.util.List;

public class ProfilePostAdapter extends RecyclerView.Adapter<ProfilePostAdapter.ProfilePostViewHolder> {
    private List<PostModel> postList;

    private final OnPostClickListener onPostClickListener;

    public ProfilePostAdapter(List<PostModel> postList, OnPostClickListener onPostClickListener) {
        this.postList = postList;
        this.onPostClickListener = onPostClickListener;
    }

    public interface OnPostClickListener {
        void onPostClick(String postId);
    }


    @NonNull
    @Override
    public ProfilePostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_profile_posts, parent, false);
        return new ProfilePostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProfilePostViewHolder holder, int position) {
        PostModel post = postList.get(position);
        holder.postTextView.setText(post.getPostText());
        holder.likeCountTV.setText(String.valueOf(post.getLikesCount()));
        holder.dislikeCountTV.setText(String.valueOf(post.getDislikesCount()));

        holder.itemView.setOnClickListener(v -> {
            String postId = post.getPostId(); // Assume PostModel has a getPostId() method
            onPostClickListener.onPostClick(postId);
        });

    }

    @Override
    public int getItemCount() {
        return postList.size();
    }


    static class ProfilePostViewHolder extends RecyclerView.ViewHolder {
        TextView postTextView,likeCountTV,dislikeCountTV;

        public ProfilePostViewHolder(@NonNull View itemView) {
            super(itemView);
            postTextView = itemView.findViewById(R.id.profilePostTV);
            likeCountTV = itemView.findViewById(R.id.likesCountTV);
            dislikeCountTV = itemView.findViewById(R.id.dislikesCountTV);
        }
    }
}
