package com.gd.socialio.models;

import java.util.HashMap;
import java.util.Map;

public class UserProfile {
    public String name;
    public String phone;
    public String email;
    public String uid;

    public Map<String, Boolean> followRequests = new HashMap<>();
    public Map<String, Boolean> blockedUsers = new HashMap<>();
    public Map<String, Boolean> followers = new HashMap<>();
    public Map<String, Boolean> following = new HashMap<>();

    public UserProfile() {
        // Initialize the followers and following lists as empty maps
        this.followers = new HashMap<>();
        this.following = new HashMap<>();
    }

    // Constructor for user with name, phone, and uid
    public UserProfile(String name, String phone, String uid) {
        this.name = name;
        this.phone = phone;
        this.uid = uid;
        this.followers = new HashMap<>();
        this.following = new HashMap<>();
    }

    // Constructor for user with email, name, phone, and uid
    public UserProfile(String name, String phone, String email, String uid) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.uid = uid;
        this.followers = new HashMap<>();
        this.following = new HashMap<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Map<String, Boolean> getFollowers() {
        return followers;
    }

    public void setFollowers(Map<String, Boolean> followers) {
        this.followers = followers;
    }

    public Map<String, Boolean> getFollowing() {
        return following;
    }

    public void setFollowing(Map<String, Boolean> following) {
        this.following = following;
    }
}