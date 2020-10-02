package com.masterofnulls.whatsappclone.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.masterofnulls.whatsappclone.R;
import com.masterofnulls.whatsappclone.User.User;
import com.masterofnulls.whatsappclone.User.UserListAdapter;
import com.masterofnulls.whatsappclone.Utils.SendNotification;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FindUserActivity extends AppCompatActivity {

    private RecyclerView mUserList;
    private RecyclerView.Adapter mUserListAdapter;
    private RecyclerView.LayoutManager mUserListLayoutManager;

    ArrayList<User> userList, contactList;

    DatabaseReference mUserDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_user);

        mUserDB = FirebaseDatabase.getInstance().getReference().child("user");

        contactList = new ArrayList<>();
        userList = new ArrayList<>();

        Button mCreate = findViewById(R.id.create);
        mCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int n = 0;
                for(User user: userList) {
                    if(user.getSelected() && !user.getUid().equals(FirebaseAuth.getInstance().getUid())) {
                        n++;
                    }
                }

                if(n <= 1) {
                    createChat();
                } else {
                    createGroupChat();
                }
            }
        });

        initializeRecyclerView();
        getContactList();
    }

    int PICK_IMAGE_INTENT = 1;
    String imageURI = "";
    private void createGroupChat() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture for the Group Chat"), PICK_IMAGE_INTENT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK) {
            if(requestCode == PICK_IMAGE_INTENT) {
                if(data.getClipData() == null) {
                    imageURI = data.getData().toString();
                } else {
                    for(int i = 0; i < data.getClipData().getItemCount(); i++) {
                        imageURI = data.getClipData().getItemAt(i).getUri().toString();
                    }
                }

                uploadIcon(imageURI);
            }
        }
    }

    private void uploadIcon(String imageURI) {
        if(!imageURI.isEmpty()) {
            final DatabaseReference newChatDB = FirebaseDatabase.getInstance().getReference().child("chat").push();
            String chatID =  newChatDB.getKey();
            final StorageReference filePath = FirebaseStorage.getInstance().getReference().child("icon").child(chatID);
            UploadTask uploadTask = filePath.putFile(Uri.parse(imageURI));

            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(final Uri uri) {
                            newChatDB.child("info").updateChildren(new HashMap<String, Object>(){
                                {
                                    put("icon", uri);
                                }
                            });
                        }
                    });
                }
            });
            createChat(newChatDB.getKey());
        }
    }

    private void createChat(String key) {

        DatabaseReference chatInfoDB = FirebaseDatabase.getInstance().getReference().child("chat").child(key).child("info");
        DatabaseReference userDB = FirebaseDatabase.getInstance().getReference().child("user");

        HashMap<String, Object> newChatMap = new HashMap();
        newChatMap.put("id", key);
        newChatMap.put("users/" + FirebaseAuth.getInstance().getUid(), true);

        boolean validChat = false;

        for(User user: userList) {
            if(user.getSelected()) {
                validChat = true;
                newChatMap.put("users/" + user.getUid(), true);
                userDB.child(user.getUid()).child("chat").child(key).setValue(true);
            }
        }

        if(validChat) {
            chatInfoDB.updateChildren(newChatMap);
            userDB.child(FirebaseAuth.getInstance().getUid()).child("chat").child(key).setValue(true);
        }
    }

    private void createChat() {
        String key = FirebaseDatabase.getInstance().getReference().child("chat").push().getKey();
        createChat(key);
    }

    private void getContactList() {

        String ISOPrefix = getCountryISO();

        Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        while(phones.moveToNext()) {
            String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String phone = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

            phone = phone.replace(" ", "");
            phone = phone.replace("-", "");
            phone = phone.replace("(", "");
            phone = phone.replace(")", "");

            if(phone.charAt(0) != '+') {
                if(phone.charAt(0) == '0') {
                    phone = phone.substring(1);
                }
                phone = ISOPrefix + phone;
            }

            User mContact = new User("", name, phone);
            contactList.add(mContact);
            getUserDetails(mContact);
        }

        phones.close();
    }

    private void getUserDetails(final User mContact) {
        Query query = mUserDB.orderByChild("phone").equalTo(mContact.getPhone());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    String phone = "", name;

                    for(DataSnapshot childSnapshot: snapshot.getChildren()) {
                        if(childSnapshot.child("phone").getValue() != null) {
                            phone = childSnapshot.child("phone").getValue().toString();
                        }

                        name = mContact.getName();

                        User mUser = new User(childSnapshot.getKey(), name, phone);
                        userList.add(mUser);
                        mUserListAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private String getCountryISO() {
        String iso = "";

        TelephonyManager telephonyManager = (TelephonyManager) getApplicationContext().getSystemService(TELEPHONY_SERVICE);

        if(telephonyManager.getNetworkCountryIso() != null) {
            if(!telephonyManager.getNetworkCountryIso().equals("")){
                iso = telephonyManager.getNetworkCountryIso();
            }
        }

        return iso;
    }

    private void initializeRecyclerView() { 
        mUserList = findViewById(R.id.userList);
        mUserList.setNestedScrollingEnabled(false);
        mUserList.setHasFixedSize(false);
        mUserListLayoutManager = new LinearLayoutManager(getApplicationContext(), RecyclerView.VERTICAL, false);
        mUserList.setLayoutManager(mUserListLayoutManager);
        mUserListAdapter = new UserListAdapter(userList);
        mUserList.setAdapter(mUserListAdapter);
    }
}