package com.example.vns_handheld004;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
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
    private String ID;
    private Button btZIG,btLAN,btRS485;
    private TextView tvid,tvZIG,tvLAN,tvRS485;
    ConstraintLayout layoutSetting;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    @Override           //OnCreate
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(STATE,"onCreate");
        setContentView(R.layout.activity_setting_activities);
        initView();
        initPreferences();
    }
    protected void initView(){
        layoutSetting = findViewById(R.id.layoutSetting);
        layoutSetting.setBackgroundColor(getResources().getColor(R.color.White));
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
        initIDlistener();
    }     //Initialize View
    private void initPreferences() {
        sharedPreferences = getSharedPreferences("IDs",MODE_PRIVATE);
        editor = sharedPreferences.edit();
        ID = sharedPreferences.getString("ID", "YA001");
        tvid.setText(ID);
    }     // init shared prefrence to save ID
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
    }     // setup USB switch
    private void initIDlistener(){
        tvid.setSelected(false);
        tvid.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        tvid.setSelected(true);
                        tvid.setPadding(9,0,0,9);  //click animation
                        openIDdialog(); // open ID dialog to change ID
                        return true;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        tvid.setSelected(false);
                        tvid.setPadding(0,0,0,0);
                        return true;
                }
                return false;
            }
        });
    }     //setup ID config dialog
    private void openIDdialog(){     // open ID dialog to change ID
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(SettingActivity.this);
        View mView = getLayoutInflater().inflate(R.layout.dialog_id,null);
        final EditText etID = mView.findViewById(R.id.etID);
        etID.requestFocus();
        etID.setText(ID);
        etID.setSelection(5);
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
        mBuilder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        mBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                dialog.dismiss();
            }
        });
        mBuilder.setView(mView);
        AlertDialog dialog = mBuilder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String new_ID = etID.getText().toString();
                if (new_ID.length() < 5) {
                    Toast.makeText(SettingActivity.this, "ID must have 5 letters!", Toast.LENGTH_SHORT).show();
                } else {
                    ID = new_ID;
                    editor.putString("ID", ID);
                    editor.commit();
                    tvid.setText(ID);
                    usbConnectionServices.updateID(ID);
                    imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                    Toast.makeText(SettingActivity.this, "ID changed successfully!", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
            }
        });
    }
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
    }    //OnClick Event
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
    @Override
    protected void onResume() {     //On Resume
        super.onResume();
        Log.e(STATE,"onResume");
        Intent intent = new Intent(SettingActivity.this, USBConnectionServices.class);
        bindService(intent,serviceConnection2,BIND_AUTO_CREATE);
        initArduinoMessagereceiver();
    }
    @Override     //Pause Status
    protected void onPause() {
        super.onPause();
        if (idUSBConnectionService)
            unbindService(serviceConnection2);
        unregisterReceiver(arduinoMessageReceiver);
    }
    //<<-----------------------Setup Connection--------------------->>
    ServiceConnection serviceConnection2 = new ServiceConnection() { //Service connection
        @Override
        public void onServiceConnected(ComponentName name, IBinder iBinder) {
            Log.e(STATE,"onServiceConnected");
            idUSBConnectionService = true;
            USBConnectionServices.CallService bider =(USBConnectionServices.CallService) iBinder;
            usbConnectionServices = bider.getService();
            usbConnectionServices.updateID(ID);
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
    private void initArduinoMessagereceiver(){   // initialize Broadcast receiver
        arduinoMessageReceiver = new ArduinoMessageReceiver(this);
        IntentFilter Connect_update = new IntentFilter();
        Connect_update.addAction("USBdisconnected");
        Connect_update.addAction("USBconnected");
        Connect_update.addAction("ZIGconnected");
        Connect_update.addAction("LANconnected");
        Connect_update.addAction("RS485connected");
        registerReceiver(arduinoMessageReceiver,Connect_update);
    }     //Init Broadcast Receiver
    //<--------------------------Handle command from PC------------------------>
    @Override
    public void showTarget(String value, String time) { //Show target frame
    }
    @Override
    public void shootResults(Shoot_result shoot_result) {

    }
    @Override
    public void show_temper(int T) {
    }

    @Override
    public void show_lane(int L) {

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