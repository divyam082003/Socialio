package com.gd.socialio.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gd.socialio.R;
import com.gd.socialio.models.FirebaseStrings;
import com.gd.socialio.models.PostModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> implements CommentAdapter.OnReplyClickListener , CommentAdapter.OnCommentDeleteListener {
    private Context context;
    private List<PostModel> postModelList;
    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;
    private String currentUserId;

    private String currentCommentId;


    public PostAdapter(Context context, List<PostModel> postModelList) {
        this.context = context;
        this.postModelList = postModelList;
        this.databaseReference = FirebaseDatabase.getInstance().getReference(FirebaseStrings.Users);
        this.firebaseAuth =  FirebaseAuth.getInstance();
        currentUserId = firebaseAuth.getCurrentUser().getUid();
    }


    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        PostModel postModel = postModelList.get(position);
        holder.userName.setText(postModel.getUserName());
        holder.postText.setText(postModel.getPostText());
        holder.dateTV.setText(postModel.getPublishDate());
        holder.likeCount.setText(String.valueOf(postModel.getLikesCount()));
        holder.dislikeCount.setText(String.valueOf(postModel.getDislikesCount()));
        holder.commentCount.setText(String.valueOf(postModel.getCommentCount()));

        holder.commentSection.setVisibility(View.GONE); // Reset to hidden
        holder.commentsRecyclerView.setLayoutManager(new LinearLayoutManager(context));

        holder.commentsRecyclerView.setAdapter(null);

        DatabaseReference postRef = databaseReference
                .child(postModel.getUserId())
                .child(FirebaseStrings.posts)
                .child(postModel.getPostId());


        holder.likeIV.setColorFilter(postModel.isLikedByCurrentUser() ? Color.BLUE : Color.GRAY);
        holder.dislikeIV.setColorFilter(postModel.isDislikedByCurrentUser() ? Color.RED : Color.GRAY);

        holder.likeLL.setOnClickListener(v -> {
            if (postModel.isLikedByCurrentUser()) {
                postRef.child(FirebaseStrings.likes).child(currentUserId).removeValue()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                if (postModel.getLikesCount() > 0) {
                                    postModel.setLikesCount(postModel.getLikesCount() - 1);
                                }
                                postModel.setLikedByCurrentUser(false);
                                holder.likeIV.setColorFilter(Color.GRAY);
                                holder.likeCount.setText(String.valueOf(postModel.getLikesCount()));
                            }
                        });
            } else {
                postRef.child(FirebaseStrings.likes).child(currentUserId).setValue(true)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                postModel.setLikesCount(postModel.getLikesCount() + 1);
                                postModel.setLikedByCurrentUser(true);
                                holder.likeIV.setColorFilter(Color.BLUE);
                                if (postModel.isDislikedByCurrentUser()) {
                                    postRef.child(FirebaseStrings.dislikes).child(currentUserId).removeValue()
                                            .addOnCompleteListener(dislikeTask -> {
                                                if (dislikeTask.isSuccessful()) {
                                                    if (postModel.getDislikesCount() > 0) {
                                                        postModel.setDislikesCount(postModel.getDislikesCount() - 1);
                                                    }
                                                    postModel.setDislikedByCurrentUser(false);
                                                    holder.dislikeIV.setColorFilter(Color.GRAY);
                                                    holder.dislikeCount.setText(String.valueOf(postModel.getDislikesCount()));
                                                }
                                            });
                                }
                                holder.likeCount.setText(String.valueOf(postModel.getLikesCount()));
                            }
                        });
            }
        });

        holder.dislikeLL.setOnClickListener(v -> {
            if (postModel.isDislikedByCurrentUser()) {
                postRef.child(FirebaseStrings.dislikes).child(currentUserId).removeValue()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                if (postModel.getDislikesCount() > 0) {
                                    postModel.setDislikesCount(postModel.getDislikesCount() - 1);
                                }
                                postModel.setDislikedByCurrentUser(false);
                                holder.dislikeIV.setColorFilter(Color.GRAY);
                                holder.dislikeCount.setText(String.valueOf(postModel.getDislikesCount()));
                            }
                        });
            } else {
                postRef.child(FirebaseStrings.dislikes).child(currentUserId).setValue(true)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                postModel.setDislikesCount(postModel.getDislikesCount() + 1);
                                postModel.setDislikedByCurrentUser(true);
                                holder.dislikeIV.setColorFilter(Color.RED);
                                if (postModel.isLikedByCurrentUser()) {
                                    postRef.child(FirebaseStrings.likes).child(currentUserId).removeValue()
                                            .addOnCompleteListener(likeTask -> {
                                                if (likeTask.isSuccessful()) {
                                                    if (postModel.getLikesCount() > 0) {
                                                        postModel.setLikesCount(postModel.getLikesCount() - 1);
                                                    }
                                                    postModel.setLikedByCurrentUser(false);
                                                    holder.likeIV.setColorFilter(Color.GRAY);
                                                    holder.likeCount.setText(String.valueOf(postModel.getLikesCount()));
                                                }
                                            });
                                }
                                holder.dislikeCount.setText(String.valueOf(postModel.getDislikesCount()));
                            }
                        });
            }
        });

        holder.commentLL.setOnClickListener(v -> {
            if (holder.commentSection.getVisibility() == View.GONE) {
                holder.commentSection.setVisibility(View.VISIBLE);
                if (postModel.getCommentCount() >= 1) {
                    CommentAdapter adapter = new CommentAdapter(context,postModel.getUserId(), postModel.getPostId(),PostAdapter.this,PostAdapter.this,postModel,position);
                    holder.commentsRecyclerView.setAdapter(adapter);
                }
            } else {
                holder.commentSection.setVisibility(View.GONE);
                holder.commentsRecyclerView.setAdapter(null);
            }
        });

        holder.sendCommentButton.setOnClickListener(v -> {
            String commentText = holder.addCommentET.getText().toString().trim();
            if (!commentText.isEmpty()) {
                if (currentCommentId != null && !currentCommentId.isEmpty()) {
                    addReplyToComment(postRef,currentCommentId,commentText,holder.addCommentET,position);
                } else {
                    addCommentToPost(postRef,commentText,holder.addCommentET,postModel,position);
                }

            } else {
                Toast.makeText(context, "Please enter a comment!", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public int getItemCount() {
        return postModelList.size();
    }

    public void updateList(List<PostModel> filteredPosts) {
        this.postModelList = filteredPosts;
        notifyDataSetChanged();
    }

    @Override
    public void onReplyClick(String commentId) {
        currentCommentId = commentId;
    }

    private void addCommentToPost(DatabaseReference postRef, String commentText,EditText editText,PostModel postModel,int position) {
        String commentId = postRef.child(FirebaseStrings.comments).push().getKey();
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        String commentDate = sdf.format(new Date());
        Map<String, Object> commentMap = new HashMap<>();
        commentMap.put(FirebaseStrings.commentText, commentText);
        commentMap.put(FirebaseStrings.commenterId, currentUserId);
        commentMap.put(FirebaseStrings.commentDate, commentDate);
        DatabaseReference commentRef = postRef.child(FirebaseStrings.comments).child(commentId);
        if (!commentText.isEmpty() && currentUserId!= null && !commentDate.isEmpty()){
            commentRef.setValue(commentMap).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    editText.setText("");
                    postModel.setCommentCount(postModel.getCommentCount() + 1);
                    notifyItemChanged(position);
                } else {
                    Toast.makeText(context, "Failed to add comment!", Toast.LENGTH_SHORT).show();
                }
            });
        }

    }

    private void addReplyToComment(DatabaseReference postRef,String commentId, String replyText,EditText editText,int position) {
        DatabaseReference replyRef = postRef.child(FirebaseStrings.comments).child(commentId).child(FirebaseStrings.replies);
        String replyId = replyRef.push().getKey();
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
        String replyDate = sdf.format(new Date());
        Map<String, Object> replyMap = new HashMap<>();
        replyMap.put(FirebaseStrings.replyText, replyText);
        replyMap.put(FirebaseStrings.replierId, currentUserId);
        replyMap.put(FirebaseStrings.replyDate, replyDate);
        if (!replyText.isEmpty() && currentUserId!=null && !replyDate.isEmpty()){
            replyRef.child(replyId)
                    .setValue(replyMap).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            editText.setText("");
                            notifyItemChanged(position);
                        } else {
                            Toast.makeText(context, "Failed to add reply!", Toast.LENGTH_SHORT).show();
                        }
                    });
        }

    }

    @Override
    public void onCommentDeleteClick(int position) {
        if (postModelList.get(position).getCommentCount() > 0){
            postModelList.get(position).setCommentCount(postModelList.get(position).getCommentCount() - 1);
            notifyItemChanged(position);
        }

    }

    static class PostViewHolder extends RecyclerView.ViewHolder {
        ImageView profileImage, postImage,likeIV,dislikeIV,commentIV;
        TextView userName,dateTV, postText,likeCount, dislikeCount,commentCount;
        Button sendCommentButton;
        EditText addCommentET;
        LinearLayout commentSection,likeLL,dislikeLL,commentLL;
        RecyclerView commentsRecyclerView;


        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.profileImage);
            postImage = itemView.findViewById(R.id.postImage);
            userName = itemView.findViewById(R.id.userName);
            dateTV = itemView.findViewById(R.id.dateTV);
            postText = itemView.findViewById(R.id.postText);
            likeIV = itemView.findViewById(R.id.likeIcon);
            dislikeIV = itemView.findViewById(R.id.dislikeIcon);
            commentIV = itemView.findViewById(R.id.commentIcon);
            likeCount = itemView.findViewById(R.id.likeCount);
            dislikeCount = itemView.findViewById(R.id.dislikeCount);
            commentCount = itemView.findViewById(R.id.commentCount);
            commentSection = itemView.findViewById(R.id.commentsSection);
            sendCommentButton = itemView.findViewById(R.id.sendCommentButton);
            addCommentET = itemView.findViewById(R.id.addCommentEditText);
            commentsRecyclerView = itemView.findViewById(R.id.commentsRecyclerView);
            likeLL = itemView.findViewById(R.id.likeLL);
            dislikeLL = itemView.findViewById(R.id.dislikeLL);
            commentLL = itemView.findViewById(R.id.commentLL);
        }
    }
}
