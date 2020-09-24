package com.bitflac.instaclone.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bitflac.instaclone.Fragments.PostDetailsFragment;
import com.bitflac.instaclone.Model.Post;
import com.bitflac.instaclone.R;
import com.squareup.picasso.Picasso;

import java.util.List;


public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.Holder> {

    private Context mcontext;
    private List<Post> mPosts;
    private boolean isSaved;

    public PhotoAdapter(Context mcontext, List<Post> mPost, boolean isSaved) {
        this.mcontext = mcontext;
        this.mPosts = mPost;
        this.isSaved = isSaved;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mcontext).inflate(R.layout.photo_item, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        final Post post = mPosts.get(position);
        Picasso.get().load(post.getImageurl()).into(holder.postImage);

        holder.postImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mcontext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit().putString("postid", post.getPostId()).apply();
                mcontext.getSharedPreferences("PUB", Context.MODE_PRIVATE).edit().putString("publisherId", post.getPublisher()).apply();
                mcontext.getSharedPreferences("isSaved",Context.MODE_PRIVATE).edit().putBoolean("isSaved", isSaved).apply();

                ((FragmentActivity) mcontext).getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new PostDetailsFragment()).commit();

            }
        });

    }

    @Override
    public int getItemCount() {
        return mPosts.size();
    }

    public static class Holder extends RecyclerView.ViewHolder {

        public ImageView postImage;

        public Holder(@NonNull View itemView) {
            super(itemView);
            postImage = itemView.findViewById(R.id.post_img);
        }
    }

}
