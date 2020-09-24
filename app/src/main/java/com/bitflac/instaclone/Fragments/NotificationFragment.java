package com.bitflac.instaclone.Fragments;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bitflac.instaclone.Adapter.NotificationAdapter;
import com.bitflac.instaclone.Model.Notification;
import com.bitflac.instaclone.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NotificationFragment extends Fragment {

    private RecyclerView recyclerView;
    private NotificationAdapter notificationAdapter;
    private List<Notification> notificationList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_notification, container, false);

        recyclerView = view.findViewById(R.id.recycler_view_noti);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        notificationList = new ArrayList<>();
        notificationAdapter = new NotificationAdapter(getContext(), notificationList);
        recyclerView.setAdapter(notificationAdapter);

        readNotification();

        return view;

    }


    private void readNotification() {
        FirebaseUser firebaseUser=FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            FirebaseDatabase.getInstance().getReference().child("Notification").child(firebaseUser.getUid())
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            notificationList.clear();
                            Log.d("NOTIFICATION<>><>",snapshot.toString());
                            for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                                notificationList.add(snapshot1.getValue(Notification.class));
                            }
                            Collections.reverse(notificationList);
                            notificationAdapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

}



