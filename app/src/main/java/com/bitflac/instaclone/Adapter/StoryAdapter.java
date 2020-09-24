package com.bitflac.instaclone.Adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bitflac.instaclone.AddStoryActivity;
import com.bitflac.instaclone.Model.Story;
import com.bitflac.instaclone.Model.User;
import com.bitflac.instaclone.R;
import com.bitflac.instaclone.StoryActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class StoryAdapter extends RecyclerView.Adapter<StoryAdapter.Holder> {

    private Context mContext;
    private List<Story> mStory;
    private FirebaseUser firebaseUser=FirebaseAuth.getInstance().getCurrentUser();

    public StoryAdapter(Context mContext, List<Story> mStory) {
        this.mContext = mContext;
        this.mStory = mStory;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == 0) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.add_story_item, parent, false);
            return new StoryAdapter.Holder(view);
        } else {
            View view = LayoutInflater.from(mContext).inflate(R.layout.story_item, parent, false);
            return new StoryAdapter.Holder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final Holder holder, int position) {

        final Story story = mStory.get(position);
        userInfo(holder, story.getUserid(), position);
        if (holder.getAdapterPosition() != 0) {
            seenStory(holder, story.getUserid());
        }

        if (holder.getAdapterPosition() == 0) {
            myStory(holder.addstory_text, holder.storyplus, false);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (holder.getAdapterPosition() == 0) {
                    myStory(holder.addstory_text, holder.storyplus, true);
                } else {
                    Intent intent = new Intent(mContext, StoryActivity.class);
                    intent.putExtra("userid",story.getUserid());
                    mContext.startActivity(intent);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return mStory.size();
    }

    public class Holder extends RecyclerView.ViewHolder {

        public CircleImageView storyphoto, storyplus, story_photo_seen;
        public TextView story_username, addstory_text;

        public Holder(@NonNull View itemView) {
            super(itemView);
            storyphoto = itemView.findViewById(R.id.story_photo);
            storyplus = itemView.findViewById(R.id.add_story);
            story_photo_seen = itemView.findViewById(R.id.story_photo_seen);
            story_username = itemView.findViewById(R.id.story_username);
            addstory_text = itemView.findViewById(R.id.addstory_text);

        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return 0;
        }
        return 1;
    }

    private void userInfo(final Holder holder, final String userid, final int pos) {
        FirebaseDatabase.getInstance().getReference().child("Users").child(userid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null) {
                    Picasso.get().load(user.getImgurl()).placeholder(R.drawable.place_holder).into(holder.storyphoto);
                }
                if (pos != 0) {
                    if (user != null) {
                        Picasso.get().load(user.getImgurl()).into(holder.story_photo_seen);
                        holder.story_username.setText(user.getUsername());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(mContext, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void myStory(final TextView textView, final ImageView imageView, final boolean click) {
        FirebaseDatabase.getInstance().getReference().child("Story")
                .child(firebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int count = 0;
                long timecurrent = System.currentTimeMillis();
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    Story story = snapshot1.getValue(Story.class);
                    if (story != null && timecurrent > story.getTimestart() && timecurrent < story.getTimeend()) {
                        count++;

                    }
                }
                textView.setText("My Story");

                if (click) {
                    if (count > 0) {
                        AlertDialog alertDialog = new AlertDialog.Builder(mContext).create();
                        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "View Story", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent = new Intent(mContext, StoryActivity.class);
                                intent.putExtra("userid",firebaseUser.getUid());
                                mContext.startActivity(intent);
                                dialogInterface.dismiss();
                            }
                        });
                        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Add Story", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent = new Intent(mContext, AddStoryActivity.class);
                                mContext.startActivity(intent);
                                dialogInterface.dismiss();
                            }
                        });

                        alertDialog.show();
                    } else {
                        Intent intent = new Intent(mContext, AddStoryActivity.class);
                        mContext.startActivity(intent);
                    }
                } else {
                    if (count > 0) {

                        textView.setText("My Story");
                        imageView.setVisibility(View.GONE);
                    } else {
                        textView.setText("Add Story");
                        imageView.setVisibility(View.VISIBLE);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(mContext, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void seenStory(final Holder holder, String userid) {

        FirebaseDatabase.getInstance().getReference().child("Story").child(userid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int i = 0;
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    Story storytime=snapshot1.getValue(Story.class);
                    if (storytime != null && !snapshot1.child("views").child(firebaseUser.getUid())
                            .exists() && System.currentTimeMillis() < storytime.getTimeend()) {
                        i++;
                    }
                }

                if (i > 0) {
                    holder.storyphoto.setVisibility(View.VISIBLE);
                    holder.story_photo_seen.setVisibility(View.GONE);
                } else {
                    holder.storyphoto.setVisibility(View.GONE);
                    holder.story_photo_seen.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(mContext, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}
