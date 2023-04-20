package com.emranbdx.foodwastedonor.Adapter;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.emranbdx.foodwastedonor.Model.Donation;
import com.emranbdx.foodwastedonor.R;

import java.text.SimpleDateFormat;
import java.util.List;

public class DonationListAdapter extends RecyclerView.Adapter<DonationListAdapter.DonationListAdapterHolder>{
    private Context context;
    private List<Donation> donationList;

    public DonationListAdapter(Context context, List<Donation> donationList) {
        this.context = context;
        this.donationList = donationList;
    }

    @NonNull
    @Override
    public DonationListAdapterHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.donation_list_item, parent, false);
        return new DonationListAdapter.DonationListAdapterHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull DonationListAdapterHolder holder, int position) {
        Donation donation=donationList.get(position);
        holder.foodAmountTV.setText("Food Amount: "+donation.getFoodAmount());
        holder.foodTypeTV.setText("Food Type: "+donation.getFoodType());
        holder.dateTV.setText("Donation Time: "+new SimpleDateFormat("hh:mm:ss aa dd-MM-yyyy").format(donation.getDonationDate()));
        holder.statusTV.setText(donation.getStatus());
    }

    @Override
    public int getItemCount() {
        return donationList.size();
    }

    public class DonationListAdapterHolder extends RecyclerView.ViewHolder{
        private TextView foodTypeTV,foodAmountTV,dateTV,statusTV;
        public DonationListAdapterHolder(@NonNull View itemView) {
            super(itemView);
            foodAmountTV=itemView.findViewById(R.id.foodAmountTVId);
            foodTypeTV=itemView.findViewById(R.id.foodTypeTVId);
            dateTV=itemView.findViewById(R.id.dateTVId);
            statusTV=itemView.findViewById(R.id.statusId);
        }
    }
}
