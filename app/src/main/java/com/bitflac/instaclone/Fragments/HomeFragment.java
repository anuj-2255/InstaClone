package com.bitflac.instaclone.Fragments;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bitflac.instaclone.Adapter.PostAdapter;
import com.bitflac.instaclone.Adapter.StoryAdapter;
import com.bitflac.instaclone.Model.Post;
import com.bitflac.instaclone.Model.Story;
import com.bitflac.instaclone.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class HomeFragment extends Fragment {

    private RecyclerView recyclerViewPosts;
    private PostAdapter postAdapter;
    private List<Post> postList;

    private RecyclerView recyclerViewStory;
    private StoryAdapter storyAdapter;
    private List<Story> storyList;

    private ProgressBar progressBar;
    private List<String> followingList;
    private FirebaseUser fUser;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        fUser=FirebaseAuth.getInstance().getCurrentUser();
        progressBar=view.findViewById(R.id.prog_bar);
        recyclerViewPosts = view.findViewById(R.id.recycler_view_posts);
        recyclerViewPosts.hasFixedSize();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        recyclerViewPosts.setLayoutManager(linearLayoutManager);
        postList = new ArrayList<>();
        postAdapter = new PostAdapter(getContext(), postList);
        recyclerViewPosts.setAdapter(postAdapter);
        recyclerViewPosts.setVisibility(View.GONE);

        recyclerViewStory = view.findViewById(R.id.recycler_view_story);
        storyList = new ArrayList<>();
        storyAdapter = new StoryAdapter(getContext(), storyList);
        LinearLayoutManager linearLayoutManager1 = new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false);
        recyclerViewStory.setLayoutManager(linearLayoutManager1);
        recyclerViewStory.setAdapter(storyAdapter);

        followingList = new ArrayList<>();

        CheckFollowingUsers();

        return view;
    }

    private void CheckFollowingUsers() {

        FirebaseDatabase.getInstance().getReference().child("Follow").child(fUser.getUid())
                .child("following").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                followingList.clear();
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    followingList.add(snapshot1.getKey());
                }
                followingList.add(fUser.getUid());

                readPosts();
                readStory();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void readPosts() {

        FirebaseDatabase.getInstance().getReference().child("Posts").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    Post post = snapshot1.getValue(Post.class);

                    for (String id : followingList) {
                        if (post != null && post.getPublisher().equals(id)) {
                            postList.add(post);
                        }
                    }
                }

                progressBar.setVisibility(View.GONE);
                recyclerViewPosts.setVisibility(View.VISIBLE);
                postAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void readStory() {
        followingList.remove(fUser.getUid());
        FirebaseDatabase.getInstance().getReference().child("Story").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long timecurrent = System.currentTimeMillis();
                storyList.clear();
                storyList.add(new Story("", 0, 0, "",
                        fUser.getUid()));
                for (String id : followingList) {

                    int countStory = 0;
                    Story story = null;
                    for (DataSnapshot snapshot1 : snapshot.child(id).getChildren()) {
                        story = snapshot1.getValue(Story.class);
                        if (story != null && timecurrent > story.getTimestart() && timecurrent < story.getTimeend()) {
                            countStory++;
                        }
                    }
                    if (countStory > 0) {
                        storyList.add(story);
                    }
                }
                storyAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


}
