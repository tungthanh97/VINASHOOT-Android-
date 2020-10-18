package com.example.vns_handheld004.Adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.vns_handheld004.R;

import java.util.List;

public class TargetAdapter extends  RecyclerView.Adapter<TargetAdapter.TargetViewHolder>  {

    List<Bitmap> bmpTarget;
    Context context;
    OnTargetClickListener onTargetClickListener;
    /**
     * Constructor này dùng để khởi tạo các giá trị
     * từ CustomListViewActivity truyền vào
     *
     * @param context  : là Activity từ CustomListView
     * @param bmpTarget  : Danh sách target image của list item truyền từ Main
     */
    public TargetAdapter(Context context, List<Bitmap> bmpTarget, OnTargetClickListener onTargetClickListener) {
        this.context = context;
        this.bmpTarget = bmpTarget;
        this.onTargetClickListener = onTargetClickListener;
    }

    @Override
    public TargetViewHolder onCreateViewHolder( ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_target_list,parent,false);
        return new TargetViewHolder(view,onTargetClickListener);
    }

    @Override
    public void onBindViewHolder(TargetViewHolder holder, int position) {
        Glide.with(context)
                .asBitmap()
                .load(bmpTarget.get(position))
                .into(holder.imageTarget);
    }
    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return bmpTarget.size();
    }


    public class TargetViewHolder extends  RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView imageTarget;
        OnTargetClickListener onTargetClickListener;
        public TargetViewHolder(View itemView,OnTargetClickListener onTargetClickListener) {
            super(itemView);
            imageTarget = itemView.findViewById(R.id.ivTarget);
            this.onTargetClickListener = onTargetClickListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int pos = getAdapterPosition();
            Log.e("Target List","on Click"+pos);
            if (pos== RecyclerView.NO_POSITION) return;
            onTargetClickListener.onTargetClick(pos);
        }
    }
    public interface OnTargetClickListener{
        void onTargetClick(int pos);
    }
}
