package com.masterofnulls.whatsappclone.Utils;

import android.database.Cursor;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.masterofnulls.whatsappclone.User.User;

import java.util.ArrayList;

import static android.content.Context.TELEPHONY_SERVICE;

public class ContactsManager{
    static private CompleteListener listener;

    static ArrayList<User> userList = new ArrayList<>(), contactList = new ArrayList<>();

    static DatabaseReference mUserDB = FirebaseDatabase.getInstance().getReference().child("user");

    static public void getContactList(AppCompatActivity activity) {

        String ISOPrefix = getCountryISO(activity);

        Cursor phones = activity.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
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
        }

        for(User contact: contactList) {
            getUserDetails(contact);
        }

        phones.close();
        listener.onSuccess();
    }

    static private void getUserDetails(final User mContact) {
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
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    static private String getCountryISO(AppCompatActivity activity) {
        String iso = "";

        TelephonyManager telephonyManager = (TelephonyManager) activity.getApplicationContext().getSystemService(TELEPHONY_SERVICE);

        if(telephonyManager.getNetworkCountryIso() != null) {
            if(!telephonyManager.getNetworkCountryIso().equals("")){
                iso = telephonyManager.getNetworkCountryIso();
            }
        }

        return iso;
    }

    static public User getContactWithID(String id, User alternative) {
        for(User user: userList) {
            if(user.getUid().equals(id)) {
                return user;
            }
        }
        return alternative;
    }

    static public void setOnCompleteListener(CompleteListener listener1) {
        listener = listener1;
    }
}
