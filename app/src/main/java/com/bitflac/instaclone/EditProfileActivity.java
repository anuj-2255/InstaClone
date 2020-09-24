package com.bitflac.instaclone;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bitflac.instaclone.Model.User;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfileActivity extends AppCompatActivity {

    private ImageView close;
    private CircleImageView imgPoflie;
    private TextView save, changephoto;
    private MaterialEditText fullname, usrname, bio;

    private FirebaseUser fUser;

    private Uri mImgUri;
    private StorageTask uploadTask;
    private StorageReference storageRef;
    private int count = 0;
    private String Url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        close = findViewById(R.id.close1);
        imgPoflie = findViewById(R.id.editimg);
        save = findViewById(R.id.save1);
        changephoto = findViewById(R.id.change_photo);
        fullname = findViewById(R.id.fullname1);
        usrname = findViewById(R.id.usrname);
        bio = findViewById(R.id.bio2);

        storageRef = FirebaseStorage.getInstance().getReference().child("Uploads");

        fUser = FirebaseAuth.getInstance().getCurrentUser();

        FirebaseDatabase.getInstance().getReference().child("Users").child(fUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null) {
                    fullname.setText(user.getName());
                    usrname.setText(user.getUsername());
                    bio.setText(user.getBio());
                    Picasso.get().load(user.getImgurl()).into(imgPoflie);
                    Url = user.getImgurl();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EditProfileActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        changephoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                count++;
                CropImage.activity().setCropShape(CropImageView.CropShape.OVAL).start(EditProfileActivity.this);
            }
        });

        imgPoflie.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                count++;
                CropImage.activity().setCropShape(CropImageView.CropShape.OVAL).start(EditProfileActivity.this);
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (count > 0) {
                    UploadImage();
                }
                UpdateProfile();
                finish();

            }
        });

    }

    private void UpdateProfile() {
        HashMap<String, Object> map = new HashMap<>();
        if (fullname.getText() != null && usrname.getText() != null && bio.getText() != null) {
            map.put("name", fullname.getText().toString());
            map.put("username", usrname.getText().toString());
            map.put("bio", bio.getText().toString());
        }

        FirebaseDatabase.getInstance().getReference().child("Users").child(fUser.getUid()).updateChildren(map);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (result != null) {
                mImgUri = result.getUri();
            }
            if (mImgUri == null) {
                Picasso.get().load(Url).into(imgPoflie);
                return;
            }
            Picasso.get().load(mImgUri).into(imgPoflie);
        } else {
            Toast.makeText(this, "failed!!", Toast.LENGTH_SHORT).show();
        }

    }

    private void UploadImage() {

        if (mImgUri != null) {
            final AlertDialog al = new AlertDialog.Builder(this).create();
            al.setMessage("Uploading...");
            al.show();
            final StorageReference fileref = storageRef.child(System.currentTimeMillis() + ".jpeg");
            uploadTask = fileref.putFile(mImgUri);
            uploadTask.continueWithTask(new Continuation() {
                @Override
                public Object then(@NonNull Task task) {
                    if (!task.isSuccessful()) {
                        Toast.makeText(EditProfileActivity.this, "Something went wrong...", Toast.LENGTH_SHORT).show();
                    }
                    return fileref.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        String Url = null;
                        if (downloadUri != null) {
                            Url = downloadUri.toString();
                        }

                        FirebaseDatabase.getInstance().getReference().child("Users").child(fUser.getUid()).child("imgurl").setValue(Url);
                        al.dismiss();

                    } else {
                        al.dismiss();
                        Toast.makeText(EditProfileActivity.this, "Upload Failed", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            Toast.makeText(this, "nothing selected", Toast.LENGTH_SHORT).show();
        }

    }
}
