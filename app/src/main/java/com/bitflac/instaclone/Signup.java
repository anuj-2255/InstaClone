package com.bitflac.instaclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;


public class Signup extends AppCompatActivity {

    EditText edname, edpassword2, edemail, edusername2;
    Button btnsignin;
    TextView loginuser;

    private FirebaseAuth mauth;
    private DatabaseReference mrootref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        edname = findViewById(R.id.edname);
        edusername2 = findViewById(R.id.edusername2);
        edemail = findViewById(R.id.edemail);
        edpassword2 = findViewById(R.id.edpassword2);
        loginuser = findViewById(R.id.tvlogin);
        btnsignin = findViewById(R.id.btnsignup);

        mrootref = FirebaseDatabase.getInstance().getReference();
        mauth = FirebaseAuth.getInstance();

        btnsignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String txtUsername = edusername2.getText().toString();
                String txtName = edname.getText().toString();
                String txtEmail = edemail.getText().toString();
                String txtPassword = edpassword2.getText().toString();

                if (TextUtils.isEmpty(txtUsername) || TextUtils.isEmpty(txtName) || TextUtils.isEmpty(txtEmail) || TextUtils.isEmpty(txtPassword)) {
                    Toast.makeText(Signup.this, "Empty Credentials", Toast.LENGTH_SHORT).show();
                } else if (txtPassword.length() < 6) {
                    Toast.makeText(Signup.this, "password is too short", Toast.LENGTH_SHORT).show();
                } else {
                    RegisterUser(txtUsername, txtName, txtEmail, txtPassword);
                }

            }
        });
        loginuser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Signup.this, MainActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
                finish();
            }
        });
    }

    private void RegisterUser(final String username, final String name, final String email, final String password) {

        mauth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                HashMap<String, Object> map = new HashMap<>();
                map.put("name", name);
                map.put("email", email);
                map.put("username", username);
                if (mauth.getCurrentUser() != null) {
                    map.put("id", mauth.getCurrentUser().getUid());
                }
                map.put("bio", "");
                map.put("imgurl", "default");

                mrootref.child("Users").child(mauth.getCurrentUser().getUid()).setValue(map);
                if (task.isSuccessful()) {
                    Toast.makeText(Signup.this, "update profile", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    finish();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(Signup.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}


