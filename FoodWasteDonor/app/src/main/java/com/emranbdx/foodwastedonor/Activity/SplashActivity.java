package com.emranbdx.foodwastedonor.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.emranbdx.foodwastedonor.MainActivity;
import com.emranbdx.foodwastedonor.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        FirebaseAuth firebaseAuth=FirebaseAuth.getInstance();
        FirebaseUser firebaseUser=firebaseAuth.getCurrentUser();
        if (firebaseUser==null){
            startActivity(new Intent(SplashActivity.this,LoginActivity.class));
        }else {
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
        }
        finish();
    }
}