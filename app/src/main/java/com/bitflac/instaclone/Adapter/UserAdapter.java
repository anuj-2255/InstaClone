package com.bitflac.instaclone.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bitflac.instaclone.Fragments.ProfileFragment;
import com.bitflac.instaclone.HomeActivity;
import com.bitflac.instaclone.Model.User;
import com.bitflac.instaclone.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.Holder> {

    private Context mcontext;
    private List<User> mUser;
    private boolean isFragment;

    private FirebaseUser firebaseUser;

    public UserAdapter(Context mcontext, List<User> mUser, boolean isFragment) {
        this.mcontext = mcontext;
        this.mUser = mUser;
        this.isFragment = isFragment;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mcontext).inflate(R.layout.user_item, parent, false);
        return new UserAdapter.Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final Holder holder, int position) {

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        final User user = mUser.get(position);
        holder.btnFollow.setVisibility(View.VISIBLE);

        holder.username.setText(user.getUsername());
        holder.name.setText(user.getName());

        Picasso.get().load(user.getImgurl()).placeholder(R.drawable.place_holder).into(holder.imageprofile);

        isfollowed(user.getId(), holder.btnFollow);

        if (user.getId().equals(firebaseUser.getUid())) {
            holder.btnFollow.setVisibility(View.GONE);
        }

        holder.btnFollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (holder.btnFollow.getText().toString().equals("Follow")) {
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid()).child("following")
                            .child(user.getId()).setValue(true);

                    addNotification(user.getId());

                    FirebaseDatabase.getInstance().getReference().child("Follow").child(user.getId()).child("followers")
                            .child(firebaseUser.getUid()).setValue(true);
                } else {
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid()).child("following")
                            .child(user.getId()).removeValue();

                    FirebaseDatabase.getInstance().getReference().child("Follow").child(user.getId()).child("followers")
                            .child(firebaseUser.getUid()).removeValue();
                }
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isFragment) {
                    mcontext.getSharedPreferences("PROFILE", Context.MODE_PRIVATE).edit().putString("profileId", user.getId()).apply();
                    ((FragmentActivity) mcontext).getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, new ProfileFragment()).commit();
                } else {
                    Intent intent=new Intent(mcontext, HomeActivity.class);
                    intent.putExtra("publisherId",user.getId());
                    mcontext.startActivity(intent);
                }
            }
        });

    }

    private void isfollowed(final String id, final Button btnFollow) {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid())
                .child("following");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(id).exists()) {
                    btnFollow.setText("Following");
                } else {
                    btnFollow.setText("Follow");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(mcontext, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public int getItemCount() {
        return mUser.size();
    }

    public class Holder extends RecyclerView.ViewHolder {

        public CircleImageView imageprofile;
        public TextView username, name;
        public Button btnFollow;

        public Holder(@NonNull View itemView) {
            super(itemView);

            imageprofile = itemView.findViewById(R.id.image_profile);
            username = itemView.findViewById(R.id.username1);
            name = itemView.findViewById(R.id.fullname);
            btnFollow = itemView.findViewById(R.id.btn_follow);

        }
    }

    private void addNotification(String userId) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("userId", firebaseUser.getUid());
        map.put("text", "started following you.");
        map.put("postid", "");
        map.put("isPost", false);

        FirebaseDatabase.getInstance().getReference().child("Notification").child(userId)
                .push().setValue(map);

    }
}
