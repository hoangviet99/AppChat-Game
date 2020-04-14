package com.example.demochat;

import androidx.annotation.NonNull;

public class Chating {
    private String mUsername, mChat;

    public Chating(String mUsername, String mChat) {
        this.mUsername = mUsername;
        this.mChat = mChat;
    }

    public String getmUsername() {
        return mUsername;
    }

    public void setmUsername(String mUsername) {
        this.mUsername = mUsername;
    }

    public String getmChat() {
        return mChat;
    }

    public void setmChat(String mChat) {
        this.mChat = mChat;
    }

    @NonNull
    @Override
    public String toString() {
        return super.toString();
    }
}
