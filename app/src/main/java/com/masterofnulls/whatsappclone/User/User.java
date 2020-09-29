package com.masterofnulls.whatsappclone.User;

import java.io.Serializable;

public class User implements Serializable {
    private String uid, name, phone, notificationKey;
    private boolean selected = false;

    public User(String uid) {
        this.uid = uid;
    }

    public User(String uid, String name, String phone) {
        this.uid = uid;
        this.name = name;
        this.phone = phone;
    }

    public String getUid() { return uid; }
    public String getPhone() { return phone; }
    public String getName() { return name; }
    public String getNotificationKey() { return notificationKey; }
    public boolean getSelected() { return selected; }

    public void setName(String name) { this.name = name; }
    public void setNotificationKey(String notificationKey) { this.notificationKey = notificationKey; }

    public void setSelected(boolean selected) { this.selected = selected; }
}
