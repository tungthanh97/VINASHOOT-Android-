package com.example.vns_handheld004.Model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Shoot_result
{
    public String No;
    public String X;
    public String Y;
    public String V;
    public String Mark;
    public String M_H;
    public String Time;
    public String getNo() {
        return No;
    }

    public String getX() {
        return X;
    }

    public String getY() {
        return Y;
    }

    public Shoot_result(String No, String X, String Y, String V, String Mark, String M_H)
    {
        this.No = No;
        this.X = X;
        this.Y = Y;
        this.V = V;
        this.Mark = Mark;
        this.M_H = M_H;
        this.Time = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
    }
    public Shoot_result(String[] result){
        this.No = result[0];
        this.X = result[1];
        this.Y = result[2];
        this.V = result[3];
        this.Mark = result[4];
        this.M_H = result[5];
        this.Time = result[6];
    }
}