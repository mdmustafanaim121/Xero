package com.project.xero.Model;

import androidx.annotation.NonNull;

import com.google.gson.Gson;

public class User {

    private String Name;
    private String Phone;
    private String Password;
    private String IsStaff;


    public User(String name, String password) {
        Name = name;
        Password = password;
        IsStaff = "false";

    }

    public String getIsStaff() {
        return IsStaff;
    }

    public void setIsStaff(String isStaff) {
        IsStaff = isStaff;
    }

    public String getPhone() {
        return Phone;
    }

    public void setPhone(String phone) {
        Phone = phone;
    }


    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getPassword() {
        return Password;
    }

    public void setPassword(String password) {
        Password = password;
    }



    public User() {

    }

    public String toJsonString() {
        return new Gson().toJson(this);
    }
}
