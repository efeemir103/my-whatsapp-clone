package com.masterofnulls.whatsappclone.Media;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.masterofnulls.whatsappclone.R;

import java.util.ArrayList;

public class MediaListAdapter extends RecyclerView.Adapter<MediaListAdapter.MediaViewHolder> {
    ArrayList<String> mediaURIList;
    Context context;

    public MediaListAdapter(Context context, ArrayList<String> mediaURIList) {
        this.context = context;
        this.mediaURIList = mediaURIList;
    }

    @NonNull
    @Override
    public MediaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_media, null, false);
        MediaViewHolder mediaViewHolder = new MediaViewHolder(layoutView);

        return mediaViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MediaViewHolder holder, int position) {
        Glide.with(context).load(Uri.parse(mediaURIList.get(position))).into(holder.mMedia);
    }

    @Override
    public int getItemCount() {
        return mediaURIList.size();
    }

    public class MediaViewHolder extends RecyclerView.ViewHolder {

        ImageView mMedia;

        public MediaViewHolder(@NonNull View itemView) {
            super(itemView);
            mMedia = itemView.findViewById(R.id.media);
        }
    }
}
