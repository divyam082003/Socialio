package com.gd.socialio.models;

public class ReplyModel {

    private String replyId;

    private String replierId;

    private String replyText;
    private String replyDate;


    public ReplyModel() {
    }


    public String getReplyId() {
        return replyId;
    }

    public void setReplyId(String replyId) {
        this.replyId = replyId;
    }

    public String getReplierId() {
        return replierId;
    }

    public void setReplierId(String replierId) {
        this.replierId = replierId;
    }

    public String getReplyText() {
        return replyText;
    }

    public void setReplyText(String replyText) {
        this.replyText = replyText;
    }

    public String getReplyDate() {
        return replyDate;
    }

    public void setReplyDate(String replyDate) {
        this.replyDate = replyDate;
    }
}
