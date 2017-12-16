package com.example.nitishkumar.socketchat;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Nitish Kumar on 14-12-2017.
 */

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MyViewHolder> {
    private List<Message> mMessageList;

    public MessageAdapter(List<Message>messageList){
        this.mMessageList=messageList;
    }
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int layout=-1;
        switch (viewType){
            case Message.TYPE_MESSAGE:
                layout=R.layout.message_item;
                break;
            case Message.TYPE_LOG:
                layout=R.layout.log_item;
                break;
        }
        View itemView= LayoutInflater.from(parent.getContext()).inflate(layout,parent,false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MessageAdapter.MyViewHolder holder, int position) {
        Message currentMessage=mMessageList.get(position);
        if(currentMessage.getmUsername()!=null)
            holder.setmUsername(currentMessage.getmUsername());

        holder.setmMessage(currentMessage.getmMessage());
    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return mMessageList.get(position).getmType();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        public TextView mMessage,mUsername;

        public MyViewHolder(View itemView) {
            super(itemView);
            mMessage=(TextView)itemView.findViewById(R.id.message);
            mUsername=(TextView)itemView.findViewById(R.id.username);
        }

        public void setmUsername(String username){
            if(mUsername==null)
                return;
            mUsername.setText(username);
        }

        public void setmMessage(String message){
            if(mMessage==null)
                return;
            mMessage.setText(message);
        }
    }




}
