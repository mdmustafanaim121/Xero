package com.project.xero.ViewHolder;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.project.xero.Interface.ItemClickListener;
import com.project.xero.Model.Food;
import com.project.xero.R;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by Rajat Sangrame on 9/11/19.
 * http://github.com/rajatsangrame
 */
public class RecommendationAdapter extends RecyclerView.Adapter<FoodViewHolder> {

    private List<Food> mFoodList;
    private ItemClickListener mListener;
    private Context mContext;

    public RecommendationAdapter(Context context, List<Food> foodList, ItemClickListener listener) {
        mContext = context;
        mFoodList = foodList;
        mListener = listener;
    }

    @NonNull
    @Override
    public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.food_item, null);
        return new FoodViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodViewHolder viewHolder, int position) {

        Food item = mFoodList.get(position);
        viewHolder.foodName.setText(item.getName());

        viewHolder.foodPopularity.setVisibility(View.VISIBLE);
        String popularity = "Popularity : " + item.getPopularity();
        viewHolder.foodPopularity.setText(popularity);

        Picasso.with(mContext).load(item.getImage())
                .into(viewHolder.foodImage);

        viewHolder.setItemClickListener(mListener);
    }

    @Override
    public int getItemCount() {

        if (mFoodList == null || mFoodList.isEmpty()) {
            return 0;
        }
        return mFoodList.size();

    }
}
