package com.bitflac.instaclone.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bitflac.instaclone.Adapter.PostAdapter;
import com.bitflac.instaclone.Model.Post;
import com.bitflac.instaclone.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class PostDetailsFragment extends Fragment {

    private String postId, publisherId;
    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private List<Post> postsList;
    private boolean isSaved, isNotification;
    FirebaseUser user;
    private List<String> savedImages;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_post_details, container, false);
        user = FirebaseAuth.getInstance().getCurrentUser();

        if (getContext() != null) {
            postId = Objects.requireNonNull(getContext()).getSharedPreferences("PREFS", Context.MODE_PRIVATE).getString("postid", "none");
            publisherId = getContext().getSharedPreferences("PUB", Context.MODE_PRIVATE).getString("publisherId", "none");
            isSaved = getContext().getSharedPreferences("isSaved", Context.MODE_PRIVATE).getBoolean("isSaved", false);
            isNotification = getContext().getSharedPreferences("POSTNOTIFICATION", Context.MODE_PRIVATE).getBoolean("post_notification", false);
        }

        recyclerView = view.findViewById(R.id.recycler_view_details);
        recyclerView.hasFixedSize();

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        postsList = new ArrayList<>();
        postAdapter = new PostAdapter(getContext(), postsList);

        recyclerView.setAdapter(postAdapter);
        if (isSaved) {
            savedImages = new ArrayList<>();
            FirebaseDatabase.getInstance().getReference().child("Saves").child(user.getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                        savedImages.add(snapshot1.getKey());
                    }
                    FirebaseDatabase.getInstance().getReference().child("Posts").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            postsList.clear();
                            for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                                Post post = snapshot1.getValue(Post.class);

                                for (String id : savedImages) {
                                    if (post != null && post.getPostId().equals(id)) {
                                        postsList.add(post);
                                    }
                                }
                            }
                            Collections.reverse(postsList);
                            postAdapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        } else {
            FirebaseDatabase.getInstance().getReference().child("Posts").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    postsList.clear();
                    for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                        Post post = snapshot1.getValue(Post.class);
                        if (isNotification) {
                            if (post != null && post.getPostId().equals(postId)) postsList.add(post);
                            if (getContext()!=null)
                            getContext().getSharedPreferences("POSTNOTIFICATION", Context.MODE_PRIVATE).edit().remove("post_notification").apply();
                        } else {
                            if (post != null && post.getPublisher().equals(publisherId)) postsList.add(post);
                        }
                    }
                    postAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
        return view;
    }
}
