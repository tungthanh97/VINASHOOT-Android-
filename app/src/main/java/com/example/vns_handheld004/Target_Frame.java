package com.example.vns_handheld004;

import android.content.Context;
import android.widget.Toast;

public class Target_Frame {
    private int target_height, target_width;
    private String name;
    Context context;
    //Construct class
    public Target_Frame(Context context){
        this.context = context;
    }

    public Target_Frame(Target_Frame that) {
        this(that.gettarget_width(),that.gettarget_height());
    }
    public Target_Frame(int width, int height){
        this.target_width = width;
        this.target_height = height;
    }
    public int gettarget_height() {
        return this.target_height;
    }

    public void settarget_height(int target_height) {
        this.target_height = target_height;
    }

    public int gettarget_width() {
        return this.target_width;
    }

    public void settarget_width(int target_width) {
        this.target_width = target_width;
    }

    public String getName() {
        return name;
    }

    //return height and width of target
    public void targetSize(String value){
        this.name = value;
        switch (value){
            case "1":
                this.target_width = 150;
                this.target_height = 150;

                break;
            case "2":
                this.target_width = 300;
                this.target_height = 300;
                break;
            case "4":
            case "6":
                this.target_width = 420;
                this.target_height = 420;
                break;
            case "4b":
            case "4c":
                this.target_width = 500;
                this.target_height = 500;
                break;
            case "4d":
                this.target_width = 430;
                this.target_height = 550;
                break;
            case "5":
                this.target_width = 230;
                this.target_height = 300;
                break;
            case "5b":
                this.target_width = 250;
                this.target_height =250;
                break;
            case "6b":
                this.target_width = 250;
                this.target_height = 420;
                break;
            case "7":
            case "7b":
                this.target_width = 420;
                this.target_height = 1000;
                break;
            case "7c":
                this.target_width = 500;
                this.target_height = 1000;
                break;
            case "7d":
                this.target_width = 250;
                this.target_height = 1000;
                break;
            case "8":
                this.target_width = 420;
                //this.target_height = 150;//có' chân
                this.target_height = 1000;//không chân
                break;
            case "8b":
                this.target_width = 430;
                this.target_height = 1500;
                break;
            case "8c":
                this.target_width = 500;
                this.target_height = 1500;
                break;
            case "8d":
                this.target_width = 250;
                this.target_height = 1500;
                break;
            case "8e":
            case "8g":
                this.target_width = 430;
                this.target_height = 1120;
                break;
            case "9":
                this.target_width = 900;
                this.target_height = 1000;
                break;
            case "8h":
                this.target_width = 1160;
                this.target_height = 1120;
                break;
            case "10":
                this.target_width = 750;
                this.target_height = 550;
                break;
            case "11":
                this.target_width = 1500;
                this.target_height = 1200;
                break;
            case "11b":
                this.target_width = 1500;
                this.target_height = 600;
                break;
            case "12":
                this.target_width = 3000;
                this.target_height = 1900;
                break;
            case "12b":
                this.target_width = 5500;
                this.target_height = 1900;
                break;
            case "12c":
                this.target_width = 1700;
                this.target_height = 500;
                break;
            case "14":
                this.target_width = 2500;
                this.target_height = 2200;
                break;
            case "14b":
                this.target_width = 4200;
                this.target_height = 2200;
                break;
            case "15":
                this.target_width = 1700;
                this.target_height = 900;
                break;
            case "16":
                this.target_width = 700;
                this.target_height = 300;
                break;
            case "17":
                this.target_width = 1800;
                this.target_height = 1500;
                break;
            case "17b":
                this.target_width = 2500;
                this.target_height = 1500;
                break;
            case "17c":
                this.target_width = 1800;
                this.target_height = 900;
                break;
            case "18":
                this.target_width = 2900;
                this.target_height = 730;
            case "18b":
                this.target_width = 4000;
                this.target_height = 1100;
                break;
            case "19":
            case "00":
                this.target_width = 1000;
                this.target_height = 1500;
                break;
            default:
                Toast.makeText(context,"Target Frame not exist",Toast.LENGTH_SHORT);
        }
    }

}
