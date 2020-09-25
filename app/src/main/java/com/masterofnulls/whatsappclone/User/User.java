package com.masterofnulls.whatsappclone.User;

public class User {
    private String uid, name, phone;

    public User(String uid, String name, String phone) {
        this.uid = uid;
        this.name = name;
        this.phone = phone;
    }

    public String getUid() { return uid; }
    public String getPhone() { return phone; }
    public String getName() { return name; }
}
