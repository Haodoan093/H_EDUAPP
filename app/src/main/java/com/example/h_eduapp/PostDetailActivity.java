package com.example.h_eduapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.example.h_eduapp.adapters.AdapterComments;
import com.example.h_eduapp.models.ModelComment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class PostDetailActivity extends AppCompatActivity {

    ////to get detail of user and post
    String myUid, myEmail, hisUid, myName, myDp,
            pImage, postId, pLikes, hisDp, hisNamel,pVideo;

    boolean mProcessComment = false;
    boolean mProcessLike = false;

    ImageButton moreBtn;
    ImageView uPictureIv, pImageIv;
    TextView nameTv, pTimeTv, pTitleTv, pDescriptionTv, pLikesTv, pCommentsTv;
    Button likeBtn, shareBtn;
    LinearLayout profileLayout;
    RecyclerView recyclerView;
    VideoView videoView;

    List<ModelComment> commentList;
    AdapterComments adapterComments;


    ProgressDialog pd;

    ////add commetns views

    EditText commentEt;
    ImageView cAvatarIv;

    ImageButton sendBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Post detail");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);


        Intent intent = getIntent();
        postId = intent.getStringExtra("postId");

        // Khởi tạo views
        uPictureIv = findViewById(R.id.uPictureIv);
        pImageIv = findViewById(R.id.pImageIv);
        nameTv = findViewById(R.id.uNameTv);
        pTimeTv = findViewById(R.id.pTimeTv);
        pTitleTv = findViewById(R.id.pTitleTv);
        pDescriptionTv = findViewById(R.id.pDesciptionTv);
        pLikesTv = findViewById(R.id.pLikesTv);
        likeBtn = findViewById(R.id.likeBtn);
        shareBtn = findViewById(R.id.shareBtn);
        profileLayout = findViewById(R.id.profileLayout);
        pCommentsTv = findViewById(R.id.pCommentsTv);
        videoView=findViewById(R.id.pVideoView);

        // Khởi tạo views cho phần comment
        commentEt = findViewById(R.id.commentEt);
        cAvatarIv = findViewById(R.id.cAvatarIv);
        sendBtn = findViewById(R.id.sendBtn);
        moreBtn = findViewById(R.id.moreBtn);
        recyclerView = findViewById(R.id.recyclerView);

        loadPostInfo();
        checkUserStatus();

        loadUserInfo();
        setLikes();
        //set title of actionbar
        actionBar.setSubtitle("SignedIn as: " + myEmail);
        loadComments();

        //send Comment button click

        sendBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                postComment();
            }
        });

        //like button click handle
        likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                likePost();
            }
        });

        moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showMoreOptions();
            }
        });
        shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String pTitle= pTitleTv.getText().toString().trim();
                String pDescription= pDescriptionTv.getText().toString().trim();
                BitmapDrawable bitmapDrawable = (BitmapDrawable) pImageIv.getDrawable();
                if (bitmapDrawable == null) {
                    shareTextonly(pTitle, pDescription);
                } else {
                    //post with image
                    //convert image to bit map
                    Bitmap bitmap = bitmapDrawable.getBitmap();

                    shareImageAndText(pTitle, pDescription, bitmap);
                }
            }
        });
        pLikesTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent= new Intent(PostDetailActivity.this, PostLikeByActivity.class);
                intent.putExtra("postId",postId);
           startActivity(intent);
            }
        });
    }
    private void addToHisNotifications(String hisUid, String pId, String notification) {
        String timestamp = String.valueOf(System.currentTimeMillis());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("pId", pId);
        hashMap.put("timestamp", timestamp);
        hashMap.put("pUid", hisUid);
        hashMap.put("notification", notification);
        hashMap.put("sUid", myUid);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(hisUid).child("Notifications").child(timestamp)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        // Thêm mã xử lý tại đây nếu cần
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Xử lý trường hợp thất bại khi thêm dữ liệu vào Firebase Database
                    }
                });
    }

    private void shareImageAndText(String pTitle, String pDescription, Bitmap bitmap) {
        String shareBody = pTitle + "\n" + pDescription;

//first we will save thisimg in cache, get the saved image uri
        Uri uri = saveImageToShare(bitmap);

        //share intent

        Intent sIntent = new Intent(Intent.ACTION_SEND);
        sIntent.putExtra(Intent.EXTRA_STREAM, uri);
        sIntent.putExtra(Intent.EXTRA_TEXT, shareBody);//text to share
        sIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject Here");
        sIntent.setType("image/png");
       startActivity(Intent.createChooser(sIntent, "Share Via"));//mess to show in share dialog

    }

    private Uri saveImageToShare(Bitmap bitmap) {
        File imageFolder = new File(getCacheDir(), "images");
        Uri uri = null;
        try {
            imageFolder.mkdir();
            File file = new File(imageFolder, "shared_image.pnd");
            FileOutputStream stream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream);
            stream.flush();
            stream.close();
            uri = FileProvider.getUriForFile(this, "com.blogspot.atifsoftwares.firebaseapp.fileprovider", file);
        } catch (Exception e) {

            Toast.makeText(this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return uri;

    }

    private void shareTextonly(String pTitle, String pDescription) {

        String shareBody = pTitle + "\n" + pDescription;

        //share sIntent
        Intent sIntent = new Intent(Intent.ACTION_SEND);
        sIntent.setType("text/plain");
        sIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject Here");
        sIntent.putExtra(Intent.EXTRA_TEXT, shareBody);//text to share
     startActivity(Intent.createChooser(sIntent, "Share Via"));//mess to show in share dialog

    }

    private void loadComments() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());

        recyclerView.setLayoutManager(linearLayoutManager);

        commentList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts").child(postId).child("Comments");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                commentList.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    ModelComment modelComment= ds.getValue(ModelComment.class);

                    commentList.add(modelComment);

                    adapterComments = new AdapterComments(getApplicationContext(),commentList,myUid,postId);


                    recyclerView.setAdapter(adapterComments);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void showMoreOptions() {
        PopupMenu popupMenu = new PopupMenu(this, moreBtn, Gravity.END);


        if (hisUid.equals(myUid)) {
            popupMenu.getMenu().add(Menu.NONE, 0, 0, "Edit");
            popupMenu.getMenu().add(Menu.NONE, 1, 0, "Delete");
        }

        //item click listener

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                int id = menuItem.getItemId();
                if (id == 0) {
                    //edit
                    Intent intent = new Intent(PostDetailActivity.this, AddPostActivity.class);
                    intent.putExtra("key", "editPost");
                    intent.putExtra("editPostId", postId);
                    // intent.putExtra("editPostImage",pImage);
                    startActivity(intent);

                }

                if (id == 1) {
                    //delete is clicked
                    beginDelte();

                }
                return false;
            }
        });

        popupMenu.show();
    }

    private void beginDelte() {
        if (pImage.equals("noImage")) {
            deleteWithoutImage();
        } else {
            deleteWithImage();
        }
    }

    private void deleteWithImage() {
        ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Deleting...");
//del image using url
        //del froom data using post id
        StorageReference picRef = FirebaseStorage.getInstance().getReferenceFromUrl(pImage);
        picRef.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {

                        Query query = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(postId);

                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@androidx.annotation.NonNull DataSnapshot snapshot) {
                                for (DataSnapshot ds : snapshot.getChildren()) {
                                    ds.getRef().removeValue();//rremove values from firebbase where pId matches

                                }
                                Toast.makeText(PostDetailActivity.this, "Deleted successfully", Toast.LENGTH_SHORT).show();
                                pd.dismiss();

                            }

                            @Override
                            public void onCancelled(@androidx.annotation.NonNull DatabaseError error) {

                            }
                        });

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@androidx.annotation.NonNull Exception e) {
                        pd.dismiss();
                        Toast.makeText(PostDetailActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void deleteWithoutImage() {
        ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Deleting...");
        Query query = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(postId);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@androidx.annotation.NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ds.getRef().removeValue();//rremove values from firebbase where pId matches

                }
                Toast.makeText(PostDetailActivity.this, "Deleted successfully", Toast.LENGTH_SHORT).show();
                pd.dismiss();

            }

            @Override
            public void onCancelled(@androidx.annotation.NonNull DatabaseError error) {

            }
        });
    }

    private void setLikes() {

        DatabaseReference likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        likesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@androidx.annotation.NonNull DataSnapshot snapshot) {
                if (snapshot.child(postId).hasChild(myUid)) {
                    likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_liked, 0, 0, 0);
                    likeBtn.setText("Liked");
                } else {
                    likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_like, 0, 0, 0);
                    likeBtn.setText("Like");
                }
            }

            @Override
            public void onCancelled(@androidx.annotation.NonNull DatabaseError error) {

            }
        });

    }

    private void likePost() {


        mProcessLike = true;

        DatabaseReference likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        likesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@androidx.annotation.NonNull DataSnapshot snapshot) {
                if (mProcessLike) {
                    if (snapshot.child(postId).hasChild(myUid)) {
                        //already liked, so remove like
                        postsRef.child(postId).child("pLikes").setValue("" + (Integer.parseInt(pLikes) - 1));
                        likesRef.child(postId).child(myUid).removeValue();
                        mProcessLike = false;

            /*            likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_like,0,0,0);
                        likeBtn.setText("Like");*/
                    } else {
                        postsRef.child(postId).child("pLikes").setValue("" + (Integer.parseInt(pLikes) + 1));
                        likesRef.child(postId).child(myUid).setValue("Liked");
                        mProcessLike = false;
                     /*   likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_liked,0,0,0);
                        likeBtn.setText("Liked");*/
                        addToHisNotifications(""+hisUid,""+postId,"Liked your posts");
                    }
                }
            }

            @Override
            public void onCancelled(@androidx.annotation.NonNull DatabaseError error) {

            }
        });
    }

    private void postComment() {
        pd = new ProgressDialog(this);
        pd.setMessage("Adding comment...");

        ///get data froom comment edit text

        String comment = commentEt.getText().toString().trim();

        if (TextUtils.isEmpty(comment)) {
            Toast.makeText(this, "Comment is empty...", Toast.LENGTH_SHORT).show();
            return;
        }
        String timeStamp = String.valueOf(System.currentTimeMillis());
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts")
                .child(postId).child("Comments");

        HashMap<String, Object> hashMap = new HashMap<>();
        //put
        hashMap.put("cId", timeStamp);

        hashMap.put("comment", comment);
        hashMap.put("timeStamp", timeStamp);
        hashMap.put("uid", myUid);
        hashMap.put("uEmail", myEmail);
        hashMap.put("uDp", myDp);
        hashMap.put("uName", myName);

        ref.child(timeStamp).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        pd.dismiss();

                        Toast.makeText(PostDetailActivity.this, "Coomment Added...", Toast.LENGTH_SHORT).show();
                        commentEt.setText("");
                        updateCommentCount();

                        addToHisNotifications(""+hisUid,""+postId,"Commented on your posts");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pd.dismiss();

                        Toast.makeText(PostDetailActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });


    }


    private void updateCommentCount() {
        mProcessComment = true;
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts").child(postId);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (mProcessComment) {
                    String comments = "" + snapshot.child("pComments").getValue();
                    int newCommentVal = Integer.parseInt(comments) + 1;

                    ref.child("pComments").setValue("" + newCommentVal);
                    mProcessComment = false;
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadUserInfo() {
        Query myRef = FirebaseDatabase.getInstance().getReference("Users");
        myRef.orderByChild("uid").equalTo(myUid).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            myName = "" + ds.child("name").getValue();
                            myDp = "" + ds.child("image").getValue();


                            ////set user img
                            try {
                                Picasso.get().load(myDp).placeholder(R.drawable.avata).into(cAvatarIv);
                            } catch (Exception e) {
                                Picasso.get().load(R.drawable.avata).into(cAvatarIv);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                }
        );
    }

    private void loadPostInfo() {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        Query query = ref.orderByChild("pId").equalTo(postId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String pTitle = "" + ds.child("pTitle").getValue();
                    String pDescr = "" + ds.child("pDescr").getValue();
                    pLikes = "" + ds.child("pLikes").getValue();
                    String pTimeStamp = "" + ds.child("pTime").getValue();
                    pImage = "" + ds.child("pImage").getValue();
                    hisDp = "" + ds.child("uDp").getValue();
                    pVideo = "" + ds.child("pVideo").getValue();
                    hisUid = "" + ds.child("uid").getValue();
                    String uEmail = "" + ds.child("uEmail").getValue();
                    hisNamel = "" + ds.child("uName").getValue();
                    String commentCount = "" + ds.child("pComments").getValue();
                    ;

                    String pTime = "";
                    if (pTimeStamp != null) {
                        //convert time stamp to dd/MM/yyyy hh:mm am/pm
                        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
                        cal.setTimeInMillis(Long.parseLong(pTimeStamp));

                        // Use SimpleDateFormat to format the date
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.ENGLISH);
                        pTime = sdf.format(cal.getTime());
                    }

                    //set data
                    pTitleTv.setText(pTitle);
                    pDescriptionTv.setText(pDescr);
                    pLikesTv.setText(pLikes + "Likes");
                    pTimeTv.setText(pTime);
                    pCommentsTv.setText(commentCount + " Comments");

                    nameTv.setText(hisNamel);
                    //set video
                    if (pVideo.equals("noVideo")) {
                        // Nếu không có video, ẩn VideoView
                        videoView.setVisibility(View.GONE);
                    } else {

                        // Nếu video tồn tại, hiển thị VideoView và tải video từ URL
                        videoView.setVisibility(View.VISIBLE);
                        Uri videoUri = Uri.parse(pVideo);
                        videoView.setVideoURI(videoUri);

                        // Tạo và thiết lập MediaController
                        MediaController mediaController = new MediaController(PostDetailActivity.this);
                        mediaController.setAnchorView(videoView);
                        videoView.setMediaController(mediaController);


                    }
                    //set image
                    if (pImage.equals("noImage")) {
                        pImageIv.setVisibility(View.GONE);
                    } else {
                        pImageIv.setVisibility(View.VISIBLE);
                        try {
                            Picasso.get().load(pImage).into(pImageIv);
                        } catch (Exception e) {
                            // Handle exception if Picasso fails to load image
                        }

                    }

                    ////set user img
                    try {
                        Picasso.get().load(hisDp).placeholder(R.drawable.avata).into(uPictureIv);
                    } catch (Exception e) {
                        Picasso.get().load(R.drawable.avata).into(uPictureIv);
                    }


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void checkUserStatus() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            //user is signed in
            myEmail = user.getEmail();
            myUid = user.getUid();
        } else {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.action_addpost).setVisible(false);
        menu.findItem(R.id.action_search).setVisible(false);
        menu.findItem(R.id.action_groupinfo).setVisible(false);
        menu.findItem(R.id.action_add_participant).setVisible(false);
        menu.findItem(R.id.action_create_group).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            FirebaseAuth.getInstance().signOut();
            checkUserStatus();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}