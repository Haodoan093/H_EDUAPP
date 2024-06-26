package com.example.h_eduapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {
    EditText mEmailEt, mNameEt, mPhoneEt, mPassswordEt, mRePasswordEt,mStudentCodeEt,mClassEt;
    Button mRegissterbtn;

    TextView mHaveAccount;

    //progressbar to display while registering user
    ProgressDialog progressDialog;

    //Declare an instance of FirebaseAuth
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //ánh xạ
        mEmailEt = findViewById(R.id.emailEt);
        mNameEt = findViewById(R.id.nameEt);
        mPhoneEt = findViewById(R.id.phoneEt);
        mPassswordEt = findViewById(R.id.passwordEt);
        mRePasswordEt = findViewById(R.id.repasswordEt);
        mRegissterbtn = findViewById(R.id.registerBtn);
        mStudentCodeEt = findViewById(R.id.studentCodeEt);
        mClassEt = findViewById(R.id.classEt);

        mHaveAccount= findViewById(R.id.have_accountTv);

        ///in the onCreate() method, initalize the FirebaseAuth instance.
        mAuth = FirebaseAuth.getInstance();


        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Registering User...");


        //  ActionBar
        ActionBar actionBar = getSupportActionBar();

        //  Đặt tiêu đề của ActionBar
        actionBar.setTitle("Create Account");

        //  Kích hoạt nút quay lại (back) trong ActionBar
        actionBar.setDisplayHomeAsUpEnabled(true);

        //  Kích hoạt nút home trong ActionBar
        actionBar.setDisplayShowHomeEnabled(true);

        mRegissterbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = mEmailEt.getText().toString().trim();
                String name = mNameEt.getText().toString().trim();
                String phone = mPhoneEt.getText().toString().trim();
                String studenCode = mStudentCodeEt.getText().toString().trim();
                String classDH = mClassEt.getText().toString().trim();
                String password = mPassswordEt.getText().toString().trim();
                String repassword = mRePasswordEt.getText().toString().trim();

                //validate
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    //set error and focus to email EditText
                    mEmailEt.setError("Invalid Email");
                    mEmailEt.requestFocus();

                } else if (password.length() < 6) {
                    //set error and focus to password EditText
                    mPassswordEt.setError("Password length at least 6 characters ");
                    mPassswordEt.requestFocus();

                } else if (!password.equals(repassword)) {
                    //set error and focus to re-password EditText
                    mRePasswordEt.setError("Passwords do not match");
                    mRePasswordEt.requestFocus();

                } else {
                    registerUser(email, password, name, phone,studenCode,classDH);
                }
            }
        });

        mHaveAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                finish();
            }
        });
    }

    private void registerUser(String email, String password, String name, String phone, String studenCode, String classDH) {
        //email and password pattern is valid, show progress dialog and start registering user
        progressDialog.show();
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // Sign in success, dismiss dialog and start register activity
                    progressDialog.dismiss();
                    FirebaseUser user = mAuth.getCurrentUser();

                    //get user email and uid from auth
                    String email= user.getEmail();
                    String uid= user.getUid();
                    //When user is registered store user info in firebase realtime database too
                    //using hashMap

                    HashMap<Object,String> hashMap= new HashMap<>();
                    //put info in hasmap
                    hashMap.put("email",email);
                    hashMap.put("name", name);
                    hashMap.put("uid",uid);
                    hashMap.put("phone",phone);
                    hashMap.put("image","");
                    hashMap.put("class",classDH);
                    hashMap.put("studentCode",studenCode);//ma sinh vien
                    hashMap.put("onlineStatus","online");
                    hashMap.put("typingTo","noOne");
                    hashMap.put("cover","");
                    //se add sau khi dang edit profile
                    //Firebse dt instance
                    FirebaseDatabase database= FirebaseDatabase.getInstance();
                    //path to store user data named "Users"
                    DatabaseReference reference= database.getReference("Users");
                    //put data within hashmap in database
                    reference.child(uid).setValue(hashMap);

                    Toast.makeText(RegisterActivity.this,"Registered...\n"+user.getEmail() ,Toast.LENGTH_SHORT).show();
                    // Khởi chạy ProfileActivity và kết thúc RegisterActivity
                    startActivity(new Intent(RegisterActivity.this, DashboardActivity.class));
                    finish();


                } else {
                    //If sign in fails, display a message to the user
                    progressDialog.dismiss();
                    Toast.makeText(RegisterActivity.this, "Authentication failed", Toast.LENGTH_SHORT).show();


                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //error, dismiss progess dialog and get and show the error message
                progressDialog.dismiss();
                Toast.makeText(RegisterActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });


    }


    @Override
    public boolean onSupportNavigateUp() {
        // Phương thức này được gọi khi nút quay lại (back) trên ActionBar được nhấn
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}
