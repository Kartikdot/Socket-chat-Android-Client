package com.example.nitishkumar.socketchat;

/**
 * Created by Nitish Kumar on 14-12-2017.
 */

public class Message {
    public static final int TYPE_MESSAGE_RECEIVED=0;
    //new user is a log
    public static final int TYPE_LOG=1;

    public static final int TYPE_MESSAGE_SENT=2;


    private int mType;
    private String mUsername;
    private String mMessage;
    private Message(){
    }

    public Message(int type,String message){
        this.mType=type;
        this.mMessage=message;
        this.mUsername=null;
    }
    public Message(int type,String username,String message){
        this.mType=type;
        this.mMessage=message;
        this.mUsername=username;
    }

    public int getmType() {
        return mType;
    }

    public String getmMessage() {
        return mMessage;
    }

    public String getmUsername() {
        return mUsername;
    }

    public void setmType(int mType) {
        this.mType = mType;
    }

    public void setmMessage(String mMessage) {
        this.mMessage = mMessage;
    }

    public void setmUsername(String mUsername) {
        this.mUsername = mUsername;
    }
}
