package com.project.xero.Model;

public class Rating {
    private String userPhone;
    private String foodId;
    private double rateValue;
    private String comment;

    public Rating() {
    }

    public Rating(String userPhone, String foodId, double rateValue, String comment) {
        this.userPhone = userPhone;
        this.foodId = foodId;
        this.rateValue = rateValue;
        this.comment = comment;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    public String getFoodId() {
        return foodId;
    }

    public void setFoodId(String foodId) {
        this.foodId = foodId;
    }

    public double getRateValue() {
        return rateValue;
    }

    public void setRateValue(double rateValue) {
        this.rateValue = rateValue;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}


