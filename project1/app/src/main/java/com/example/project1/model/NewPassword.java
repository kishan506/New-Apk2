package com.example.project1.model;

import com.google.gson.annotations.SerializedName;

public class NewPassword {

    @SerializedName("phone")
    private String phone;

    @SerializedName("password")
    private String password;

    public NewPassword( String password) {

        this.password = password;
    }

    // Getter methods if needed


    public String getPassword() {
        return password;
    }
}
