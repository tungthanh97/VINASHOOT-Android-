package com.example.vns_handheld004.View;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Insets;
import android.graphics.Matrix;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowInsets;

import android.view.WindowMetrics;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;


import com.example.vns_handheld004.R;
import com.example.vns_handheld004.Target_Frame;

import java.util.ArrayList;
import java.util.List;


public class PixelGridView extends com.ortiz.touchview.TouchImageView {
    private int numColumns, numRows ;
    private int cellWidth, cellHeight;
    private int radius=0,maxRadius=0,distance,delayMilliseconds;
    private float Image_w,Image_h;
    private float cw,ch;
    private int target_width,target_height, focusBullet=0; //Target real size
    private float bwidth,bheight;
    private List<Bullet_point> Latest_Bullet_points = new ArrayList<>(); //Latest Bullet point
    private List<Bullet_point> Bullet_points = new ArrayList<>();  //Bullet points on the view Target
    private ArrayList<List<Bullet_point>> Bullet_list = new ArrayList<>(); //List of bullet points
    private Canvas alteredCanvas;
    private Bitmap bitmap;
    Context context;
    List<Integer> alphas = new ArrayList<>();
    List<Integer> spreadRadius = new ArrayList<>();
    Paint centerPaint,spreadPaint;
    // image default width and height
    int newHeight = 750;
    int newWidth = 1080;
    private int gridsize= 50;
    //Declare
    public PixelGridView(Context context) {
        this(context, null);
        this.setWillNotDraw(false);
    }

    public PixelGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.PixelGridView, 0, 0);
        int rows = a.getInt(R.styleable.PixelGridView_row, 0);
        int columns = a.getInt(R.styleable.PixelGridView_column, 0);
//        newWidth = getScreenWidth((Activity)context);         // take screen size
        bitmap = Bitmap.createBitmap(newWidth,newHeight,Bitmap.Config.ARGB_8888);;
        delayMilliseconds= a.getInt(R.styleable.PixelGridView_spread_delay_milliseconds, delayMilliseconds);
        int centerColor = a.getColor(R.styleable.PixelGridView_spread_center_color, ContextCompat.getColor(context, R.color.Red));
        int spreadColor = a.getColor(R.styleable.PixelGridView_spread_spread_color, ContextCompat.getColor(context, R.color.Red));
        distance = a.getInt(R.styleable.PixelGridView_spread_distance, distance);
        setNumColumns(columns);
        setNumRows(rows);
        a.recycle();
        centerPaint = new Paint();
        centerPaint.setColor(centerColor);
        centerPaint.setAntiAlias(true);
        // Initially opaque with diffusion distance of 0
        alphas.add(255);
        spreadRadius.add(0);
        spreadPaint = new Paint();
        spreadPaint.setAntiAlias(true);
        spreadPaint.setAlpha(255);
        spreadPaint.setColor(spreadColor);

    }

    public void setNumColumns(int numColumns) {
        this.numColumns = numColumns;
    }

    public void setGridsize(int gridsize) {
        this.gridsize = gridsize;
        if (gridsize == 0) {
            this.numColumns = 1;
            this.numRows = 1;
        }
        else{
            this.numColumns = target_width/gridsize;
            this.numRows = target_height/gridsize;
        }
        calculateDimensions();
    }

    public void setNumRows(int numRows) {
        this.numRows = numRows;
    }

    public void setFocusBullet(int focusBullet) {
        this.focusBullet = focusBullet;
        invalidate();
    }
    public int getFocusBullet() {
        return focusBullet;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        calculateDimensions();
        super.onSizeChanged(w, h, oldw, oldh);
    }

    private void calculateDimensions() {
        if (numColumns < 1 || numRows < 1) {
            return;
        }

        float scale1 = ((float) newWidth)/bwidth;
        float scale2 = ((float) newHeight)/bheight;
        float scale = Math.min(scale1,scale2);
        Image_w = bwidth * scale;
        Image_h = bheight * scale;
        cw = (newWidth - Image_w) /2;
        ch = (newHeight- Image_h)/2;
        cellWidth = (int) (((float)Image_w) / target_width * gridsize) ;
        cellHeight = (int)(((float)Image_h) / target_height * gridsize);
        invalidate();
    }

//    @Override
    protected void onDraw(Canvas canvas) {
        if (bitmap == null) return;
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        canvas.drawColor(Color.WHITE);
        Bitmap alteredBmp =  bitmap.copy(Bitmap.Config.ARGB_8888, true); //Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        alteredCanvas = new Canvas(alteredBmp);
        drawalter();
        super.setImageBitmap(alteredBmp);
        super.onDraw(canvas);

    }
    protected void drawalter(){
        if (numColumns == 0 || numRows == 0) {
            return;
        }
//
//        Bitmap toDisk = Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);
//        canvas.setBitmap(toDisk);
        //init Paint
        Paint linePaint = new Paint();
        linePaint.setStrokeWidth(1);
        linePaint.setColor((getResources().getColor(R.color.Silver)));
        alteredCanvas.save();
        int y_center = (int)(ch + Image_h);
        int x_center = (int) newWidth /2 ;
        int new_numColumns = numColumns/2  ;

        for (int i = 0; i <= new_numColumns; i++) {
            alteredCanvas.drawLine(x_center + i * cellWidth, y_center, x_center + i * cellWidth, (int) ch, linePaint);
            alteredCanvas.drawLine(x_center - i * cellWidth, y_center, x_center - i * cellWidth, (int) ch, linePaint);
        }

        for (int i = 1; i <= numRows; i++) {
            alteredCanvas.drawLine(cw, ch + Image_h - i * cellHeight, cw + Image_w, ch + Image_h - i * cellHeight, linePaint);
        }
        for(Bullet_point p : Bullet_points) {
            if (p.getId() == focusBullet)
                drawfocus(alteredCanvas,p.getX(),p.getY(),p.getSize());
            else
                p.draw_point(context, alteredCanvas, Image_w, Image_h, cw,ch);
        }
        alteredCanvas.restore();
    }
    // create copy instance of Bullet list
    private List<Bullet_point> copy_Bullet_list(List<Bullet_point> origin){
        List<Bullet_point> copy = new ArrayList<>();
        for(Bullet_point bullet_point:origin)
            copy.add(bullet_point);
        return copy;
    }
    public void update_target(Target_Frame target_frame){
        this.target_width = target_frame.gettarget_width();
        this.target_height = target_frame.gettarget_height();
        if (gridsize == 0) {
            this.numColumns = 1;
            this.numRows = 1;
        }
        else{
            this.numColumns = target_width/gridsize;
            this.numRows = target_height/gridsize;
        }
        calculateDimensions();
    }
    public void setNewImageBitmap(Bitmap bm) {
        List<Bullet_point> copy_latest_list = copy_Bullet_list(Latest_Bullet_points);
        Bullet_list.add(copy_latest_list);
        Bullet_points.clear();
        Latest_Bullet_points.clear();
        bm = resizeBitmap(bm);
        this.bitmap=bm;
        setImageResource(android.R.color.transparent);
        focusBullet = 0;
        super.setImageBitmap(bm);
    }

    public void openImageBitmap(Bitmap bm,int position) {
        Log.e("Cell width",Integer.toString(cellWidth));
        Log.e("Cell height",Integer.toString(cellHeight));
        if (position == Bullet_list.size()) //if the position is the latest
            Bullet_points = copy_Bullet_list(Latest_Bullet_points);
        else{
            Bullet_points = copy_Bullet_list(Bullet_list.get(position)); //load bullet points data
        }
        bm = resizeBitmap(bm);
        this.bitmap=bm;
        setImageResource(android.R.color.transparent);
        focusBullet = 0;
        super.setImageBitmap(bm);
    }
    public void Update_Result(int count, int x, int y){ //update result and UI
        Bullet_point new_point= new Bullet_point(count, x,y,8,target_width,target_height);
        focusBullet = count;
        Bullet_points.add(new_point);
        invalidate();
    }
    public void update_latest_result(int count, int x, int y){ //update and save latest result
        Bullet_point new_point= new Bullet_point(count, x,y,8,target_width,target_height);
        Latest_Bullet_points.add(new_point);
    }
    private Bitmap resizeBitmap(Bitmap bm){
        bheight = bm.getHeight();
        bwidth = ((float)target_width)/target_height* bheight;
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
    private static int getScreenWidth(@NonNull Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowMetrics windowMetrics = activity.getWindowManager().getCurrentWindowMetrics();
            Insets insets = windowMetrics.getWindowInsets()
                    .getInsetsIgnoringVisibility(WindowInsets.Type.systemBars());
            return windowMetrics.getBounds().width() - insets.left - insets.right;
        } else {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            return displayMetrics.widthPixels;
        }
    }
    private void drawfocus(Canvas canvas,int X,int Y,int size)//draw focus point
     {
        float ratio_w = Image_w/ target_width;
        float ratio_h = Image_h/ target_height;
        int targetX = (int)(ratio_w* (X + ((float)target_width)/2) + cw);
        int targetY = (int)(ratio_h* (target_height-Y) + ch);
        float ratio = Math.max(ratio_w, ratio_h);
        radius = (int)((ratio*((float) size)/2));
        maxRadius = radius*2;
        if ((targetX > (Image_w+cw)) || (targetX < cw) || (targetY > (Image_h+ch)) || (targetY < ch))
            Toast.makeText(context,"Invalid Result", Toast.LENGTH_SHORT);
        else {
            drawCircle(canvas,targetX,targetY);
        }
    }
    private void drawCircle(Canvas canvas,int centerX,int centerY)  //draw circle around focus point
    {
        for (int i = 0; i < spreadRadius.size(); i++) {
            int alpha = alphas.get(i);
            spreadPaint.setAlpha(alpha);
            int width = spreadRadius.get(i);
            // Drawing a diffused circle
            canvas.drawCircle(centerX, centerY, radius + width, spreadPaint);
            // Every time the radius of diffusion circle increases, the circular transparency decreases.
            if (alpha > 0 && width < maxRadius*3) {
                alpha = alpha - distance-10 > 0 ? alpha - distance -10: 1;
                alphas.set(i, alpha);
                spreadRadius.set(i, width + distance);
            }
        }
        // When the radius of the outermost diffusion circle reaches the maximum radius, a new diffusion circle is added.
        if (spreadRadius.get(spreadRadius.size() - 1) > radius) {
            spreadRadius.add(0);
            alphas.add(255);
        }
        // Over 5 diffusion circles, delete the first drawn circle, that is, the outermost circle
        if (spreadRadius.size() >= 5) {
            alphas.remove(0);
            spreadRadius.remove(0);
        }
        // The circle in the middle
        canvas.drawCircle(centerX, centerY, radius, centerPaint);
        // Delayed updating to achieve diffuse visual impairment
        final Handler handler = new Handler(Looper.getMainLooper());
        postInvalidateDelayed(delayMilliseconds);
    }
}



