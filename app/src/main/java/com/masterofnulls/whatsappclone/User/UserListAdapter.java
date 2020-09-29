package com.masterofnulls.whatsappclone.User;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.masterofnulls.whatsappclone.R;

import java.util.ArrayList;
import java.util.HashMap;

public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.UserListViewHolder> {

    ArrayList<User> userList;

    public UserListAdapter(ArrayList<User> userList) {
        this.userList = userList;
    }

    @NonNull
    @Override
    public UserListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, null, false);
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutView.setLayoutParams(lp);

        UserListViewHolder rcv = new UserListViewHolder(layoutView);
        return rcv;
    }

    @Override
    public void onBindViewHolder(@NonNull final UserListViewHolder holder, int position) {
        holder.mName.setText(userList.get(position).getName());
        holder.mPhone.setText(userList.get(position).getPhone());

        holder.mAdd.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                userList.get(holder.getAdapterPosition()).setSelected(b);
            }
        });
    }

    private  void createChat(int position) {
        String key = FirebaseDatabase.getInstance().getReference().child("chat").push().getKey();

        HashMap<String, Object> newChatMap = new HashMap();
        newChatMap.put("id", key);
        newChatMap.put("users/" + FirebaseAuth.getInstance().getUid(), true);
        newChatMap.put("users/" + userList.get(position).getUid(), true);

        DatabaseReference chatInfoDB = FirebaseDatabase.getInstance().getReference().child("chat").child(key).child("info");
        chatInfoDB.updateChildren(newChatMap);

        DatabaseReference userDB = FirebaseDatabase.getInstance().getReference().child("user");

        userDB.child(FirebaseAuth.getInstance().getUid()).child("chat").child(key).setValue(true);
        userDB.child(userList.get(position).getUid()).child("chat").child(key).setValue(true);

    }

    @Override
    public int getItemCount() {
        return userList.size();
    }


    static public class UserListViewHolder extends RecyclerView.ViewHolder {
        public TextView mName, mPhone;
        public CheckBox mAdd;
        public LinearLayout mLayout;
        public UserListViewHolder(View view) {
            super(view);
            mName = view.findViewById(R.id.name);
            mPhone = view.findViewById(R.id.phone);
            mAdd = view.findViewById(R.id.add);
            mLayout = view.findViewById(R.id.layout);
        }
    }
}
