package com.project.xero.ViewHolder;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.project.xero.Interface.ItemClickListener;
import com.project.xero.R;

public class FoodViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView foodName;
    public TextView foodPopularity;
    public ImageView foodImage;

    private ItemClickListener itemClickListener;

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public FoodViewHolder(@NonNull View itemView) {
        super(itemView);

        foodName = (TextView) itemView.findViewById(R.id.foodName);
        foodImage = (ImageView) itemView.findViewById(R.id.foodImg);
        foodPopularity = (TextView) itemView.findViewById(R.id.foodPopularity);
        itemView.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        itemClickListener.onClick(view, getAdapterPosition(), false);

    }
}
