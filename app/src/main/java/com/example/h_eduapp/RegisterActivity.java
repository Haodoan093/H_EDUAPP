package com.example.h_eduapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
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

public class RegisterActivity extends AppCompatActivity {
    EditText mEmailEt, mPassswordEt;
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
        mPassswordEt = findViewById(R.id.passwordEt);
        mRegissterbtn = findViewById(R.id.registerBtn);

        mHaveAccount= findViewById(R.id.have_accountTv);

        ///in the onCreate() method, initalize the FirebaseAuth instance.
        mAuth = FirebaseAuth.getInstance();


        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Registering User...");


//        // ActionBar
//        ActionBar actionBar = getSupportActionBar();
//
//        // Đặt tiêu đề của ActionBar
//        actionBar.setTitle("Create Account");
//
//        // Kích hoạt nút quay lại (back) trong ActionBar
//        actionBar.setDisplayHomeAsUpEnabled(true);
//
//        // Kích hoạt nút home trong ActionBar
//        actionBar.setDisplayShowHomeEnabled(true);

        mRegissterbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = mEmailEt.getText().toString().trim();
                String password = mPassswordEt.getText().toString().trim();

                //validate
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    //set error and focuss to email edittext
                    mEmailEt.setError("Invalid Email");
                    mEmailEt.setFocusable(true);

                } else if (password.length() < 6) {
                    //set error and focuss to password edittext
                    mPassswordEt.setError("Password length at least 6 characters ");
                    mPassswordEt.setFocusable(true);

                } else {
                    registerUser(email, password);
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

    private void registerUser(String email, String password) {
        //email and password patten is valid, show progress dialog and start registering user
        progressDialog.show();
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // Sign in success, dismiss dialog and start register activity
                    progressDialog.dismiss();
                    FirebaseUser user = mAuth.getCurrentUser();
                    Toast.makeText(RegisterActivity.this,"Registered...\n"+user.getEmail() ,Toast.LENGTH_SHORT).show();
                    // Khởi chạy ProfileActivity và kết thúc RegisterActivity
                    startActivity(new Intent(RegisterActivity.this, ProfileActivity.class));
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
