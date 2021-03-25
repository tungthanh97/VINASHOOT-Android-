package com.example.vns_handheld004.Adapter;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import androidx.annotation.NonNull;

import com.example.vns_handheld004.R;

import java.util.ArrayList;

public class DeviceListAdapter extends ArrayAdapter<BluetoothDevice> {

    private LayoutInflater mLayoutInflater;
    private ArrayList<BluetoothDevice> mDevices;
    private int mViewResourceId;
    public DeviceListAdapter(@NonNull Context context, int resourceID, ArrayList<BluetoothDevice> devices) {
        super(context, resourceID,devices);
        this.mDevices = devices;
        mLayoutInflater = LayoutInflater.from(context);
        mViewResourceId = resourceID;
    }
    static class ViewHolder{ TextView deviceName; TextView deviceAddress;}
    public View getView(int pos, View convertView, ViewGroup parent)
    {
        ViewHolder viewHolder = new ViewHolder();
        if(convertView == null)
        {
            convertView = mLayoutInflater.inflate(mViewResourceId,null);
            viewHolder.deviceName = (TextView) convertView.findViewById(R.id.tvDeviceName);
            viewHolder.deviceAddress = (TextView) convertView.findViewById(R.id.tvDeviceAddress);
            convertView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        BluetoothDevice device = mDevices.get(pos);
        if (device != null)
        {
            viewHolder.deviceName.setText(device.getName());
            viewHolder.deviceAddress.setText(device.getAddress());
        }
      return convertView;
    }
}
