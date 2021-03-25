package com.example.vns_handheld004;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.example.vns_handheld004.Adapter.DeviceListAdapter;
import com.example.vns_handheld004.Util.BluetoothConnectionService;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class BluetoothDialog extends AppCompatDialogFragment  {
    private View view;
    private ListView lvBTDevice;
    private TextView tvEmpty;
    BluetoothAdapter mBluetoothAdapter;
    BluetoothDevice mBTDevice;
    SettingActivity settingActivity;
    private final String prefix_name = "HD";
    private final String TAG = "BluetoothDialog";
    public DeviceListAdapter mDeviceListAdapter;
    public ArrayList<BluetoothDevice> mBTDevices = new ArrayList<>();
    public ArrayList<BluetoothDevice> mPairedDevices = new ArrayList<>();
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        settingActivity = (SettingActivity) getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(settingActivity);
        LayoutInflater inflater =settingActivity.getLayoutInflater();
        view = inflater.inflate(R.layout.dialog_bluetooth,null);
        builder.setView(view)
                .setTitle("Bluetooth")
                .setNegativeButton("Done", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("Scan", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        settingActivity.registerReceiver(mBondBroadcastReceiver,filter);
        initView();
        discoverDevices();
        getPairedDevices();
        return builder.create();
    }
    @Override
    public void onResume()
    {
        //*Change positive handler
        super.onResume();
        final AlertDialog d = (AlertDialog)getDialog();
        if(d != null)
        {
            Button positiveButton = (Button) d.getButton(Dialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    discoverDevices();
                }
            });
        }
    }
    //Init View
    private void initView(){
        tvEmpty = view.findViewById(R.id.emptyElement);
        lvBTDevice = view.findViewById(R.id.lvBTDevice);
        lvBTDevice.setOnItemClickListener((parent, view, position, id) -> { //list View Available devices click listener
            mBluetoothAdapter.cancelDiscovery();
            mBTDevice = mBTDevices.get(position);
            pairBT();
        });
        lvBTDevice.setEmptyView(tvEmpty);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }
    private void pairBT()
    {
        //*Pair BT devices nearby
        try {
            for (BluetoothDevice dv:mPairedDevices)
                if (dv.getName().equals(mBTDevice.getName())){
                    settingActivity.connectBT(mBTDevice);
                    return;
                }
            Boolean isBonded = createBond(mBTDevice);
            Log.e("Bond",isBonded.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean createBond(BluetoothDevice btDevice)
            throws Exception {
        Class class1 = Class.forName("android.bluetooth.BluetoothDevice");
        Method createBondMethod = class1.getMethod("createBond");
        Boolean returnValue = (Boolean) createBondMethod.invoke(btDevice);
        return returnValue.booleanValue();
    }
    public void discoverDevices()
    {
        mBTDevices.clear();
        if (mDeviceListAdapter  != null)
        {
            mDeviceListAdapter.clear();
            mDeviceListAdapter.notifyDataSetChanged();
        }
        if (mBluetoothAdapter.isDiscovering()){
            mBluetoothAdapter.cancelDiscovery();
        }
        IntentFilter discoverIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        settingActivity.registerReceiver(mBroadcastReceiver,discoverIntent);
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        settingActivity.registerReceiver(mBroadcastReceiver, filter);
        checkBTPermission();
        mBluetoothAdapter.startDiscovery();
    }
    //Broadcast Receiver tim kiem Device
    BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) { //Ket noi
            final String action = intent.getAction();
            if (action.equals(BluetoothDevice.ACTION_FOUND)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getName()== null || device.getName().isEmpty()) return;
                if (!device.getName().contains(prefix_name)) return;
                for (BluetoothDevice dv : mBTDevices ) if (dv.getAddress().equals(device.getAddress())) return;
                //for (BluetoothDevice dv : mPairedDevices) if (dv.getAddress().equals(device.getAddress())) return;
                mBTDevices.add(device);
                Log.e("Main",device.getName());
                mDeviceListAdapter = new DeviceListAdapter(settingActivity,R.layout.device_adapter_view,mBTDevices);
                lvBTDevice.setAdapter(mDeviceListAdapter);
            }
            if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)){
            }
        }
    };
    BroadcastReceiver mBondBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) { //Kiem tra ket noi
            final String action = intent.getAction();
            try {
                if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                        /*                    showToast("DEVICE CONNECTED");*/
                        //mBTDevice.getClass().getMethod("setPairingConfirmation", boolean.class).invoke(mBTDevice, true);
                        settingActivity.connectBT(mBTDevice);
                        getPairedDevices();
                    }
                    if (device.getBondState() == BluetoothDevice.BOND_NONE) {
                        Toast.makeText(settingActivity,"DEVICE NOT CONNECTED",Toast.LENGTH_SHORT).show();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
    @SuppressLint("NewApi")
    private  void checkBTPermission()
    {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP){
            int permissionCheck = settingActivity.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
            permissionCheck += settingActivity.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
            if (permissionCheck != 0){
                this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},1001);
            }
        }
    }
    private void getPairedDevices() {
        Set<BluetoothDevice> pairedDevice =
                mBluetoothAdapter.getBondedDevices();
        mPairedDevices.clear();
        if (pairedDevice.size() > 0) {
            for (BluetoothDevice device : pairedDevice) {
                mPairedDevices.add(device);
            }
        }
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mBluetoothAdapter.isDiscovering()){
            mBluetoothAdapter.cancelDiscovery();
        }
        settingActivity.unregisterReceiver(mBroadcastReceiver);
        settingActivity.unregisterReceiver(mBondBroadcastReceiver);
    }
}
