package com.example.vns_handheld004;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.res.ResourcesCompat;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.vns_handheld004.Broadcast.ArduinoMessageReceiver;
import com.example.vns_handheld004.Broadcast.MyBroadcastListener;
import com.example.vns_handheld004.Model.Shoot_result;
import com.example.vns_handheld004.Services.USBConnectionServices;

import org.w3c.dom.Text;

public class SettingActivity extends AppCompatActivity implements MyBroadcastListener, View.OnClickListener {
    //Declaration
    ArduinoMessageReceiver arduinoMessageReceiver;
    private static final String STATE = "Setting";
    private boolean idUSBConnectionService = false, isTouched = false;;
    private USBConnectionServices usbConnectionServices;
    private SwitchCompat swUSB;
    private Button btZIG,btLAN,btRS485;
    private TextView tvid,tvZIG,tvLAN,tvRS485;
    //OnCreate
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(STATE,"onCreate");
        setContentView(R.layout.activity_setting_activities);
        initView();
    }
    //Initialize View
    protected void initView(){
        swUSB= findViewById(R.id.sw_usb);
        swUSB.setOnClickListener(this);
        btZIG=  findViewById(R.id.bt_zig);
        btZIG.setOnClickListener(this);
        btLAN=  findViewById(R.id.bt_lan);
        btLAN.setOnClickListener(this);
        btRS485= findViewById(R.id.bt_rs485);
        btRS485.setOnClickListener(this);
        tvid = findViewById(R.id.app_id);
        tvZIG = findViewById(R.id.tvZIGstt);
        tvLAN = findViewById(R.id.tvLANstt);
        tvRS485 = findViewById(R.id.tvRSstt);
        initSwitch();
    }
    private void initSwitch(){
        swUSB.setOnTouchListener((view, motionEvent) -> {
            isTouched = true;
            return false;
        });

        swUSB.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isTouched) {
                isTouched = false;
                if (isChecked) {
                    usbConnectionServices.openUSBconnection();
                }
                else {
                    usbConnectionServices.closeUSBconnection();
                }
            }
        });
    }
    //OnClick Event
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_zig:{
                usbConnectionServices.checkZIGconnection();
                checking(tvZIG);
                break;
            }
            case R.id.bt_lan:{
                usbConnectionServices.checkLANconnection();
                checking(tvLAN);
                break;
            }
            case R.id.bt_rs485:{
                usbConnectionServices.checkRS485connection();
                checking(tvRS485);
                break;
            }
        }
    }
    private void checking(TextView textView){
        textView.setText("Checking...");
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                String text = textView.getText().toString();
                if (text.equals("Checking...")) textView.setText("Disconnected");
            }
        }, 3000);
    }
    //On Resume
    @Override
    protected void onResume() {
        super.onResume();
        Log.e(STATE,"onResume");
        Intent intent = new Intent(SettingActivity.this, USBConnectionServices.class);
        bindService(intent,serviceConnection2,BIND_AUTO_CREATE);
        initArduinoMessagereceiver();
    }
    //Pause Status
    @Override
    protected void onPause() {
        super.onPause();
        if (idUSBConnectionService)
            unbindService(serviceConnection2);
        unregisterReceiver(arduinoMessageReceiver);
    }
    ServiceConnection serviceConnection2 = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder iBinder) {
            Log.e(STATE,"onServiceConnected");
            idUSBConnectionService = true;
            USBConnectionServices.CallService bider =(USBConnectionServices.CallService) iBinder;
            usbConnectionServices = bider.getService();
            if (usbConnectionServices.getArduinoStatus())
                swUSB.setChecked(true);
            else
                swUSB.setChecked(false);
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            idUSBConnectionService = false;
            Toast.makeText(SettingActivity.this, "USB Service has stopped. Please restart app", Toast.LENGTH_SHORT).show();
        }
    };
    //Init Broadcast Receiver
    private void initArduinoMessagereceiver(){   // initialize Broadcast receiver
        arduinoMessageReceiver = new ArduinoMessageReceiver(this);
        IntentFilter Connect_update = new IntentFilter();
        Connect_update.addAction("USBdisconnected");
        Connect_update.addAction("USBconnected");
        Connect_update.addAction("ZIGconnected");
        Connect_update.addAction("LANconnected");
        Connect_update.addAction("RS485connected");
        registerReceiver(arduinoMessageReceiver,Connect_update);
    }
    //<--------------------------Handle command from PC------------------------>
    @Override
    public void showTarget(String value, String time) { //Show target frame
    }

    @Override
    public void shootResults(Shoot_result shoot_result) {

    }

    @Override
    public void show_temper(int T, int L, String ID) {
        String title = "VINASHOOT HANDHELD ID:" + ID;
        tvid.setText(title);
    }

    @Override
    public void connectStatus(String result) {
        switch (result) {
            case "USBconnected": {
                swUSB.setChecked(true);
                break;
            }
            case "USBdisconnected": {
                swUSB.setChecked(false);
                break;
            }
            case "ZIGconnected":{
                connectedStatus(tvZIG);
                break;
            }
            case "LANconnected":{
                connectedStatus(tvLAN);
                break;
            }
            case "RS485connected":{
                connectedStatus(tvRS485);
                break;
            }
        }
    }
    public void connectedStatus(TextView textView){
        textView.setText("Connected");
    }
}