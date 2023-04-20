package com.emranbdx.foodwastedonor.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.emranbdx.foodwastedonor.Model.Donor;
import com.emranbdx.foodwastedonor.R;
import com.emranbdx.foodwastedonor.Utils.Config;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Date;

public class SignUpActivity extends AppCompatActivity {
    private EditText nameEditText,emailEditText,mobileEditText,passwordEditText,confirmPasswordEditText;
    private Button registerButton;
    private ProgressBar signUpProgressBar;
    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private String userToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        nameEditText=findViewById(R.id.signUpNameEditTextId);
        emailEditText=findViewById(R.id.signUpEmailEditTextId);
        mobileEditText=findViewById(R.id.signUpMobileEditTextId);
        passwordEditText=findViewById(R.id.signUpPasswordEditTextId);
        confirmPasswordEditText=findViewById(R.id.signUpConfirmPasswordEditTextId);
        registerButton=findViewById(R.id.signUpButtonId);
        signUpProgressBar=findViewById(R.id.signUpProgressBarId);
        Config config=new Config();
        String dbUrl=config.getDbUrl();
        firebaseDatabase=FirebaseDatabase.getInstance(dbUrl);
        firebaseAuth=FirebaseAuth.getInstance();
        registerButton.setOnClickListener(v -> {
            String name=nameEditText.getText().toString();
            String email=emailEditText.getText().toString();
            String mobile=mobileEditText.getText().toString();
            String password=passwordEditText.getText().toString();
            String confirmPassword=confirmPasswordEditText.getText().toString();
            if (name.length()==0){
                nameEditText.setError("Enter Name");
            }
            if (email.length()==0){
                emailEditText.setError(getResources().getString(R.string.enter_email));
            }
            if (mobile.length()==0){
                mobileEditText.setError(getResources().getString(R.string.mobile_number));
            }
            if (password.length()==0){
                passwordEditText.setError(getResources().getString(R.string.enter_password));
            }
            if (confirmPassword.length()==0){
                confirmPasswordEditText.setError(getResources().getString(R.string.confirm_password));
            }
            if ((name.length()!=0&&email.length()!=0)&&((mobile.length()!=0&&password.length()!=0)&&confirmPassword.length()!=0)){
                registerIdToFirebase(name,email,mobile,password);
            }
        });
    }

    private void registerIdToFirebase(String name,String email,String mobile,String password) {
        signUpProgressBar.setVisibility(View.VISIBLE);
        firebaseAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    setDataInFirebase(name,email,mobile,password,firebaseAuth);
                }
                else if (!task.isSuccessful()){
                    if(task.getException() instanceof FirebaseAuthUserCollisionException){
                        Toast.makeText(getApplicationContext(),"User Already Exist",Toast.LENGTH_SHORT).show();
                    }
                    else{
                        Toast.makeText(getApplicationContext(),"Error: "+task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                    }
                    signUpProgressBar.setVisibility(View.GONE);
                }

            }
        });

    }

    private void setDataInFirebase(String name, String email, String mobile,String password, FirebaseAuth firebaseAuth) {
        Date date=new Date();
        Donor donor=new Donor(name,email,mobile,password,firebaseAuth.getUid(),date);
        getUserUniqueId(donor);
    }
    private void getUserUniqueId(Donor donor) {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                if (task.isSuccessful()) {
                    userToken = task.getResult();
                    donor.setUserToken(userToken);
                    saveData(donor);
                }
            }
        });
    }

    private void saveData(Donor donor) {
        DatabaseReference databaseReference=firebaseDatabase.getReference("Donor");
        databaseReference.child(firebaseAuth.getUid()).setValue(donor).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    signUpProgressBar.setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(),"Registration Successful !",Toast.LENGTH_SHORT).show();
                    finish();
                }
                else if (!task.isSuccessful()){
                    Toast.makeText(getApplicationContext(),task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}