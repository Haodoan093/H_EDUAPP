package com.example.h_eduapp;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

public class GroupCreateActivity extends AppCompatActivity {
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;
    private static final int IMAGE_PICK_CAMERA_CODE = 400;
    private static final int IMAGE_PICK_GALLERY_CODE = 300;
    String cameraPermissions[];
    String storagePermisssions[];
    Uri image_uri = null;
    private ActionBar actionBar;
    private FirebaseAuth firebaseAuth;

    private ImageView groupIconIv;
    private EditText groupTitleEt, groupDescriptionEt;

    private FloatingActionButton creatGroupBtn;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_group_create);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        actionBar = getSupportActionBar();
        actionBar.setTitle("Creat group");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        groupIconIv = findViewById(R.id.groupIconIv);
        groupTitleEt = findViewById(R.id.groupTitleEt);
        groupDescriptionEt = findViewById(R.id.groupDescriptionEt);
        creatGroupBtn = findViewById(R.id.creatGroupBtn);

        cameraPermissions = new String[]{android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE}; // Thêm quyền WRITE_EXTERNAL_STORAGE vào đây

        storagePermisssions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        firebaseAuth = FirebaseAuth.getInstance();

        checkUser();

        //pick image
        groupIconIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImagePickDialog();
            }
        });
        //hand click
        creatGroupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startCreatGroup();
            }
        });
    }

    private void startCreatGroup() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Creating group");


        //input title,description
        String groupTitle = groupTitleEt.getText().toString().trim();
        String grooupDescriptioon = groupDescriptionEt.getText().toString().trim();

        if (TextUtils.isEmpty(groupTitle)) {
            Toast.makeText(this, "Please enter group title...", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.show();
        String g_timestamp = "" + System.currentTimeMillis();
        if (image_uri == null) {
            creatGroup(
                    "" + g_timestamp,
                    "" + groupTitle,
                    "" + grooupDescriptioon,
                    "");
        } else {
           //creating group with icon image
            //upload image

            //image name and path
            String fileNameAndPath="Group_Imgs/"+"image"+g_timestamp;

            StorageReference storageReference= FirebaseStorage.getInstance().getReference(fileNameAndPath);
            storageReference.putFile(image_uri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Task<Uri> p_UriTask= taskSnapshot.getStorage().getDownloadUrl();
                            while (!p_UriTask.isSuccessful());
                            Uri p_dowlooadUri=p_UriTask.getResult();
                            if(p_UriTask.isSuccessful()){
                                creatGroup(
                                        "" + g_timestamp,
                                        "" + groupTitle,
                                        "" + grooupDescriptioon,
                                        ""+p_dowlooadUri);
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();

                            Toast.makeText(GroupCreateActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }

    }

    private void creatGroup(String g_timestamp, String groupTitle, String groupDescription, String groupIcon) {


        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("groupId", "" + g_timestamp);
        hashMap.put("groupTitle", "" + groupTitle);
        hashMap.put("groupDescription", "" + groupDescription);
        hashMap.put("groupIcon", "" + groupIcon);
        hashMap.put("timestamp", "" + g_timestamp);

        hashMap.put("createdBy", "" + firebaseAuth.getUid());

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(g_timestamp).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                      //created ss

                        //setup member inf(add current user in group' participants list

                        HashMap<String,String> hashMap1= new HashMap<>();
                        hashMap1.put("uid",firebaseAuth.getUid());
                        hashMap1.put("role","creator");
                        hashMap1.put("timestamp",g_timestamp);

                        DatabaseReference ref1= FirebaseDatabase.getInstance().getReference("Groups");
                        ref1.child(g_timestamp).child("Participants").child(firebaseAuth.getUid())
                                .setValue(hashMap1)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        progressDialog.dismiss();
                                        Toast.makeText(GroupCreateActivity.this, "Group created", Toast.LENGTH_SHORT).show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        progressDialog.dismiss();
                                        Toast.makeText(GroupCreateActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(GroupCreateActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });


    }

    private void showImagePickDialog() {
        //options(camera, gallary

        String options[] = {"Camera", "Gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Choose Image From");

        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {


                if (which == 0) {
                    // Mục Camera được chọn
                    if (!checkCameraPermission()) {
                        // Kiểm tra và yêu cầu quyền truy cập camera nếu chưa được cấp
                        requestCameraPermission();
                    } else {
                        // Quyền truy cập camera đã được cấp, chọn ảnh từ camera
                        pickFromCamera();
                    }


                }
                if (which == 1) {

                    // Mục Gallery được chọn
                    if (!checkStoragePermission()) {
                        // Kiểm tra và yêu cầu quyền truy cập bộ nhớ nếu chưa được cấp
                        requestStoragePermission();
                    } else {
                        // Quyền truy cập bộ nhớ đã được cấp, chọn ảnh từ gallery
                        pickFromGallery();
                    }
                }


            }

        });

        // Tạo và hiển thị dialog
        builder.create().show();
    }


    private void pickFromCamera() {
        // Kiểm tra quyền truy cập bộ nhớ
        if (!checkStoragePermission()) {
            requestStoragePermission();
        } else {
            // Intent để mở camera
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.TITLE, "Group Imagge Icon Title");
            values.put(MediaStore.Images.Media.DESCRIPTION, "Group Imagge Icon Description");
            image_uri = this.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
            startActivityForResult(cameraIntent, IMAGE_PICK_CAMERA_CODE);
        }
    }

    private void pickFromGallery() {
        // Kiểm tra quyền truy cập bộ nhớ
        if (!checkStoragePermission()) {
            requestStoragePermission();
        } else {
            // Intent để mở thư viện ảnh
            Intent galleryIntent = new Intent(Intent.ACTION_PICK);
            galleryIntent.setType("image/*");
            startActivityForResult(galleryIntent, IMAGE_PICK_GALLERY_CODE);
        }
    }

    private boolean checkStoragePermission() {
        //check if storage permission is enabled or not
        //return true if enable
        //return if not enable
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return result;

    }

    private void requestStoragePermission() {
        //request runtime storage permisssion
        ActivityCompat.requestPermissions(this, storagePermisssions, STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission() {
        // Kiểm tra xem quyền truy cập camera đã được cấp chưa
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestCameraPermission() {
        //request runtime storage permisssion
        ActivityCompat.requestPermissions(this, cameraPermissions, CAMERA_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //This method called when user press Allow or Deny from permission request dialog
        //here we will handle permission cases(allowed & denied)
        switch (requestCode) {
            case CAMERA_REQUEST_CODE: {
                //picking from camera, first check if camera and storage permissions allowed or not
                if (grantResults.length > 0) {
                    boolean camareAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (camareAccepted && writeStorageAccepted) {
                        //permission enabled
                        pickFromCamera();

                    } else {
                        Toast.makeText(this, "Please enable camara and storage permission", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
            case STORAGE_REQUEST_CODE: {
                if (grantResults.length > 0) {

                    boolean writeStorageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (writeStorageAccepted) {
                        //permission enabled
                        pickFromGallery();


                    } else {
                        Toast.makeText(this, "Please enable storage permission", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        // this method will be called after picking image from camera or gallery
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_PICK_GALLERY_CODE) {
                // image is picked from gallery, get uri of image
                image_uri = data.getData();
                groupIconIv.setImageURI(image_uri);
                // uploadProfileCoverPhoto(image_uri);
            } else if (requestCode == IMAGE_PICK_CAMERA_CODE) {
                groupIconIv.setImageURI(image_uri);
                //  uploadProfileCoverPhoto(image_uri);
            }
        }
    }

    private void checkUser() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            actionBar.setSubtitle(user.getEmail());
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}