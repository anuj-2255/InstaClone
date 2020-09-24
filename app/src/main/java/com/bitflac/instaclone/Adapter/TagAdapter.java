package com.bitflac.instaclone.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bitflac.instaclone.R;

import java.util.List;

public class TagAdapter extends RecyclerView.Adapter<TagAdapter.Holder> {

    private Context context;
    private List<String> mTgas;
    private List<String> mTagsCount;

    public TagAdapter(Context context, List<String> mTgas, List<String> mTagsCount) {
        this.context = context;
        this.mTgas = mTgas;
        this.mTagsCount = mTagsCount;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.tag_item,parent,false);
        return new TagAdapter.Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        String tag_position="#"+(mTgas.get(position));
        holder.tag.setText(tag_position);
        String tag_post_count=mTagsCount.get(position)+" posts";
        holder.noOfPosts.setText(tag_post_count);
    }

    @Override
    public int getItemCount() {
        return mTgas.size();
    }

    public class Holder extends RecyclerView.ViewHolder{

        public TextView tag,noOfPosts;

        public Holder(@NonNull View itemView) {
            super(itemView);
            tag=itemView.findViewById(R.id.hash_tag);
            noOfPosts=itemView.findViewById(R.id.no_of_posts);
        }
    }

    public void filter(List<String> filtertags,List<String> filtertagscount){
        this.mTgas=filtertags;
        this.mTagsCount=filtertagscount;
        notifyDataSetChanged();
    }
}
