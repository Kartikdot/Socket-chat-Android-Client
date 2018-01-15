package com.example.nitishkumar.socketchat;

import android.app.Application;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;

/**
 * Created by Nitish Kumar on 12-12-2017.
 */

public class ChatApp extends Application {
    private final String CHAT_URL="https://salty-lowlands-83532.herokuapp.com/";
    private Socket mSocket;
    {
        try {
            mSocket= IO.socket(CHAT_URL);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public Socket getSocket(){
        return mSocket;
    }
}
