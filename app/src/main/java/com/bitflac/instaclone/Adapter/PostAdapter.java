package com.bitflac.instaclone.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bitflac.instaclone.CommentActivity;
import com.bitflac.instaclone.FollowersActivity;
import com.bitflac.instaclone.Fragments.ProfileFragment;
import com.bitflac.instaclone.Model.Post;
import com.bitflac.instaclone.Model.User;
import com.bitflac.instaclone.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hendraanggrian.appcompat.widget.SocialTextView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.Holder> {

    private Context mcontext;
    private List<Post> mPosts;

    private FirebaseUser firebaseUser;

    public PostAdapter(Context context, List<Post> mPosts) {
        this.mcontext = context;
        this.mPosts = mPosts;
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mcontext).inflate(R.layout.post_item, parent, false);
        return new PostAdapter.Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final Holder holder, int position) {

        final Post post = mPosts.get(position);
        Picasso.get().load(post.getImageurl()).into(holder.postImage, new Callback() {
            @Override
            public void onSuccess() {
                holder.pg_bar.setVisibility(View.GONE);
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(mcontext, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        holder.description.setText(post.getDescription());

        FirebaseDatabase.getInstance().getReference().child("Users").child(post.getPublisher()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);

                assert user != null;
                if (user.getImgurl().equals("default")) {
                    holder.imageProfile.setImageResource(R.mipmap.ic_launcher);
                } else {
                    Picasso.get().load(user.getImgurl()).into(holder.imageProfile);
                }
                holder.username.setText(user.getUsername());
                holder.author.setText(user.getName());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(mcontext, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        isLiked(post.getPostId(), holder.like);
        noOfLikes(post.getPostId(), holder.noOfLikes);
        getComments(post.getPostId(), holder.noOfComments);
        isSaved(post.getPostId(), holder.save);

        holder.like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (holder.like.getTag().equals("like")) {
                    FirebaseDatabase.getInstance().getReference().child("Likes").child(post.getPostId()).child(firebaseUser.getUid())
                            .setValue(true);
                    addNotification(post.getPostId(), post.getPublisher());
                } else {
                    FirebaseDatabase.getInstance().getReference().child("Likes").child(post.getPostId()).child(firebaseUser.getUid())
                            .removeValue();
                    //FirebaseDatabase.getInstance().getReference().child("Notification").child(post.getPostId())
                    //      .child(post.getPostId()).removeValue();
                }
            }
        });
        holder.comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mcontext, CommentActivity.class);
                intent.putExtra("postId", post.getPostId());
                intent.putExtra("authorId", post.getPublisher());
                mcontext.startActivity(intent);
            }
        });

        holder.noOfComments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mcontext, CommentActivity.class);
                intent.putExtra("postId", post.getPostId());
                intent.putExtra("authorId", post.getPublisher());
                mcontext.startActivity(intent);
            }
        });

        holder.save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (holder.save.getTag().equals("save")) {
                    FirebaseDatabase.getInstance().getReference().child("Saves").child(firebaseUser.getUid()).child(post.getPostId())
                            .setValue(true);
                } else {
                    FirebaseDatabase.getInstance().getReference().child("Saves").child(firebaseUser.getUid()).child(post.getPostId())
                            .removeValue();
                }
            }
        });

        holder.imageProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mcontext.getSharedPreferences("PROFILE", Context.MODE_PRIVATE).edit()
                        .putString("profileId", post.getPublisher()).apply();
                ((FragmentActivity) mcontext).getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new ProfileFragment()).commit();
            }
        });

        holder.username.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mcontext.getSharedPreferences("PROFILE", Context.MODE_PRIVATE).edit()
                        .putString("profileId", post.getPublisher()).apply();
                ((FragmentActivity) mcontext).getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new ProfileFragment()).commit();
            }
        });

        holder.author.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mcontext.getSharedPreferences("PROFILE", Context.MODE_PRIVATE).edit()
                        .putString("profileId", post.getPublisher()).apply();
                ((FragmentActivity) mcontext).getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new ProfileFragment()).commit();
            }
        });

        holder.noOfLikes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mcontext, FollowersActivity.class);
                intent.putExtra("id", post.getPostId());
                intent.putExtra("title", "likes");
                mcontext.startActivity(intent);
            }
        });
        holder.more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final PopupMenu popupMenu = new PopupMenu(mcontext, view);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case R.id.edit:
                                editPost(post.getPostId());
                                return true;

                            case R.id.delete:
                                FirebaseDatabase.getInstance().getReference().child("Posts")
                                        .child(post.getPostId()).removeValue()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(mcontext, "Deleted!!", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                return true;

                            case R.id.report:
                                Toast.makeText(mcontext, "Reported!!", Toast.LENGTH_SHORT).show();
                                return true;

                            default:
                                return false;
                        }
                    }
                });
                popupMenu.inflate(R.menu.post_menu);
                popupMenu.getMenu().findItem(R.id.report).setVisible(false);
                if (!post.getPublisher().equals(firebaseUser.getUid())) {
                    popupMenu.getMenu().findItem(R.id.delete).setVisible(false);
                    popupMenu.getMenu().findItem(R.id.edit).setVisible(false);
                    popupMenu.getMenu().findItem(R.id.report).setVisible(true);
                }
                popupMenu.show();
            }
        });

    }


    private void isSaved(final String postId, final ImageView image) {
        FirebaseUser auth=FirebaseAuth.getInstance().getCurrentUser();
        if (auth != null) {
            FirebaseDatabase.getInstance().getReference().child("Saves").child(auth.getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.child(postId).exists()) {
                        image.setImageResource(R.drawable.ic_save_black);
                        image.setTag("saved");
                    } else {
                        image.setImageResource(R.drawable.ic_save);
                        image.setTag("save");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(mcontext, error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mPosts.size();
    }

    public class Holder extends RecyclerView.ViewHolder {

        public ImageView imageProfile, postImage, like, comment, save, more;
        public TextView username, noOfLikes, author, noOfComments, time;
        SocialTextView description;
        ProgressBar pg_bar;

        public Holder(@NonNull View itemView) {
            super(itemView);

            imageProfile = itemView.findViewById(R.id.profile_image);
            postImage = itemView.findViewById(R.id.post_image);
            like = itemView.findViewById(R.id.like);
            comment = itemView.findViewById(R.id.comment);
            time = itemView.findViewById(R.id.time);
            save = itemView.findViewById(R.id.save);
            more = itemView.findViewById(R.id.more);
            pg_bar=itemView.findViewById(R.id.pg_bar);

            username = itemView.findViewById(R.id.username1);
            noOfLikes = itemView.findViewById(R.id.no_of_likes);
            author = itemView.findViewById(R.id.author);
            noOfComments = itemView.findViewById(R.id.no_of_comments);
            description = itemView.findViewById(R.id.description);
        }
    }

    private void isLiked(String postId, final ImageView imageView) {
        FirebaseDatabase.getInstance().getReference().child("Likes").child(postId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(firebaseUser.getUid()).exists()) {
                    imageView.setImageResource(R.drawable.ic_liked);
                    imageView.setTag("liked");
                } else {
                    imageView.setImageResource(R.drawable.ic_like);
                    imageView.setTag("like");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(mcontext, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void noOfLikes(String postId, final TextView text) {
        FirebaseDatabase.getInstance().getReference().child("Likes").child(postId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String get_count=(snapshot.getChildrenCount())+" likes";
                text.setText( get_count);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(mcontext, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getComments(String postid, final TextView text) {
        FirebaseDatabase.getInstance().getReference().child("Comments").child(postid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String s = "View All " + snapshot.getChildrenCount() + " comments";
                text.setText(s);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(mcontext, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addNotification(String postId, String publisherId) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("userId", firebaseUser.getUid());
        map.put("text", "Liked Your Post.");
        map.put("postid", postId);
        map.put("Post", true);

        FirebaseDatabase.getInstance().getReference().child("Notification").child(publisherId)
                .push().setValue(map);

    }

    private void editPost(final String postid) {
        final AlertDialog.Builder alert = new AlertDialog.Builder(mcontext);
        alert.setTitle("Edit Post");

        final EditText editText = new EditText(mcontext);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        editText.setLayoutParams(lp);
        alert.setView(editText);
        getText(postid, editText);
        alert.setPositiveButton("Edit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                HashMap<String, Object> map = new HashMap<>();
                map.put("description", editText.getText().toString());
                FirebaseDatabase.getInstance().getReference().child("Posts")
                        .child(postid).updateChildren(map);

            }
        });
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        alert.show();
    }

    private void getText(String postid, final EditText editText) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Posts")
                .child(postid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Post post=snapshot.getValue(Post.class);
                if (post!=null){
                    editText.setText(post.getDescription());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(mcontext, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}
