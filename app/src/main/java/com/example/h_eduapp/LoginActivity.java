package com.example.h_eduapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    EditText mEmailEt, mPassswordEt;
    Button mLoginbtn;

    TextView mNotHaveAccount, mRecoverPassTv;

    //progressbar to display while registering user
    ProgressDialog progressDialog;

    //Declare an instance of FirebaseAuth
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //anh xa
        mEmailEt = findViewById(R.id.emailEt);
        mPassswordEt = findViewById(R.id.passwordEt);
        mLoginbtn = findViewById(R.id.loginBtn);
        mNotHaveAccount = findViewById(R.id.nothave_accountTv);
        mRecoverPassTv = findViewById(R.id.recoverPassTv);


        ///in the onCreate() method, initalize the FirebaseAuth instance.
        mAuth = FirebaseAuth.getInstance();

        //init progress dialog
        progressDialog = new ProgressDialog(this);


        mLoginbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = mEmailEt.getText().toString();
                String password = mPassswordEt.getText().toString().trim();
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    //invalid email patten set error
                    mEmailEt.setError("Invalid Email");
                    mEmailEt.setFocusable(true);
                } else {
                    loginUser(email, password);
                }
            }


        });
        // not have account
        mNotHaveAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
                finish();
            }
        });
        //recover passs textview click
        mRecoverPassTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showRecoverPasswordDialoog();
            }
        });


    }

    private void showRecoverPasswordDialoog() {
        //AlerDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Recover Password");

        //set layout linear layout
        LinearLayout linearLayout = new LinearLayout(this);

        //views to set in dialog
        final EditText emailEt = new EditText(this);
        emailEt.setHint("Email");
        emailEt.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

        emailEt.setMinEms(16);


        linearLayout.addView(emailEt);
        linearLayout.setPadding(10, 10, 10, 10);
        builder.setView(linearLayout);


        //buttons recover
        builder.setPositiveButton("Recover", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //input email
                String email = emailEt.getText().toString().trim();
               beginRecovery(email);
            }
        });
        //buttons cancel
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //disniss dialog
                dialogInterface.dismiss();

            }
        });


    ///show dialog
        builder.show();

    }
    private void beginRecovery(String email) {
        progressDialog.setMessage("Sending email...");

        progressDialog.show();
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        progressDialog.dismiss();
                        if(task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this,"Email sent",Toast.LENGTH_SHORT).show();
                        }
                        else{
                            Toast.makeText(LoginActivity.this,"Failed...",Toast.LENGTH_SHORT).show();
                        }

 

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        //get and show proper error messagge
                        Toast.makeText(LoginActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();

                    }
                });
    }

    private void loginUser(String email, String password) {
        progressDialog.setMessage("Loging In...");
        progressDialog.show();
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // Sign in success, dismiss dialog and start register activity
                    progressDialog.dismiss();
                    FirebaseUser user = mAuth.getCurrentUser();
                    Toast.makeText(LoginActivity.this, "Registered...\n" + user.getEmail(), Toast.LENGTH_SHORT).show();
                    // Khởi chạy ProfileActivity và kết thúc RegisterActivity
                    startActivity(new Intent(LoginActivity.this, ProfileActivity.class));
                    finish();

                } else {
                    //If sign in fails, display a message to the user
                    progressDialog.dismiss();
                    Toast.makeText(LoginActivity.this, "Authentication failed", Toast.LENGTH_SHORT).show();

                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //error, dismiss progess dialog and get and show the error message
                progressDialog.dismiss();
                Toast.makeText(LoginActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
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