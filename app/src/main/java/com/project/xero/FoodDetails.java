package com.project.xero;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.project.xero.Common.Common;
import com.project.xero.Database.Database;
import com.project.xero.Model.Food;
import com.project.xero.Model.Order;
import com.project.xero.Model.Rating;
import com.squareup.picasso.Picasso;
import com.stepstone.apprating.AppRatingDialog;
import com.stepstone.apprating.listener.RatingDialogListener;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;


public class FoodDetails extends AppCompatActivity implements RatingDialogListener {

    TextView foodName, foodPrice, foodDescription;
    ImageView foodImage;
    CollapsingToolbarLayout collapsingToolbarLayout;
    FloatingActionButton btnCart, btnRating;
    ElegantNumberButton numberButton;
    RatingBar ratingBar;

    String foodId = "";

    //Firebase Database
    FirebaseDatabase database;
    DatabaseReference foods;
    DatabaseReference ratingTb;

    Food currentFood;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_details);

        //Firebase
        database = FirebaseDatabase.getInstance();
        foods = database.getReference("Food");
        ratingTb = database.getReference("Rating");

        //Init view
        numberButton = findViewById(R.id.number_button);
        btnCart = findViewById(R.id.btn_Cart);
        btnRating = (FloatingActionButton) findViewById(R.id.btn_rating);
        ratingBar = (RatingBar) findViewById(R.id.ratingBar);

        btnRating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showRatingDialog();
            }
        });


        btnCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Database(getBaseContext()).addToCart(new Order(
                        foodId,
                        currentFood.getName(),
                        numberButton.getNumber(),
                        currentFood.getPrice(),
                        currentFood.getDiscount()
                ));
                Toast.makeText(FoodDetails.this, "Order Successfully added to the cart", Toast.LENGTH_SHORT).show();
            }
        });

        foodDescription = findViewById(R.id.food_description);
        foodName = findViewById(R.id.food_name);
        foodPrice = findViewById(R.id.food_price);
        foodImage = findViewById(R.id.food_image);

        collapsingToolbarLayout = findViewById(R.id.collapsing);
        collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.ExpandedAppbar);
        collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.CollapsedAppbar);

        //Get Food Id from Intent
        if (getIntent() != null) {
            foodId = getIntent().getStringExtra("FoodId");
        }
        if (!foodId.isEmpty()) {
            if (Common.isConnectedToInternet(getBaseContext())) {

                getDetailsFood(foodId);
                getRatingFood(foodId);
            } else {
                Toast.makeText(FoodDetails.this, "Please, check your internet connection", Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            Toast.makeText(this, "Error: foodId = " + foodId, Toast.LENGTH_SHORT).show();
        }
    }

    private void getRatingFood(String foodId) {
        com.google.firebase.database.Query foodRating = ratingTb.orderByChild("foodId").equalTo(foodId);

        foodRating.addValueEventListener(new ValueEventListener() {
            double count = 0, sum = 0;

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Rating item = postSnapshot.getValue(Rating.class);
                    assert item != null;
                    sum += item.getRateValue();
                    count++;
                }
                if (count != 0) {
                    double average = sum / count;
                    ratingBar.setRating((float) average);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void showRatingDialog() {
        new AppRatingDialog.Builder()
                .setPositiveButtonText("Submit")
                .setNegativeButtonText("Cancel")
                .setNoteDescriptions(Arrays.asList("Poor", "Average", "Good", "Very Good", "Excellent"))
                .setDefaultRating(1)
                .setTitle("Rate this food")
                .setDescription("Please give us your valuable ratings")
                .setTitleTextColor(R.color.colorPrimary)
                .setDescriptionTextColor(R.color.colorPrimary)
                .setHint("Please write your comment")
                .setHintTextColor(R.color.colorAccent)
                .setCommentTextColor(android.R.color.white)
                .setCommentBackgroundColor(R.color.colorPrimaryDark)
                .setWindowAnimation(R.style.RatingDialogFadeAnim)
                .create(FoodDetails.this)
                .show();
    }

    // getDetailsFood() method
    private void getDetailsFood(String foodId) {
        foods.child(foodId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                currentFood = dataSnapshot.getValue(Food.class);

                Picasso.with(getBaseContext()).load(currentFood.getImage()).into(foodImage);
                collapsingToolbarLayout.setTitle(currentFood.getName());

                foodPrice.setText(currentFood.getPrice());
                foodName.setText(currentFood.getName());
                foodDescription.setText(currentFood.getDescription());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onNegativeButtonClicked() {

    }

    @Override
    public void onPositiveButtonClicked(int i, @NotNull String s) {

        final Rating rating = new Rating(Common.curUser.getPhone(),
                foodId,
                (double) i,
                s);
        ratingTb.child(Common.curUser.getPhone()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.child(Common.curUser.getPhone()).hasChild(foodId)) {
                    //remove old value
                    ratingTb.child(Common.curUser.getPhone()).child(foodId + "").removeValue();

                    //Update new Value
                    ratingTb.child(Common.curUser.getPhone())
                            .child(foodId + "")
                            .setValue(rating);
                } else {
                    //Update new val
//                    ratingTb.child(Common.curUser.getPhone()).setValue(rating);
                    ratingTb.child(Common.curUser.getPhone())
                            .child(foodId + "")
                            .setValue(rating);
                }


                Toast.makeText(FoodDetails.this, "Thank you for your ratings", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
}

