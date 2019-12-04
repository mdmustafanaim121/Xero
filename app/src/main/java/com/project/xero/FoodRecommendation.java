package com.project.xero;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.project.xero.Common.Common;
import com.project.xero.Interface.ItemClickListener;
import com.project.xero.Model.Food;
import com.project.xero.Model.Rating;
import com.project.xero.ViewHolder.RecommendationAdapter;
import com.project.xero.util.Helper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class FoodRecommendation extends AppCompatActivity {

    private static final String TAG = "FoodRecommendation";
    private RecyclerView mRecyclerView;
    private DatabaseReference mFoodDataBase;
    private DatabaseReference mUserDataBase;
    private DatabaseReference mRatingDataBase;
    private List<Food> mFoodList = new ArrayList<>();
    private List<Food> mSearchList = new ArrayList<>();
    private String categoryId = "";
    private RecommendationAdapter mAdapter;
    private List<String> mSuggestList = new ArrayList<>();
    private MaterialSearchBar sMaterialSearchBar;
    private boolean mShowPopularity = true;

    private ItemClickListener mListener = new ItemClickListener() {
        @Override
        public void onClick(View view, int position, boolean isLongClick) {

            Intent foodDetail = new Intent(FoodRecommendation.this, FoodDetails.class);
            foodDetail.putExtra("FoodId", mFoodList.get(position).getFoodId());
            Log.i(TAG, "onClick: " + mFoodList.get(position).getName());
            startActivity(foodDetail);

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_recomendation);

        mFoodDataBase = FirebaseDatabase.getInstance().getReference("Food");
        mUserDataBase = FirebaseDatabase.getInstance().getReference("user");
        mRatingDataBase = FirebaseDatabase.getInstance().getReference("Rating");

        mRecyclerView = findViewById(R.id.recycler_food);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        hasUserRatedFood();

        //region Search
        sMaterialSearchBar = findViewById(R.id.searchBar);
        sMaterialSearchBar.setHint("Enter your food:");
        sMaterialSearchBar.setSpeechMode(false);
        sMaterialSearchBar.setLastSuggestions(mSuggestList);
        sMaterialSearchBar.setCardViewElevation(10);
        sMaterialSearchBar.addTextChangeListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //When user starts typing they get suggestions

                List<String> suggest = new ArrayList<String>();
                for (String search : mSuggestList) {
                    if (search.toLowerCase().contains(sMaterialSearchBar.getText().toLowerCase()))
                        suggest.add(search);

                }
                sMaterialSearchBar.setLastSuggestions(suggest);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        sMaterialSearchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled) {
                //Restore Original Adapter
                if (!enabled) {
                    mAdapter = new RecommendationAdapter(FoodRecommendation.this, mFoodList,
                            mListener);
                    mRecyclerView.setAdapter(mAdapter);
                }
            }

            @Override
            public void onSearchConfirmed(CharSequence text) {
                //When Search finish
                //show result of adapter
                startSearch(text);

            }

            @Override
            public void onButtonClicked(int buttonCode) {

            }
        });
        //endregion
    }

    private void startSearch(CharSequence text) {

        mSearchList = new ArrayList<>();
        mFoodDataBase.orderByChild("Name").equalTo(text.toString())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                            Food item = postSnapshot.getValue(Food.class);
                            assert item != null;
                            mSearchList.add(item);
                        }

                        updateAdapter(mSearchList);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }

    /*
     * Ref:
     * https://stackoverflow.com/questions/53308643/firebaserecycleradapter-sort-compare-reorder-data-before-populate#comment93498815_53308643
     *
     * Need custom Adapter
     * */
    private void loadPopularityData() {

        if (getIntent() != null)
            categoryId = getIntent().getStringExtra("CategoryId");
        if (categoryId != null && !categoryId.isEmpty()) {
            if (!Common.isConnectedToInternet(getBaseContext())) {

                Toast.makeText(this, "Please, check your internet connection",
                        Toast.LENGTH_SHORT).show();
                return;
            }
        }

        mFoodDataBase.orderByChild("MenuId").equalTo(categoryId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                            Food item = postSnapshot.getValue(Food.class);
                            assert item != null;
                            mSuggestList.add(item.getName());
                            mFoodList.add(item);
                        }

                        /*
                         * Note : update gradle MIN_VERSION to KITKAT or find alternative for sorting
                         */
                        Collections.sort(mFoodList, new Comparator<Food>() {
                            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                            @Override
                            public int compare(Food o1, Food o2) {
                                try {
                                    return Integer.compare(o2.getPopularity(), o1.getPopularity());

                                } catch (NullPointerException | NumberFormatException e) {
                                    return 0;
                                }
                            }
                        });
                        updateAdapter(mFoodList);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }

    private void updateAdapter(List<Food> foodList) {

        mAdapter = new RecommendationAdapter(FoodRecommendation.this,
                foodList, mListener);
        mRecyclerView.setAdapter(mAdapter);
    }

    //region Collaborative Filtering
    private void hasUserRatedFood() {

        mRatingDataBase.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.hasChild(Common.curUser.getPhone())) {

                            mShowPopularity = false;
                            mode.onResult(false, true);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                        mShowPopularity = true;
                        mode.onResult(true, false);
                    }
                });
    }

    private ActivityMode mode = new ActivityMode() {
        @Override
        public void onResult(boolean showPopularity, boolean showRecommendation) {

            initUi(showRecommendation);

        }
    };

    private void initUi(boolean showRecommendation) {

        findViewById(R.id.progress_bar).setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.VISIBLE);

        if (showRecommendation) {

            findViewById(R.id.textView).setVisibility(View.VISIBLE);
            loadRecommendationData();

        } else {

            sMaterialSearchBar.setVisibility(View.VISIBLE);
            loadPopularityData();
        }
    }

    private void loadRecommendationData() {


        mRatingDataBase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                List<Rating> ratingList = new ArrayList<>();

                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {

                    for (DataSnapshot itr : postSnapshot.getChildren()) {

                        Rating rating = itr.getValue(Rating.class);
                        ratingList.add(rating);
                    }

                    Log.i(TAG, "onDataChange: " + ratingList.size());

                }

                Helper.getFoodRecommendation(Common.curUser, ratingList);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    interface ActivityMode {
        void onResult(boolean showPopularity, boolean showRecommendation);
    }
    //endregion
}
