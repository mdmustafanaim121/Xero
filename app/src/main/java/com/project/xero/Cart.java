package com.project.xero;

import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.project.xero.Common.Common;
import com.project.xero.Database.Database;
import com.project.xero.Model.Food;
import com.project.xero.Model.MyResponse;
import com.project.xero.Model.Notification;
import com.project.xero.Model.Order;
import com.project.xero.Model.Request;
import com.project.xero.Model.Sender;
import com.project.xero.Model.Token;
import com.project.xero.Remote.APIService;
import com.project.xero.ViewHolder.CartAdapter;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Cart extends AppCompatActivity {

    private static final String TAG = "Cart";
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    //Firebase Database
    FirebaseDatabase database;
    DatabaseReference requests;

    TextView textTotalPrice;
    Button btnPlace;

    List<Order> cart = new ArrayList<>();

    CartAdapter adapter;

    APIService mService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        //Init messaging service
        mService = Common.getFCMService();

        //Firebase
        database = FirebaseDatabase.getInstance();
        requests = database.getReference("Requests");

        //init
        recyclerView = findViewById(R.id.listCart);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        textTotalPrice = findViewById(R.id.total_price);
        btnPlace = findViewById(R.id.btn_place_order);

        btnPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (cart.size() > 0)
                    showAlertDialog();
                else
                    Toast.makeText(Cart.this, "Your Cart is empty", Toast.LENGTH_SHORT).show();
            }

        });

        loadListFood();

    }

    //Method showAlertDialog()
    private void showAlertDialog() {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(Cart.this);
        alertDialog.setTitle("One more step!");
        alertDialog.setMessage("Enter your address and comment any details for the restaurant: ");

        LayoutInflater inflater = this.getLayoutInflater();
        View order_address_comment = inflater.inflate(R.layout.order_address_comment, null);

        final MaterialEditText xAddress = (MaterialEditText) order_address_comment.findViewById(R.id.xAddress);
        final MaterialEditText xComment = (MaterialEditText) order_address_comment.findViewById(R.id.xComment);

        alertDialog.setIcon(R.drawable.ic_shopping_cart_black_24dp);

        alertDialog.setView(order_address_comment);
        alertDialog.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                //Create new Request
                Request request = new Request(
                        Common.curUser.getPhone(),
                        Common.curUser.getName(),
                        xAddress.getText().toString(),
                        textTotalPrice.getText().toString(),
                        xComment.getText().toString(),
                        "0",
                        cart
                );

                //Submit to Firebase
                //We will using System.CurrentMills to key
                String order_number = String.valueOf(System.currentTimeMillis());
                requests.child(order_number)
                        .setValue(request);

                //Delete cart
                new Database(getBaseContext()).cleanCart();

                sendNotificationOrder(order_number);
                // Toast.makeText(Cart.this, "Thank you, Order Place.", Toast.LENGTH_SHORT).show();
                //finish();

                updateRecommendation(cart);
            }
        });

        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        alertDialog.show();

    }

    private void sendNotificationOrder(final String order_number) {
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query data = tokens.orderByChild("isServerToken").equalTo(true);
        data.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Token serverToken = postSnapshot.getValue(Token.class);

                    //Raw payload to send
                    Notification not = new Notification("Xero", "You have a new order" + order_number);
                    Sender content = new Sender(serverToken.getToken(), not);

                    mService.sendNotification(content)
                            .enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                    if (response.code() == 200) {
                                        if (response.body().success == 1) {
                                            Toast.makeText(Cart.this, "Thank you, Your order has been placed", Toast.LENGTH_SHORT).show();
                                            finish();
                                        } else {
                                            Toast.makeText(Cart.this, "There was an error while placing your order", Toast.LENGTH_SHORT).show();

                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Call<MyResponse> call, Throwable t) {

                                    Log.d("ERROR", t.getMessage());
                                }
                            });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    //Method for load food
    private void loadListFood() {
        cart = new Database(this).getCarts();
        adapter = new CartAdapter(cart, this);
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);

        //Calculate total price
        int total = 0;
        for (Order order : cart) {
            total += (Integer.parseInt(order.getPrice())) * (Integer.parseInt(order.getQuantity()));
        }
        Locale locale = new Locale("en", "US");
        NumberFormat fmt = NumberFormat.getCurrencyInstance();
        textTotalPrice.setText(fmt.format(total));
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getTitle().equals(Common.DELETE))
            deleteCart(item.getOrder());
        return true;
    }

    private void deleteCart(int position) {
        cart.remove(position);
        new Database(this).cleanCart();
        for (Order item : cart)
            new Database(this).addToCart(item);
        //refresh
        loadListFood();
    }


    //region Update Food Recommendation
    private void updateRecommendation(final List<Order> orderCartList) {

        if (orderCartList == null || orderCartList.isEmpty()) {
            return;
        }

        final DatabaseReference foodReference = database.getReference("Food");

        foodReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Food foodModel = postSnapshot.getValue(Food.class);

                    assert foodModel != null;
                    Log.i(TAG, "onDataChange: " + foodModel.getPopularity());

                    for (Order order : orderCartList) {

                        if (foodModel.getFoodId().equals(order.getProductId())) {

                            int popularity = foodModel.getPopularity();
                            popularity = popularity + 1;


                            foodReference.child(foodModel.getFoodId()).child("Popularity")
                                    .setValue(popularity);

                            Log.i(TAG, "updateRecommendation Popularity : " + order.getProductName());


                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
    //endregion

}

