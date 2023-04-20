package com.emranbdx.foodwastedonor.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.emranbdx.foodwastedonor.MainActivity;
import com.emranbdx.foodwastedonor.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {
    private EditText emailEditText,passwordEditText;
    private Button loginButton;
    private TextView signUpTextView;
    private FirebaseAuth firebaseAuth;
    private ProgressBar progressBar;
    private FirebaseDatabase firebaseDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        emailEditText=findViewById(R.id.loginEmailId);
        passwordEditText=findViewById(R.id.loginPasswordId);
        loginButton=findViewById(R.id.loginButtonId);
        signUpTextView=findViewById(R.id.signUpTextViewId);
        progressBar=findViewById(R.id.loginProgressBarId);
        firebaseAuth=FirebaseAuth.getInstance();
        firebaseDatabase=FirebaseDatabase.getInstance();
        loginButton.setOnClickListener(v -> {
            String email=emailEditText.getText().toString();
            String password=passwordEditText.getText().toString();
            if (email.length()==0){
                emailEditText.setError(getResources().getString(R.string.enter_email));
            }
            if (password.length()==0){
                passwordEditText.setError(getResources().getString(R.string.enter_password));
            }
            if(email.length()!=0&&password.length()!=0){
                progressBar.setVisibility(View.VISIBLE);
                loginUser(email,password);
            }

        });
        signUpTextView.setOnClickListener(v -> {
            Intent intent=new Intent(LoginActivity.this,SignUpActivity.class);
            startActivity(intent);
        });
    }

    private void loginUser(String email, String password) {
        firebaseAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                String currentUserId=null;
                SharedPreferences.Editor mySharedPreference=getSharedPreferences("MySharedPreference",MODE_PRIVATE).edit();
                if (task.isSuccessful()){
                    try{
                        currentUserId=firebaseAuth.getCurrentUser().getUid();
                        mySharedPreference.putString("userId",currentUserId);
                        mySharedPreference.apply();
                        Intent intent=new Intent(LoginActivity.this, MainActivity.class);
                        Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                        startActivity(intent);
                        finish();
                    }
                    catch (Throwable throwable){
                        throwable.printStackTrace();
                    }
                }
                else{
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(),"Error: "+task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}