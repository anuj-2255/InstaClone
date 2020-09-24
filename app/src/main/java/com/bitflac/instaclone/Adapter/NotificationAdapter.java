package com.bitflac.instaclone.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bitflac.instaclone.Fragments.PostDetailsFragment;
import com.bitflac.instaclone.Fragments.ProfileFragment;
import com.bitflac.instaclone.Model.Notification;
import com.bitflac.instaclone.Model.Post;
import com.bitflac.instaclone.Model.User;
import com.bitflac.instaclone.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.Holder> {

    private Context context;
    private List<Notification> mNotification;

    public NotificationAdapter(Context context, List<Notification> mNotification) {
        this.context = context;
        this.mNotification = mNotification;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.notification_item, parent, false);
        return new NotificationAdapter.Holder(view);
    }

    @SuppressLint("LongLogTag")
    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        final Notification notification = mNotification.get(position);
        getUser(holder.imagePrfl, holder.usrname, notification.getUserId());
        holder.comment.setText(notification.getText());
        Log.d("VASDGFNGHHJJJJJJJJJJJJJJJJJJJJ", String.valueOf(notification.isPost()));
        if (notification.isPost()) {
            holder.postImg.setImageResource(R.drawable.place_holder);
            holder.postImg.setVisibility(View.VISIBLE);
            getPostImage(holder.postImg, notification.getPostid());
        } else {
            holder.postImg.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (notification.isPost()) {
                    boolean post_notificaton = true;
                    context.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit()
                            .putString("postid", notification.getPostid()).apply();

                    context.getSharedPreferences("POSTNOTIFICATION",Context.MODE_PRIVATE).edit()
                            .putBoolean("post_notification", post_notificaton).apply();

                    ((FragmentActivity) context).getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, new PostDetailsFragment()).commit();

                } else {
                    context.getSharedPreferences("PROFILE", Context.MODE_PRIVATE).edit()
                            .putString("profileId", notification.getUserId()).apply();

                    ((FragmentActivity) context).getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, new ProfileFragment()).commit();
                }
            }
        });

    }


    @Override
    public int getItemCount() {
        return mNotification.size();
    }

    public class Holder extends RecyclerView.ViewHolder {

        public ImageView postImg;
        public CircleImageView imagePrfl;
        public TextView usrname;
        public TextView comment;

        public Holder(@NonNull View itemView) {
            super(itemView);

            imagePrfl = itemView.findViewById(R.id.imgnoti);
            postImg = itemView.findViewById(R.id.postimagenoti);
            usrname = itemView.findViewById(R.id.usernamenoti);
            comment = itemView.findViewById(R.id.commnetnoti);

        }
    }

    private void getPostImage(final ImageView imageView, final String postId) {
        FirebaseDatabase.getInstance().getReference().child("Posts").child(postId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Post post = snapshot.getValue(Post.class);
                Log.d("POST_IMAGE>>>>>>>>", Objects.requireNonNull(post).getImageurl());
                Picasso.get().load(post.getImageurl()).into(imageView);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getUser(final CircleImageView circleImageView, final TextView textView, final String userId) {
        FirebaseDatabase.getInstance().getReference().child("Users").child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null) {
                    if (user.getImgurl().equals("default")) {
                        circleImageView.setImageResource(R.mipmap.ic_launcher);
                        textView.setText(user.getUsername());
                    } else {
                        Picasso.get().load(user.getImgurl()).into(circleImageView);
                        textView.setText(user.getUsername());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
