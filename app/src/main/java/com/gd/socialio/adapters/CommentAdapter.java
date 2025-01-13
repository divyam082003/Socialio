package com.gd.socialio.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gd.socialio.R;
import com.gd.socialio.models.CommentModel;
import com.gd.socialio.models.FirebaseStrings;
import com.gd.socialio.models.PostModel;
import com.gd.socialio.models.ReplyModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private Context context;

    private List<CommentModel> commentList;
    private String postId;

    private String postAuthorId;

    private DatabaseReference databaseReference;
    private DatabaseReference postReference;

    private FirebaseAuth firebaseAuth;

    private String currentUserId;

    private PostModel postModel;

    private int postPosition;


    private OnReplyClickListener onReplyClickListener;

    private OnCommentDeleteListener onCommentDeleteListener;

    public CommentAdapter(Context context, String postAuthorId, String postId, OnReplyClickListener replyClickListener,OnCommentDeleteListener onCommentDeleteListener,PostModel postModel,int position) {
        this.context = context;
        this.postId = postId;
        this.postAuthorId = postAuthorId;
        this.commentList = new ArrayList<>();
        this.firebaseAuth = FirebaseAuth.getInstance();
        currentUserId = firebaseAuth.getCurrentUser().getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference(FirebaseStrings.Users);
        postReference = FirebaseDatabase.getInstance().getReference(FirebaseStrings.Users).child(postAuthorId)
                .child(FirebaseStrings.posts).child(postId);
        fetchCommentsForPost(postAuthorId,postId);
        this.onReplyClickListener = replyClickListener;
        this.onCommentDeleteListener = onCommentDeleteListener;
        this.postModel = postModel;
        this.postPosition = position;
    }

    private void fetchCommentsForPost(String userId,String postId) {
        databaseReference.child(userId).child(FirebaseStrings.posts).child(postId).child(FirebaseStrings.comments)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            commentList.clear();
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                CommentModel comment = snapshot.getValue(CommentModel.class);
                                if (comment != null) {
                                    comment.setCommentId(snapshot.getKey());
                                    commentList.add(comment);
                                }
                            }
                            notifyDataSetChanged();
                        }

                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
    }

    private void fetchRepliesForComment(CommentModel comment) {
        DatabaseReference repliesRef = postReference.child(FirebaseStrings.comments).child(comment.getCommentId()).child(FirebaseStrings.replies);

        repliesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                HashMap<String, ReplyModel> repliesMap = new HashMap<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String replyId = snapshot.child(FirebaseStrings.replyId).getValue(String.class);
                    String replierId = snapshot.child(FirebaseStrings.replierId).getValue(String.class);
                    String replyDate = snapshot.child(FirebaseStrings.replyDate).getValue(String.class);
                    String replyText = snapshot.child(FirebaseStrings.replyText).getValue(String.class);
                    ReplyModel reply = new ReplyModel();
                    reply.setReplyId(replyId);
                    reply.setReplierId(replierId);
                    reply.setReplyText(replyText);
                    reply.setReplyDate(replyDate);
                    if (reply != null) {
                        repliesMap.put(snapshot.getKey(), reply);
                    }
                }
                comment.setReplies(repliesMap); // Set the map of replies in the comment
                notifyDataSetChanged(); // Refresh the adapter
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public interface OnReplyClickListener {
        void onReplyClick(String commentId);
    }

    public interface OnCommentDeleteListener{
        void onCommentDeleteClick(int position);
    }


    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        CommentModel comment = commentList.get(position);
        if (comment.getCommenterId()!= null){
            databaseReference.child(comment.getCommenterId()).child(FirebaseStrings.name).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String userName = dataSnapshot.getValue(String.class);
                    if (userName != null) {
                        holder.userName.setText(userName);
                    } else {
                        holder.userName.setText("Unknown User");
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }
        else {
            databaseReference.child(currentUserId).child(FirebaseStrings.name).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String userName = dataSnapshot.getValue(String.class);
                    if (userName != null) {
                        holder.userName.setText(userName);
                    } else {
                        holder.userName.setText("Unknown User");
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }
        holder.commentTime.setText(comment.getCommentDate());
        holder.commentText.setText(comment.getCommentText());
        DatabaseReference commentLikesRef = databaseReference.child(comment.getCommenterId())
                .child(FirebaseStrings.posts).child(postId).child(FirebaseStrings.comments)
                .child(comment.getCommentId()).child(FirebaseStrings.commentLikes);
        commentLikesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                int likeCount = (int) dataSnapshot.getChildrenCount();
                holder.commentLikeCountTV.setText(String.valueOf(likeCount));


                if (dataSnapshot.hasChild(currentUserId)) {
                    holder.commentLikeTV.setTextColor(Color.BLUE);
                    holder.commentLikeTV.setTag("liked");
                } else {
                    holder.commentLikeTV.setTextColor(Color.GRAY);
                    holder.commentLikeTV.setTag("not_liked");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        holder.commentLikeTV.setOnClickListener(v -> {
            String tag = (String) holder.commentLikeTV.getTag();
            if ("liked".equals(tag)) {
                commentLikesRef.child(currentUserId).removeValue();
                comment.setLikedByCurrentUser(false);
                notifyItemChanged(position);
            } else {
                commentLikesRef.child(currentUserId).setValue(true);
                comment.setLikedByCurrentUser(true);
                notifyItemChanged(position);
            }
        });
        holder.commentReplyTV.setOnClickListener(v -> {
            if (holder.replyRecyclerView.getVisibility() == View.GONE) {
                if (onReplyClickListener != null) {
                    onReplyClickListener.onReplyClick(comment.getCommentId()); // Send commentId to PostAdapter
                    holder.replyRecyclerView.setVisibility(View.VISIBLE);
                    holder.commentReplyTV.setTextColor(Color.BLUE);
                    fetchRepliesForComment(comment);
                    holder.replyRecyclerView.setLayoutManager(new LinearLayoutManager(context));
                    HashMap<String, ReplyModel> repliesMap = comment.getReplies();
                    if (repliesMap != null) {
                        ReplyAdapter replyAdapter = new ReplyAdapter(new ArrayList<>(repliesMap.values())); // Convert map values to list
                        holder.replyRecyclerView.setAdapter(replyAdapter);
                    }
                }

            } else {
                if (onReplyClickListener != null) {
                    onReplyClickListener.onReplyClick(null);
                    holder.replyRecyclerView.setVisibility(View.GONE);
                    holder.commentReplyTV.setTextColor(Color.GRAY);
                }
            }
        });
        if (currentUserId.equals(postAuthorId) || currentUserId.equals(comment.getCommenterId())) {
            holder.commentDeleteTV.setVisibility(View.VISIBLE);
        }
        else {
            holder.commentDeleteTV.setVisibility(View.GONE);
        }
        holder.commentDeleteTV.setOnClickListener(v -> {
            // Create a confirmation dialog
            new AlertDialog.Builder(context)
                    .setTitle("Delete Comment")
                    .setMessage("Are you sure you want to delete this comment?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        // Proceed with deletion if confirmed
                        DatabaseReference commentRef = databaseReference.child(postAuthorId)
                                .child(FirebaseStrings.posts).child(postId)
                                .child(FirebaseStrings.comments).child(comment.getCommentId());

                        commentRef.removeValue().addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                notifyDataSetChanged();
                                if (onCommentDeleteListener != null) {
                                    onCommentDeleteListener.onCommentDeleteClick(postPosition);
                                }
                            } else {
                                Toast.makeText(context, "Failed to delete comment", Toast.LENGTH_SHORT).show();
                            }
                        });
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> {
                        // Dismiss the dialog without doing anything
                        dialog.dismiss();
                    })
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    public static class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView userName, commentTime, commentText, commentLikeTV,commentLikeCountTV, commentReplyTV,commentDeleteTV;
        RecyclerView replyRecyclerView;
        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.commentUserName);
            commentTime = itemView.findViewById(R.id.commentTime);
            commentText = itemView.findViewById(R.id.commentText);
            commentLikeTV = itemView.findViewById(R.id.commentLikeTV);
            commentLikeCountTV = itemView.findViewById(R.id.commentLikeCountTV);
            commentReplyTV = itemView.findViewById(R.id.commentReplyTV);
            replyRecyclerView = itemView.findViewById(R.id.recyclerViewReplies);
            commentDeleteTV = itemView.findViewById(R.id.commentDeleteTV);

        }
    }
}
