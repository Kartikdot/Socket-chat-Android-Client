package com.example.nitishkumar.socketchat;

import android.content.Intent;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;


import org.json.JSONException;
import org.json.JSONObject;


import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import static com.example.nitishkumar.socketchat.MainActivity.mSocket;


public class LoginActivity extends AppCompatActivity {
    private EditText mUsernameEditText;
    private Button mLogin;
    private String mUsername;
    private Boolean isConnected;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mUsernameEditText=(EditText)findViewById(R.id.username_editText);
        mLogin=(Button)findViewById(R.id.login);
        isConnected=false;
        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login(view);
            }
        });
        mSocket.on(Socket.EVENT_CONNECT,onConnect);
        mSocket.on("login",onLogin);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSocket.off("login",onLogin);
        mSocket.off(Socket.EVENT_CONNECT,onConnect);
    }

    private void login(View v) {
        String username=mUsernameEditText.getText().toString().trim();
        if(TextUtils.isEmpty(username)){
            Snackbar.make(v,"Enter username",Snackbar.LENGTH_SHORT).show();
            mUsernameEditText.requestFocus();
            return;
        }
        mUsername=username;
        mSocket.connect();

    }

    private Emitter.Listener onConnect=new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            if(!isConnected){
                isConnected=true;
                mSocket.emit("add user",mUsername);
            }
            else{
                Log.w("-->>","onConnect Failure");
            }
        }
    };

    private Emitter.Listener onLogin=new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data= (JSONObject) args[0];
            int numUsers=0;
            try {
                numUsers=data.getInt("numUsers");
            }catch (JSONException e){
                e.printStackTrace();
            }
            Intent i=new Intent();
            i.putExtra("username",mUsername);
            i.putExtra("numUsers",numUsers);
            setResult(RESULT_OK,i);
            finish();
        }
    };
}
