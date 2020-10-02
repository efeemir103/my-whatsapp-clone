package com.masterofnulls.whatsappclone.Chat;

import com.masterofnulls.whatsappclone.User.User;

import java.io.Serializable;
import java.util.ArrayList;

public class Chat implements Serializable {
    private String chatId, chatName, chatIcon;

    private ArrayList<User> users = new ArrayList<>();

    public Chat(String chatId) {
        this.chatId = chatId;
        this.chatName = "";
        this.chatIcon = "";
    }

    public String getChatId() { return chatId; }

    public String getChatName() {return chatName; }

    public void setChatName(String chatName) { this.chatName = chatName; }

    public String getChatIcon() { return chatIcon; }

    public void setChatIcon(String chatIcon) { this.chatIcon = chatIcon; }

    public ArrayList<User> getUsers() { return users; }

    public void addUser(User user) { users.add(user); }

    public void updateUser(User user) {

        for(int i = 0; i < users.size(); i++) {
            if(users.get(i).getUid().equals(user.getUid())) {
                    users.set(i, user);
            }
        }
    }
}
