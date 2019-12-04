package com.project.xero.Model;

import androidx.annotation.Nullable;

public class Food {

    private String Name;
    private String Image;
    private String Description;
    private String Price;
    private String Discount;
    private String MenuId;
    private String FoodId;
    private int Popularity;

    public Food(){

    }
    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getImage() {
        return Image;
    }

    public void setImage(String image) {
        Image = image;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public String getPrice() {
        return Price;
    }

    public void setPrice(String price) {
        Price = price;
    }

    public String getDiscount() {
        return Discount;
    }

    public void setDiscount(String discount) {
        Discount = discount;
    }

    public String getMenuId() {
        return MenuId;
    }

    public void setMenuId(String menuId) {
        MenuId = menuId;
    }

    public String getFoodId() {
        return FoodId;
    }

    public void setFoodId(String foodId) {
        this.FoodId = foodId;
    }

    public int getPopularity() {
        return Popularity;
    }

    public void setPopularity(int popularity) {
        this.Popularity = popularity;
    }

    public Food(String name, String image, String description, String price, String discount, String menuId) {
        Name = name;
        Image = image;
        Description = description;
        Price = price;
        Discount = discount;
        MenuId = menuId;
    }


    @Override
    public boolean equals(@Nullable Object obj) {
        Food food = (Food) obj;
        return this.getFoodId().equals(food != null ? food.getFoodId() : false);
    }

}
