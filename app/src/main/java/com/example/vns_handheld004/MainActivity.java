package com.example.vns_handheld004;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.HorizontalScrollView;

import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.ViewCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.GridLayoutManager;
import com.example.vns_handheld004.Adapter.ShootAdapter;
import com.example.vns_handheld004.Adapter.TargetAdapter;
import com.example.vns_handheld004.Broadcast.ArduinoMessageReceiver;
import com.example.vns_handheld004.Broadcast.MyBroadcastListener;
import com.example.vns_handheld004.Model.Shoot_result;
import com.example.vns_handheld004.Services.BluetoothServices;
import com.example.vns_handheld004.Services.USBConnectionServices;
import com.example.vns_handheld004.Util.FixedGridLayoutManager;
import com.example.vns_handheld004.View.PixelGridView;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends AppCompatActivity implements MyBroadcastListener, View.OnClickListener,TargetDialog.TargetDialogListener,ShootAdapter.OnTableViewListener,TargetAdapter.OnTargetClickListener {
    // Declare
    ArduinoMessageReceiver arduinoMessageReceiver;
    private static final String STATE = "Main Activity";
    private boolean idUSBConnectionService = false, isRestart = false, is_Landscape = true;
    private Intent intent;
    private String ID;
    private BluetoothServices bluetoothServices;
    private ImageButton btnSetting, btnGridsize;
    private PixelGridView mImageBorder;
    private TextView tvtime, tvtemp, tvFrameNo, tvid, tvTargetName;
    private int gridsize = 100, Target_count = 0, Target_select,Target_previous;
    private Bitmap bitmap;
    private static CountDownTimer timer;
    private Target_Frame target_frame = new Target_Frame(this);
    private TargetAdapter targetAdapter ;
    private LinearLayoutManager TargetlayoutManager;
    private LinearLayout layoutMain;
    int scrollX = 0;
    Paint borderPaint = new Paint();
    private List<Bitmap> BitmapList_show = new ArrayList<>(),BitmapList_origin= new ArrayList<>();
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    List<Target_Frame> TargetList = new ArrayList<>();
    List<Shoot_result> shootList = new ArrayList<>();
    List<List<Shoot_result>> resultList= new ArrayList<>();
    List<String> laneList = new ArrayList<>();
    RecyclerView rvShootresult,rvTarget;
    NestedScrollView nsvTable;
    HorizontalScrollView headerScroll;
    ShootAdapter shootAdapter;
    @Override
    //<--Create status-->
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.e(STATE, "onCreate");
        initView();
        initPreferences();
        intent = new Intent(MainActivity.this, BluetoothServices.class);
        if (isMyServiceRunning()) stopService(intent);
        try {
            startService(intent );
        }
        catch (Exception e)
        {
            Toast.makeText(this,"Please shut down previous running app",Toast.LENGTH_SHORT).show();
        }
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        initArduinoMessagereceiver();
        checkBTPermission();
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            is_Landscape = true;
        }
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            is_Landscape = false;
        }
        super.onConfigurationChanged(newConfig);
        setContentView(R.layout.activity_main);
    }
    //Resume Status
    @Override
    protected void onResume() {
        super.onResume();
        Log.e(STATE, "onResume");
        ID = sharedPreferences.getString("ID", "HD001"); // Load ID from Preference
        tvid.setText(ID);
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
    //<--Initialize View-->
    protected void initView() {
        layoutMain = findViewById(R.id.layoutMain);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        //getSupportActionBar().hide();
        layoutMain.setBackgroundColor(getResources().getColor(R.color.White));
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
        nsvTable = findViewById(R.id.nsvTableResult);
        initTargetList();
        setOnListener();
        initTable();
        init_Paints();
        fakedata();
    }
    //init ID storage
    private void initPreferences() {
        sharedPreferences = getSharedPreferences("IDs",MODE_PRIVATE);
        editor = sharedPreferences.edit();
        ID = sharedPreferences.getString("ID", "HD001"); // Load ID from Preference
        tvid.setText(ID);
    }
    //<--------------------------------init Activity Listener--------------------->
    //Init Listener
    protected void setOnListener(){
        btnSetting.setOnClickListener(this);
        btnGridsize.setOnClickListener(this);
//        nsvTable.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
//            @Override
//            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
//
//            }
//        });
//        nsvTable.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
//            @Override
//            public void onScrollChanged() {
//                int scrollX = nsvTable.getScrollX();
//                Log.e("scrollX", String.valueOf(scrollX));
//                headerScroll.scrollTo(scrollX, 0);
//            }
//        });
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
//                Log.e("scrollX", String.valueOf(scrollX));
                headerScroll.scrollTo(scrollX, 0);
            }
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }
        });
    }
    //Init Recycle VIew to show TARGET LIST
    private void initTargetList() {
        TargetlayoutManager = new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false);
        rvTarget = findViewById(R.id.rvTarget);
        rvTarget.setLayoutManager(TargetlayoutManager);
        targetAdapter = new TargetAdapter(MainActivity.this,BitmapList_show,this);
        rvTarget.setAdapter(targetAdapter);
    }
    //Init ID dialog to change ID
    private void openIDdialog(){
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(MainActivity.this);
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
                    Toast.makeText(MainActivity.this, "ID must have 5 letters!", Toast.LENGTH_SHORT).show();
                } else {
                    ID = new_ID;
                    editor.putString("ID", ID);
                    editor.commit();
                    tvid.setText(ID);
                    bluetoothServices.updateID(ID);
                    imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                    Toast.makeText(MainActivity.this, "ID changed successfully!", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
            }
        });
    }
    //Init Shooting result table Recycle View
    protected void initTable() {
        FixedGridLayoutManager manager = new FixedGridLayoutManager();
        manager.setTotalColumnCount(1);
        rvShootresult.setHasFixedSize(false);
        rvShootresult.setLayoutManager(manager);
//        ViewCompat.setNestedScrollingEnabled(rvShootresult, false);
        rvShootresult.addItemDecoration(new DividerItemDecoration(MainActivity.this, DividerItemDecoration.VERTICAL));
    }
    protected void init_Paints(){
        borderPaint.setStrokeWidth(8);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setColor((getResources().getColor(R.color.Red)));
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
                //bluetoothServices.sendShootResult();
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
    //<-----------------------------------Init Connection------------------------------>
    //USB SERVICE CONNECTION
    ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder iBinder) {
            Log.e(STATE, "onServiceConnected");
            BluetoothServices.CallService bider = (BluetoothServices.CallService) iBinder;
            bluetoothServices = bider.getService();
            idUSBConnectionService = true;
            bluetoothServices.ID_Phone = ID;
            if (!isRestart) {
                bluetoothServices.updateID(ID);
                openSetting();
            }
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.e(STATE, "onServiceConnected");
            idUSBConnectionService = false;
            bluetoothServices = null;
            Toast.makeText(MainActivity.this, "Bluetooth has disconnected", Toast.LENGTH_SHORT).show();
        }
    };
    // Initialize Message receiver
    private void initArduinoMessagereceiver() {   // initialize Broadcast receiver
        arduinoMessageReceiver = new ArduinoMessageReceiver(this);
        IntentFilter MessageFilter = new IntentFilter();
        MessageFilter.addAction("Shooting_result");
        MessageFilter.addAction("Load_target");
        MessageFilter.addAction("Show_lane");
        MessageFilter.addAction("Show_temper");
        registerReceiver(arduinoMessageReceiver, MessageFilter);
    }
    private void openSetting() {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent2 = new Intent(MainActivity.this, SettingActivity.class);
                startActivity(intent2);
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
    public void showTarget(String value, String time,String lane) { //Show target frame
        List<Shoot_result> new_shootList = new_shootList(shootList);
        while (value.charAt(0) == '0') value = value.substring(1);
        if (Target_count != 0){
            resultList.add(new_shootList);
            shootList.clear();
            shootAdapter = new ShootAdapter(MainActivity.this, shootList, this);
            updateResultTable();
            BitmapList_show.set(Target_previous-1,BitmapList_origin.get(Target_previous-1));;
        }
        setTimer(Integer.parseInt(time));
        target_frame.setName(value);
        target_frame.targetSize(value);
        Target_Frame tmp_target = new Target_Frame(target_frame);
        TargetList.add(tmp_target);
        Target_count++;
        Target_select = Target_count;
        Target_previous = Target_select;
        open_image(value);
        String targetName = is_Landscape?"Target "+value : value;
        tvTargetName.setText(targetName);
        if (value.equals("A11")) tvTargetName.setText("AARM-11");
        if (value.equals("A12")) tvTargetName.setText("AARM-12");
        mImageBorder.setGridsize(gridsize);
        tvFrameNo.setText(lane);
        laneList.add(lane);
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
            Bitmap alteredBmp = resizeBitmap(bitmap);
            BitmapList_origin.add(bitmap); // add origin bitmap to List View
            // add border on new bitmap
            alteredBmp =  alteredBmp.copy(Bitmap.Config.ARGB_8888, true);
            Canvas border_canvas = new Canvas(alteredBmp);
            border_canvas.drawRect(2, 2, alteredBmp.getWidth()-2,alteredBmp.getHeight()-2,borderPaint);
            BitmapList_show.add(alteredBmp); // add show bitmap to List View
            targetAdapter.notifyDataSetChanged();
            TargetlayoutManager.scrollToPosition(Target_count-1);
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
            updateResultTable();
            mImageBorder.Update_Result(id, x, y); //update result on UI
        }
    }
    @Override
    public void show_temper(String T) {
//        float temp = (float) T / 10;
        String temp = new StringBuilder(T).insert(2,".").toString();
        tvtemp.setText(temp);
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
        if (Target_select == Target_count) //select the last one
            selected_result = shootList.get(pos);
        else
            selected_result = resultList.get(Target_select - 1).get(pos);
        int focus_bullet = Integer.parseInt(selected_result.getNo());
        mImageBorder.setFocusBullet(focus_bullet);
        updateResultTable();
    }
    //On Target Image click event
    @Override
    public void onTargetClick(int pos) {
        update_selected_target(pos);
    }
    // update Target list
    protected void update_selected_target(int position){ // update table and bitmap with clicked Target
        String targetName, lane;
        Target_select = position+1;
        if (Target_select == Target_count) {// if selected target is the latest
            shootAdapter = new ShootAdapter(MainActivity.this, shootList, this); //update latest Shoot result
            mImageBorder.update_target(target_frame); // update latest target
        }
        else {
            shootAdapter = new ShootAdapter(MainActivity.this, resultList.get(position), this); //update Shoot result
            mImageBorder.update_target(TargetList.get(position)); //update Target Frame
        }
        mImageBorder.openImageBitmap(BitmapList_origin.get(position),Target_select);
        if (Target_select == Target_count) {// if selected target is the latest
            shootAdapter = new ShootAdapter(MainActivity.this, shootList, this); //update latest Shoot result
            mImageBorder.update_target(target_frame); // update latest target
            targetName = target_frame.getName();
        }
        else {
            shootAdapter = new ShootAdapter(MainActivity.this, resultList.get(position), this); //update Shoot result
            mImageBorder.update_target(TargetList.get(position)); //update Target Frame
            targetName  = TargetList.get(position).getName();
        }
        lane = laneList.get(position);
        tvFrameNo.setText(lane);
        mImageBorder.openImageBitmap(BitmapList_origin.get(position),Target_select);    //update target image
        updateResultTable();
        targetName = is_Landscape? "Target " + targetName : targetName;
        tvTargetName.setText( targetName );   //update target name
        if (targetName .equals("A11")) tvTargetName.setText("AARM-11");
        if (targetName .equals("A12")) tvTargetName.setText("AARM-12");
        //update Target list
        BitmapList_show.set(Target_previous-1,BitmapList_origin.get(Target_previous-1));
        Bitmap alteredBmp =  resizeBitmap(BitmapList_origin.get(position).copy(Bitmap.Config.ARGB_8888, true));
        Canvas border_canvas = new Canvas(alteredBmp);
        border_canvas.drawRect(2, 2, alteredBmp.getWidth()-2,alteredBmp.getHeight()-2,borderPaint);
        BitmapList_show.set(position,alteredBmp);
        targetAdapter.notifyDataSetChanged();
        Target_previous=Target_select;
    }
    //<-------------------------reuse function--------------------------->>
    private Bitmap resizeBitmap(Bitmap bm){
        int bheight = bm.getHeight();
        int bwidth = bm.getWidth();
        int newWidth=105,newHeight=105;
        Bitmap resizedBitmap1 = Bitmap.createScaledBitmap(bm,(int)bwidth,(int)bheight,true);
        float scaleWidth = ((float) newWidth) / bwidth;
        float scaleHeight = ((float) newHeight) / bheight;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(Math.min(scaleWidth, scaleHeight), Math.min(scaleWidth, scaleHeight));
        Bitmap resizedBitmap2 = Bitmap.createBitmap(
                resizedBitmap1, 0, 0, (int)bwidth, (int)bheight, matrix, false);
        Bitmap outputimage = Bitmap.createBitmap(newWidth,newHeight, Bitmap.Config.ARGB_8888);
        // "RECREATE" THE NEW BITMAP
        Canvas can = new Canvas(outputimage);
        can.drawBitmap(resizedBitmap2, ((float)(newWidth- resizedBitmap2.getWidth()) / 2), ((float)(newHeight - resizedBitmap2.getHeight()) / 2), null);
        return outputimage;
    }
    private void fakedata(){
        showTarget("7","00","1");
        shootList.add(new Shoot_result("1", "2", "3", "4", "5", "6"));
        shootList.add(new Shoot_result("1", "2", "3", "4", "5", "6"));
        shootList.add(new Shoot_result("1", "2", "3", "4", "5", "6"));
        shootList.add(new Shoot_result("1", "2", "3", "4", "5", "6"));
        shootList.add(new Shoot_result("1", "2", "3", "4", "5", "6"));
        shootList.add(new Shoot_result("1", "2", "3", "4", "5", "6"));
        //showTarget("18","00","1");
        shootList.add(new Shoot_result("1", "2", "3", "4", "5", "6"));
        shootList.add(new Shoot_result("1", "2", "3", "4", "5", "6"));
        shootList.add(new Shoot_result("1", "2", "3", "4", "5", "6"));
        shootList.add(new Shoot_result("1", "2", "3", "4", "5", "6"));
        shootList.add(new Shoot_result("1", "2", "3", "4", "5", "6"));
        shootAdapter = new ShootAdapter(MainActivity.this, shootList, this);
        updateResultTable();
    }
    private void updateResultTable(){
        rvShootresult.setAdapter(shootAdapter);         //update result table
        scrollX =0;
        headerScroll.scrollTo(0, 0);
    }
    @SuppressLint("NewApi")
    private  void checkBTPermission()
    {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP){
            int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
            permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
            if (permissionCheck != 0){
                this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},1001);
            }
        }
    }
    private boolean isMyServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (service.service.getClassName().contains(".Services.BluetoothServices")) {
                return true;
            }
        }
        return false;
    }

}