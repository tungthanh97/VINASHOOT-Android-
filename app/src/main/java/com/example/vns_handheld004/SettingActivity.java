package com.example.vns_handheld004;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.example.vns_handheld004.Services.BluetoothServices;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.vns_handheld004.Broadcast.ArduinoMessageReceiver;
import com.example.vns_handheld004.Broadcast.MyBroadcastListener;
import com.example.vns_handheld004.Model.Shoot_result;
import com.example.vns_handheld004.Services.USBConnectionServices;

public class SettingActivity extends AppCompatActivity implements MyBroadcastListener, View.OnClickListener {
    //Declaration
    ArduinoMessageReceiver arduinoMessageReceiver;
    private static final String STATE = "Setting";
    private boolean idUSBConnectionService = false, isTouched = false;;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothServices bluetoothServices;
    private SwitchCompat swUSB;
    private String ID;
    private Button btZIG,btLAN,btRS485;
    private TextView tvid,tvBluetooth,tvZIG,tvLAN,tvRS485;
    private CardView cvBluetooth;
    private static String Bluetooth_connected_device ="";
    LinearLayout layoutSetting;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    //region Initialize------------------------
    @Override           //OnCreate
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(STATE,"onCreate");
        setContentView(R.layout.activity_setting_activities);
        initView();
        initPreferences();
        checkBluetoth();
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
        }
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
        }
        super.onConfigurationChanged(newConfig);
        setContentView(R.layout.activity_setting_activities);
    }
    protected void initView(){
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
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
        tvBluetooth =findViewById(R.id.tvBluetoothSTT);
        tvZIG = findViewById(R.id.tvZIGstt);
        tvLAN = findViewById(R.id.tvLANstt);
        tvRS485 = findViewById(R.id.tvRSstt);
        cvBluetooth =findViewById(R.id.cvBluetooth);
        cvBluetooth.setOnClickListener(this);
        initSwitch();
        initIDlistener();
    }     //Initialize View
    private void initPreferences() {
        sharedPreferences = getSharedPreferences("IDs",MODE_PRIVATE);
        editor = sharedPreferences.edit();
        ID = sharedPreferences.getString("ID", "HD001");
        tvid.setText(ID);
    }     // init shared prefrence to save ID
    private void initSwitch(){
       ;
        swUSB.setOnTouchListener((view, motionEvent) -> {
            isTouched = true;
            return false;
        });
        swUSB.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isTouched) {
                isTouched = false;
                if (isChecked) {
                    boolean kt = bluetoothServices.turnOnBluetooth();
                    //if (!(kt && bluetoothServices.getBluetoothStatus())) swUSB.setChecked(false);
                }
                else {
                    boolean kt = bluetoothServices.turnOffBluetooth();
                    tvBluetooth.setText("Connect to nearby device");
                    Bluetooth_connected_device = "";
                    if (!kt && bluetoothServices.getBluetoothStatus()) swUSB.setChecked(true);
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
    //endregion------------------------------------
    //region Event Handler
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
                    bluetoothServices.updateID(ID);
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
                bluetoothServices.checkZIGconnection();
                checking(tvZIG);
                break;
            }
            case R.id.bt_lan:{
                bluetoothServices.checkLANconnection();
                checking(tvLAN);
                break;
            }
            case R.id.bt_rs485:{
                bluetoothServices.checkRS485connection();
                checking(tvRS485);
                break;
            }
            case R.id.cvBluetooth:{
                open_BT_dialog();
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
                if (text.equals("Checking...")) textView.setText("No Connection");
            }
        }, 3000);
    }
    @Override
    protected void onResume() {     //On Resume
        super.onResume();
        Log.e(STATE,"onResume");
        Intent intent = new Intent(SettingActivity.this, BluetoothServices.class);
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
    //endregion-----------------------
    //region Setup Connection------------------------
    ServiceConnection serviceConnection2 = new ServiceConnection() { //Service connection
        @Override
        public void onServiceConnected(ComponentName name, IBinder iBinder) {
            Log.e(STATE,"onServiceConnected");
            idUSBConnectionService = true;
            BluetoothServices.CallService bider =(BluetoothServices.CallService) iBinder;
            bluetoothServices = bider.getService();
            if (bluetoothServices.isBluetoothDeviceConnected() && Bluetooth_connected_device != "")  tvBluetooth.setText("Connected to "+Bluetooth_connected_device);
            else {
                tvBluetooth.setText("Connect to nearby device");
                Bluetooth_connected_device = "";
            }
            if (bluetoothServices.getBluetoothStatus())
                swUSB.setChecked(true);
            else
                swUSB.setChecked(false);
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            idUSBConnectionService = false;
            bluetoothServices = null;
            Toast.makeText(SettingActivity.this, "Bluetooth has disconnected", Toast.LENGTH_SHORT).show();
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
    public void connectBT(BluetoothDevice mBTdevice)
    {
        Bluetooth_connected_device = mBTdevice.getName();
        bluetoothServices.connectBT(mBTdevice);
    }
    private void checkBluetoth(){
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }
    private void showToast(String msg){
        Toast toast = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
    }
    //<--------------------------Handle command from PC------------------------>
    @Override
    public void connectStatus(String result) {
        switch (result) {
            case "USBconnected": {
                tvBluetooth.setText("Connected to "+Bluetooth_connected_device);
                break;
            }
            case "USBdisconnected": {
                tvBluetooth.setText("Connect to nearby device");
                Bluetooth_connected_device = "";
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
    //BLUETOOTH
    private void open_BT_dialog()
    {
        if (!bluetoothServices.getBluetoothStatus()) return;
        BluetoothDialog bluetoothDialog = new BluetoothDialog();
        bluetoothDialog.show(getSupportFragmentManager(), "Bluetooth Dialog");
    }
    //just useless override
    @Override
    public void showTarget(String value, String time,String lane) { //Show target frame
    }
    @Override
    public void shootResults(Shoot_result shoot_result) {

    }
    @Override
    public void show_temper(String T) {
    }

    //endregion
}