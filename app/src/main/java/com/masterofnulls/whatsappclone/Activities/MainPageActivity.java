package com.masterofnulls.whatsappclone.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.masterofnulls.whatsappclone.Chat.Chat;
import com.masterofnulls.whatsappclone.Chat.ChatListAdapter;
import com.masterofnulls.whatsappclone.R;
import com.masterofnulls.whatsappclone.User.User;

import com.masterofnulls.whatsappclone.Utils.CompleteListener;
import com.masterofnulls.whatsappclone.Utils.ContactsManager;
import com.onesignal.OneSignal;

import java.util.ArrayList;

public class MainPageActivity extends AppCompatActivity {

    private RecyclerView mChatList;
    private RecyclerView.Adapter mChatListAdapter;
    private RecyclerView.LayoutManager mChatListLayoutManager;

    ArrayList<Chat> chatList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);

        OneSignal.startInit(this).init();
        OneSignal.setSubscription(true);
        OneSignal.idsAvailable(new OneSignal.IdsAvailableHandler() {
            @Override
            public void idsAvailable(String userId, String registrationId) {
                FirebaseDatabase.getInstance().getReference().child("user").child(FirebaseAuth.getInstance().getUid()).child("notificationKey").setValue(userId);
            }
        });
        OneSignal.setInFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification);

        Fresco.initialize(this);

        chatList = new ArrayList<>();

        Button mLogout = findViewById(R.id.logout);
        FloatingActionButton mFindUser = findViewById(R.id.findUser);

        mFindUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), FindUserActivity.class));
            }
        });

        mLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OneSignal.setSubscription(false);
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });

        getPermissions();
        initializeRecyclerView();
        ContactsManager.setOnCompleteListener(new CompleteListener() {
            @Override
            public void onSuccess() {
                getUserChatList();
            }
        });
    }

    private void getUserChatList() {
        DatabaseReference mUserChatDB = FirebaseDatabase.getInstance().getReference()
                .child("user").child(FirebaseAuth.getInstance().getUid()).child("chat");

        mUserChatDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    for(DataSnapshot childSnapshot: snapshot.getChildren()) {
                        Chat mChat = new Chat(childSnapshot.getKey());
                        boolean exists = false;
                        for(Chat chatIterator: chatList) {
                            if(chatIterator.getChatId().equals(mChat.getChatId())) {
                                exists = true;
                                break;
                            }
                        }
                        if(!exists) {
                            chatList.add(mChat);
                        }
                    }

                    for(Chat chat: chatList) {
                        getChatData(chat.getChatId());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getChatData(String chatId) {
        DatabaseReference mChatDB = FirebaseDatabase.getInstance().getReference()
                .child("chat").child(chatId).child("info");
        mChatDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    String chatId = "", chatIcon = "";

                    if(snapshot.child("id").getValue() != null) {
                        chatId = snapshot.child("id").getValue().toString();
                    }

                    if(snapshot.child("icon").getValue() != null) {
                        chatIcon = snapshot.child("icon").getValue().toString();
                        for(Chat chat: chatList) {
                            if(chat.getChatId().equals(chatId)) {
                                chat.setChatIcon(chatIcon);
                            }
                        }
                    }

                    for(DataSnapshot userSnapshot: snapshot.child("users").getChildren()) {
                        for(Chat chat: chatList) {
                            if(chat.getChatId().equals(chatId)) {
                                chat.addUser(new User(userSnapshot.getKey(), "", ""));
                            }
                        }
                    }

                    for(Chat chat: chatList) {
                        if(chat.getChatId().equals(chatId)) {
                            for(User user: chat.getUsers()) {
                                getUserData(user.getUid());
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getUserData(String uid) {
        DatabaseReference mUserDB = FirebaseDatabase.getInstance().getReference().child("user").child(uid);
        mUserDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    User user = ContactsManager.getContactWithID(snapshot.getKey(), new User(snapshot.getKey(), "", snapshot.child("phone").getValue().toString()));

                    if(snapshot.child("notificationKey").getValue() != null) {
                        user.setNotificationKey(snapshot.child("notificationKey").getValue().toString());
                    }


                    for(int i = 0; i < chatList.size(); i++) {
                        chatList.get(i).updateUser(user);
                    }

                    mChatListAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void initializeRecyclerView() {
        mChatList = findViewById(R.id.chatList);
        mChatList.setNestedScrollingEnabled(false);
        mChatList.setHasFixedSize(false);
        mChatListLayoutManager = new LinearLayoutManager(getApplicationContext(), RecyclerView.VERTICAL, false);
        mChatList.setLayoutManager(mChatListLayoutManager);
        mChatListAdapter = new ChatListAdapter(getApplicationContext(), chatList);
        mChatList.setAdapter(mChatListAdapter);
    }

    private void getPermissions() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.WRITE_CONTACTS, Manifest.permission.READ_CONTACTS}, 1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        ContactsManager.getContactList(this);
    }
}