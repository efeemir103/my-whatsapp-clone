package com.masterofnulls.whatsappclone.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.masterofnulls.whatsappclone.Media.MediaListAdapter;
import com.masterofnulls.whatsappclone.Message.Message;
import com.masterofnulls.whatsappclone.Message.MessageListAdapter;
import com.masterofnulls.whatsappclone.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView mMessageList, mMediaList;
    private RecyclerView.Adapter mMessageListAdapter, mMediaListAdapter;
    private RecyclerView.LayoutManager mMessageListLayoutManager, mMediaListLayoutManager;

    private EditText mMessage;

    ArrayList<Message> messageList;

    String chatID;

    DatabaseReference mChatDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chatID = getIntent().getExtras().getString("chatID");

        mChatDB = FirebaseDatabase.getInstance().getReference().child("chat").child(chatID);

        messageList = new ArrayList<>();

        Button mSend = findViewById(R.id.send);
        Button mAddMedia = findViewById(R.id.addMedia);

        mMessage = findViewById(R.id.message);

        mSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });
        mAddMedia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGallery();
            }
        });

        initializeMessage();
        initializeMedia();
        getChatMessages();
    }

    private void getChatMessages() {
        mChatDB.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if(snapshot.exists()) {
                    String text = "", creatorID = "";

                    ArrayList<String> mediaURLList = new ArrayList<>();

                    if(snapshot.child("text").getValue() != null) {
                        text = snapshot.child("text").getValue().toString();
                    }

                    if(snapshot.child("creator").getValue() != null) {
                        creatorID = snapshot.child("creator").getValue().toString();
                    }

                    if(snapshot.child("media").getChildrenCount() > 0){
                        for(DataSnapshot mediaSnapshot: snapshot.child("media").getChildren()) {
                            mediaURLList.add(mediaSnapshot.getValue().toString());
                        }
                    }

                    Message mMessage = new Message(snapshot.getKey(), creatorID, text, mediaURLList);
                    messageList.add(mMessage);
                    mMessageListLayoutManager.scrollToPosition(messageList.size()-1);
                    mMessageListAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) { }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) { }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) { }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void initializeMessage() {
        mMessageList = findViewById(R.id.messageList);
        mMessageList.setNestedScrollingEnabled(false);
        mMessageList.setHasFixedSize(false);
        mMessageListLayoutManager = new LinearLayoutManager(getApplicationContext(), RecyclerView.VERTICAL, false);
        mMessageList.setLayoutManager(mMessageListLayoutManager);
        mMessageListAdapter = new MessageListAdapter(messageList);
        mMessageList.setAdapter(mMessageListAdapter);
    }

    int PICK_IMAGE_INTENT = 1;
    ArrayList<String> mediaURIList = new ArrayList<>();

    private void initializeMedia() {
        mMediaList = findViewById(R.id.mediaList);
        mMediaList.setNestedScrollingEnabled(false);
        mMediaList.setHasFixedSize(false);
        mMediaListLayoutManager = new LinearLayoutManager(getApplicationContext(), RecyclerView.HORIZONTAL, false);
        mMediaList.setLayoutManager(mMediaListLayoutManager);
        mMediaListAdapter = new MediaListAdapter(getApplicationContext(), mediaURIList);
        mMediaList.setAdapter(mMediaListAdapter);
    }

    int mediaUploadIndex = 0;
    ArrayList<String> mediaUploadIDList = new ArrayList<>();
    private void sendMessage() {
        String messageId = mChatDB.push().getKey();
        final DatabaseReference newMessageDB = mChatDB.push();

        final Map<String, Object> newMessageMap = new HashMap<>();

        newMessageMap.put("creator", FirebaseAuth.getInstance().getUid());

        if(!mMessage.getText().toString().isEmpty()) {
            newMessageMap.put("text", mMessage.getText().toString());
        }

        if(!mediaURIList.isEmpty()) {
            for(String mediaURI: mediaURIList) {
                String mediaID = newMessageDB.child("media").push().getKey();
                mediaUploadIDList.add(mediaID);
                final StorageReference filePath = FirebaseStorage.getInstance().getReference().child("chat").child(messageId).child(mediaID);

                UploadTask uploadTask = filePath.putFile(Uri.parse(mediaURI));

                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                newMessageMap.put("/media/" + mediaUploadIDList.get(mediaUploadIndex) + "/", uri.toString());
                                mediaUploadIndex++;
                                if(mediaUploadIndex == mediaURIList.size()) {
                                    updateDatabaseWithNewMessage(newMessageDB, newMessageMap);
                                }
                            }
                        });
                    }
                });
            }
        } else {
            if(!mMessage.getText().toString().isEmpty()) {
                updateDatabaseWithNewMessage(newMessageDB, newMessageMap);
            }
        }
    }

    private void updateDatabaseWithNewMessage(DatabaseReference newMessageDB, Map<String, Object> newMessageMap) {
        newMessageDB.updateChildren(newMessageMap);
        mMessage.setText(null);
        mediaURIList.clear();
        mediaUploadIDList.clear();
        mediaUploadIndex = 0;
        mMediaListAdapter.notifyDataSetChanged();
    }

    private void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture(s)"), PICK_IMAGE_INTENT);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK) {
            if(requestCode == PICK_IMAGE_INTENT) {
                if(data.getClipData() == null) {
                    mediaURIList.add(data.getData().toString());
                } else {
                    for(int i = 0; i < data.getClipData().getItemCount(); i++) {
                        mediaURIList.add(data.getClipData().getItemAt(i).getUri().toString());
                    }
                }

                mMediaListAdapter.notifyDataSetChanged();
            }
        }
    }
}