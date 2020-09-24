package com.bitflac.instaclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bitflac.instaclone.Adapter.CommentAdapter;
import com.bitflac.instaclone.Model.Comment;
import com.bitflac.instaclone.Model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CommentAdapter commentAdapter;
    private List<Comment> commentList;

    private EditText addComment;
    private CircleImageView imageProfile;
    private TextView postComment;

    private String postId;
    private String authorId;

    FirebaseUser fUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        Toolbar toolbar = findViewById(R.id.toolbar2);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Comments");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.hasFixedSize();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        commentList = new ArrayList<>();

        Intent intent = getIntent();
        postId = intent.getStringExtra("postId");
        authorId = intent.getStringExtra("authorId");
        commentAdapter = new CommentAdapter(this, commentList, postId);
        recyclerView.setAdapter(commentAdapter);

        addComment = findViewById(R.id.add_comment);
        imageProfile = findViewById(R.id.img_profile);
        postComment = findViewById(R.id.post1);

        fUser = FirebaseAuth.getInstance().getCurrentUser();
        getUserImage();

        getComment();

        postComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(addComment.getText().toString())) {
                    Toast.makeText(CommentActivity.this, "no comment added", Toast.LENGTH_SHORT).show();
                } else {
                    putComment();
                    addNotification();
                }
            }
        });
    }

    private void addNotification() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("userId", fUser.getUid());
        map.put("text", "commented on your post: " + "'" + addComment.getText().toString() + "'");
        map.put("postid", postId);
        map.put("Post", true);

        FirebaseDatabase.getInstance().getReference().child("Notification").child(authorId)
                .push().setValue(map);

    }

    private void putComment() {
        HashMap<String, Object> map = new HashMap<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Comments").child(postId);
        String id = ref.push().getKey();
        map.put("id", id);
        map.put("comment", addComment.getText().toString());
        map.put("publisher", fUser.getUid());

        if (id != null) {
            ref.child(id).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(CommentActivity.this, "comment added", Toast.LENGTH_SHORT).show();
                        addComment.getText().clear();
                    } else {
                        if (task.getException() != null) {
                            Toast.makeText(CommentActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
        } else {
            Toast.makeText(this, "id cant be null", Toast.LENGTH_SHORT).show();
        }

    }

    private void getComment() {

        FirebaseDatabase.getInstance().getReference().child("Comments").child(postId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                commentList.clear();

                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    Comment comment = snapshot1.getValue(Comment.class);
                    commentList.add(comment);
                }

                commentAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void getUserImage() {
        FirebaseDatabase.getInstance().getReference().child("Users").child(fUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null) {
                    if (user.getImgurl().equals("default")) {
                        imageProfile.setImageResource(R.mipmap.ic_launcher);
                    } else {
                        Picasso.get().load(user.getImgurl()).placeholder(R.drawable.place_holder).into(imageProfile);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}
