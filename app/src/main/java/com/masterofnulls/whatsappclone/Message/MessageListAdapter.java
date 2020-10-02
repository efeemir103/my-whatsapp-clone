package com.masterofnulls.whatsappclone.Message;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.google.firebase.auth.FirebaseAuth;
import com.masterofnulls.whatsappclone.R;
import com.masterofnulls.whatsappclone.User.User;
import com.stfalcon.frescoimageviewer.ImageViewer;

import java.util.ArrayList;

public class MessageListAdapter extends RecyclerView.Adapter<MessageListAdapter.MessageListViewHolder> {
    ArrayList<Message> messageList;
    ArrayList<User> userList;

    Context context;

    public MessageListAdapter(Context context, ArrayList<Message> messageList, ArrayList<User> userList) {
        this.context = context;
        this.messageList = messageList;
        this.userList = userList;
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
        String senderID = messageList.get(position).getSenderId();
        User user = findUserWithID(senderID);
        holder.mMessage.setText(messageList.get(position).getMessage());
        if(userList.size() <= 2) {
            holder.mSenderName.setVisibility(View.GONE);
            holder.mSenderPhone.setVisibility(View.GONE);
        } else {
            holder.mSenderName.setText(user.getName());
            holder.mSenderPhone.setText(user.getPhone());
        }

        if(messageList.get(position).getMediaURLList().isEmpty()) {
            holder.mViewMedia.setVisibility(View.GONE);
        } else {
            holder.mViewMedia.setImageURI(messageList.get(position).getMediaURLList().get(0));
        }

        if(messageList.get(position).getSenderId().equals(FirebaseAuth.getInstance().getUid())) {
            holder.mMessageLeftMargin.setLayoutParams(new LinearLayout.LayoutParams(
                    0,
                    0,
                    1
            ));
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

    private User findUserWithID(String id) {
        for(User user: userList) {
            if(id.equals(user.getUid())) {
                return user;
            }
        }

        return null;
    }

    @Override
    public int getItemCount() { return messageList.size(); }

    static public class MessageListViewHolder extends RecyclerView.ViewHolder {
        public View mMessageLeftMargin;
        public TextView mMessage, mSenderName, mSenderPhone;
        public SimpleDraweeView mViewMedia;
        public LinearLayout mLayout;

        public MessageListViewHolder(View view) {
            super(view);
            mLayout = view.findViewById(R.id.layout);
            mMessageLeftMargin = view.findViewById(R.id.messageLeftMargin);
            mMessage = view.findViewById(R.id.message);
            mSenderName = view.findViewById(R.id.senderName);
            mSenderPhone = view.findViewById(R.id.senderPhone);
            mViewMedia = view.findViewById(R.id.viewMedia);
        }
    }

}