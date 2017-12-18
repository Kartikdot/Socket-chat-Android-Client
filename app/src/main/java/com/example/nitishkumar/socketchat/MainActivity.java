package com.example.nitishkumar.socketchat;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;



import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class MainActivity extends AppCompatActivity {
    private EditText editMessage;

    private Button sendButton;
    public static Socket mSocket;
    private ChatApp app;
    private boolean mTyping;
    private String mUsername;
    TextView typingView;
    private Boolean isConnected;
    private String TAG="-->>";
    private RecyclerView recyclerView;
    private MessageAdapter mAdapter;
    private List<Message>messageList;
    private static final int TIMER=500;
    private static final int REQUEST_CODE=0;
    private Handler typingHandler=new Handler();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle(null);
        isConnected=false;
        mTyping=false;
        app=new ChatApp();
        initializeSocket();
        signIn();
        setUpUI();
    }

    public void initializeSocket(){
        mSocket=app.getSocket();

        mSocket.on(Socket.EVENT_DISCONNECT,onDisconnect);
        mSocket.on(Socket.EVENT_CONNECT_ERROR,onConnectError);
        mSocket.on("user joined",onUserJoined);
        mSocket.on("user left",onUserLeft);
        mSocket.on("new message",onNewMessage);
        mSocket.on("typing",onTyping);
        mSocket.on("stop typing",onStopTyping);
    }
    private void signIn(){
        mUsername=null;
        Intent i=new Intent(this,LoginActivity.class);
        startActivityForResult(i,REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode!= Activity.RESULT_OK) {
            finish();
            return;
        }
        isConnected=true;
        Snackbar.make(findViewById(android.R.id.content),"Welcome to Socket Chat", Snackbar.LENGTH_SHORT).show();
        mUsername=data.getStringExtra("username");
        int numUsers=data.getIntExtra("numUsers",1);
        addParticipantsLog(numUsers);
    }

    public void setUpUI(){
        messageList=new ArrayList<>();
        recyclerView=(RecyclerView)findViewById(R.id.recycler_view);
        mAdapter=new MessageAdapter(messageList);
        RecyclerView.LayoutManager layoutManager=new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);
        editMessage=(EditText)findViewById(R.id.editMessage);
        typingView=findViewById(R.id.typing);
        sendButton=(Button)findViewById(R.id.sendButton);

        editMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(mUsername==null)
                    return;
                if(!mSocket.connected())
                    return;
                if(TextUtils.isEmpty(editMessage.getText()))
                    return;
                if(mTyping==false) {
                    mTyping = true;
                    mSocket.emit("typing");
                }
                typingHandler.removeCallbacks(onTypingTimeout);
                typingHandler.postDelayed(onTypingTimeout,TIMER);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptSend();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(isConnected) {
            mSocket.disconnect();
            isConnected = false;
        }


        mSocket.off(Socket.EVENT_DISCONNECT,onDisconnect);
        mSocket.off(Socket.EVENT_CONNECT_ERROR,onConnectError);
        mSocket.off("user joined",onUserJoined);
        mSocket.off("user left",onUserLeft);
        mSocket.off("new message",onNewMessage);
        mSocket.off("typing",onTyping);
        mSocket.off("stop typing",onStopTyping);

        mTyping=false;

    }


    private Emitter.Listener onDisconnect=new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    isConnected=false;
                    Log.w(TAG, "onDisconnect");
                }
            });
        }
    };

    private Emitter.Listener onConnectError=new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.e(TAG,"error connecting");
                }
            });
        }
    };

    private Emitter.Listener onNewMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            Log.w(TAG,"onNewMesage");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data= (JSONObject)args[0];
                    String username=null;
                    String message=null;
                    try {
                        username=data.getString("username");
                        message=data.getString("message");
                    } catch (JSONException e) {
                        Log.e(TAG,e.getMessage());
                        e.printStackTrace();
                    }

                    addMessage(username,message,Message.TYPE_MESSAGE_RECEIVED);
                }
            });
        }
    };

    private Emitter.Listener onUserJoined = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            Log.w(TAG,"onUserJoined");
            runOnUiThread(new Runnable() {
                @SuppressLint("StringFormatInvalid")
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String username=null;
                    int numUsers;
                    try {
                        username = data.getString("username");
                        numUsers = data.getInt("numUsers");
                    } catch (JSONException e) {
                        Log.e(TAG, e.getMessage());
                        return;
                    }

                    addLog(username+" has joined");
                    addParticipantsLog(numUsers);
                }
            });
        }
    };

    private Emitter.Listener onUserLeft = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            Log.w(TAG,"onUserLeft");
            runOnUiThread(new Runnable() {
                @SuppressLint("StringFormatInvalid")
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String username;
                    int numUsers;
                    try {
                        username = data.getString("username");
                        numUsers = data.getInt("numUsers");
                    } catch (JSONException e) {
                        Log.e(TAG, e.getMessage());
                        return;
                    }

                    addLog(username+" left");
                    addParticipantsLog(numUsers);
                    removeTyping();
                }
            });
        }
    };

    private Emitter.Listener onTyping = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            Log.w(TAG,"onTyping");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String username;
                    try {
                        username = data.getString("username");
                    } catch (JSONException e) {
                        Log.e(TAG, e.getMessage());
                        return;
                    }
                    addTyping(username);
                }
            });
        }
    };

    private Emitter.Listener onStopTyping = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            Log.w(TAG,"onStopTyping");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    removeTyping();
                }
            });
        }
    };

    private void addLog(String message){
        messageList.add(new Message(Message.TYPE_LOG,message));
        mAdapter.notifyItemInserted(messageList.size()-1);
        scrollUp();
    }

    private void addMessage(String username,String message, int messageType){
        messageList.add(new Message(messageType,username,message));
        mAdapter.notifyItemInserted(messageList.size()-1);
        scrollUp();
    }
    private void addParticipantsLog(int numUsers) {
        addLog("There are "+numUsers+" users in the chat room");
        scrollUp();
    }

    private void addTyping(String username){
        typingView.setText(username.trim()+" is typing");
    }

    private void removeTyping() {
        typingView.setText(null);
    }

    private void attemptSend() {
        if (mUsername==null) return;
        if (!mSocket.connected()) return;
        if(mTyping) {
            mTyping = false;
            mSocket.emit("stop typing");
        }
        String message = editMessage.getText().toString().trim();
        if (TextUtils.isEmpty(message)) {
            editMessage.requestFocus();
            return;
        }

        editMessage.setText("");
        addMessage(mUsername, message,Message.TYPE_MESSAGE_SENT);

        // perform the sending message attempt.
        mSocket.emit("new message", message);
    }

    private Runnable onTypingTimeout=new Runnable() {
        @Override
        public void run() {
            if(mTyping==false)
                return;

            mTyping=false;
            mSocket.emit("stop typing");
        }
    };
    private void scrollUp(){
        recyclerView.scrollToPosition(mAdapter.getItemCount()-1);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.logout:
                logout();
        }
        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        mSocket.disconnect();
        isConnected=false;
        mTyping=false;
        messageList.clear();
        this.recreate();
    }
}
