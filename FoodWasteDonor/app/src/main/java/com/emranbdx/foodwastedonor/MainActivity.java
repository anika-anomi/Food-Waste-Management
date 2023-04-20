package com.emranbdx.foodwastedonor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.emranbdx.foodwastedonor.Activity.DonationHistoryActivity;
import com.emranbdx.foodwastedonor.Model.Donation;
import com.emranbdx.foodwastedonor.Model.Donor;
import com.emranbdx.foodwastedonor.Utils.Config;
import com.emranbdx.foodwastedonor.Utils.FcmNotificationsSender;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private EditText foodAmountEditText,foodTypeEditText,locationEditText;
    private Button donateButton;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    private FirebaseDatabase firebaseDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        progressDialog=new ProgressDialog(this);
        firebaseAuth=FirebaseAuth.getInstance();
        firebaseDatabase=FirebaseDatabase.getInstance(new Config().getDbUrl());
        foodAmountEditText=findViewById(R.id.foodAmountEditTextId);
        foodTypeEditText=findViewById(R.id.foodTypeEditTextId);
        locationEditText=findViewById(R.id.locationEditTextId);
        donateButton=findViewById(R.id.donationButtonId);
        donateButton.setOnClickListener(view -> {
            String foodAmount=foodAmountEditText.getText().toString();
            String foodType=foodTypeEditText.getText().toString();
            String location=locationEditText.getText().toString();
            if (foodAmount.isEmpty()){
                foodAmountEditText.setError("Enter Food Amount");
                foodAmountEditText.requestFocus();
            }
            if (foodType.isEmpty()){
                foodTypeEditText.setError("Enter Food Type");
                foodTypeEditText.requestFocus();
            }
            if (location.isEmpty()){
                locationEditText.setError("Enter Location");
                locationEditText.requestFocus();
            }
            if (!foodAmount.isEmpty()&&!foodType.isEmpty()&&!location.isEmpty()){
                getUserDetails(foodAmount,foodType,location);
            }
        });
    }

    private void getUserDetails(String foodAmount, String foodType, String location) {
        progressDialog.setTitle("Please Wait...");
        progressDialog.setMessage("Your donation is submitting");
        progressDialog.show();
        DatabaseReference databaseReference=firebaseDatabase.getReference("Donor");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot:snapshot.getChildren()){
                    if (dataSnapshot.getKey().contentEquals(firebaseAuth.getCurrentUser().getUid())){
                        String donationId= UUID.randomUUID().toString();
                        Date donationDate=new Date();
                        Donor donor=dataSnapshot.getValue(Donor.class);
                        Donation donation=new Donation(donor,foodAmount,foodType,location,donationDate,donationId);
                        donation.setStatus("Pending");
                        saveDataInFirebase(donation);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                progressDialog.cancel();
            }
        });

    }

    private void saveDataInFirebase(Donation donation) {
        DatabaseReference databaseReference=firebaseDatabase.getReference("Donation").child(donation.getDonationId());
        databaseReference.setValue(donation).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
              if (task.isSuccessful()){
                  Toast.makeText(MainActivity.this, "Donation Submitted to the Admin", Toast.LENGTH_SHORT).show();
                  foodAmountEditText.setText(null);
                  foodTypeEditText.setText(null);
                  locationEditText.setText(null);
                  foodAmountEditText.requestFocus();
                  sendNotificationToAdmin(donation);
              }else {
                  Toast.makeText(MainActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
              }
              progressDialog.cancel();
            }
        });
    }

    private void sendNotificationToAdmin(Donation donation) {
        DatabaseReference databaseReference=firebaseDatabase.getReference("Admin_User_Token");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot:snapshot.getChildren()){
                    String adminUserToken=dataSnapshot.getValue(String.class);
                    sendNotification(donation,adminUserToken);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendNotification(Donation donation,String userToken) {
        FcmNotificationsSender fcmNotificationsSender=new FcmNotificationsSender(userToken,"Donation Request","Hello Admin,\nDonor Name: "+donation.getDonor().getName()+"\nFood Amount: "+donation.getFoodAmount()+"\nFood Type: "+donation.getFoodType()+"\nLocation: "+donation.getLocation()+"\nIts time to approve it.",MainActivity.this,MainActivity.this);
        fcmNotificationsSender.SendNotifications();
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        MenuInflater menuInflater=getMenuInflater();
        menuInflater.inflate(R.menu.main_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId()==R.id.exitId){
            finishAffinity();
        }
        if (item.getItemId()==R.id.historyId){
            startActivity(new Intent(MainActivity.this, DonationHistoryActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }
}