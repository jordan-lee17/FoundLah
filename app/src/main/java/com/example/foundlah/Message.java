package com.example.foundlah;

import com.google.firebase.Timestamp;

public class Message {
    private String text;
    private String sender;

    private Long timestamp;

    public Message(){
    }

    public Message(String text, String sender, Long timestamp){
        this.text = text;
        this.sender = sender;
        this.timestamp = timestamp;
    }

    public String getText(){
        return text;
    }

    public String getSender(){
        return sender;
    }

    public Long getTimestamp(){
        return timestamp;
    }
}