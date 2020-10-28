package com.example.vns_handheld004.Broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.example.vns_handheld004.Services.USBConnectionServices;

public class BootDeviceReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();

        String message = "BootDeviceReceiver onReceive, action is " + action;

        Toast.makeText(context, message, Toast.LENGTH_LONG).show();


        if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            //Start USBService
            Intent serviceIntent = new Intent(context, USBConnectionServices.class);
            serviceIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startService(serviceIntent);
        }
    }
}

