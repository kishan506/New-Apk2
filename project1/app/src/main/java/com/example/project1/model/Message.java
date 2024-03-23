package com.example.project1.model;

public class Message {
    private String text;
    private String sender;
    private String sender_Name;

    public Message(String text, String sender,String sender_Name) {
        this.text = text;
        this.sender = sender;
        this.sender_Name=sender_Name;
    }
    public Message()
    {}

    public void setText(String text) {
        this.text = text;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getText() {
        return text;
    }

    public String getSender() {
        return sender;
    }

    public String getSender_Name() {
        return sender_Name;
    }

    public void setSender_Name(String sender_Name) {
        this.sender_Name = sender_Name;
    }
}
