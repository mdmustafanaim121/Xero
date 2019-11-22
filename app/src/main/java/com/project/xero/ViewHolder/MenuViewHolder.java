package com.project.xero.ViewHolder;


import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.project.xero.Interface.ItemClickListener;
import com.project.xero.R;

import org.w3c.dom.Text;

public class MenuViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView txtView;
    public ImageView imgView;

    private ItemClickListener itemClickListener;
    public MenuViewHolder(View itemView)
    {
        super(itemView);

        txtView = (TextView)itemView.findViewById(R.id.menuName);
        imgView = (ImageView)itemView.findViewById(R.id.menuImg);

        itemView.setOnClickListener(this);
    }

    public void setItemClickListener(ItemClickListener itemClickListener){
        this.itemClickListener = itemClickListener;
    }

    @Override
    public void onClick(View view) {

        itemClickListener.onClick(view,getAdapterPosition(),false);

    }

}
