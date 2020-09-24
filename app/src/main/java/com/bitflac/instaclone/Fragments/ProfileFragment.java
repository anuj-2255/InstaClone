package com.bitflac.instaclone.Fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bitflac.instaclone.Adapter.PhotoAdapter;
import com.bitflac.instaclone.EditProfileActivity;
import com.bitflac.instaclone.FollowersActivity;
import com.bitflac.instaclone.Model.Post;
import com.bitflac.instaclone.Model.User;
import com.bitflac.instaclone.OptionActivity;
import com.bitflac.instaclone.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {
    private RecyclerView recyclerView_saved;
    private PhotoAdapter photoAdapterSaves;
    private List<Post> mySavedPosts;

    private ProgressBar progressBar;
    private RecyclerView recyclerView_post;
    private PhotoAdapter photoAdapter;
    private List<Post> myPhotolist;
    private CircleImageView imgProfile;
    private Button editProfile;
    private ImageView options, myPics, savedPics;
    private TextView posts, followers, following, fullname, bio, username;

    private FirebaseUser fUser;
    String profileId, data;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        fUser = FirebaseAuth.getInstance().getCurrentUser();
        if (getContext() != null) {
            data = getContext().getSharedPreferences("PROFILE", Context.MODE_PRIVATE).getString("profileId", "none");
            getContext().getSharedPreferences("PROFILE", Context.MODE_PRIVATE).edit().remove("profileId").apply();
            if (data.equals("none")) {
                profileId = fUser.getUid();
            } else {
                profileId = data;
            }
        }

        progressBar=view.findViewById(R.id.pgbar);
        imgProfile = view.findViewById(R.id.img_prfle);
        options = view.findViewById(R.id.options);
        myPics = view.findViewById(R.id.my_pics);
        savedPics = view.findViewById(R.id.saved_pics);
        posts = view.findViewById(R.id.posts);
        followers = view.findViewById(R.id.followers);
        following = view.findViewById(R.id.following);
        fullname = view.findViewById(R.id.fullname);
        bio = view.findViewById(R.id.bio);
        username = view.findViewById(R.id.username3);
        editProfile = view.findViewById(R.id.edit_profile);
        recyclerView_post = view.findViewById(R.id.recycler_view_pics);
        recyclerView_post.hasFixedSize();
        myPhotolist = new ArrayList<>();
        photoAdapter = new PhotoAdapter(getContext(), myPhotolist, false);
        recyclerView_post.setLayoutManager(new GridLayoutManager(getContext(), 3));
        recyclerView_post.setAdapter(photoAdapter);

        recyclerView_saved = view.findViewById(R.id.recycler_view_saved);
        recyclerView_saved.hasFixedSize();
        recyclerView_saved.setLayoutManager(new GridLayoutManager(getContext(), 3));
        mySavedPosts = new ArrayList<>();
        photoAdapterSaves = new PhotoAdapter(getContext(), mySavedPosts, true);
        recyclerView_saved.setAdapter(photoAdapterSaves);

        recyclerView_post.setVisibility(View.GONE);
        recyclerView_saved.setVisibility(View.GONE);

        userInfo();
        getFollowersAndFollowingCount();
        getPostCount();
        myPhotos();
        getSavedPosts();

        if (profileId.equals(fUser.getUid())) {
            editProfile.setText("Edit Profile");
        } else {
            checkFollowingStatus();
        }

        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String btnText = editProfile.getText().toString();
                if (btnText.equals("Edit Profile")) {
                    startActivity(new Intent(getContext(), EditProfileActivity.class));
                } else {
                    if (btnText.equals("Follow")) {
                        FirebaseDatabase.getInstance().getReference().child("Follow")
                                .child(fUser.getUid()).child("following").child(profileId).setValue(true);

                        FirebaseDatabase.getInstance().getReference().child("Follow").child(profileId).child("followers").child(fUser.getUid())
                                .setValue(true);

                        addNotification();
                    } else {
                        FirebaseDatabase.getInstance().getReference().child("Follow")
                                .child(fUser.getUid()).child("following").child(profileId).removeValue();

                        FirebaseDatabase.getInstance().getReference().child("Follow").child(profileId).child("followers").child(fUser.getUid())
                                .removeValue();
                    }
                }
            }
        });



        myPics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recyclerView_post.setVisibility(View.VISIBLE);
                recyclerView_saved.setVisibility(View.GONE);
            }
        });

        savedPics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                savedPics.setImageResource(R.drawable.ic_save_black);
                recyclerView_post.setVisibility(View.GONE);
                recyclerView_saved.setVisibility(View.VISIBLE);

            }
        });

        followers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), FollowersActivity.class);
                intent.putExtra("id", profileId);
                intent.putExtra("title", "followers");
                startActivity(intent);
            }
        });

        following.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), FollowersActivity.class);
                intent.putExtra("id", profileId);
                intent.putExtra("title", "following");
                startActivity(intent);
            }
        });

        options.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(), OptionActivity.class));
            }
        });

        return view;
    }

    private void getSavedPosts() {

        final List<String> savedIds = new ArrayList<>();
        FirebaseDatabase.getInstance().getReference().child("Saves").child(fUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    savedIds.add(snapshot1.getKey());
                }
                FirebaseDatabase.getInstance().getReference().child("Posts").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        mySavedPosts.clear();
                        for (DataSnapshot snapshot2 : snapshot.getChildren()) {
                            Post post = snapshot2.getValue(Post.class);

                            for (String id : savedIds) {
                                if (post != null && post.getPostId().equals(id)) {
                                    mySavedPosts.add(post);
                                }
                            }
                        }
                        photoAdapterSaves.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void myPhotos() {
        FirebaseDatabase.getInstance().getReference().child("Posts").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                myPhotolist.clear();
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    Post post = snapshot1.getValue(Post.class);
                    if (post != null && post.getPublisher().equals(profileId)) {
                        myPhotolist.add(post);
                    }
                }
                Collections.reverse(myPhotolist);
                photoAdapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
                recyclerView_post.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkFollowingStatus() {
        FirebaseDatabase.getInstance().getReference().child("Follow").child(fUser.getUid()).child("following").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(profileId).exists()) {
                    editProfile.setText("Following");
                } else {
                    editProfile.setText("Follow");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getPostCount() {

        FirebaseDatabase.getInstance().getReference().child("Posts").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int counter = 0;
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    Post post = snapshot1.getValue(Post.class);
                    if (post != null && post.getPublisher().equals(profileId)) {
                        counter++;
                    }
                    posts.setText(String.valueOf(counter));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });

    }

    private void getFollowersAndFollowingCount() {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Follow").child(profileId);
        ref.child("followers").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                followers.setText("" + snapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
        DatabaseReference ref2 = FirebaseDatabase.getInstance().getReference().child("Follow").child(profileId);
        ref2.child("following").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                following.setText("" + snapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });

    }

    private void addNotification() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("userId", fUser.getUid());
        map.put("text", "Started following you");
        map.put("postid", "");
        map.put("Post", false);

        FirebaseDatabase.getInstance().getReference().child("Notification").child(profileId)
                .push().setValue(map);

    }

    private void userInfo() {

        FirebaseDatabase.getInstance().getReference().child("Users").child(profileId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null) {
                    Picasso.get().load(user.getImgurl()).into(imgProfile);
                    username.setText(user.getUsername());
                    fullname.setText(user.getName());
                    bio.setText(user.getBio());
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
    }

}
