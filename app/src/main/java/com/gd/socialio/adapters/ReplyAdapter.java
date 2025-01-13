package com.gd.socialio.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gd.socialio.R;
import com.gd.socialio.models.FirebaseStrings;
import com.gd.socialio.models.ReplyModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class ReplyAdapter extends RecyclerView.Adapter<ReplyAdapter.ReplyViewHolder> {

    private List<ReplyModel> replyList;

    DatabaseReference userRef;

    public ReplyAdapter(List<ReplyModel> replyList) {
        this.replyList = replyList;
        this.userRef =  FirebaseDatabase.getInstance().getReference(FirebaseStrings.Users);
    }

    @NonNull
    @Override
    public ReplyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment_reply, parent, false);
        return new ReplyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReplyViewHolder holder, int position) {
        ReplyModel reply = replyList.get(position);

        userRef.child(reply.getReplierId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String userName = snapshot.child(FirebaseStrings.name).getValue(String.class);
                holder.replyUserName.setText(userName);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        holder.replyText.setText(reply.getReplyText());
        String replyDate = reply.getReplyDate();
        if (replyDate != null && !replyDate.isEmpty()) {
            holder.replyTime.setText(replyDate);
        } else {
            holder.replyTime.setText("No Date Available");
        }

    }

    @Override
    public int getItemCount() {
        return replyList.size();
    }

    static class ReplyViewHolder extends RecyclerView.ViewHolder {
        TextView replyUserName, replyText, replyTime;

        public ReplyViewHolder(@NonNull View itemView) {
            super(itemView);
            replyUserName = itemView.findViewById(R.id.replyUserName);
            replyText = itemView.findViewById(R.id.replyText);
            replyTime = itemView.findViewById(R.id.replyTime);
        }
    }
}
