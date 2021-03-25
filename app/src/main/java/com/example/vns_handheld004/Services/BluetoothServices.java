package com.example.vns_handheld004.Services;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.vns_handheld004.BluetoothDialog;
import com.example.vns_handheld004.MainActivity;
import com.example.vns_handheld004.R;
import com.example.vns_handheld004.SettingActivity;
import com.example.vns_handheld004.Util.BluetoothConnectionService;
import com.felhr.usbserial.UsbSerialInterface;

import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import static android.bluetooth.BluetoothProfile.STATE_CONNECTED;


public class BluetoothServices extends Service{
    //Declaration
    public  String ID_Phone ;//5 byte
    public static final String ID_PC = "YA000";//5 byte
    public static final String SEND_START = "@@";
    public static final String RECEIVE_START = "&&";
    private static final String bluetoothState= "Connect to nearby device";
    public static String MSG_STOP = ">>";
    final String TAG = "BluetoothService";
    private String message = "";
    private int shoot_count = 0,countTimeTestData=0;
    BluetoothDevice mBTDevice;
    private boolean is_Bluetooth_On = true,is_Device_connected =false;
    public static Charset encodingType = StandardCharsets.UTF_8;
    private final IBinder iBinder = new CallService();
    Timer test_timer;
    BluetoothAdapter mBluetoothAdapter;
    BluetoothConnectionService mBluetoothConnection;
    private static final UUID MY_UUID_INSECURE =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    //
    public BluetoothServices() {
    }
    @Override
    public void onCreate() {
        Log.e(TAG, "onCreate");
        ID_Phone = getString(R.string.ID_PHONE);
        super.onCreate();
        registerReceiver();
        checkBluetoth();
        turnOnBluetooth();
        StringBuilder sb = new StringBuilder();
        sb.append((char)13);
        sb.append((char)10);
        MSG_STOP=sb.toString();
    }
    @Override
    public boolean onUnbind(Intent intent) {
        Log.e(TAG, "onUnbind");
        return super.onUnbind(intent);
    }
    @Override
    public void onRebind(Intent intent) {
        Log.e(TAG, "onRebind");
        super.onRebind(intent);
    }

    private void registerReceiver(){
        IntentFilter filter = new IntentFilter("Device Connected");
        registerReceiver(mBondBroadcastReceiver,filter);
        filter = new IntentFilter("Device Disconnected");
        registerReceiver(mBondBroadcastReceiver,filter);
        filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mBTReceiver, filter);
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver,new IntentFilter("incomingMessage"));
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }
    @Override
    public IBinder onBind(Intent intent) {
        Log.e(TAG, "onBind");
        return iBinder;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBondBroadcastReceiver);
        unregisterReceiver(mBTReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
    }

    //Broadcast Receiver
    private final BroadcastReceiver mBTReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int bluetoothState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR);
                switch (bluetoothState) {
                    case BluetoothAdapter.STATE_ON: //BLT dc bat
                    {
                        //showToast("Bluetooth turned on");
                        is_Bluetooth_On = true;
                        break;
                    }
                    case BluetoothAdapter.STATE_OFF:
                    {
                        //showToast("Bluetooth turned off");
                        is_Bluetooth_On = false;
                        is_Device_connected = false;
                        if (is_Device_connected) notconnected_broadcast();
                        break;
                    }
                }
            }
        }
    };
    //region Broadcast receiver
    //Broadcast Receiver gui tin nhan
    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String text = intent.getStringExtra("theMessage");
            read_data(text);
        }
    };
    //Broadcast ket noi thiet bi
    BroadcastReceiver mBondBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) { //Kiem tra ket noi
        final String action = intent.getAction();
        try {
            if (action.equals("Device Connected")) {
                //showToast("DEVICE CONNECTED");
                is_Device_connected = true;
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        checkBluetoothConnection();
                    }
                }, 2000);
                }
            if (action.equals("Device Disconnected")) {
                if (test_timer!= null) test_timer.cancel();
                is_Device_connected=false;
                notconnected_broadcast();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        }
    };
    public class CallService extends Binder {
        public BluetoothServices getService() {
            return BluetoothServices.this;
        }
    }    //Interface to call Service from Service
    //endregion
    //region Bluetooth
    public boolean getBluetoothStatus() {
        return is_Bluetooth_On;
    }
    public boolean isBluetoothDeviceConnected() {
        return  is_Device_connected;
    }
    private void checkBluetoth(){
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null)
            showToast("Device does not support bluetooth");
    }
    public boolean turnOnBluetooth()
    {
        try
        {
            if (mBluetoothAdapter == null) {

                showToast("Device does not support bluetooth");
                return false;
            }
            if (!mBluetoothAdapter.isEnabled()){
                is_Bluetooth_On = false;
                showToast("Turning On Bluetooth...");
                mBluetoothAdapter.enable();
            }
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }
    public boolean turnOffBluetooth()
    {
        try
        {
            if (mBluetoothAdapter == null) {
                showToast("Device does not support bluetooth");
                return false;
            }
            if (mBluetoothAdapter.isEnabled()){
                is_Bluetooth_On = true;
                showToast("Turning Off Bluetooth...");
                mBluetoothAdapter.disable();
            }
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }
    public void connectBT(BluetoothDevice mBTDevice)
    {
        this.mBTDevice = mBTDevice;
        mBluetoothConnection = new BluetoothConnectionService(getApplicationContext());
        mBluetoothAdapter.cancelDiscovery();
        mBluetoothConnection.startClient(mBTDevice,MY_UUID_INSECURE );
    }
    private void showToast(String msg){
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Context context = getApplicationContext();
                Toast toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
                toast.show();
            }
        });
    }
    //endregion
    //region Truyen thong gui nhan
    public void read_data(String data) {
        //*Doc data
        message = message + data.toString();
        int Start = message.indexOf(RECEIVE_START);  //latest start byte index
        if (message.length() < (Start+10)) return;
        Boolean is_requestDataCommand = message.charAt(Start+8) =='@';
        if (is_requestDataCommand)
        {
            message = message.substring(Start+9);
            return;
        }
        int Stop = message.indexOf(MSG_STOP, Start + 1) + 2; //Stop byte index
        if ((Start != -1) && (Stop != 1)) {     //meet stop byte
            Log.e("Bluetooth","receive message");
            String command = message.substring(Start, Stop);
            if (match_received_msg(command)) { //finish reading message
                Log.e("Message: ",message);
                implement_msg_command(command);
            }
               // implement message command
            if (message.length() > (Stop+2)) message = message.substring(Stop+1);
            else message ="";
        }
    }
    protected int data_length(String msg) {
        char ch = msg.charAt(2);
        return (int)ch;
    }
    protected String Message_data(String msg) {
        int data_length = data_length(msg);

        String data = msg.substring(14, 14 + data_length); // message data
        return data;
    }    // return message data
    protected int Message_command(String msg) {
        char cmd = msg.charAt(13); // message data
        return (int) cmd;
    }    //return message command
    protected boolean match_msg_length(String msg) {
        int real_msg_length = msg.length();
        int msg_length = 17 + data_length(msg);
        return (msg_length == real_msg_length);
    }    //check received message length
    protected boolean match_checksum(String msg) {
        int data_length = data_length(msg);
        int check_sum = (int) msg.charAt(14 + data_length);
        int check_error = generate_checksum(msg) ;
        if (check_sum>check_error)
            check_error = (check_error-check_sum) + 256;
        else
            check_error = check_error - check_sum;
        return (check_sum == check_error);
    }    //check received message checksum
    protected boolean match_id(String msg) {
        String ID_receive = msg.substring(3, 8);
        String ID_send = msg.substring(8, 13);
        return (ID_receive.equals(ID_Phone) && ID_send.equals(ID_PC));
    }    //check received message id
    protected boolean match_received_msg(String msg) {
        if (!match_msg_length(msg)) return false;
        if (!match_id(msg)) return false;
        if ((Message_command(msg)!=1) && (!is_Bluetooth_On)) return false;
        //if (!match_checksum(msg)) return false;

        return true;
    }    // check message is valid or not
    protected void implement_msg_command(String msg) {
        int command_code = Message_command(msg);
        switch (command_code) {
            case 1:
                test_command(msg);
                break;
            case 2:
                load_image(msg);
                break;
            case 3:
                shooting_result(msg);
                break;
            case 4:
                break;
            case 5:
                show_temper(msg);
                break;
            case 7:
                ZIGconnected(msg);
                break;
            case 8:
                LANconnected(msg);
                break;
            case 9:
                RS485connected(msg);
                break;
        }

    }    //implement message command

    //<----Handle received command---->
    protected void test_command(String msg) { //command code 01
        if (textToHex(Message_data(msg)).equals("01"))
            connected_broadcast();
        else
            notconnected_broadcast();
    }    //receive USB connect msg -01
    private void connected_broadcast() {            //send connected broadcast
        is_Device_connected = true;
        Log.e("ARDUINO", "CONNECTED");
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Context context = getApplicationContext();
                Toast toast = Toast.makeText(context, "Device connected", Toast.LENGTH_SHORT);
                toast.show();
            }
        });
        Intent intent = new Intent();
        intent.setAction("USBconnected");
        timerSendShootData();
        sendBroadcast(intent);
    }
    public void notconnected_broadcast() {
        is_Device_connected = false;
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Context context = getApplicationContext();
                Toast toast = Toast.makeText(context, "Device not connected. Please check connection again", Toast.LENGTH_SHORT);
                toast.show();
            }
        });
        refreshBluetooth();
        Intent intent = new Intent();
        intent.setAction("USBdisconnected");
        sendBroadcast(intent);
    }     //USB not connected
    private void load_image(String msg) { //command code 02
        String data= Message_data(msg);
        if (data.length() != 6){
//            send_data(2,1,"0");
            return;
        }
        send_data(2,1,Character.toString((char)1));
        shoot_count = 0;
        String target_code = data.substring(0,3);
        String Countdown = data.substring(3,5);
        String lane = Integer.toString(data.charAt(5) & 0xFF);
        Intent intent = new Intent();
        intent.setAction("Load_target");
        intent.putExtra("target code",target_code);
        intent.putExtra("time",Countdown);
        intent.putExtra("lane",lane);
        sendBroadcast(intent);
    }    //load target frame image
    private boolean is_result_valid(String data){
        if (data.length() != 8) return false;
        if (data.charAt(6)>60) return false;
        return data.charAt(7) <= 50;
    }    //receive shooting result
    private void shooting_result(String msg){ //command code 03
        String data= Message_data(msg);
        Log.e("Having result",msg);
        if (!is_result_valid(data)){
//            send_data(3,1,"0");
            return;
        }
        shoot_count ++;
        String No = Integer.toString(shoot_count);
        String X = Integer.toString(Sbyte_to_int(data.substring(0,2))-32768);
        String Y = Integer.toString(Sbyte_to_int(data.substring(2,4)));
        String V = Integer.toString(Sbyte_to_int(data.substring(4,6)));
        String Mark = Integer.toString(data.charAt(6));
        String M_H = data.charAt(7) == 1? "H":"M";
        String Time = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
        Intent intent = new Intent();
        intent.setAction("Shooting_result");
        String[] result = new String[7];
        result[0]=No;
        result[1]=X;
        result[2]=Y;
        result[3]=V;
        result[4]=Mark;
        result[5]=M_H;
        result[6]=Time;
        intent.putExtra("Results",result);
        sendBroadcast(intent);
    }    // Receive shoot result
    private void show_temper(String msg) { //command code 04
        String data= Message_data(msg);
        if (data.length() != 3){
//            send_data(2,1,"0");
            return;
        }
        //int T = String2Int(data);

        Intent intent = new Intent();
        intent.setAction("Show_temper");
        intent.putExtra("Temperature",data);
        sendBroadcast(intent);
    }   // Receive temperature, Frame Name and ID
    public void checkZIGconnection(){  //command code 07
        send_data(7,0,"");
    }
    public void checkLANconnection(){  //command code 08
        send_data(8,0,"");
    }
    public void checkRS485connection(){  //command code 09
        send_data(9,0,"");
    }
    public void ZIGconnected(String msg){
        if (textToHex(Message_data(msg)).equals("01")){
            Intent intent = new Intent();
            intent.setAction("ZIGconnected");
            sendBroadcast(intent);
        }
    }
    public void LANconnected(String msg){
        if (textToHex(Message_data(msg)).equals("01")){
            Intent intent = new Intent();
            intent.setAction("LANconnected");
            sendBroadcast(intent);
        }
    }
    public void RS485connected(String msg){
        if (textToHex(Message_data(msg)).equals("01")){
            Intent intent = new Intent();
            intent.setAction("RS485connected");
            sendBroadcast(intent);
        }
    }
    public void checkBluetoothConnection() {
        if (is_Bluetooth_On) {
            Log.e(TAG,"checkBluetoothConnection");
            //String S = SEND_START + (char) 0 + ID_PC + ID_Phone  + (char) 1+ MSG_STOP;
            String Message = SEND_START + (char) 0 + ID_PC + ID_Phone + (char) 1+ (char)0 /*generate_checksum(S)*/ + MSG_STOP;
            sendMessagetoArduino(Message);
        } else
            notconnected_broadcast();
    }    // to check USB connection
    //region Send data
    public void sendMessagetoArduino(String Text) {
        byte[] bytes = Text.getBytes(Charset.defaultCharset());
        if (CheckBluetoothConnection()) mBluetoothConnection.write(bytes);
    }     // send Message to Arduino
    public void send_data(int command_code, int data_length, String data) {
        if (is_Device_connected) {
            //String S = SEND_START + (char) data_length + ID_PC + ID_Phone  + (char) command_code +data + MSG_STOP;
            String Message = SEND_START + (char) data_length + ID_PC + ID_Phone + (char) command_code + data + (char) 0/*generate_checksum(S)*/ + MSG_STOP;
            sendMessagetoArduino(Message);
        }
    }    //send data to PC
    public void updateID(String ID){
        send_data(4,5,ID);
        this.ID_Phone=ID;

    }
    //re-use FUnction
    private int generate_checksum(String S) {
        int i, x, checksum, sum = 0;
        for (i = 0; i < S.length(); i++) {
            x = (int) (S.charAt(i));
            sum += x;
        }
        checksum = sum % 256;
        return checksum;
    }     // checksum
    int Sbyte_to_int(String data){
        int res1=data.charAt(0)& (0xFF);
        int res2=(int)(data.charAt(1))& (0xFF);
        //int res1 = c1 >>8;
        //int res2 = c2 & (0xFF);
        return res1*256 + res2 ;
    }    //get byte String
    public static String textToHex(String text) //convert text to hex
    {
        byte[] buf = null;
        buf = text.getBytes(encodingType);
        char[] HEX_CHARS = "0123456789abcdef".toCharArray();
        char[] chars = new char[2 * buf.length];
        for (int i = 0; i < buf.length; ++i) {
            chars[2 * i] = HEX_CHARS[(buf[i] & 0xF0) >> 4];
            chars[2 * i + 1] = HEX_CHARS[buf[i] & 0x0F];
        }
        return new String(chars);
    }
    public int String2Int(String s) //Convert integer String to integer
    {
        int result=0;
        for (int i=0;i<s.length();i++){
            result = result *10 + Integer.parseInt(String.valueOf(s.charAt(i)));
        }
        return result;
    }
    public Boolean CheckBluetoothConnection()
    {
        if (mBluetoothConnection == null || (mBTDevice == null)|| mBluetoothConnection.mConnectedThread == null)
        {
            return false;
        }
        return true;
    }
    public void sendTestData()
    {
        String cmd_code ="064 ";
        String[] char_split = cmd_code.split(" ");
        byte[] bytes=new byte[char_split.length];
        for(int i=0;i<char_split.length;i++){
            bytes[i]=(byte)(String2Int(char_split[i]) & 0xFF);
        }
        if (CheckBluetoothConnection()) mBluetoothConnection.write(bytes);
        else
        {
            if (test_timer!= null) test_timer.cancel();
            is_Device_connected=false;
            notconnected_broadcast();
        }
    }
    public boolean createBond(BluetoothDevice btDevice)
            throws Exception {
        Class class1 = Class.forName("android.bluetooth.BluetoothDevice");
        Method createBondMethod = class1.getMethod("createBond");
        Boolean returnValue = (Boolean) createBondMethod.invoke(btDevice);
        return returnValue.booleanValue();
    }
    private void timerSendShootData(){
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                read_data("");
                countTimeTestData++;
                if (countTimeTestData == 20){
                    countTimeTestData = 0;
                    sendTestData();
                }
            }
        };
        long delay = 100L;
        test_timer = new Timer("Timer");
        test_timer.schedule(timerTask, 10, delay);
    }
    private void refreshBluetooth()
    {
        if (mBluetoothAdapter.isEnabled())
        {
            mBluetoothAdapter.disable();
            final Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                        mBluetoothAdapter.enable();
                }
            }, 100);
        }
    }
    //endregion
}
