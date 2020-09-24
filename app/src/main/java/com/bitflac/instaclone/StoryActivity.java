package com.bitflac.instaclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bitflac.instaclone.Model.Story;
import com.bitflac.instaclone.Model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import jp.shts.android.storiesprogressview.StoriesProgressView;

public class StoryActivity extends AppCompatActivity implements StoriesProgressView.StoriesListener {

    int counter = 0;
    long pressTime = 0L;
    long limit = 500L;

    StoriesProgressView storiesProgressView;
    ImageView image, story_photo;
    TextView story_username;

    LinearLayout r_seen;
    TextView seen_number;
    ImageView story_delete;

    List<String> images;
    List<String> storyids;
    String userid;

    FirebaseUser firebaseUser;

    private View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    pressTime = System.currentTimeMillis();
                    storiesProgressView.pause();
                    return false;
                case MotionEvent.ACTION_UP:
                    long now = System.currentTimeMillis();
                    storiesProgressView.resume();
                    return limit < now - pressTime;
            }
            return false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        r_seen = findViewById(R.id.r_seen);
        seen_number = findViewById(R.id.seen_number);
        story_delete = findViewById(R.id.story_delete);

        storiesProgressView = findViewById(R.id.stories);
        image = findViewById(R.id.image_story);
        story_photo = findViewById(R.id.story_photo2);
        story_username = findViewById(R.id.text_username);

        r_seen.setVisibility(View.GONE);
        story_delete.setVisibility(View.GONE);

        userid = getIntent().getStringExtra("userid");

        if (userid != null && userid.equals(firebaseUser.getUid())) {
            r_seen.setVisibility(View.VISIBLE);
            story_delete.setVisibility(View.VISIBLE);
        }

        getStories(userid);
        userInfo(userid);

        View reverse = findViewById(R.id.reverse);
        reverse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                storiesProgressView.reverse();
            }
        });
        reverse.setOnTouchListener(onTouchListener);

        View skip = findViewById(R.id.skip);
        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                storiesProgressView.skip();
            }
        });
        skip.setOnTouchListener(onTouchListener);

        r_seen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(StoryActivity.this, FollowersActivity.class);
                intent.putExtra("id", userid);
                intent.putExtra("storyid", storyids.get(counter));
                intent.putExtra("title", "views");
                startActivity(intent);
            }
        });

        story_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Story")
                        .child(userid).child(storyids.get(counter));
                reference.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(StoryActivity.this, "Deleted!!!", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(StoryActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

    }

    @Override
    public void onNext() {
        if ((counter + 1) > images.size()) {
            finish();
        }
        Picasso.get().load(images.get(++counter)).into(image);
        addViews(storyids.get(counter));
        seenNumber(storyids.get(counter));
    }

    @Override
    public void onPrev() {
        if ((counter - 1) < 0) {
            Picasso.get().load(images.get(counter)).into(image);
            return;
        }
        Picasso.get().load(images.get(--counter)).into(image);

    }

    @Override
    public void onComplete() {
        finish();
    }

    @Override
    protected void onDestroy() {
        storiesProgressView.destroy();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        storiesProgressView.pause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        storiesProgressView.resume();
        super.onResume();
    }

    private void getStories(final String userid) {
        images = new ArrayList<>();
        storyids = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Story")
                .child(userid);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                images.clear();
                storyids.clear();
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    Story story = snapshot1.getValue(Story.class);
                    long timecurrent = System.currentTimeMillis();
                    if (story != null && timecurrent > story.getTimestart() && timecurrent < story.getTimeend()) {
                        images.add(story.getImageurl());
                        storyids.add(story.getStoryid());
                    }
                }

                storiesProgressView.setStoriesCount(images.size());
                storiesProgressView.setStoryDuration(5000L);
                storiesProgressView.setStoriesListener(StoryActivity.this);
                Picasso.get().load(images.get(counter)).into(image, new Callback() {
                    @Override
                    public void onSuccess() {
                        storiesProgressView.startStories(counter);
                    }

                    @Override
                    public void onError(Exception e) {
                        Toast.makeText(StoryActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

                addViews(storyids.get(counter));
                seenNumber(storyids.get(counter));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(StoryActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void userInfo(String userid) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users")
                .child(userid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null) {
                    Picasso.get().load(user.getImgurl()).into(story_photo);
                    story_username.setText(user.getUsername());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(StoryActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addViews(String storyid) {
        if (userid.equals(firebaseUser.getUid())) {
            return;
        }
        FirebaseDatabase.getInstance().getReference().child("Story").child(userid)
                .child(storyid).child("views").child(firebaseUser.getUid()).setValue(true);
    }

    private void seenNumber(String storyid) {
        FirebaseDatabase.getInstance().getReference().child("Story").child(userid)
                .child(storyid).child("views").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String seen_count = "" + snapshot.getChildrenCount();
                seen_number.setText(seen_count);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(StoryActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
