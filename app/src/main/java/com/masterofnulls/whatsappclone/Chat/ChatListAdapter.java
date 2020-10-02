package com.masterofnulls.whatsappclone.Chat;

import android.content.Context;
import android.content.Intent;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.google.firebase.auth.FirebaseAuth;
import com.masterofnulls.whatsappclone.Activities.ChatActivity;
import com.masterofnulls.whatsappclone.R;
import com.masterofnulls.whatsappclone.User.User;

import java.util.ArrayList;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatListViewHolder> {
    ArrayList<Chat> chatList;
    Context context;

    public ChatListAdapter(Context context, ArrayList<Chat> chatList) {
        this.context = context;
        this.chatList = chatList;
    }

    @NonNull
    @Override
    public ChatListAdapter.ChatListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat, null, false);
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutView.setLayoutParams(lp);

        ChatListAdapter.ChatListViewHolder rcv = new ChatListAdapter.ChatListViewHolder(layoutView);
        return rcv;
    }

    @Override
    public void onBindViewHolder(@NonNull final ChatListViewHolder holder, final int position) {
        ArrayList<User> users = chatList.get(position).getUsers();
        if(users != null) {
            ArrayList<String> userNames = new ArrayList<>();
            for(User user: users) {
                if(!user.getUid().equals(FirebaseAuth.getInstance().getUid())) {
                    if(user.getName().isEmpty()) {
                        userNames.add(user.getPhone());
                    } else {
                        userNames.add(user.getName());
                    }
                }
            }
            if(userNames.size() <= 1) {
                chatList.get(0).setChatName(userNames.get(0));
                holder.mTitle.setText(userNames.get(0));
            } else {
                StringBuilder title = new StringBuilder("Chat Room with ");
                for(int i = 0; i < userNames.size() - 2; i++) {
                    title.append(userNames.get(i)).append(", ");
                }
                title.append(userNames.get(userNames.size() - 2)).append(" and ").append(userNames.get(userNames.size() - 1));
                chatList.get(position).setChatName(title.toString());
                holder.mTitle.setText(title.toString());
            }
        } else {
            holder.mTitle.setText(chatList.get(position).getChatId());
        }

        if(!chatList.get(position).getChatIcon().isEmpty()) {
            holder.mIcon.setImageURI(chatList.get(position).getChatIcon());
            holder.mIcon.setBackground(null);
        }

        holder.mLayout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), ChatActivity.class);

                intent.putExtra("chat", chatList.get(holder.getAdapterPosition()));

                view.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    static public class ChatListViewHolder extends RecyclerView.ViewHolder {
        public SimpleDraweeView mIcon;
        public TextView mTitle;
        public LinearLayout mLayout;

        public ChatListViewHolder(View view) {
            super(view);
            mIcon = view.findViewById(R.id.chatIcon);
            mTitle = view.findViewById(R.id.title);
            mLayout = view.findViewById(R.id.layout);
        }
    }
}
