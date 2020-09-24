package com.bitflac.instaclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    ImageView imgicon;
    EditText edemail, edpassword;
    Button btnlogin;
    TextView signup;
    LinearLayout linearLayout;

    private FirebaseAuth mauth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imgicon = findViewById(R.id.imgicon);
        linearLayout = findViewById(R.id.lineralayout);
        signup = findViewById(R.id.tvsignup);
        edemail = findViewById(R.id.edemail);
        edpassword = findViewById(R.id.edpassword);
        btnlogin = findViewById(R.id.btnlogin);
        mauth = FirebaseAuth.getInstance();

        linearLayout.animate().alpha(0f).setDuration(10);

        TranslateAnimation animation = new TranslateAnimation(0, 0, 0, -1000);
        animation.setDuration(1000);
        animation.setFillAfter(false);
        animation.setAnimationListener(new MyAnimation());

        imgicon.setAnimation(animation);


        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, Signup.class));
            }
        });

        btnlogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String txtemail = edemail.getText().toString();
                String txtpassword = edpassword.getText().toString();

                if (TextUtils.isEmpty(txtemail) || TextUtils.isEmpty(txtpassword)) {
                    Toast.makeText(MainActivity.this, "empty credentials", Toast.LENGTH_SHORT).show();
                } else {
                    loginUser(txtemail, txtpassword);
                }
            }
        });

    }

    private void loginUser(String email, String password) {
        mauth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(MainActivity.this, "loged in", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(MainActivity.this, HomeActivity.class));
                    finish();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private class MyAnimation implements Animation.AnimationListener {

        @Override
        public void onAnimationStart(Animation animation) {

        }

        @Override
        public void onAnimationEnd(Animation animation) {
            imgicon.clearAnimation();
            imgicon.setVisibility(View.INVISIBLE);
            linearLayout.animate().alpha(1f).setDuration(1000);

        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user=mauth.getCurrentUser();
        if (user!=null){
            startActivity(new Intent(MainActivity.this,HomeActivity.class));
            finish();
        }
    }
}
