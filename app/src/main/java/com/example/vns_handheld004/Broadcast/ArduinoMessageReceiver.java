package com.example.vns_handheld004.Broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.example.vns_handheld004.Model.Shoot_result;

import java.util.Objects;

public class ArduinoMessageReceiver extends BroadcastReceiver {
    private MyBroadcastListener listener;
    public ArduinoMessageReceiver(MyBroadcastListener listener){
        this.listener= listener;
    }
    @Override
    public void onReceive(Context context, Intent intent) {
//        Log.e("Listener","receive message "+ intent.getAction());
        switch (Objects.requireNonNull(intent.getAction())) {
            case "Load_target": //cmd 002
                String target_code = intent.getStringExtra("target code");
                String Countdown = intent.getStringExtra("time");
                listener.showTarget(target_code, Countdown);
                break;
            case "Shooting_result": //cmd 003
                String[] result= intent.getStringArrayExtra("Results");
                Shoot_result shoot_result = new Shoot_result(result);
                listener.shootResults(shoot_result);
                break;
            case "Show_temper": //cmd 05
                int T = intent.getIntExtra("Temperature",0);
                listener.show_temper(T);
                break;
            case "Show_lane": //cmd 04
                int L = intent.getIntExtra("Lane",0);
                listener.show_lane(L);
                break;
            case "USBdisconnected":
                listener.connectStatus("USBdisconnected");
//            Toast.makeText(context, "USB disconnected", Toast.LENGTH_SHORT).show();
                break;
            case "USBconnected":
                listener.connectStatus("USBconnected");
//            Toast.makeText(context, "USB connected", Toast.LENGTH_SHORT).show();
                break;
            case "ZIGconnected":
                listener.connectStatus("ZIGconnected");
                Log.e("ZIG","connected");
//            Toast.makeText(context, "USB connected", Toast.LENGTH_SHORT).show();
                break;
            case "LANconnected":
                listener.connectStatus("LANconnected");
//            Toast.makeText(context, "USB connected", Toast.LENGTH_SHORT).show();
                break;
            case "RS485connected":
                listener.connectStatus("RS485connected");
//            Toast.makeText(context, "USB connected", Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
