package com.example.vns_handheld004.Services;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.nio.charset.StandardCharsets;
import java.nio.charset.Charset;

import com.example.vns_handheld004.Model.Shoot_result;
import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.nio.CharBuffer;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class USBConnectionServices extends Service implements UsbSerialInterface.UsbReadCallback {
    //Declaration
    public  String ID_Phone = "YA001";//5 byte
    public static final String ID_PC = "YA000";//5 byte
    public static final String SEND_START = "@@";
    public static final String RECEIVE_START = "&&";
    public static final String MSG_STOP = ">>";
    private String message = null;
    private int shoot_count = 0;
    private boolean is_connected = false;
    public static Charset encodingType = StandardCharsets.UTF_8;
    private final IBinder iBinder = new CallService();
    UsbManager usbManager;
    UsbDevice device;
    UsbSerialDevice serialPort;
    UsbDeviceConnection connection;
    public final String ACTION_USB_PERMISSION = "com.example.vns_handheld004.USB_PERMISSION";

    public USBConnectionServices() {
    }
    @Override
    public void onCreate() {
        Log.e("Service", "onCreate");
        super.onCreate();
        usbManager = (UsbManager) getSystemService(this.USB_SERVICE);
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(broadcastReceiver, filter);
    }
    @Override
    public IBinder onBind(Intent intent) {
        Log.e("Service", "onBind");
        return iBinder;
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }
    @Override
    public boolean onUnbind(Intent intent) {
        Log.e("Service", "onUnbind");
        return super.onUnbind(intent);
    }
    @Override
    public void onRebind(Intent intent) {
        Log.e("Service", "onRebind");
        super.onRebind(intent);
    }
    @Override
    public void onDestroy() {
        Log.e("Service", "onDestroy");
        unregisterReceiver(broadcastReceiver);
        super.onDestroy();
    }
    //<<-----------------------------USB connect Broadcast receiver----------------------->>
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() { //Broadcast Receiver to automatically start and stop the Serial connection.
        @Override
        public synchronized void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACTION_USB_PERMISSION)) {
                Log.e("ACTION", "PORT OPEN");
                boolean granted = intent.getExtras().getBoolean(UsbManager.EXTRA_PERMISSION_GRANTED);
                if (granted) {
                    connection = usbManager.openDevice(device);
                    serialPort = UsbSerialDevice.createUsbSerialDevice(device, connection);
                    if (serialPort != null) {
                        if (serialPort.open()) { //Set Serial Connection Parameters.
                            Log.e("SERIAL", "PORT OPEN");
                            serialPort.setBaudRate(9600);
                            serialPort.setDataBits(UsbSerialInterface.DATA_BITS_8);
                            serialPort.setStopBits(UsbSerialInterface.STOP_BITS_1);
                            serialPort.setParity(UsbSerialInterface.PARITY_NONE);
                            serialPort.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);
                            serialPort.read(USBConnectionServices.this);
                        } else {
                            Log.d("SERIAL", "PORT NOT OPEN");
                            Toast.makeText(USBConnectionServices.this, "PORT NOT OPEN", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.d("SERIAL", "PORT IS NULL");
                        Toast.makeText(USBConnectionServices.this, "PORT IS NULL", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.d("SERIAL", "PERM NOT GRANTED");
                    Toast.makeText(USBConnectionServices.this, "PERM NOT GRANTED", Toast.LENGTH_SHORT).show();
                }
            } else if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)) {
                ArduinoAttached();
            } else if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_DETACHED)) {
                ArduinoDetached();
            }
        }
    };
    private void ArduinoAttached() {
        Toast.makeText(USBConnectionServices.this, "ARDUINO ATTACHED", Toast.LENGTH_SHORT).show();
        Log.e("Arduino","Attached");
        openSerialport();
    }     //When Arduino attached
    private void connected_broadcast() {            //send connected broadcast
        is_connected = true;
        Log.e("ARDUINO", "CONNECTED");
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Context context = getApplicationContext();
                Toast toast = Toast.makeText(context, "USB connected", Toast.LENGTH_SHORT);
                toast.show();
            }
        });
        Intent intent = new Intent();
        intent.setAction("USBconnected");
        sendBroadcast(intent);
    }
    public void notconnected_broadcast() {
        is_connected= false;
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Context context = getApplicationContext();
                Toast toast = Toast.makeText(context, "USB not connected. Please check connection again", Toast.LENGTH_SHORT);
                toast.show();
            }
        });
        Intent intent = new Intent();
        intent.setAction("USBdisconnected");
        sendBroadcast(intent);
    }     //USB not connected
    public void disconnected_broadcast() {
        is_connected = false;
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Context context = getApplicationContext();
                Toast toast = Toast.makeText(context, "USB disconnected", Toast.LENGTH_SHORT);
                toast.show();
            }
        });
        Intent intent = new Intent();
        intent.setAction("USBdisconnected");
        sendBroadcast(intent);
    }     //USB disconnected
    public synchronized void openUSBconnection(){
        if (serialPort != null) { //if Serial port opened
//            Log.e("ACTION", "CHECK CONNECT1");
            check_USB_connection();
        }
        else
            notconnected_broadcast();
    }    //open USB connection
    public synchronized void openSerialport() {
        HashMap<String, UsbDevice> usbDevices = usbManager.getDeviceList();
        if (!usbDevices.isEmpty()) {
            boolean keep = true;
            for (Map.Entry<String, UsbDevice> entry : usbDevices.entrySet()) {
                device = entry.getValue();
                int deviceVID = device.getVendorId();
                if (deviceVID == 0x2341 || deviceVID == 1659)//Arduino Vendor ID
                {
                    PendingIntent pi = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
                    usbManager.requestPermission(device, pi);
                    keep = false;
                    Log.e("ACTION", "OPEN SERIAL PORT");
                } else {
                    connection = null;
                    device = null;
                    notconnected_broadcast();
                }
                if (!keep)
                    break;
            }
        } else {
            notconnected_broadcast();
        }
    }    //open Serial port
    public void ArduinoDetached() {
        Log.e("Arduino", "Detached");
        disconnected_broadcast();
    }    // wait 100ms and check usb status
    public void closeUSBconnection() {
        Log.e("Arduino", "close_port");
        disconnected_broadcast();
    }    // close USB connection by button OFF
    private boolean isUSBconnected(){
        if (serialPort == null) return false;
        return serialPort.isOpen();
    }    //check USB connection Status
    public boolean getArduinoStatus(){
        return is_connected;
    }
    public class CallService extends Binder {
        public USBConnectionServices getService() {
            return USBConnectionServices.this;
        }
    }    //Interface to call Service from Service

    //<----------------------------------FUNCTION------------------------------------>
    private int generate_checksum(String S) {
        int i, x, checksum, sum = 0;
        for (i = 0; i < S.length(); i++) {
            x = (int) (S.charAt(i));
            sum += x;
        }
        checksum = sum % 256;
        return checksum;
    }     // checksum
    byte toBytes(char ch) {
        byte toByte = (byte) (0xFF & (int) ch);
        return toByte;
    }    //character to Byte
    int Sbyte_to_int(String data){
        int c1=data.charAt(0);
        int c2=data.charAt(1);
        int res1 = c1 & 0xFF;
        int res2 = c2 & 0xFF;;
        return res1*256 + res2 ;
    }    //get byte String
    //<------------------------------------ SEND MESSAGE--------------------------------->
    public void sendMessagetoArduino(String Text) {
        int i, l = Text.length();
        byte[] toByte = new byte[1];
//        Log.e(" send message:", Text);
        for (i = 0; i < l; i++) {
            toByte[0] = toBytes(Text.charAt(i));
            serialPort.write(toByte);
        }
    }     // send Message to Arduino
    public void check_USB_connection() {
        if (isUSBconnected()) {
            String S = SEND_START + (char) 0 + ID_PC + ID_Phone  + (char) 1+ MSG_STOP;
            String Message = SEND_START + (char) 0 + ID_PC + ID_Phone + (char) 1+ (char) generate_checksum(S) + MSG_STOP;
            sendMessagetoArduino(Message);
        } else
            notconnected_broadcast();
    }    // to check USB connection
    public void send_data(int command_code, int data_length, String data) {
        if (is_connected) {
            String S = SEND_START + (char) data_length + ID_PC + ID_Phone  + (char) command_code +data + MSG_STOP;
            String Message = SEND_START + (char) data_length + ID_PC + ID_Phone + (char) command_code + data + (char) generate_checksum(S) + MSG_STOP;
            sendMessagetoArduino(Message);
        }
    }    //send data to PC

    ///////////////////////////////////////////////////////////*************************************///////////////////////////////////////////////////////////////////////
    //<----------------------------------------RECEIVE MESSAGE------------------------------>
    /////////// FUNCTION TO CHECK MESSAGE RECEIVED
    //Defining a Callback which triggers whenever msg is read.
    @Override
    public void onReceivedData(byte[] arg0) {
        String data = null;
//        Log.e("Arduino", "Receive Message");
        try {
            data = new String(arg0, "ISO-8859-1");
//            Log.e("Message:", data);
            read_data(data);
        } catch (UnsupportedEncodingException e) {
            Log.e("Arduino:", "msg error");
            e.printStackTrace();
        }
    }
    public void read_data(String data){
//        if (!isEmpty(data)) { //data not empty
//            message = message.concat(data); //save data
//        }
        message = message + data.toString();
        int Start = message.lastIndexOf(RECEIVE_START);  //latest start byte index
        int Stop = message.indexOf(MSG_STOP,Start+1)+2; //Stop byte index
        if ((Start != -1) && (Stop != 1)){     //meet stop byte
//            Log.e("Arduino","receive message");
            message = message.substring(Start,Stop);
            if (match_received_msg(message))     //finish reading message
                implement_msg_command(message); // implement message command
            message = null;
        }
    }
    public static String textToHex(String text) {
        byte[] buf = null;
        buf = text.getBytes(encodingType);
        char[] HEX_CHARS = "0123456789abcdef".toCharArray();
        char[] chars = new char[2 * buf.length];
        for (int i = 0; i < buf.length; ++i) {
            chars[2 * i] = HEX_CHARS[(buf[i] & 0xF0) >>> 4];
            chars[2 * i + 1] = HEX_CHARS[buf[i] & 0x0F];
        }
        return new String(chars);
    }    //convert text to hex
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
        if ((Message_command(msg)!=1) && (!is_connected)) return false;
        if (!match_msg_length(msg)) return false;
        if (!match_checksum(msg)) return false;
        if (!match_id(msg)) return false;

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
    private void load_image(String msg) { //command code 02
        String data= Message_data(msg);
        if (data.length() != 5){
//            send_data(2,1,"0");
            return;
        }
        send_data(2,1,Character.toString((char)1));
        shoot_count = 0;
        String target_code = data.substring(0,3);
        String Countdown = data.substring(3);
        Intent intent = new Intent();
        intent.setAction("Load_target");
        intent.putExtra("target code",target_code);
        intent.putExtra("time",Countdown);
        sendBroadcast(intent);
    }    //load target frame image
    private boolean is_result_valid(String data){
        if (data.length() != 8) return false;
        if (data.charAt(6)>60) return false;
        return data.charAt(7) <= 50;
    }    //receive shooting result
    private void shooting_result(String msg){ //command code 03
        String data= Message_data(msg);
        if (!is_result_valid(data)){
//            send_data(3,1,"0");
            return;
        }
        send_data(3,1,Character.toString((char)1)); //Confirm data received
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
        if (data.length() != 8){
//            send_data(2,1,"0");
            return;
        }
        send_data(4,1,Character.toString((char)1));
        int T = Sbyte_to_int(data.substring(0,2));
        int L = data.charAt(2) & 0xFF;
        String ID = data.substring(3);
        Intent intent = new Intent();
        intent.setAction("Show_temper");
        intent.putExtra("Temperature",T);
        intent.putExtra("FrameNo",L);
        intent.putExtra("ID",ID);
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
    public void updateID(String ID){
        this.ID_Phone=ID;
    }
}
