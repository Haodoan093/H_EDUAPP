package com.example.h_eduapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AddPostActivity extends AppCompatActivity {
    ActionBar actionBar;
    FirebaseAuth firebaseAuth;
    FirebaseDatabase userDatabase;
    EditText titleEt, descriptionEt;
    ImageView imageIv;
    VideoView videoVv;
    Button videoBtn,imageBtn;
    Button uploadBtn;
    DatabaseReference databaseReference;
    Uri image_uri = null;
    Uri video_uri = null; // Thêm biến cho URI của video đính kèm

    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;
    private static final int IMAGE_PICK_CAMERA_CODE = 400;
    private static final int IMAGE_PICK_GALLERY_CODE = 300;

    // Update constants for file types
    private static final int VIDEO_PICK_CODE = 500;
    private static final int FILE_PICK_CODE = 600;

    //arrays of permissions to be requested
    String cameraPermissions[];
    String storagePermisssions[];

    //user info
    String name, email, uid, dp;

    String editPostId, editPostImage;
    ProgressDialog pd;
    boolean isUserDataReady = false;

    //info of post to be edited
    String editTitle, editDescription, editImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);


        actionBar = getSupportActionBar();
        actionBar.setTitle("Add new post");

        // Ánh xạ các thành phần trong layout XML
        titleEt = findViewById(R.id.pTitleEt);
        descriptionEt = findViewById(R.id.pDescriptionEt);
        imageIv = findViewById(R.id.pImageIv);
        imageBtn = findViewById(R.id.pImageBtn);
        uploadBtn = findViewById(R.id.pUploadBtn);

        //video
        videoVv = findViewById(R.id.pVideoView);
        videoBtn = findViewById(R.id.pVideoBtn);

        MediaController mediaController= new MediaController(this);
        mediaController.setAnchorView(videoVv);
        videoVv.setMediaController(mediaController);


        pd = new ProgressDialog(this);

        //enable back button in ac

        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        firebaseAuth = FirebaseAuth.getInstance();

        checkUserStatus();

        //get data through intent from previous activites's adapter
//phan sua
        Intent intent = getIntent();
        String isUpdateKey = "" + intent.getStringExtra("key");
        editPostId = "" + intent.getStringExtra("editPostId");
        editPostImage = "" + intent.getStringExtra("editPostImage");
//get d
        String action = intent.getAction();
        String type = intent.getType();
        if (Intent.ACTION_SEND.equals(action) && type != null) {
            handSendText(intent);
        } else {
            handSendImage(intent);
        }

        if (isUpdateKey.equals("editPost")) {
            actionBar.setTitle("Update Post");
            uploadBtn.setText("Update");
            loadPostData();

        } else {
            actionBar.setTitle("Add new Post");
            uploadBtn.setText("Update");
        }
        //get soome info of current user to include in post
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        Query query = databaseReference.orderByChild("email").equalTo(email);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    name = "" + ds.child("name").getValue();
                    email = "" + ds.child("email").getValue();
                    dp = "" + ds.child("image").getValue();
                }
                // Gán giá trị cho biến cờ

                isUserDataReady = true;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Xử lý lỗi nếu có
            }
        });


        //init arrays of permisssion
        // Khai báo mảng cameraPermissions
        cameraPermissions = new String[]{android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE}; // Thêm quyền WRITE_EXTERNAL_STORAGE vào đây

        // Khai báo mảng galleryPermissions
        storagePermisssions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};


        actionBar.setSubtitle(email);

        //get iamge form camera /gallary
        imageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImagePickDialog();
            }
        });
        videoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickVideoFromGallery(); // Gọi phương thức để chọn video từ thư viện
            }
        });
        checkUserStatus();
        //upload btn
        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String title = titleEt.getText().toString().trim();
                String desciption = descriptionEt.getText().toString().trim();

                if (TextUtils.isEmpty(title)) {
                    Toast.makeText(AddPostActivity.this, "Enter title", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(desciption)) {
                    Toast.makeText(AddPostActivity.this, "Enter desciption", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (isUpdateKey.equals("editPost")) {
                    beginUpdate(title, desciption, editPostId);
                } else {
                    uploadData(title, desciption);
                }


            }
        });


    }

    private void handSendImage(Intent intent) {
             Uri imageUri=(Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
             if(imageUri!=null){
                 image_uri=imageUri;

                 imageIv.setImageURI(imageUri);
             }
    }

    private void handSendText(Intent intent) {
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (sharedText != null) {
            descriptionEt.setText(sharedText);
        }
    }


    private void beginUpdate(String title, String desciption, String editPostId) {

        pd.setMessage("Updating Post...");

        pd.show();

        if (!editImage.equals("noImage")) {
            //without image
            updateWasWithImage(title, desciption, editPostId);
        } else if (imageIv.getDrawable() != null) {
            updateWithNowImage(title, desciption, editPostId);
        } else {
            //withoud image
            updateWithoutImage(title, desciption, editPostId);
        }
    }

    private void updateWithoutImage(String title, String desciption, String editPostId) {
        HashMap<String, Object> hashMap = new HashMap<>();

        hashMap.put("uid", uid);
        hashMap.put("uName", name);//khong thay
        hashMap.put("uDp", dp);
        hashMap.put("uEmail", email);


        hashMap.put("pTitle", title);
        hashMap.put("pDescr", desciption);
        hashMap.put("pImage", "noImage");//khong thay


        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");

        ref.child(editPostId).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        pd.dismiss();
                        Toast.makeText(AddPostActivity.this, "Updated", Toast.LENGTH_SHORT).show();

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pd.dismiss();
                        Toast.makeText(AddPostActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateWithNowImage(String title, String desciption, String editPostId) {
        String timeStamp = String.valueOf(System.currentTimeMillis());
        String filePathAndName = "Posts/" + "post_" + timeStamp;
        Bitmap bitmap = ((BitmapDrawable) imageIv.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        //image
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] data = baos.toByteArray();
        StorageReference ref = FirebaseStorage.getInstance().getReference().child(filePathAndName);
        ref.putBytes(data)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful()) ;

                        String downloadUri = uriTask.getResult().toString();
                        if (uriTask.isSuccessful()) {
                            HashMap<String, Object> hashMap = new HashMap<>();

                            hashMap.put("uid", uid);
                            hashMap.put("uName", name);//khong thay
                            hashMap.put("uDp", dp);
                            hashMap.put("uEmail", email);


                            hashMap.put("pTitle", title);
                            hashMap.put("pDescr", desciption);
                            hashMap.put("pImage", downloadUri);//khong thay


                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");

                            ref.child(editPostId).updateChildren(hashMap)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            pd.dismiss();
                                            Toast.makeText(AddPostActivity.this, "Updated", Toast.LENGTH_SHORT).show();

                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            pd.dismiss();
                                            Toast.makeText(AddPostActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });


                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pd.dismiss();
                        Toast.makeText(AddPostActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateWasWithImage(String title, String desciption, String editPostId) {
        StorageReference mPictureRef = FirebaseStorage.getInstance().getReferenceFromUrl(editPostImage);

        mPictureRef.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //xoa anh cu , load anh moi
                        //for post-image name, id, publish time
                        String timeStamp = String.valueOf(System.currentTimeMillis());
                        String filePathAndName = "Posts/" + "post_" + timeStamp;
                        Bitmap bitmap = ((BitmapDrawable) imageIv.getDrawable()).getBitmap();
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        //image
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                        byte[] data = baos.toByteArray();
                        StorageReference ref = FirebaseStorage.getInstance().getReference().child(filePathAndName);
                        ref.putBytes(data)
                                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                                        while (!uriTask.isSuccessful()) ;

                                        String downloadUri = uriTask.getResult().toString();
                                        if (uriTask.isSuccessful()) {
                                            HashMap<String, Object> hashMap = new HashMap<>();

                                            hashMap.put("uid", uid);
                                            hashMap.put("uName", name);//khong thay
                                            hashMap.put("uDp", dp);
                                            hashMap.put("uEmail", email);


                                            hashMap.put("pTitle", title);
                                            hashMap.put("pDescr", desciption);
                                            hashMap.put("pImage", downloadUri);//khong thay


                                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");

                                            ref.child(editPostId).updateChildren(hashMap)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void unused) {
                                                            pd.dismiss();
                                                            Toast.makeText(AddPostActivity.this, "Updated", Toast.LENGTH_SHORT).show();

                                                        }
                                                    }).addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            pd.dismiss();
                                                            Toast.makeText(AddPostActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                        }
                                                    });


                                        }
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        pd.dismiss();
                                        Toast.makeText(AddPostActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pd.dismiss();
                        Toast.makeText(AddPostActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadPostData() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        Query query = ref.orderByChild("pId").equalTo(editPostId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    editTitle = "" + ds.child("pTitle").getValue();
                    editDescription = "" + ds.child("pDescr").getValue();
                    editImage = "" + ds.child("pImage").getValue();

                    titleEt.setText(editTitle);
                    descriptionEt.setText(editDescription);

                    if (!editImage.equals("noImage")) {
                        try {
                            Picasso.get().load(editImage).into(imageIv);
                        } catch (Exception e) {

                        }
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void uploadData(String title, String desciption) {
        if (!isUserDataReady) {
            // Nếu chưa sẵn sàng, không thực hiện upload và hiển thị thông báo hoặc xử lý phù hợp
            Toast.makeText(this, "Please wait, user data is not ready yet", Toast.LENGTH_SHORT).show();
            return;
        }
        pd.setMessage("PPublishing post...");
        pd.show();


        //for post-image name,post-id, post-publish-time

        String timeStamp = String.valueOf(System.currentTimeMillis());
        String filePathAndName = "Posts/" + "post_" + timeStamp;
        // Đường dẫn và tên cho ảnh và video
        String imageFilePathAndName = "Posts/" + "post_" + timeStamp ;
        String videoFilePathAndName = "Posts/" + "post_" + timeStamp + ".mp4";

        // Kiểm tra xem cả ảnh và video đã được chọn hay không
        if (imageIv.getDrawable() != null && video_uri != null) {
            // Nếu cả ảnh và video đều đã được chọn
            // Tải ảnh lên Firebase Storage

            Bitmap bitmap = ((BitmapDrawable) imageIv.getDrawable()).getBitmap();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            //image
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] data = baos.toByteArray();


            StorageReference imageRef = FirebaseStorage.getInstance().getReference().child(imageFilePathAndName);
            imageRef.putBytes(data)
                    .addOnSuccessListener(imageTaskSnapshot -> {
                        // Nếu tải ảnh lên thành công
                        Task<Uri> imageUriTask = imageTaskSnapshot.getStorage().getDownloadUrl();
                        Task<Uri> videoUriTask = uploadVideoToStorage(videoFilePathAndName);

                        // Khi cả ảnh và video đều đã được tải lên
                        Tasks.whenAllComplete(imageUriTask, videoUriTask)
                                .addOnSuccessListener(results -> {
                                    // Lấy URL của ảnh và video
                                    String imageDownloadUri = imageUriTask.getResult().toString();
                                    String videoDownloadUri = videoUriTask.getResult().toString();

                                    // Tạo HashMap chứa thông tin của bài đăng
                                    HashMap<String, Object> hashMap = new HashMap<>();
                                    hashMap.put("uid", uid);
                                    hashMap.put("uName", name);
                                    hashMap.put("uEmail", email);
                                    hashMap.put("uDp", dp);
                                    hashMap.put("pLikes", "0");
                                    hashMap.put("pComments", "0");
                                    hashMap.put("pId", timeStamp);
                                    hashMap.put("pTitle", title);
                                    hashMap.put("pDescr", desciption);
                                    hashMap.put("pImage", imageDownloadUri); // URL của ảnh
                                    hashMap.put("pVideo", videoDownloadUri); // URL của video
                                    hashMap.put("pTime", timeStamp);

                                    // Lưu thông tin bài đăng vào Firebase Realtime Database
                                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                                    ref.child(timeStamp).setValue(hashMap)
                                            .addOnSuccessListener(unused -> {
                                                pd.dismiss();
                                                Toast.makeText(AddPostActivity.this, "Post published", Toast.LENGTH_SHORT).show();

                                                // Đặt lại các trường nhập liệu
                                                titleEt.setText("");
                                                descriptionEt.setText("");
                                                imageIv.setImageURI(null);
                                                videoVv.setVideoURI(null);
                                                video_uri = null;
                                                image_uri = null;

                                                // Gửi thông báo
                                                prepareNotification(
                                                        timeStamp,
                                                        name + " added new post",
                                                        title + "\n" + desciption,
                                                        "PostNotification",
                                                        "POST"
                                                );
                                            })
                                            .addOnFailureListener(e -> {
                                                pd.dismiss();
                                                Toast.makeText(AddPostActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            });
                                });
                    })
                    .addOnFailureListener(e -> {
                        pd.dismiss();
                        Toast.makeText(AddPostActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
        else if (video_uri != null) {
            // Nếu có video được chọn
            StorageReference videoRef = FirebaseStorage.getInstance().getReference().child(filePathAndName);
            videoRef.putFile(video_uri)
                    .addOnSuccessListener(taskSnapshot -> {
                        // Nếu tải video lên thành công
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful()) ;
                        String downloadUri = uriTask.getResult().toString();

                        // Tạo HashMap chứa thông tin của bài đăng
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("uid", uid);
                        hashMap.put("uName", name);
                        hashMap.put("uEmail", email);
                        hashMap.put("uDp", dp);
                        hashMap.put("pLikes", "0");
                        hashMap.put("pComments", "0");
                        hashMap.put("pId", timeStamp);
                        hashMap.put("pTitle", title);
                        hashMap.put("pDescr", desciption);
                        hashMap.put("pImage", "noImage");
                        hashMap.put("pVideo", downloadUri); // URL của video
                        hashMap.put("pTime", timeStamp);

                        // Lưu thông tin bài đăng vào Firebase Realtime Database
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                        ref.child(timeStamp).setValue(hashMap)
                                .addOnSuccessListener(unused -> {
                                    pd.dismiss();
                                    Toast.makeText(AddPostActivity.this, "Post published", Toast.LENGTH_SHORT).show();

                                    // Đặt lại các trường nhập liệu
                                    titleEt.setText("");
                                    descriptionEt.setText("");
                                    imageIv.setImageURI(null);
                                    videoVv.setVideoURI(null);
                                    video_uri = null;

                                    // Gửi thông báo
                                    prepareNotification(
                                            timeStamp,
                                            name + " added new post",
                                            title + "\n" + desciption,
                                            "PostNotification",
                                            "POST"
                                    );
                                })
                                .addOnFailureListener(e -> {
                                    pd.dismiss();
                                    Toast.makeText(AddPostActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    })
                    .addOnFailureListener(e -> {
                        pd.dismiss();
                        Toast.makeText(AddPostActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
       else  if (imageIv.getDrawable() != null) {


            Bitmap bitmap = ((BitmapDrawable) imageIv.getDrawable()).getBitmap();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            //image
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] data = baos.toByteArray();


            //post with image
            StorageReference ref = FirebaseStorage.getInstance().getReference().child(filePathAndName);
            ref.putBytes(data)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                            while (!uriTask.isSuccessful()) ;

                            String downloadUri = uriTask.getResult().toString();
                            if (uriTask.isSuccessful()) {
                                HashMap<Object, String> hashMap = new HashMap<>();

                                hashMap.put("uid", uid);
                                hashMap.put("uName", name);//khong thay
                                hashMap.put("uDp", dp);
                                hashMap.put("uEmail", email);
                                hashMap.put("pLikes", "0");
                                hashMap.put("pComments", "0");
                                hashMap.put("pId", timeStamp);
                                hashMap.put("pTitle", title);
                                hashMap.put("pDescr", desciption);
                                hashMap.put("pImage", downloadUri);//khong thay
                                hashMap.put("pTime", timeStamp);
                                hashMap.put("pVideo", "noVideo"); // URL của video

                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");

                                ref.child(timeStamp).setValue(hashMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                pd.dismiss();
                                                Toast.makeText(AddPostActivity.this, "Post published", Toast.LENGTH_SHORT).show();

                                                //resets views
                                                titleEt.setText("");
                                                descriptionEt.setText("");
                                                imageIv.setImageURI(null);
                                                videoVv.setVideoURI(null);
                                                image_uri = null;
                                                //send notification
                                                prepareNotification(
                                                        "" + timeStamp,
                                                        "" + name + "added new post",
                                                        "" + title + "\n" + desciption,
                                                        "PostNotification",
                                                        "POST"
                                                );
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(AddPostActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();

                                            }
                                        });


                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            pd.dismiss();
                            Toast.makeText(AddPostActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            //post without image
            HashMap<Object, String> hashMap = new HashMap<>();

            hashMap.put("uid", uid);
            hashMap.put("uName", name);
            hashMap.put("uEmail", email);
            hashMap.put("pLikes", "0");
            hashMap.put("pComments", "0");
            hashMap.put("uDp", dp);
            hashMap.put("pId", timeStamp);
            hashMap.put("pTitle", title);
            hashMap.put("pDescr", desciption);
            hashMap.put("pImage", "noImage");
            hashMap.put("pTime", timeStamp);
            hashMap.put("pVideo", "noVideo"); // URL của video

            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");

            ref.child(timeStamp).setValue(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            pd.dismiss();
                            Toast.makeText(AddPostActivity.this, "Post published", Toast.LENGTH_SHORT).show();

                            //resets views
                            titleEt.setText("");
                            descriptionEt.setText("");
                            imageIv.setImageURI(null);
                            image_uri = null;
                            //send notification
                            prepareNotification(
                                    "" + timeStamp,
                                    "" + name + "added new post",
                                    "" + title + "\n" + desciption,
                                    "PostNotification",
                                    "POST"
                            );
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(AddPostActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    });


        }

    }
    // Phương thức tải video lên Firebase Storage
    private Task<Uri> uploadVideoToStorage(String videoFilePathAndName) {
        StorageReference videoRef = FirebaseStorage.getInstance().getReference().child(videoFilePathAndName);
        return videoRef.putFile(video_uri)
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return videoRef.getDownloadUrl();
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
            values.put(MediaStore.Images.Media.TITLE, "Temp Pick");
            values.put(MediaStore.Images.Media.DESCRIPTION, "Temp Description");
            image_uri = this.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
            startActivityForResult(cameraIntent, IMAGE_PICK_CAMERA_CODE);
        }
    }
    private void pickVideoFromGallery() {
        // Kiểm tra quyền truy cập bộ nhớ
        if (!checkStoragePermission()) {
            requestStoragePermission();
        } else {
            // Intent để mở thư viện ảnh
            Intent galleryIntent = new Intent(Intent.ACTION_PICK);
            galleryIntent.setType("video/*");
            startActivityForResult(galleryIntent, VIDEO_PICK_CODE);
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
    protected void onStart() {
        super.onStart();
        checkUserStatus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkUserStatus();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.action_addpost).setVisible(false);
        menu.findItem(R.id.action_search).setVisible(false);
        menu.findItem(R.id.action_create_group).setVisible(false);
        menu.findItem(R.id.action_add_participant).setVisible(false);
        menu.findItem(R.id.action_groupinfo).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            firebaseAuth.signOut();
            checkUserStatus();
        }
        return super.onOptionsItemSelected(item);
    }

    private void checkUserStatus() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {

            // Người dùng đã đăng nhập
            uid = user.getUid();
            email = user.getEmail();
        } else {
            // Người dùng chưa đăng nhập, chuyển hướng về màn hình đăng nhập
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    private void prepareNotification(String pId, String title, String description, String nofiticationType, String nofiticationTopic) {

        String NOTIFICATION_TOPIC = "/topics/" + nofiticationTopic;
        String NOTIFICATION_TITLE = title;

        String NOTIFICATION_MESSAGE = description;
        String NOTIFICATION_TYPE = nofiticationType;


        ///prepare jsonwhat to send and where to send

        JSONObject notificcationJo = new JSONObject();
        JSONObject notificcationBodyJo = new JSONObject();

        try {
            //what to send
            notificcationBodyJo.put("nofiticationType", NOTIFICATION_TYPE);
            notificcationBodyJo.put("sender", uid);

            notificcationBodyJo.put("pId", pId);

            notificcationBodyJo.put("pTitle", NOTIFICATION_TITLE);
            notificcationBodyJo.put("pDescription", NOTIFICATION_MESSAGE);

            //where to send
            notificcationJo.put("to", NOTIFICATION_TOPIC);
            notificcationJo.put("data", notificcationBodyJo);


        } catch (JSONException ex) {
            Toast.makeText(this, "" + ex.getMessage(), Toast.LENGTH_SHORT).show();
        }

        sendPostNotification(notificcationJo);

    }

    private void sendPostNotification(JSONObject notificcationJo) {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest("https://fcm.googleapis.com/fcm/send", notificcationJo,

                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("FCM_RESPONSE", "onResponse: " + response.toString());
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(AddPostActivity.this, "" + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                //put required headers
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", "key=AAAA_3xU70w:APA91bFHhr6DmJ9BTQFFucefE6Qg_1ILBpt_IvtAr670YWoLV-MR3bOYgtr6VxL3fRQahHQsEkTjDzdAq98YT42gbZ1V5i4kgORjKmjn6-ujpNLufmNzbPhob3npHx_VajUfw5lkLk_-");
                return headers;
            }
        };
        Volley.newRequestQueue(this).add(jsonObjectRequest);
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
        if (resultCode == RESULT_OK && requestCode == VIDEO_PICK_CODE) {
            // Người dùng đã chọn một video từ thư viện
            video_uri = data.getData(); // Lưu trữ URI của video được chọn
            videoVv.setVideoURI(video_uri);

            videoVv.start();
            // Hiển thị video hoặc thực hiện các thao tác khác ở đây
        }
        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_PICK_GALLERY_CODE) {
                // image is picked from gallery, get uri of image
                image_uri = data.getData();
                imageIv.setImageURI(image_uri);
                // uploadProfileCoverPhoto(image_uri);
            } else if (requestCode == IMAGE_PICK_CAMERA_CODE) {
                imageIv.setImageURI(image_uri);
                //  uploadProfileCoverPhoto(image_uri);
            }
        }
    }

}