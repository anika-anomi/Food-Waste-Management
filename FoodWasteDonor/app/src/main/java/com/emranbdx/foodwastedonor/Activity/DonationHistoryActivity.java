package com.emranbdx.foodwastedonor.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.emranbdx.foodwastedonor.Adapter.DonationListAdapter;
import com.emranbdx.foodwastedonor.Model.Donation;
import com.emranbdx.foodwastedonor.R;
import com.emranbdx.foodwastedonor.Utils.Config;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class DonationHistoryActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private List<Donation> donationList;
    private FirebaseDatabase firebaseDatabase;
    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donation_history_actvity);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("My Donation");
        firebaseAuth=FirebaseAuth.getInstance();
        progressDialog=new ProgressDialog(this);
        firebaseDatabase=FirebaseDatabase.getInstance(new Config().getDbUrl());
        donationList=new ArrayList<Donation>();
        recyclerView=findViewById(R.id.historyRecyclerViewId);
        layoutManager=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        getHistory();
    }

    private void getHistory() {
        progressDialog.setTitle("Pls Wait...");
        progressDialog.setMessage("Getting your donation history.");
        progressDialog.show();
        DatabaseReference databaseReference=firebaseDatabase.getReference("Donation");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                donationList.clear();
                for (DataSnapshot dataSnapshot:snapshot.getChildren()){
                    Donation donation=dataSnapshot.getValue(Donation.class);
                    if (donation.getDonor().getId().contentEquals(firebaseAuth.getCurrentUser().getUid())){
                        donationList.add(donation);
                    }
                }
                createMainPage();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DonationHistoryActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                progressDialog.cancel();
            }
        });
    }

    private void createMainPage() {
        Collections.sort(donationList, new Comparator<Donation>() {
            public int compare(Donation o1, Donation o2) {
                Date date1=new Date();
                Date date2=new Date();
                date1=o1.getDonationDate();
                date2=o2.getDonationDate();
                return date2.compareTo(date1);
            }
        });
        DonationListAdapter donationListAdapter=new DonationListAdapter(DonationHistoryActivity.this,donationList);
        recyclerView.setAdapter(donationListAdapter);
        progressDialog.cancel();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}