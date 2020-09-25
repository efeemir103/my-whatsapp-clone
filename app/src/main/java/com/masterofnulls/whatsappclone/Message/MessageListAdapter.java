package com.masterofnulls.whatsappclone.Message;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.masterofnulls.whatsappclone.R;
import com.stfalcon.frescoimageviewer.ImageViewer;

import java.util.ArrayList;

public class MessageListAdapter extends RecyclerView.Adapter<MessageListAdapter.MessageListViewHolder> {
    ArrayList<Message> messageList;

    public MessageListAdapter(ArrayList<Message> messageList) {
        this.messageList = messageList;
    }

    @NonNull
    @Override
    public MessageListAdapter.MessageListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, null, false);
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutView.setLayoutParams(lp);

        MessageListAdapter.MessageListViewHolder rcv = new MessageListAdapter.MessageListViewHolder(layoutView);

        return rcv;
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageListAdapter.MessageListViewHolder holder, int position) {
        holder.mMessage.setText(messageList.get(position).getMessage());
        holder.mSender.setText(messageList.get(position).getSenderId());

        if(messageList.get(holder.getAdapterPosition()).getMediaURLList().isEmpty()) {
            holder.mViewMedia.setVisibility(View.GONE);
        }

        holder.mViewMedia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new ImageViewer.Builder(view.getContext(), messageList.get(holder.getAdapterPosition()).getMediaURLList())
                        .setStartPosition(0)
                        .show();
            }
        });
    }

    @Override
    public int getItemCount() { return messageList.size(); }

    static public class MessageListViewHolder extends RecyclerView.ViewHolder {
        public TextView mMessage, mSender;
        public Button mViewMedia;
        public LinearLayout mLayout;

        public MessageListViewHolder(View view) {
            super(view);
            mLayout = view.findViewById(R.id.layout);
            mMessage = view.findViewById(R.id.message);
            mSender = view.findViewById(R.id.sender);
            mViewMedia = view.findViewById(R.id.viewMedia);
        }
    }

}