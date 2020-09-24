package com.bitflac.instaclone;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.hendraanggrian.appcompat.socialview.Hashtag;
import com.hendraanggrian.appcompat.widget.HashtagArrayAdapter;
import com.hendraanggrian.appcompat.widget.SocialAutoCompleteTextView;
import com.theartofdev.edmodo.cropper.CropImage;


import java.util.HashMap;
import java.util.List;

public class PostActivity extends AppCompatActivity {

    private Uri imageuri;
    private String imageUrl;

    private ImageView close, imageAdded;
    private TextView post;
    SocialAutoCompleteTextView description;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        close = findViewById(R.id.close);
        imageAdded = findViewById(R.id.image_added);
        post = findViewById(R.id.post);
        description = findViewById(R.id.description);

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(PostActivity.this, HomeActivity.class));
                finish();
            }
        });

        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                upload();
                finish();
            }
        });

        CropImage.activity().setAspectRatio(1,1).start(PostActivity.this);


    }

    private void upload() {

        if (imageuri != null) {
            final AlertDialog al = new AlertDialog.Builder(this).create();
            al.setMessage("Uploading...");
            al.show();
            final StorageReference filepath = FirebaseStorage.getInstance().getReference("Posts")
                    .child(System.currentTimeMillis() + "." + getFilesExtension(imageuri));

            final StorageTask uploadtask = filepath.putFile(imageuri);

            uploadtask.continueWithTask(new Continuation() {
                @Override
                public Object then(@NonNull Task task) {
                    if (!task.isSuccessful()) {
                        if (task.getException() != null)
                            Toast.makeText(PostActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    return filepath.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    Uri downloadUri = task.getResult();
                    if (downloadUri != null) {
                        imageUrl = downloadUri.toString();
                    } else {
                        Toast.makeText(PostActivity.this, "Some went wrong", Toast.LENGTH_SHORT).show();

                    }

                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                    String postId = ref.push().getKey();

                    HashMap<String, Object> map = new HashMap<>();
                    map.put("postId", postId);
                    map.put("imageurl", imageUrl);
                    map.put("description", description.getText().toString());
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user != null) {
                        map.put("publisher", user.getUid());
                    } else {
                        Toast.makeText(PostActivity.this, "something went wrong", Toast.LENGTH_SHORT).show();
                    }

                    if (postId != null) {
                        ref.child(postId).setValue(map);
                    }

                    DatabaseReference mhashtagref = FirebaseDatabase.getInstance().getReference().child("HashTags");
                    List<String> hashTags = description.getHashtags();
                    if (!hashTags.isEmpty()) {
                        for (String tag : hashTags) {
                            map.clear();

                            map.put("tag", tag.toLowerCase());
                            map.put("postid", postId);

                            if (postId != null) {
                                mhashtagref.child(tag.toLowerCase()).child(postId).setValue(map);
                            }
                        }
                    }
                    al.dismiss();
                    startActivity(new Intent(PostActivity.this, HomeActivity.class));

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    al.dismiss();
                    Toast.makeText(PostActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "No image was selected", Toast.LENGTH_SHORT).show();
        }

    }

    private String getFilesExtension(Uri uri) {
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(this.getContentResolver().getType(uri));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (result != null) {
                imageuri = result.getUri();
            }

            imageAdded.setImageURI(imageuri);
        } else {
            Toast.makeText(this, "Try Again!!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(PostActivity.this, HomeActivity.class));
            finish();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        final ArrayAdapter<Hashtag> hashTagAdapter = new HashtagArrayAdapter<>(getApplicationContext());

        FirebaseDatabase.getInstance().getReference().child("HashTags").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    String key = snapshot1.getKey();
                    if (key != null)
                        hashTagAdapter.add(new Hashtag(key, (int) snapshot1.getChildrenCount()));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(PostActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        description.setHashtagAdapter(hashTagAdapter);
    }
}
