package com.example.vns_handheld004;

import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.HorizontalScrollView;

import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.GridLayoutManager;
import com.example.vns_handheld004.Adapter.ShootAdapter;
import com.example.vns_handheld004.Adapter.TargetAdapter;
import com.example.vns_handheld004.Broadcast.ArduinoMessageReceiver;
import com.example.vns_handheld004.Broadcast.MyBroadcastListener;
import com.example.vns_handheld004.Model.Shoot_result;
import com.example.vns_handheld004.Services.USBConnectionServices;
import com.example.vns_handheld004.Util.FixedGridLayoutManager;
import com.example.vns_handheld004.View.PixelGridView;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements MyBroadcastListener, View.OnClickListener,TargetDialog.TargetDialogListener,ShootAdapter.OnTableViewListener,TargetAdapter.OnTargetClickListener, IdDialog.IdDialogListener {
    // Declare
    ArduinoMessageReceiver arduinoMessageReceiver;
    private static final String STATE = "Main Activity";
    private boolean idUSBConnectionService = false, isRestart = false;
    Intent intent;
    private USBConnectionServices usbConnectionServices;
    private ImageButton btnSetting, btnGridsize;
    private PixelGridView mImageBorder;
    private TextView tvtime, tvtemp, tvFrameNo, tvid, tvTargetName;
    private int gridsize = 100, Target_count = 0, Target_select;
    private Bitmap bitmap;
    private static CountDownTimer timer;
    private Target_Frame target_frame = new Target_Frame(this);
    private TargetAdapter targetAdapter ;
    private AlertDialog dialog;
    int scrollX = 0;
    private List<Bitmap> bitmapArrayList = new ArrayList<>();
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    List<Target_Frame> TargetList = new ArrayList<>();
    List<Shoot_result> shootList = new ArrayList<>();
    ArrayList<List<Shoot_result>> resultList= new ArrayList<>();
    RecyclerView rvShootresult,rvTarget;

    HorizontalScrollView headerScroll;

    ShootAdapter shootAdapter;

    @Override
    //<--Create status-->
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.e(STATE, "onCreate");
        intent = new Intent(MainActivity.this, USBConnectionServices.class);
        startService(intent);
        initView();
        initArduinoMessagereceiver();
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
        }
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
        }
        super.onConfigurationChanged(newConfig);
        setContentView(R.layout.activity_main);
    }
    //Resume Status
    @Override
    protected void onResume() {
        super.onResume();
        Log.e(STATE, "onResume");
        bindService(intent, serviceConnection, BIND_AUTO_CREATE);
    }

    //Pause Status
    @Override
    protected void onPause() {
        super.onPause();
        isRestart = true;
        Log.e(STATE, "onPause");
        if (idUSBConnectionService)
            unbindService(serviceConnection);
//        unregisterReceiver(arduinoMessageReceiver);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.e(STATE, "onRestart");
        isRestart = true;
    }

    //DESTROY STATUS
    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(intent);
        Log.e(STATE, "onDestroy");
        unregisterReceiver(arduinoMessageReceiver);
    }
    //

    //<--Initialize View-->
    protected void initView() {
        btnSetting = findViewById(R.id.btnSetting);
        btnGridsize = findViewById(R.id.btnGridsize);
        mImageBorder = findViewById(R.id.PgvTarget);
        mImageBorder.setNumColumns(1);
        mImageBorder.setNumRows(1);
        //TextView
        tvtime = findViewById(R.id.value_time);
        tvtemp = findViewById(R.id.value_temp);
        tvFrameNo = findViewById(R.id.value_daiban);
        tvTargetName = findViewById(R.id.tvTargetName);
        tvid = findViewById(R.id.app_id);
        rvShootresult = findViewById(R.id.rvShootresults);
        headerScroll = findViewById(R.id.headerScroll);
        initTargetList();
        setOnListener();
        initTable();
        initPreferences();
    }

    private void initPreferences() {
//        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences = getSharedPreferences("IDs",MODE_PRIVATE);
        editor = sharedPreferences.edit();
        String savedData = sharedPreferences.getString("ID", "YA001");
        tvid.setText(savedData);
    }
    //Init Target List Recycle VIew
    private void initTargetList() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false);
        rvTarget = findViewById(R.id.rvTarget);
        rvTarget.setLayoutManager(layoutManager);
        targetAdapter = new TargetAdapter(MainActivity.this,bitmapArrayList,this);
        rvTarget.setAdapter(targetAdapter);
    }
    protected void setOnListener(){
        btnSetting.setOnClickListener(this);
        btnGridsize.setOnClickListener(this);
        //Make app id text animation on touch
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
        rvTarget.setOnClickListener(this);
        headerScroll.setOnTouchListener(new View.OnTouchListener() { //make headerScroll unscrollable
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        rvShootresult.setOnClickListener(this);
        rvShootresult.addOnScrollListener(new RecyclerView.OnScrollListener() { //header scroll with table
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                scrollX += dx;
                headerScroll.scrollTo(scrollX, 0);
            }
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }
        });
    }
    //Init Shooting result table Recycle View
    private void openIDdialog(){
        Log.e(STATE, "openIDDialog");
        IdDialog IdDialog = new IdDialog();
        IdDialog.show(getSupportFragmentManager(), "ID Dialog");
    }
    protected void initTable() {
        FixedGridLayoutManager manager = new FixedGridLayoutManager();
        manager.setTotalColumnCount(1);
        rvShootresult.setLayoutManager(manager);
        rvShootresult.addItemDecoration(new DividerItemDecoration(MainActivity.this, DividerItemDecoration.VERTICAL));
    }

    //<--OnClick View Event-->
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnSetting: {
                Intent intent2 = new Intent(MainActivity.this, SettingActivity.class);
                startActivity(intent2);
                break;
            }
            case R.id.btnGridsize:
                openGridDialog();
                break;
        }
    }
    // open dialog set up grid size
    private void openGridDialog() {
        Bundle bundle = new Bundle();
        bundle.putInt("Gridsize", gridsize);
        Log.e(STATE, "openDialog");
        TargetDialog targetDialog = new TargetDialog();
        targetDialog.setArguments(bundle);
        targetDialog.show(getSupportFragmentManager(), "Target Dialog");
    }

    //USB SERVICE CONNECTION
    ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder iBinder) {
            Log.e(STATE, "onServiceConnected");
            idUSBConnectionService = true;
            USBConnectionServices.CallService bider = (USBConnectionServices.CallService) iBinder;
            usbConnectionServices = bider.getService();
            if (!isRestart)
                openUSBconnection();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.e(STATE, "onServiceConnected");
            idUSBConnectionService = false;
            Toast.makeText(MainActivity.this, "USB Service has stopped. Please restart app", Toast.LENGTH_SHORT).show();
        }
    };

    // Initialize Message receiver
    private void initArduinoMessagereceiver() {   // initialize Broadcast receiver
        arduinoMessageReceiver = new ArduinoMessageReceiver(this);
        IntentFilter MessageFilter = new IntentFilter();
        MessageFilter.addAction("Shooting_result");
        MessageFilter.addAction("Load_target");
        registerReceiver(arduinoMessageReceiver, MessageFilter);
    }

    private void openUSBconnection() {
        usbConnectionServices.openSerialport();
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                usbConnectionServices.check_USB_connection();
            }
        }, 1000);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent2 = new Intent(MainActivity.this, SettingActivity.class);
                if (!usbConnectionServices.getArduinoStatus()) {
                    startActivity(intent2);
                    Log.e("ACTION:", "OPEN SETTING");
                }
            }
        }, 2000);
    }

    //<--------------------------Handle command from PC------------------------>
    @Override
    public void connectStatus(String value) {
    }
    //Function void to create a shoot List deep copy instance
    private List<Shoot_result> new_shootList(List<Shoot_result> shootList){
        List<Shoot_result> tmp = new ArrayList<>();
        for(Shoot_result shoot_result: shootList){
            tmp.add(shoot_result);
        }
        return tmp;
    }
    @Override
    public void showTarget(String value, String time) { //Show target frame
        List<Shoot_result> new_shootList = new_shootList(shootList);
        while (value.charAt(0) == '0') value = value.substring(1);
        if (Target_count != 0){
            resultList.add(new_shootList);
            shootList.clear();
            shootAdapter = new ShootAdapter(MainActivity.this, shootList, this);
            rvShootresult.setAdapter(shootAdapter);
        }
        setTimer(Integer.parseInt(time));
        target_frame.targetSize(value);
        Target_Frame tmp_target = new Target_Frame(target_frame);
        TargetList.add(tmp_target);
        open_image(value);
        Target_count++;
        Target_select = Target_count;
        tvTargetName.setText("Target name: " + value);
        mImageBorder.setGridsize(gridsize);
    }
    //<--Open Image -->
    protected void open_image(String image_name) {
        mImageBorder.update_target(target_frame);
        AssetManager assetManager = getAssets();
        try (
                InputStream inputStream = assetManager.open("image/" + image_name + ".png"); //open image
        ) {
            bitmap = BitmapFactory.decodeStream(inputStream); //Load image
            mImageBorder.setNewImageBitmap(bitmap); //display image
            bitmapArrayList.add(bitmap); // add bitmap to List View
            targetAdapter.notifyDataSetChanged();
        } catch (IOException e) {

        }
    }
    // add new item to array
    @Override
    public void shootResults(Shoot_result value) {
        shootList.add(0, value);
        int id = Integer.parseInt(value.getNo());
        int x = Integer.parseInt(value.getX());
        int y = Integer.parseInt(value.getY());
        mImageBorder.update_latest_result(id,x,y);
        if (Target_count == Target_select) { //If the result showed on board is table is the latest one
            shootAdapter = new ShootAdapter(MainActivity.this, shootList, this);
            rvShootresult.setAdapter(shootAdapter);
            mImageBorder.Update_Result(id, x, y); //update result on UI
        }
    }

    @Override
    public void show_temper(int T, int L, String ID) {
        float temp = (float) T / 10;
        tvtemp.setText(Float.toString(temp));
        tvFrameNo.setText(Integer.toString(L));
        tvid.setText(ID);
    }

    public void setTimer(int time) {
        long msTime = time * 1000;
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        timer = new CountDownTimer(msTime, 1000) {
            int counter = time;

            public void onTick(long millisUntilFinished) {
                counter--;
                tvtime.setText(String.valueOf(counter));
            }

            public void onFinish() {
                Toast.makeText(MainActivity.this, "FINISH", Toast.LENGTH_SHORT);
                timer = null;
            }
        }.start();
    }

    //Apply grid size
    @Override
    public void applysize(int value) {
        gridsize = value;
        mImageBorder.setGridsize(gridsize);
    }
    // on Table row click event
    @Override
    public void onTableClick(int pos) {
        Shoot_result selected_result;
        if (Target_select == Target_count)
            selected_result = shootList.get(pos);
        else
            selected_result = resultList.get(Target_select - 1).get(pos);
        int focus_bullet = Integer.parseInt(selected_result.getNo());
        mImageBorder.setFocusBullet(focus_bullet);
    }
    //On Target Image click event
    @Override
    public void onTargetClick(int pos) {
        update_selected_target(pos);
    }
    protected void update_selected_target(int position){ // update table and bitmap with clicked Target
        String FrameName;
        Target_select = position+1;
        if (Target_select == Target_count) {// if selected target is the latest
            shootAdapter = new ShootAdapter(MainActivity.this, shootList, this); //update latest Shoot result
            mImageBorder.update_target(target_frame); // update latest target
        }
        else {
            shootAdapter = new ShootAdapter(MainActivity.this, resultList.get(position), this); //update Shoot result
            mImageBorder.update_target(TargetList.get(position)); //update Target Frame
        }
        mImageBorder.openImageBitmap(bitmapArrayList.get(position),Target_select);
        if (Target_select == Target_count) {// if selected target is the latest
            shootAdapter = new ShootAdapter(MainActivity.this, shootList, this); //update latest Shoot result
            mImageBorder.update_target(target_frame); // update latest target
            FrameName = target_frame.getName();
        }
        else {
            shootAdapter = new ShootAdapter(MainActivity.this, resultList.get(position), this); //update Shoot result
            mImageBorder.update_target(TargetList.get(position)); //update Target Frame
            FrameName = TargetList.get(position).getName();
        }
        mImageBorder.openImageBitmap(bitmapArrayList.get(position),Target_select);
        rvShootresult.setAdapter(shootAdapter);
        tvFrameNo.setText("Frame name: "+FrameName);
    }

    @Override
    public void applyID() {
        String savedData = sharedPreferences.getString("ID", "YA001");
        tvid.setText(savedData);
    }
}