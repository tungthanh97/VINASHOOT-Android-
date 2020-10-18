package com.example.vns_handheld004;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.example.vns_handheld004.R;

public class IdDialog extends AppCompatDialogFragment {
    private static final String STATE = "IdDialog";
    private View view;
    private AlertDialog.Builder builder;
    Context context;
    private EditText etid;
    private TextView tvWarning;
    SharedPreferences sharedPreferences;
    private  IdDialogListener listener;
    SharedPreferences.Editor editor;
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context=context;
        initPreferences();
        try{
            listener = (IdDialog.IdDialogListener) context;
        } catch(ClassCastException e) {
            throw new ClassCastException(context.toString() + "must implement TargetDialogListener");
        }
    }
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        view = inflater.inflate(R.layout.dialog_id,null);
        Log.e(STATE,"OnCreateDialog");
        builder.setView(view)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String ID = etid.getText().toString();
                        if (ID.length()<5) {
                            tvWarning.setText("ID must have 5 letters");
                        }
                        else {
                            editor.putString("ID", ID);
                            editor.commit();
                            listener.applyID();
                        }
                    }
                });
        initView();
        return builder.create();
    }
    //init View
    private void initView(){
        tvWarning = view.findViewById(R.id.tvWarning);
        etid = (EditText) view.findViewById(R.id.etID);
        etid.requestFocus();
        String savedData = sharedPreferences.getString("ID", "YA001");
        etid.setText(savedData);
//        getDialog().getWindow().setSoftInputMode(
//                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }
//    init shared prefrerences
    private void initPreferences() {
//        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences = getActivity().getSharedPreferences("IDs",0);
        editor = sharedPreferences.edit();
    }
    public interface IdDialogListener {
        void applyID();
    }
}