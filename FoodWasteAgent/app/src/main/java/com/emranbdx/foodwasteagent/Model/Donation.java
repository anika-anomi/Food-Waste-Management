package com.emranbdx.foodwasteagent.Model;

import java.io.Serializable;
import java.util.Date;

public class Donation implements Serializable {
    private Donor donor;
    private String foodAmount;
    private String foodType;
    private String location;
    private Date donationDate;
    private String status;
    private Date deliveryDate;
    private String donationId;

    public Donation() {
    }

    public Donation(Donor donor, String foodAmount, String foodType, String location, Date donationDate, String donationId) {
        this.donor = donor;
        this.foodAmount = foodAmount;
        this.foodType = foodType;
        this.location = location;
        this.donationDate = donationDate;
        this.donationId = donationId;
    }

    public String getDonationId() {
        return donationId;
    }

    public Donor getDonor() {
        return donor;
    }

    public void setDonor(Donor donor) {
        this.donor = donor;
    }

    public String getFoodAmount() {
        return foodAmount;
    }

    public void setFoodAmount(String foodAmount) {
        this.foodAmount = foodAmount;
    }

    public String getFoodType() {
        return foodType;
    }

    public void setFoodType(String foodType) {
        this.foodType = foodType;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Date getDonationDate() {
        return donationDate;
    }

    public void setDonationDate(Date donationDate) {
        this.donationDate = donationDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(Date deliveryDate) {
        this.deliveryDate = deliveryDate;
    }
}
