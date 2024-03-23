package com.example.project1.model;

public class LoginResponse {
    private String message;
    private int uid;
    private String username;

    public String getFname() {
        return username;
    }

    public void setFname(String username) {
        this.username = username;
    }

    @Override
    public String toString() {
        return "LoginResponse{" +
                "message='" + message + '\'' +
                ", uid=" + uid +
                ", fname='" + username + '\'' +
                '}';
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }
}
