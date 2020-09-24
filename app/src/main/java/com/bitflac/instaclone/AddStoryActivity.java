package com.bitflac.instaclone;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.HashMap;

public class AddStoryActivity extends AppCompatActivity {

    private Uri mImageUri;
    String myUrl = "";
    private StorageTask storageTask;
    StorageReference storageReference;
    FirebaseUser user=FirebaseAuth.getInstance().getCurrentUser();
    String myid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_story);

        storageReference = FirebaseStorage.getInstance().getReference().child("Story");

        CropImage.activity()
                .setAspectRatio(9, 16)
                .start(AddStoryActivity.this);

    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getMimeTypeFromExtension(contentResolver.getType(uri));
    }

    private void publishStory() {
        final AlertDialog al = new AlertDialog.Builder(this).create();
        al.setMessage("Posting...");
        al.show();

        if (mImageUri != null) {
            final StorageReference imageReference = storageReference.child(System.currentTimeMillis() + "." + getFileExtension(mImageUri));

            storageTask = imageReference.putFile(mImageUri);
            storageTask.continueWithTask(new Continuation() {
                @Override
                public Object then(@NonNull Task task) {
                    if (!task.isSuccessful()) {
                        Toast.makeText(AddStoryActivity.this, "Uploading failed...", Toast.LENGTH_SHORT).show();
                    }
                    return imageReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloaduri = task.getResult();
                        if (downloaduri != null) {
                            myUrl = downloaduri.toString();
                        }
                        if (user!=null) {
                            myid = user.getUid();
                        }

                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Story").child(myid);
                        String storyid = reference.push().getKey();
                        long timeend = System.currentTimeMillis() + 86400000; // 1 day

                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("imageurl", myUrl);
                        hashMap.put("timestart", ServerValue.TIMESTAMP);
                        hashMap.put("timeend", timeend);
                        hashMap.put("storyid", storyid);
                        hashMap.put("userid", myid);

                        if (storyid != null) {
                            reference.child(storyid).setValue(hashMap);
                        }
                        al.dismiss();
                        finish();
                    } else {
                        Toast.makeText(AddStoryActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(AddStoryActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "No Image Selected!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (result != null) {
                mImageUri = result.getUri();
            }

            publishStory();
        } else {
            Toast.makeText(this, "Nothing Selected!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(AddStoryActivity.this, HomeActivity.class));
            finish();
        }
    }
}

