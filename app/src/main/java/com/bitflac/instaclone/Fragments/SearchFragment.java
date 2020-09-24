package com.bitflac.instaclone.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bitflac.instaclone.Adapter.TagAdapter;
import com.bitflac.instaclone.Adapter.UserAdapter;
import com.bitflac.instaclone.Model.User;
import com.bitflac.instaclone.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.hendraanggrian.appcompat.widget.SocialAutoCompleteTextView;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {

    private RecyclerView recyclerView, recyclerViewTags;
    private SocialAutoCompleteTextView search_bar;
    private List<User> mUsers;
    private UserAdapter userAdapter;

    private List<String> mHashTags;
    private List<String> mHashTagsCount;
    private TagAdapter tagAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_search, container, false);
        recyclerView = view.findViewById(R.id.recycler_view_users);
        recyclerView.hasFixedSize();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        recyclerViewTags = view.findViewById(R.id.recycler_view_tags);
        recyclerViewTags.hasFixedSize();
        recyclerViewTags.setLayoutManager(new LinearLayoutManager(getContext()));

        mHashTags = new ArrayList<>();
        mHashTagsCount = new ArrayList<>();
        tagAdapter = new TagAdapter(getContext(), mHashTags, mHashTagsCount);
        recyclerViewTags.setAdapter(tagAdapter);

        mUsers = new ArrayList<>();
        userAdapter = new UserAdapter(getContext(), mUsers, true);
        recyclerView.setAdapter(userAdapter);

        search_bar = view.findViewById(R.id.search_bar);

        readUsers();
        readTags();

        search_bar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                searchUser(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {
                filter(editable.toString());
            }
        });

        return view;
    }

    private void readTags() {

        FirebaseDatabase.getInstance().getReference().child("HashTags").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mHashTags.clear();
                mHashTagsCount.clear();

                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    mHashTags.add(snapshot1.getKey());
                    mHashTagsCount.add(snapshot1.getChildrenCount() + "");
                }

                tagAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void readUsers() {

        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (TextUtils.isEmpty(search_bar.getText().toString())) {
                    mUsers.clear();
                    for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                        User user = snapshot1.getValue(User.class);
                        mUsers.add(user);
                    }
                    userAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void searchUser(final String s) {
        Query query = FirebaseDatabase.getInstance().getReference().child("Users")
                .orderByChild("username").startAt(s).endAt(s + "\uf8ff");

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mUsers.clear();
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    User user = snapshot1.getValue(User.class);
                    mUsers.add(user);
                }
                userAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void filter(String text) {
        List<String> mSearchTags = new ArrayList<>();
        List<String> mSearchTagsCount = new ArrayList<>();

        for (String s : mHashTags) {
            if (s.toLowerCase().contains(text.toLowerCase())) {
                mSearchTags.add(s);
                mSearchTagsCount.add(mHashTagsCount.get(mHashTags.indexOf(s)));
            }
        }
        tagAdapter.filter(mSearchTags, mSearchTagsCount);
    }
}


