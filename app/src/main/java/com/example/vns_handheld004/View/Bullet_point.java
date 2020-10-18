package com.example.vns_handheld004.View;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.widget.Toast;

import com.example.vns_handheld004.R;

public class Bullet_point {
    private int X,Y,Size,Target_width,Target_height,id;
    Paint brush = new Paint(), brush2 = new Paint();
    //Construct
    public Bullet_point(Bullet_point that){
        this(that.getId(),that.getX(),that.getY(),that.getSize(),that.gettarget_width(),that.gettarget_height());
    }
    public void settarget_width(int target_width) {
        this.Target_width = target_width;
    }

    public int gettarget_width() {
        return this.Target_width;
    }

    public void settarget_height(int target_height) {
        this.Target_height = target_height;
    }

    public int gettarget_height() {
        return this.Target_height;
    }

    public void setX(int x) {
        this.X = x;
    }

    public int getX() {
        return this.X;
    }

    public void setY(int y) {
        this.Y = y;
    }

    public int getY() {
        return this.Y;
    }

    public void setSize(int size) {
        this.Size = size;
    }

    public int getSize() {
        return this.Size;
    }

    public void setId(int id) {
        this.id = id;
    }
    public int getId(){ return this.id;}
    public Bullet_point(int id, int x, int y, int size, int target_width, int target_height){
        this.id = id;
        this.X = x;
        this.Y = y;
        this.Size = size;
        this.Target_width = target_width;
        this.Target_height = target_height;
    }
    public void draw_point(Context context, Canvas canvas, float width, float height,  float cw, float ch){
        brush.setColor(context.getResources().getColor(R.color.Red));
        brush2.setColor(context.getResources().getColor(R.color.LightRed));
        float ratio_w = width/ Target_width;
        float ratio_h = height/ Target_height;
        int targetX = (int)(ratio_w* (X + ((float)Target_width)/2) + cw);
        int targetY = (int)(ratio_h* (Target_height-Y) + ch);
        float ratio = Math.max(ratio_w, ratio_h);
        int targetSize = (int)((ratio*((float) Size)/2));
        if ((targetX > (width+cw)) || (targetX < cw) || (targetY > (height+ch)) || (targetY < ch))
            Toast.makeText(context,"Invalid Result", Toast.LENGTH_SHORT);
        else {
            canvas.drawCircle(targetX, targetY, targetSize, brush);
            canvas.drawCircle(targetX, targetY, (float) (targetSize*1.5), brush2);
        }
    }
}
