package com.gd.socialio.models;

import java.util.List;

public class PostModel {
    private String userName;
    private String postText;
    private int likesCount;
    private int dislikesCount;

    private int commentCount;
    private String postId;
    private String userId;
    private boolean isPublic;
    private boolean isLikedByCurrentUser;
    private boolean isDislikedByCurrentUser;

    private String publishDate;

    private List<CommentModel> comments;



    public PostModel(String userName, String postText, int likesCount, int dislikesCount, String postId, String userId,
                     String publishDate, boolean isPublic, boolean isLikedByCurrentUser, boolean isDislikedByCurrentUser) {
        this.userName = userName;
        this.postText = postText;
        this.likesCount = likesCount;
        this.dislikesCount = dislikesCount;
        this.postId = postId;
        this.userId = userId;
        this.publishDate = publishDate;
        this.isPublic = isPublic;
        this.isLikedByCurrentUser = isLikedByCurrentUser;
        this.isDislikedByCurrentUser = isDislikedByCurrentUser;
    }

    public PostModel(String userName, String postText, int likesCount, int dislikesCount,int commentCount, String postId, String userId,
                     String publishDate, boolean isPublic, boolean isLikedByCurrentUser, boolean isDislikedByCurrentUser) {
        this.userName = userName;
        this.postText = postText;
        this.likesCount = likesCount;
        this.dislikesCount = dislikesCount;
        this.commentCount = commentCount;
        this.postId = postId;
        this.userId = userId;
        this.publishDate = publishDate;
        this.isPublic = isPublic;
        this.isLikedByCurrentUser = isLikedByCurrentUser;
        this.isDislikedByCurrentUser = isDislikedByCurrentUser;
    }



    public PostModel(String postText, int likesCount, int dislikesCount, String postId) {
        this.postText = postText;
        this.likesCount = likesCount;
        this.dislikesCount = dislikesCount;
        this.postId = postId;
    }

    public String getUserName() { return userName; }
    public String getPostText() { return postText; }
    public int getLikesCount() { return likesCount; }
    public int getDislikesCount() { return dislikesCount; }
    public String getPostId() { return postId; }
    public String getUserId() { return userId; }
    public boolean isPublic() { return isPublic; }
    public boolean isLikedByCurrentUser() { return isLikedByCurrentUser; }
    public boolean isDislikedByCurrentUser() { return isDislikedByCurrentUser; }

    public void setLikesCount(int likesCount) { this.likesCount = likesCount; }
    public void setDislikesCount(int dislikesCount) { this.dislikesCount = dislikesCount; }
    public void setLikedByCurrentUser(boolean liked) { isLikedByCurrentUser = liked; }
    public void setDislikedByCurrentUser(boolean disliked) { isDislikedByCurrentUser = disliked; }
    public String getPublishDate() {
        return publishDate;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

    public List<CommentModel> getComments() { return comments; }
    public void setComments(List<CommentModel> comments) { this.comments = comments; }
    public void addComment(CommentModel comment) {
        if (this.comments != null) {
            this.comments.add(comment);
        }
    }

}
