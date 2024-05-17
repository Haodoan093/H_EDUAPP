package com.example.h_eduapp.fragments;

import static android.app.Activity.RESULT_OK;

import com.example.h_eduapp.AddPostActivity;
import com.example.h_eduapp.MainActivity;
import com.example.h_eduapp.R;
import com.example.h_eduapp.SettinggsActivity;
import com.example.h_eduapp.adapters.AdapterPosts;
import com.example.h_eduapp.models.ModelPoost;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.database.FirebaseDatabase;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.Manifest;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.storage.FirebaseStorage;

import com.google.android.gms.auth.api.signin.internal.Storage;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {
    TextInputEditText addpost_btn;
    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    //storage
    StorageReference storageReference;
    //path where img of user profile and cover will be stored
    String storagePath = "User_Profile_Cover_Imgs/";

    //view from xml

    ImageView avatarIv, coverIv,avatarIv1;
    FloatingActionButton fab;
    TextView nameTv, emailTv, phoneTv;

    ProgressDialog pd;

    RecyclerView postRecyclerview;

    //permesssions contants;
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;
    private static final int IMAGE_PICK_CAMERA_CODE = 400;
    private static final int IMAGE_PICK_GALLERY_CODE = 300;

    //arrays of permissions to be requested
    String cameraPermissions[];
    String storagePermisssions[];

    //uri of picked image

    List<ModelPoost> postlist;
    AdapterPosts adapterPosts;
    String uid;

    Uri image_uri;

    //for checking profile or cover photo

    String profileOrCoverPhoto;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        //init firebase
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        postlist = new ArrayList<>();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Users");

        storageReference = FirebaseStorage.getInstance().getReference();

        //init view
        addpost_btn = view.findViewById(R.id.addpost_btn);
        avatarIv = view.findViewById(R.id.avatarIv);
        avatarIv1 = view.findViewById(R.id.avatarIv1);
        nameTv = view.findViewById(R.id.nameTv);
        emailTv = view.findViewById(R.id.emailTv);
        phoneTv = view.findViewById(R.id.phoneTv);
        coverIv = view.findViewById(R.id.coverIv);


        fab = view.findViewById(R.id.fab);
        postRecyclerview = view.findViewById(R.id.recyclerview_posts);
        //init arrays of permisssion
        // Khai báo mảng cameraPermissions
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}; // Thêm quyền WRITE_EXTERNAL_STORAGE vào đây

// Khai báo mảng galleryPermissions
        storagePermisssions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        //init progess dialog

        pd = new ProgressDialog(getActivity());


//        We have to get info of currently signed in user. We can get it using user's email
//                or uid
        // i'm gona retrieve user detail using email.
        // By using orderByChild qeury we will show the detail from a node
        // whose key named email has value equal to cureently signed in email
        // It will search all nodes , where the key matches it will get its details

        Query query = databaseReference.orderByChild("email").equalTo(user.getEmail());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //check until requiredd data get
                for (DataSnapshot ds : snapshot.getChildren()) {
                    //get data
                    String name = "" + ds.child("name").getValue();
                    String email = "" + ds.child("email").getValue();
                    String phone = "" + ds.child("phone").getValue();
                    String image = "" + ds.child("image").getValue();
                    String cover = "" + ds.child("cover").getValue();
                    String studentCode = "" + ds.child("studentCode").getValue();
                    nameTv.setText(name);
                    emailTv.setText(email);
                    phoneTv.setText(studentCode);
                    try {
                        // if image is received the get
                        Picasso.get().load(image).into(avatarIv);
                        Picasso.get().load(image).into(avatarIv1);
                    } catch (Exception e) {
                        // if there is any exception while getting image the get default
                        Picasso.get().load(R.drawable.ic_default_img_white).into(avatarIv);
                        Picasso.get().load(R.drawable.ic_default_img_white).into(avatarIv1);
                    }
                    try {
                        // if image is received the get
                        Picasso.get().load(cover).into(coverIv);
                    } catch (Exception e) {
                        // if there is any exception while getting image the get default
                        //   Picasso.get().load(R.drawable.ic_default_img_white).into(coverIv);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        //fab button click
        addpost_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), AddPostActivity.class));
            }
        });
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showEditProfileDialog();
            }
        });

        checkUserStatus();
        loadMyPosts();
        // Inflate the layout for this fragment
        return view;
    }

    private void loadMyPosts() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());

        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);

        postRecyclerview.setLayoutManager(linearLayoutManager);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");

        Query query = ref.orderByChild("uid").equalTo(uid);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postlist.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ModelPoost modelPoost = ds.getValue(ModelPoost.class);

                    postlist.add(modelPoost);


                    adapterPosts = new AdapterPosts(getActivity(), postlist);

                    postRecyclerview.setAdapter(adapterPosts);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), "" + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void searchMyPosts(String searchQuery) {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());

        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);

        postRecyclerview.setLayoutManager(linearLayoutManager);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");

        Query query = ref.orderByChild("uid").equalTo(uid);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postlist.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ModelPoost mypost = ds.getValue(ModelPoost.class);


                    if (mypost.getpTitle().toLowerCase().contains(searchQuery.toLowerCase()) ||
                            mypost.getpDescr().toLowerCase().contains(searchQuery.toLowerCase())) {
                        postlist.add(mypost);
                    }


                    adapterPosts = new AdapterPosts(getActivity(), postlist);

                    postRecyclerview.setAdapter(adapterPosts);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), "" + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private boolean checkStoragePermission() {
        //check if storage permission is enabled or not
        //return true if enable
        //return if not enable
        boolean result = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return result;

    }

    private void requestStoragePermission() {
        //request runtime storage permisssion
        ActivityCompat.requestPermissions(getActivity(), storagePermisssions, STORAGE_REQUEST_CODE);
    }

    //camera
    private boolean checkCameraPermission() {
        // Kiểm tra xem quyền truy cập camera đã được cấp chưa
        boolean result = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)
                == (PackageManager.PERMISSION_GRANTED);
        return result;
    }


    private void requestCameraPermission() {
        //request runtime storage permisssion
        ActivityCompat.requestPermissions(getActivity(), cameraPermissions, CAMERA_REQUEST_CODE);
    }

    private void showEditProfileDialog() {

        String options[] = {"Edit profile picture ", "Edit cover photo", "Edit name", "Edit phone","Change password", "More"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle("Choose Action");

        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                //handle dialog item clicks
                if (which == 0) {
                    //edit profile
                    pd.setMessage("Updating Proflie Picture");
                    profileOrCoverPhoto = "image";//i.e changing profile picture, make sure to assign same value

                    showImagePicDialog();
                } else if (which == 1) {
                    //edit Cover
                    pd.setMessage("Updating Cover Photo");
                    profileOrCoverPhoto = "cover";
                    showImagePicDialog();
                } else if (which == 2) {
                    //Edit name
                    pd.setMessage("Updating Name");
                    showNamePhoneUpdateDialog("name");

                } else if (which == 3) {
                    //Edit phone
                    pd.setMessage("Updating Phone");
                    showNamePhoneUpdateDialog("phone");

                } else if (which == 4) {
                    //Edit phone
                    pd.setMessage("Changing password");
                    showChangePasswordDialog();

                } else if (which == 5) {
                    //Edit phone
                    pd.setMessage("More");
                    showInfo();

                }
            }


        });
        //crat and show dialog
        builder.create().show();
    }

    private void showChangePasswordDialog() {
        View view= LayoutInflater.from(getActivity()).inflate(R.layout.dialog_update_password,null);
        final EditText passwordEt= view.findViewById(R.id.passwordEt);
        final EditText newPasswordEt= view.findViewById(R.id.newPasswordEt);
        Button updatePasswordBtn=view.findViewById(R.id.updatePasswordBtn);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);

        AlertDialog dialog= builder.create();
        dialog.show();


        updatePasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String oldPassword= passwordEt.getText().toString().trim();
                String newPassword=newPasswordEt.getText().toString().trim();

                if(TextUtils.isEmpty(oldPassword)){
                    Toast.makeText(getActivity(), "Enter your current password", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(newPassword.length()<6){
                    Toast.makeText(getActivity(), "Password length must atleast o characters...", Toast.LENGTH_SHORT).show();

                    return;
                }
                dialog.dismiss();
                updatePassword(oldPassword,newPassword);
            }
        });
    }

    private void updatePassword(String oldPassword, String newPassword) {
pd.show();

final FirebaseUser user1= firebaseAuth.getCurrentUser();
        AuthCredential authCredential= EmailAuthProvider.getCredential(user1.getEmail(),oldPassword);
        user1.reauthenticate(authCredential)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        user1.updatePassword(newPassword)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        pd.dismiss();
                                        Toast.makeText(getActivity(), "password updated", Toast.LENGTH_SHORT).show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        pd.dismiss();
                                        Toast.makeText(getActivity(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pd.dismiss();
                        Toast.makeText(getActivity(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showInfo() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getLayoutInflater();
        //Dialog được tạo dựa trên dialog_edit_product.xml gồm:
        //textViewMaSP, editTextTenSP, editTextSoSP, editTextGiaSP
        View dialogView = inflater.inflate(R.layout.dialog_info_edit, null);
        dialogBuilder.setView(dialogView);
        final TextView edtMaSV = dialogView.findViewById(R.id.edtMaSV);
        final EditText edtTenSV = dialogView.findViewById(R.id.edtTenSV);
        final EditText edtLop = dialogView.findViewById(R.id.edtLop);
        final EditText edtSDT = dialogView.findViewById(R.id.edtSDT);

        EditText edtEmail = dialogView.findViewById(R.id.edtEmail);
        Query query = databaseReference.orderByChild("email").equalTo(user.getEmail());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //check until requiredd data get
                for (DataSnapshot ds : snapshot.getChildren()) {
                    //get data
                    String name = "" + ds.child("name").getValue();
                    String email = "" + ds.child("email").getValue();
                    String phone = "" + ds.child("phone").getValue();
                    String studentCode = "" + ds.child("studentCode").getValue();

                    String classDH = "" + ds.child("class").getValue();

                    edtMaSV.setText(studentCode);
                    edtTenSV.setText(name);
                    edtLop.setText(classDH);
                    edtSDT.setText(phone);

                    edtEmail.setText(email);


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        dialogBuilder.setNegativeButton("Close", null);
        dialogBuilder.setTitle("Thông tin cá nhân");
        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
    }

    private void showNamePhoneUpdateDialog(String key) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Update " + key);

        LinearLayout linearLayout = new LinearLayout(getActivity());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setPadding(10, 10, 10, 10);

        // Add edit text
        EditText editText = new EditText(getActivity());
        editText.setHint("Enter " + key);
        linearLayout.addView(editText); // Thêm editText vào linearLayout

        builder.setView(linearLayout);

        // Add button in dialog to update
        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Input text from edit text
                String value = editText.getText().toString().trim();

                // Check if user has entered something or not
                if (!TextUtils.isEmpty(value)) {
                    pd.show();
                    HashMap<String, Object> result = new HashMap<>();
                    result.put(key, value);
                    databaseReference.child(user.getUid()).updateChildren(result)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    pd.dismiss();
                                    Toast.makeText(getActivity(), "Updated... ", Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    pd.dismiss();
                                    Toast.makeText(getActivity(), " " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                    //if user edit his name , also change it from hist posts
                    if (key.equals("name")) {
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                        Query query = ref.orderByChild("uid").equalTo(uid);
                        query.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot ds : snapshot.getChildren()) {
                                    String child = ds.getKey();
                                    snapshot.getRef().child(child).child("uName").setValue(value);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                        //update name in current users comments on posts
                        ref.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot ds : snapshot.getChildren()) {
                                    String child = ds.getKey();
                                    if (snapshot.child(child).hasChild("Comments")) {
                                        String child1 = "" + snapshot.child(child).getKey();
                                        Query child2 = FirebaseDatabase.getInstance().getReference("Posts")
                                                .child(child1).child("Comments").orderByChild("uid")
                                                .equalTo(uid);
                                        child2.addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                for (DataSnapshot ds : snapshot.getChildren()) {
                                                    String child = ds.getKey();
                                                    snapshot.getRef().child(child).child("uName").setValue(value);
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                } else {
                    Toast.makeText(getActivity(), "Please Enter " + key, Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Add button in dialog to cancel
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        // Create and show dialog
        builder.create().show();
    }


    private void showImagePicDialog() {
        String options[] = {"Camera", "Gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle("Pick Image From");

        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                // Xử lý sự kiện khi người dùng chọn từng mục trong dialog
                switch (which) {
                    case 0:
                        // Mục Camera được chọn
                        if (!checkCameraPermission()) {
                            // Kiểm tra và yêu cầu quyền truy cập camera nếu chưa được cấp
                            requestCameraPermission();
                        } else {
                            // Quyền truy cập camera đã được cấp, chọn ảnh từ camera
                            pickFromCamera();
                        }
                        break;
                    case 1:
                        // Mục Gallery được chọn
                        if (!checkStoragePermission()) {
                            // Kiểm tra và yêu cầu quyền truy cập bộ nhớ nếu chưa được cấp
                            requestStoragePermission();
                        } else {
                            // Quyền truy cập bộ nhớ đã được cấp, chọn ảnh từ gallery
                            pickFromGallery();
                        }
                        break;
                }
            }
        });

        // Tạo và hiển thị dialog
        builder.create().show();
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
                        Toast.makeText(getActivity(), "Please enable camara and storage permission", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(getActivity(), "Please enable storage permission", Toast.LENGTH_SHORT).show();
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
                uploadProfileCoverPhoto(image_uri);
            } else if (requestCode == IMAGE_PICK_CAMERA_CODE) {
                uploadProfileCoverPhoto(image_uri);
            }
        }
    }

    private void uploadProfileCoverPhoto(Uri imageUri) {
        // Hiển thị tiến trình
        pd.show();

        // Đường dẫn và tên của hình ảnh để lưu trong kho lưu trữ Firebase
        String filePathAndname = storagePath + profileOrCoverPhoto + "_" + user.getUid();

        StorageReference storageReference2nd = storageReference.child((filePathAndname));
        storageReference2nd.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Lấy URI của hình ảnh đã tải lên
                        storageReference2nd.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri downloadUri) {
                                // Nếu URI được tạo thành công, cập nhật vào cơ sở dữ liệu người dùng
                                if (downloadUri != null) {
                                    // Tạo một HashMap để chứa dữ liệu cập nhật
                                    HashMap<String, Object> results = new HashMap<>();
                                    results.put(profileOrCoverPhoto, downloadUri.toString());

                                    // Thực hiện cập nhật vào cơ sở dữ liệu
                                    databaseReference.child(user.getUid()).updateChildren(results)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    // Cập nhật thành công, ẩn tiến trình và hiển thị thông báo
                                                    pd.dismiss();
                                                    Toast.makeText(getActivity(), "Image Updated", Toast.LENGTH_SHORT).show();
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    // Xảy ra lỗi khi cập nhật cơ sở dữ liệu, ẩn tiến trình và hiển thị thông báo lỗi
                                                    pd.dismiss();
                                                    Toast.makeText(getActivity(), "Error Updating Image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                    if (profileOrCoverPhoto.equals("image")) {
                                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                                        Query query = ref.orderByChild("uid").equalTo(uid);
                                        query.addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                for (DataSnapshot ds : snapshot.getChildren()) {
                                                    String child = ds.getKey();
                                                    snapshot.getRef().child(child).child("uDp").setValue(downloadUri.toString());
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });

                                        //update user image in current users comments on posts
                                        //update name in current users comments on posts
                                        ref.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                for (DataSnapshot ds : snapshot.getChildren()) {
                                                    String child = ds.getKey();
                                                    if (snapshot.child(child).hasChild("Comments")) {
                                                        String child1 = "" + snapshot.child(child).getKey();
                                                        Query child2 = FirebaseDatabase.getInstance().getReference("Posts")
                                                                .child(child1).child("Comments").orderByChild("uid")
                                                                .equalTo(uid);
                                                        child2.addValueEventListener(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                for (DataSnapshot ds : snapshot.getChildren()) {
                                                                    String child = ds.getKey();
                                                                    snapshot.getRef().child(child).child("uDp").setValue(downloadUri.toString());
                                                                }
                                                            }

                                                            @Override
                                                            public void onCancelled(@NonNull DatabaseError error) {

                                                            }
                                                        });
                                                    }
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });

                                    }
                                } else {
                                    // Nếu không nhận được URI, ẩn tiến trình và hiển thị thông báo lỗi
                                    pd.dismiss();
                                    Toast.makeText(getActivity(), "Failed to get Image URI", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Xảy ra lỗi khi tải lên hình ảnh, ẩn tiến trình và hiển thị thông báo lỗi
                        pd.dismiss();
                        Toast.makeText(getActivity(), "Error Uploading Image 2: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }


    private void pickFromCamera() {
        // Kiểm tra quyền truy cập bộ nhớ
        if (!checkStoragePermission()) {
            requestStoragePermission();
        } else {
            // Intent để mở camera
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.TITLE, "Temp Pic");
            values.put(MediaStore.Images.Media.DESCRIPTION, "Temp Description");
            image_uri = getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
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


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);// to show menu option in fragment

        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.menu_main, menu);

        MenuItem item = menu.findItem(R.id.action_search);
        menu.findItem(R.id.action_groupinfo).setVisible(false);
        menu.findItem(R.id.action_create_group).setVisible(false);
        menu.findItem(R.id.action_add_participant).setVisible(false);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                //goi khi search
                if (!TextUtils.isEmpty(s)) {
                    searchMyPosts(s);
                } else {
                    loadMyPosts();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                // goi khi khong tim kiem
                if (!TextUtils.isEmpty(s)) {
                    searchMyPosts(s);
                } else {
                    loadMyPosts();
                }
                return false;
            }
        });
        super.onCreateOptionsMenu(menu, menuInflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            firebaseAuth.signOut();
            checkUserStatus();
        } else if (id == R.id.action_addpost) {
            startActivity(new Intent(getActivity(), AddPostActivity.class));
        } else if (id == R.id.action_settings) {
            //go to settinggs activity
            startActivity((new Intent(getActivity(), SettinggsActivity.class)));
        }
        return super.onOptionsItemSelected(item);
    }

    private void checkUserStatus() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            uid = user.getUid();
            // Người dùng đã đăng nhập
        } else {
            // Người dùng chưa đăng nhập, chuyển hướng về màn hình đăng nhập
            startActivity(new Intent(getActivity(), MainActivity.class));
            getActivity().finish();
        }
    }
}