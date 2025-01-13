package com.gd.socialio.models;

import java.util.HashMap;

public class CommentModel {

    private String commentId;

    private String commenterId ;
    private String commentText;

    private HashMap<String, ReplyModel> replies = new HashMap<>();
    private String commentDate;
    private boolean edited = false;

    private boolean isLikedByCurrentUser;
    private boolean isDislikedByCurrentUser;

    private int likesCount;
    private int dislikesCount;

    public CommentModel() {
    }

    public CommentModel(String commenterId, String commentText, HashMap<String,ReplyModel> replies, String commentDate, boolean edited, boolean isLikedByCurrentUser, boolean isDislikedByCurrentUser, int likesCount, int dislikesCount) {
        this.commenterId = commenterId;
        this.commentText = commentText;
        this.replies = replies;
        this.commentDate = commentDate;
        this.edited = edited;
        this.isLikedByCurrentUser = isLikedByCurrentUser;
        this.isDislikedByCurrentUser = isDislikedByCurrentUser;
        this.likesCount = likesCount;
        this.dislikesCount = dislikesCount;
    }

    public String getCommenterId() {
        return commenterId;
    }



    public String getCommentText() {
        return commentText;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }


    public HashMap<String, ReplyModel> getReplies() {
        return replies;
    }

    public void setReplies(HashMap<String, ReplyModel> replies) {
        this.replies = replies;
    }

    public String getCommentDate() {
        return commentDate;
    }


    public boolean isEdited() {
        return edited;
    }


    public int getLikesCount() {
        return likesCount;
    }

    public void setLikesCount(int likesCount) {
        this.likesCount = likesCount;
    }

    public int getDislikesCount() {
        return dislikesCount;
    }

    public void setDislikesCount(int dislikesCount) {
        this.dislikesCount = dislikesCount;
    }

    public boolean isLikedByCurrentUser() {
        return isLikedByCurrentUser;
    }

    public void setLikedByCurrentUser(boolean likedByCurrentUser) {
        isLikedByCurrentUser = likedByCurrentUser;
    }

    public boolean isDislikedByCurrentUser() {
        return isDislikedByCurrentUser;
    }

    public void setDislikedByCurrentUser(boolean dislikedByCurrentUser) {
        isDislikedByCurrentUser = dislikedByCurrentUser;
    }

    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    public void addReply(ReplyModel newReply) {
    }
}
