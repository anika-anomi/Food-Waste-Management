package com.emranbdx.foodwasteagent.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.emranbdx.foodwasteagent.Model.Donation;
import com.emranbdx.foodwasteagent.R;
import com.emranbdx.foodwasteagent.Utils.Config;
import com.emranbdx.foodwasteagent.Utils.FcmNotificationsSender;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;

public class DonationDetailsActivity extends AppCompatActivity {
    private Donation donation;
    private FirebaseDatabase firebaseDatabase;
    private TextView statusTextView,donorNameTextView,mobileNumberTV,foodAmountTV,foodTypeTV,donationDateTV,locationTV;
    private Button deliverButton;
    private LinearLayout linearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donation_details);
        getSupportActionBar().setTitle("Donation Details");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        firebaseDatabase=FirebaseDatabase.getInstance(new Config().getDbUrl());
        statusTextView=findViewById(R.id.statusTV);
        donorNameTextView=findViewById(R.id.donorNameId);
        mobileNumberTV=findViewById(R.id.mobileNumberId);
        foodAmountTV=findViewById(R.id.foodAmountId);
        foodTypeTV=findViewById(R.id.foodTypeId);
        donationDateTV=findViewById(R.id.donationDateId);
        locationTV=findViewById(R.id.locationId);
        deliverButton=findViewById(R.id.deliveredButtonId);
        linearLayout=findViewById(R.id.buttonLL);
        donation= (Donation) getIntent().getSerializableExtra("DonationClass");
        statusTextView.setText(donation.getStatus());
        donorNameTextView.setText(donation.getDonor().getName());
        mobileNumberTV.setText(donation.getDonor().getMobileNumber());
        foodAmountTV.setText(donation.getFoodAmount());
        foodTypeTV.setText(donation.getFoodType());
        donationDateTV.setText(new SimpleDateFormat("dd-MMM-yyyy").format(donation.getDonationDate()));
        locationTV.setText(donation.getLocation());
        deliverButton.setOnClickListener(view -> {
            setDonationStatus("Delivered");
        });
    }
    private void setDonationStatus(String status){
        ProgressDialog progressDialog=new ProgressDialog(this);
        progressDialog.setTitle("Pls Wait...");
        progressDialog.setMessage("Data is sending");
        progressDialog.show();
        DatabaseReference databaseReference=firebaseDatabase.getReference("Donation").child(donation.getDonationId()).child("status");
        databaseReference.setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    sendNotificationToAdmin(donation);
                    sendNotificationToTheDonor(status,donation);
                    finish();
                }else {
                    Toast.makeText(DonationDetailsActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
                progressDialog.cancel();
            }
        });
    }

    private void sendNotificationToTheDonor(String status, Donation donation) {
        FcmNotificationsSender fcmNotificationsSender=new FcmNotificationsSender(donation.getDonor().getUserToken(),"Donation Update!","Hello, "+donation.getDonor().getName()+"\n"+"Your donation "+donation.getFoodAmount()+" present status "+status,DonationDetailsActivity.this,DonationDetailsActivity.this);
        fcmNotificationsSender.SendNotifications();
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
                Toast.makeText(DonationDetailsActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendNotification(Donation donation,String userToken) {
        FcmNotificationsSender fcmNotificationsSender=new FcmNotificationsSender(userToken,"Delivery Report!","Hello Admin,\nDonor Name: "+donation.getDonor().getName()+"\nFood Amount: "+donation.getFoodAmount()+"\nFood Type: "+donation.getFoodType()+"\nLocation: "+donation.getLocation()+"\nHas been successfully delivered..",DonationDetailsActivity.this,DonationDetailsActivity.this);
        fcmNotificationsSender.SendNotifications();
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId()==android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}