package com.emranbdx.foodwasteadmin.Activity;

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

import com.emranbdx.foodwasteadmin.Model.Donation;
import com.emranbdx.foodwasteadmin.R;
import com.emranbdx.foodwasteadmin.Utils.Config;
import com.emranbdx.foodwasteadmin.Utils.FcmNotificationsSender;
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
    private Button rejectButton,approveButton;
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
        rejectButton=findViewById(R.id.rejectId);
        approveButton=findViewById(R.id.approveId);
        linearLayout=findViewById(R.id.buttonLL);
        donation= (Donation) getIntent().getSerializableExtra("DonationClass");
        if (donation.getStatus().contentEquals("Pending")){
            linearLayout.setVisibility(View.VISIBLE);
        }
        statusTextView.setText(donation.getStatus());
        donorNameTextView.setText(donation.getDonor().getName());
        mobileNumberTV.setText(donation.getDonor().getMobileNumber());
        foodAmountTV.setText(donation.getFoodAmount());
        foodTypeTV.setText(donation.getFoodType());
        donationDateTV.setText(new SimpleDateFormat("dd-MMM-yyyy").format(donation.getDonationDate()));
        locationTV.setText(donation.getLocation());
        rejectButton.setOnClickListener(view -> {
            setDonationStatus("Rejected");
        });
        approveButton.setOnClickListener(view -> {
            setDonationStatus("Approved");
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
                    if (status.contentEquals("Rejected")){
                        Toast.makeText(DonationDetailsActivity.this, "Donation Rejected Successfully", Toast.LENGTH_SHORT).show();
                    }else {
                        Toast.makeText(DonationDetailsActivity.this, "Donation Approved Successfully", Toast.LENGTH_SHORT).show();
                        sendNotificationToTheAgent(donation);
                    }
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

    private void sendNotificationToTheAgent(Donation donation) {
        DatabaseReference databaseReference=firebaseDatabase.getReference("Agent_User_Token");
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
        FcmNotificationsSender fcmNotificationsSender=new FcmNotificationsSender(userToken,"Delivery Request","Hello Agent,\nDonor Name: "+donation.getDonor().getName()+"\nFood Amount: "+donation.getFoodAmount()+"\nFood Type: "+donation.getFoodType()+"\nLocation: "+donation.getLocation()+"\nIts time to delivery it.",DonationDetailsActivity.this,DonationDetailsActivity.this);
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