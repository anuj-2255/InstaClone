package com.bitflac.instaclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.MenuItem;
import com.bitflac.instaclone.Fragments.HomeFragment;
import com.bitflac.instaclone.Fragments.NotificationFragment;
import com.bitflac.instaclone.Fragments.ProfileFragment;
import com.bitflac.instaclone.Fragments.SearchFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class HomeActivity extends AppCompatActivity {
    private BottomNavigationView bottomNavigationView;
    private Fragment selectorfragment;
    private FirebaseAuth mauth;
    private ConnectivityManager connectivityManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        if (!isConnected()){
            final AlertDialog alertDialog=new AlertDialog.Builder(this).create();
            alertDialog.setMessage("Check your Internet");
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL,"OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    alertDialog.dismiss();
                    finish();
                }
            });
            alertDialog.show();
        }
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_home:
                        selectorfragment = new HomeFragment();
                        break;

                    case R.id.nav_search:
                        selectorfragment = new SearchFragment();
                        break;

                    case R.id.nav_add:
                        selectorfragment = null;
                        startActivity(new Intent(HomeActivity.this, PostActivity.class));

                    case R.id.nav_fav:
                        selectorfragment = new NotificationFragment();
                        break;

                    case R.id.nav_profile:
                        selectorfragment = new ProfileFragment();
                        break;
                }
                if (selectorfragment != null) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectorfragment).commit();
                }
                return true;
            }
        });

        Bundle intent = getIntent().getExtras();
        if (intent != null) {
            String profileId = intent.getString("publisherId");

            getSharedPreferences("PROFILE", MODE_PRIVATE).edit().putString("profileId", profileId).apply();

            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ProfileFragment()).commit();

        } else {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
        }

    }
    private boolean isConnected(){
        connectivityManager= (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            return connectivityManager.getActiveNetworkInfo()!=null&&connectivityManager.getActiveNetworkInfo().isConnected();
        }
        return false;
    }
}
