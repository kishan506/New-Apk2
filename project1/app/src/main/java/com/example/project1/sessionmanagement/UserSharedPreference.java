package com.example.project1.sessionmanagement;


import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

public class UserSharedPreference {
    Context context;
    String IP, phone;

    public UserSharedPreference(AppCompatActivity context) {
        this.context = context;
    }

//    SharedPreferences sh = context.getSharedPreferences("MySharedPref", context.MODE_PRIVATE);

    public void addUserDetails(int user_id) {
        Log.d("from pref", user_id + "");
          SharedPreferences sh = context.getSharedPreferences("MySharedPref", context.MODE_PRIVATE);
        SharedPreferences.Editor myEdit = sh.edit();
        myEdit.putInt("userId", user_id);
        myEdit.commit();
    }

    public void addIP(String IP) {
        Log.d("from pref", IP + "");
         SharedPreferences sh = context.getSharedPreferences("MySharedPref", context.MODE_PRIVATE);
        SharedPreferences.Editor myEdit = sh.edit();
        myEdit.putString("useIP", IP);
        myEdit.commit();
    }

    public void addPhone(String phone) {
        Log.d("from pref", phone + "");
        SharedPreferences sh = context.getSharedPreferences("MySharedPref", context.MODE_PRIVATE);
        SharedPreferences.Editor myEdit = sh.edit();
        myEdit.putString("userPhone", phone);
        myEdit.commit();
    }

    public int getUserDetails() {
        SharedPreferences sh = context.getSharedPreferences("MySharedPref", context.MODE_PRIVATE);
        return sh.getInt("userId", 0);
    }

    public String getIP() {
        SharedPreferences sh = context.getSharedPreferences("MySharedPref", context.MODE_PRIVATE);
        Log.d("dd", "getIP: " + sh.getAll());
        Log.d("gg", "getIP: " + sh.getString("useIP", ""));
//        Map<String, ?> map = new HashMap<String,String>();
//        map = sh.getAll();
        String z = sh.getString("useIP", "");
//        Log.d("ff", "addIP: "+map.get("userIP"));

        return z;

    }

    public String getPhone() {
        SharedPreferences sh = context.getSharedPreferences("MySharedPref", context.MODE_PRIVATE);

//        Log.d("dd", "getIP: "+sh.getAll());
//        Log.d("gg", "getIP: "+sh.getString("useIP",""));
//        Map<String, ?> map = new HashMap<String,String>();
//        map = sh.getAll();
        String z = sh.getString("userPhone", "");
//        Log.d("ff", "addIP: "+map.get("userIP"));

        return z;

    }

    public void removeId() {
        SharedPreferences sh = context.getSharedPreferences("MySharedPref", context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sh.edit();
        editor.remove("userId");
        editor.apply();
    }

    public void setpassword(String password) {
//        Log.d("from pref", phone + "");
        SharedPreferences sh = context.getSharedPreferences("MySharedPref", context.MODE_PRIVATE);
        SharedPreferences.Editor myEdit = sh.edit();
        myEdit.putString("password", password);
        myEdit.commit();

    }
    public void setfname(String fname) {
//        Log.d("from pref", phone + "");
        SharedPreferences sh = context.getSharedPreferences("MySharedPref", context.MODE_PRIVATE);
        SharedPreferences.Editor myEdit = sh.edit();
        myEdit.putString("fname", fname);
        myEdit.commit();
    }

    public String getPassword() {
        SharedPreferences sh = context.getSharedPreferences("MySharedPref", context.MODE_PRIVATE);

        String z = sh.getString("password", "");

        return z;

    } public String getFname() {
        SharedPreferences sh = context.getSharedPreferences("MySharedPref", context.MODE_PRIVATE);

        String z = sh.getString("fname", "");

        return z;

    }public String getchatGroupId() {
        SharedPreferences sh = context.getSharedPreferences("MySharedPref", context.MODE_PRIVATE);

        String z = sh.getString("chatGroupId", "");

        return z;

    }
    public void setchatGroupId(String chatGroupId) {
//        Log.d("from pref", phone + "");
        SharedPreferences sh = context.getSharedPreferences("MySharedPref", context.MODE_PRIVATE);
        SharedPreferences.Editor myEdit = sh.edit();
        myEdit.putString("chatGroupId", chatGroupId);
        myEdit.commit();
    } public void setTaskId(int taskId) {
//        Log.d("from pref", phone + "");
        SharedPreferences sh = context.getSharedPreferences("MySharedPref", context.MODE_PRIVATE);
        SharedPreferences.Editor myEdit = sh.edit();
        myEdit.putString("taskId", String.valueOf(taskId));
        myEdit.commit();
    }
    public String getTaskId() {
        SharedPreferences sh = context.getSharedPreferences("MySharedPref", context.MODE_PRIVATE);

        String z = sh.getString("taskId", "");

        return z;

    }
}
