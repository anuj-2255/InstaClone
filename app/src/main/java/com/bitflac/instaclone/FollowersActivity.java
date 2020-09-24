package com.bitflac.instaclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.bitflac.instaclone.Adapter.UserAdapter;
import com.bitflac.instaclone.Model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FollowersActivity extends AppCompatActivity {

    private String id;
    private String title;
    private List<String> idList;

    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private List<User> mUsers;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_followers);

        Intent intent = getIntent();
        id = intent.getStringExtra("id");
        title = intent.getStringExtra("title");

        Toolbar toolbar = findViewById(R.id.toolbarfollow);
        setSupportActionBar(toolbar);
        if (getSupportActionBar()!=null){
            getSupportActionBar().setTitle(title);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        recyclerView = findViewById(R.id.recycler_view_follow);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        idList = new ArrayList<>();
        mUsers = new ArrayList<>();
        userAdapter = new UserAdapter(this, mUsers, false);
        recyclerView.setAdapter(userAdapter);

        switch (title) {
            case "followers":
                getFollowers();
                break;

            case "following":
                getFollowings();
                break;

            case "likes":
                getLikes();
                break;

            case "views":
                getViews();
                break;

        }
    }

    private void getViews(){
        String intent=getIntent().getStringExtra("storyid");
        DatabaseReference reference;
        if (intent != null) {
            reference = FirebaseDatabase.getInstance().getReference().child("Story")
                    .child(id).child(intent).child("views");

            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    idList.clear();
                    for (DataSnapshot snapshot1:snapshot.getChildren()){
                        idList.add(snapshot1.getKey());
                        Log.d("LISTTTSTSTTS", Objects.requireNonNull(snapshot.getKey()));
                    }
                    showUsers();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    private void getFollowers() {
        FirebaseDatabase.getInstance().getReference().child("Follow").child(id).child("followers").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                idList.clear();
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    idList.add(snapshot1.getKey());
                }
                showUsers();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(FollowersActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getFollowings() {
        FirebaseDatabase.getInstance().getReference().child("Follow").child(id).child("following").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                idList.clear();
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    idList.add(snapshot1.getKey());
                    Log.d("IDLIST>>>>><<>>>>>>",id);
                }
                showUsers();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(FollowersActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getLikes() {
        FirebaseDatabase.getInstance().getReference().child("Likes").child(id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                idList.clear();
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    idList.add(snapshot1.getKey());
                }
                showUsers();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(FollowersActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showUsers() {
        FirebaseDatabase.getInstance().getReference().child("Users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mUsers.clear();
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    User user = snapshot1.getValue(User.class);
                    for (String id : idList) {
                        if (user != null && user.getId().equals(id)) {
                            mUsers.add(user);
                            Log.d("DATAAAA>>>>>>", mUsers.toString());
                        }
                    }
                }
                userAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(FollowersActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
