package com.emranbdx.foodwasteagent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import com.emranbdx.foodwasteagent.Activity.DonationDetailsActivity;
import com.emranbdx.foodwasteagent.Adapter.DonationListAdapter;
import com.emranbdx.foodwasteagent.Model.Donation;
import com.emranbdx.foodwasteagent.Utils.Config;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private List<Donation> donationList;
    private FirebaseDatabase firebaseDatabase;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        progressDialog=new ProgressDialog(this);
        firebaseDatabase=FirebaseDatabase.getInstance(new Config().getDbUrl());
        donationList=new ArrayList<Donation>();
        recyclerView=findViewById(R.id.historyRecyclerViewId);
        layoutManager=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        getHistory("Approved");
        SharedPreferences sharedPreferences=getSharedPreferences("UserId",MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        String userUniqueId=sharedPreferences.getString("id","");
        if (userUniqueId.length()==0){
            getUserUniqueId(editor);
        }
    }
    private void getHistory(String status) {
        progressDialog.setTitle("Pls Wait...");
        progressDialog.setMessage("Getting "+status+" History.");
        progressDialog.show();
        DatabaseReference databaseReference=firebaseDatabase.getReference("Donation");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                donationList.clear();
                for (DataSnapshot dataSnapshot:snapshot.getChildren()){
                    Donation donation=dataSnapshot.getValue(Donation.class);
                    if (donation.getStatus().contentEquals(status)){
                        donationList.add(donation);
                    }
                }
                createMainPage();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
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
        DonationListAdapter donationListAdapter=new DonationListAdapter(MainActivity.this,donationList);
        recyclerView.setAdapter(donationListAdapter);
        donationListAdapter.setOnItemClickListener(new DonationListAdapter.OnItemClickListener() {
            @Override
            public void onClick(int position) {
                Intent intent=new Intent(MainActivity.this, DonationDetailsActivity.class);
                Bundle bundle=new Bundle();
                bundle.putSerializable("DonationClass",donationList.get(position));
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
        progressDialog.cancel();
    }
    private void getUserUniqueId(SharedPreferences.Editor editor) {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                if (task.isSuccessful()){
                    String userToken=task.getResult();
                    editor.putString("id",userToken);
                    editor.apply();
                    DatabaseReference databaseReference=firebaseDatabase.getReference("Agent_User_Token").child(userToken);
                    databaseReference.setValue(userToken);
                }
            }
        });

    }

}