package com.example.vns_handheld004;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import android.widget.Button;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.example.vns_handheld004.R;

public class TargetDialog extends AppCompatDialogFragment implements View.OnClickListener {
    private Spinner spin;
    private View view;
    private int Gridsize; //the selected grid size
    private int[] btn_id = {R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3,R.id.btn4,R.id.btn5};
    private Button[] btn = new Button[6];
    private Button btn_unfocus;
    private static final String STATE = "TargetDialog";
    private TargetDialogListener listener;
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        view = inflater.inflate(R.layout.dialog_target,null);
        Log.e(STATE,"OnCreateDialog");
        Gridsize = getArguments().getInt("Gridsize");
        if (savedInstanceState != null)
            Gridsize = savedInstanceState.getInt("Gridsize");
        builder.setView(view)
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.applysize(Gridsize);
                    }
                });
        initView();
        return builder.create();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        Log.e(STATE,"onAttach");
        super.onAttach(context);
        try{
            listener = (TargetDialogListener)context;
        } catch(ClassCastException e) {
            throw new ClassCastException(context.toString() + "must implement TargetDialogListener");
        }
    }
    @Override
    public void onClick(View v) {
        //setForcus(btn_unfocus, (Button) findViewById(v.getId()));
        //Or use switch
        switch (v.getId()){
            case R.id.btn0 :
                setFocus(btn_unfocus, btn[0]);
                Gridsize = 0;
                break;

            case R.id.btn1 :
                setFocus(btn_unfocus, btn[1]);
                Gridsize = 50;
                break;

            case R.id.btn2 :
                setFocus(btn_unfocus, btn[2]);
                Gridsize = 100;
                break;

            case R.id.btn3 :
                setFocus(btn_unfocus, btn[3]);
                Gridsize = 200;
                break;
            case R.id.btn4 :
                setFocus(btn_unfocus, btn[4]);
                Gridsize = 300;
                break;
            case R.id.btn5 :
                setFocus(btn_unfocus, btn[5]);
                Gridsize = 400;
                break;
        }
    }

    private void setFocus(Button btn_unfocus, Button btn_focus){
        btn_unfocus.setTextColor(getResources().getColor(R.color.Black));
        btn_unfocus.setBackgroundColor(getResources().getColor(R.color.White));
        btn_focus.setTextColor(getResources().getColor(R.color.White));
        btn_focus.setBackgroundColor(getResources().getColor(R.color.Gray));
        this.btn_unfocus = btn_focus;
    }
    //Init Spinner
    private void initView(){
        Log.e(STATE,"initView");
        for(int i = 0; i < btn.length; i++){
            btn[i] = (Button) view.findViewById(btn_id[i]);
            btn[i].setBackgroundColor(getResources().getColor(R.color.White));
            btn[i].setOnClickListener(this);
        }
        btn_unfocus = btn[1];
        switch (Gridsize){
            case 0:
                setFocus(btn_unfocus, btn[0]);
                break;
            case 50:
                setFocus(btn_unfocus, btn[1]);
                break;
            case 200:
                setFocus(btn_unfocus, btn[3]);
                break;
            case 300:
                setFocus(btn_unfocus, btn[4]);
                break;
            case 400:
                setFocus(btn_unfocus, btn[5]);
                break;
            default:
                setFocus(btn_unfocus, btn[2]);
                break;
        }
    }
    public interface TargetDialogListener {
        void applysize(int value);
    }
}
