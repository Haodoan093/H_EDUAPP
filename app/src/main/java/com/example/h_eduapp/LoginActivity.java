package com.example.h_eduapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
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

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class LoginActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 100;
    GoogleSignInClient mGoogleSignInClient;
    EditText mEmailEt, mPassswordEt;
    Button mLoginbtn;

    TextView mNotHaveAccount, mRecoverPassTv;

    //progressbar to display while registering user
    ProgressDialog progressDialog;

    //Declare an instance of FirebaseAuth
    private FirebaseAuth mAuth;


    //Login Google
    SignInButton mGoogleLoginBtn;

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
        mGoogleLoginBtn = findViewById(R.id.googleLoginBtn);


        //  ActionBar
        ActionBar actionBar = getSupportActionBar();

        //  Đặt tiêu đề của ActionBar
        actionBar.setTitle("Login");

        //  Kích hoạt nút quay lại (back) trong ActionBar
        actionBar.setDisplayHomeAsUpEnabled(true);

        //  Kích hoạt nút home trong ActionBar
        actionBar.setDisplayShowHomeEnabled(true);

        //before mAuth
        //Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);


        ///in the onCreate() method, initalize the FirebaseAuth instance.
        mAuth = FirebaseAuth.getInstance();

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


        //handle google login btn click
        mGoogleLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);

            }
        });


        //init progress dialog
        progressDialog = new ProgressDialog(this);


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
//    sets the min width of a EditView to fit a text of n 'M ' letters regardle
//                extension and text size
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
                        if (task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, "Email sent", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(LoginActivity.this, "Failed...", Toast.LENGTH_SHORT).show();
                        }


                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        //get and show proper error messagge
                        Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
    }

    private void loginUser(String email, String password) {
        // Hiển thị hộp thoại tiến trình
        progressDialog.setMessage("Loging In...");
        progressDialog.show();

        // Đăng nhập vào Firebase Authentication
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // Nếu đăng nhập thành công
                    progressDialog.dismiss(); // Ẩn hộp thoại tiến trình
                    FirebaseUser user = mAuth.getCurrentUser(); // Lấy thông tin người dùng hiện tại

                    // Kiểm tra nếu người dùng mới
                    if(task.getResult().getAdditionalUserInfo().isNewUser()){
                        // Lấy email và uid của người dùng từ Authentication
                        String email= user.getEmail();
                        String uid= user.getUid();

                        // Tạo một HashMap để lưu thông tin của người dùng
                        HashMap<Object,String> hashMap= new HashMap<>();
                        hashMap.put("email",email); // Đưa email vào HashMap
                        hashMap.put("name",""); // Đưa tên vào HashMap (ở đây bạn có thể đưa thông tin tên nếu có)
                        hashMap.put("uid",uid); // Đưa UID vào HashMap
                        hashMap.put("phone",""); // Đưa số điện thoại vào HashMap (ở đây bạn có thể đưa thông tin số điện thoại nếu có)
                        hashMap.put("image",""); // Đưa link hình ảnh vào HashMap (ở đây bạn có thể đưa thông tin ảnh đại diện nếu có)
                        hashMap.put("cover","");
                        hashMap.put("onlineStatus","online");

                        // Lấy tham chiếu đến Firebase Database
                        FirebaseDatabase database= FirebaseDatabase.getInstance();
                        DatabaseReference reference= database.getReference("Users"); // Tham chiếu đến "Users" trong database

                        // Lưu dữ liệu của người dùng vào Firebase Realtime Database
                        reference.child(uid).setValue(hashMap); // Lưu HashMap vào "Users" với key là UID của người dùng
                    }

                    // Hiển thị thông báo đăng nhập thành công
                    Toast.makeText(LoginActivity.this, "Registered...\n" + user.getEmail(), Toast.LENGTH_SHORT).show();

                    // Khởi chạy DashboardActivity và kết thúc LoginActivity
                    startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
                    finish();
                } else {
                    // Nếu đăng nhập thất bại, hiển thị thông báo
                    progressDialog.dismiss(); // Ẩn hộp thoại tiến trình
                    Toast.makeText(LoginActivity.this, "Authentication failed", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Nếu có lỗi, hiển thị thông báo lỗi
                progressDialog.dismiss(); // Ẩn hộp thoại tiến trình
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                Toast.makeText(this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
            }

        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct){

        AuthCredential firebaseCredential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(firebaseCredential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information

                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(LoginActivity.this,""+user.getEmail(),Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
                            finish();

                        } else {
                            // If sign in fails, display a message to the user.

                            Toast.makeText(LoginActivity.this,"Login Failed",Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(LoginActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });

    }

}