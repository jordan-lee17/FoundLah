package com.example.foundlah;

import java.util.List;

public class Convo {
    private String user1;
    private String user2;
    private String chatroomID;

    public Convo(){}

    public String getUser1(){
        return user1;
    }

    public String getUser2(){
        return user2;
    }

    public void setUsers(List<String> users){
        if (users != null && users.size() == 2) {
            this.user1 = users.get(0);
            this.user2 = users.get(1);
        } else {
            this.user1 = "Unknown";
            this.user2 = "Unknown";
        }
    }

    public String getChatroomID(){
        if(user1.compareTo(user2) < 0){
            return user1 + "_" + user2;
        }
        else{
            return user2 + "_" + user1;
        }
    }
}
