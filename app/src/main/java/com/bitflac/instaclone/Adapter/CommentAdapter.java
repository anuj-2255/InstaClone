package com.bitflac.instaclone.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bitflac.instaclone.HomeActivity;
import com.bitflac.instaclone.Model.Comment;
import com.bitflac.instaclone.Model.User;
import com.bitflac.instaclone.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.Holer> {

    private Context mcontext;
    private List<Comment> mComments;

    String postId;

    FirebaseUser fUser;

    public CommentAdapter(Context mcontext, List<Comment> mcomment, String postId) {
        this.mcontext = mcontext;
        this.mComments = mcomment;
        this.postId = postId;
    }

    @NonNull
    @Override
    public Holer onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mcontext).inflate(R.layout.comment_item, parent, false);
        return new CommentAdapter.Holer(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final Holer holder, int position) {

        fUser = FirebaseAuth.getInstance().getCurrentUser();

        final Comment comment = mComments.get(position);

        holder.comment.setText(comment.getComment());

        FirebaseDatabase.getInstance().getReference().child("Users").child(comment.getPublisher()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null) {
                    holder.username.setText(user.getUsername());
                }
                if (user != null) {
                    if (user.getImgurl().equals("default")) {
                        holder.imageProfile.setImageResource(R.mipmap.ic_launcher);
                    } else {
                        Picasso.get().load(user.getImgurl()).into(holder.imageProfile);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        holder.comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mcontext, HomeActivity.class);
                intent.putExtra("publisherId", comment.getPublisher());
                mcontext.startActivity(intent);
            }
        });

        holder.imageProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mcontext, HomeActivity.class);
                intent.putExtra("publisherId", comment.getPublisher());
                mcontext.startActivity(intent);
            }
        });


        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (comment.getPublisher().endsWith(fUser.getUid())) {
                    final AlertDialog alertDialog = new AlertDialog.Builder(mcontext).create();
                    alertDialog.setTitle("Do You Want to Deelete");
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "NO", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "YES", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(final DialogInterface dialogInterface, int i) {
                            FirebaseDatabase.getInstance().getReference().child("Comments")
                                    .child(postId).child(comment.getId()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        Toast.makeText(mcontext, "Comment Deleted", Toast.LENGTH_SHORT).show();
                                        dialogInterface.dismiss();
                                    }
                                }
                            });
                        }
                    });
                    alertDialog.show();
                }
                return true;
            }
        });

    }

    @Override
    public int getItemCount() {
        return mComments.size();
    }

    public class Holer extends RecyclerView.ViewHolder {

        public CircleImageView imageProfile;
        public TextView username, comment;

        public Holer(@NonNull View itemView) {
            super(itemView);
            imageProfile = itemView.findViewById(R.id.image_profile1);
            username = itemView.findViewById(R.id.username2);
            comment = itemView.findViewById(R.id.comment2);
        }
    }
}
