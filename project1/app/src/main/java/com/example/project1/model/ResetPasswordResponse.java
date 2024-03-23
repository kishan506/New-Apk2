package com.example.project1.model;

public class ResetPasswordResponse {
    String  phone, password

;

    public ResetPasswordResponse(String phone, String password

) {
        this.phone = phone;
        this.password

 = password

;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPasswor() {
        return password

;
    }

    public void setPasswor(String password

) {
        this.password

 = password

;
    }
}
