package com.example.vns_handheld004.Adapter;


import android.content.Context;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.vns_handheld004.R;
import com.example.vns_handheld004.Model.Shoot_result;

import java.util.List;

public class ShootAdapter extends RecyclerView.Adapter<ShootAdapter.ShootViewHolder>
{
    private int selectedPos = RecyclerView.NO_POSITION;
    private static final int TYPE_ROW = 0;
    private static final int TYPE_ROW_COLORFUL = 1;
    private OnTableViewListener mOnTableViewListener;
    private List<Shoot_result > shootList;
    private List<Shoot_result > filteredShootList;
    private Context context;
    public ShootAdapter(Context context, List<Shoot_result > shootList, OnTableViewListener onTableViewListener)
    {
        this.context = context;
        this.shootList = shootList;
        this.filteredShootList = shootList;
        this.mOnTableViewListener = onTableViewListener;
    }

    @Override
    public int getItemViewType(int position)
    {
        if (position % 2 == 0)
        {
            return TYPE_ROW_COLORFUL;
        }

        return TYPE_ROW;
    }

    @Override
    public ShootViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType)
    {
        if (viewType == TYPE_ROW)
        {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_shoot_odd, viewGroup, false);
            return new ShootViewHolder(view,mOnTableViewListener);
        } else
        {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_shoot_even,
                    viewGroup, false);
            return new ShootViewHolder(view,mOnTableViewListener);
        }
    }
    

    @Override
    public void onBindViewHolder(ShootViewHolder holder, int position)
    {
        holder.itemView.setSelected(selectedPos == position);
        Shoot_result Shoot_result = filteredShootList.get(position);
        holder.txtNo.setText(Shoot_result.No);
        holder.txtXaxis.setText(Shoot_result.X);
        holder.txtYaxis.setText(Shoot_result.Y);
        holder.txtV.setText(Shoot_result.V);
        holder.txtMark.setText(Shoot_result.Mark);
        holder.txtM_H.setText(Shoot_result.M_H);
        holder.txtTime.setText(Shoot_result.Time);
    }

    @Override
    public int getItemCount()
    {
        return filteredShootList.size();
    }


    public class ShootViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        public TextView txtNo, txtXaxis, txtYaxis, txtV, txtMark, txtM_H, txtTime;
        OnTableViewListener onTableViewListener;
        public ShootViewHolder(View view, OnTableViewListener onTableViewListener)
        {
            super(view);
            txtNo = view.findViewById(R.id.txtNo);
            txtXaxis = view.findViewById(R.id.txt_axisX);
            txtYaxis = view.findViewById(R.id.txt_axisY);
            txtV = view.findViewById(R.id.txt_V);
            txtMark = view.findViewById(R.id.txt_Mark);
            txtM_H = view.findViewById(R.id.txt_M_H);
            txtTime = view.findViewById(R.id.txt_Time);
            this.onTableViewListener = onTableViewListener;
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (getAdapterPosition() == RecyclerView.NO_POSITION) return;
            notifyItemChanged(selectedPos);
            selectedPos = getLayoutPosition();
            notifyItemChanged(selectedPos);
            onTableViewListener.onTableClick(getAdapterPosition());
        }
    }
    public interface OnTableViewListener{
        void onTableClick(int pos);
    }
}
