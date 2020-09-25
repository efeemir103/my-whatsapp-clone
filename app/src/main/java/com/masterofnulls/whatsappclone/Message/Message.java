package com.masterofnulls.whatsappclone.Message;

import java.util.ArrayList;

public class Message {

    private String messageId, senderId, message;

    ArrayList<String> mediaURLList;

    public Message(String messageId, String senderId, String message, ArrayList<String> mediaURLList) {
        this.messageId = messageId;
        this.senderId = senderId;
        this.message = message;
        this.mediaURLList = mediaURLList;

    }

    public String getMessageId() { return messageId; }

    public String getSenderId() { return senderId; }

    public String getMessage() { return message; }

    public ArrayList<String> getMediaURLList() { return mediaURLList; }
}
