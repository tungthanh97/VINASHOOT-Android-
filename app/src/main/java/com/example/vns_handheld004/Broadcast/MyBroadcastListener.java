package com.example.vns_handheld004.Broadcast;

import com.example.vns_handheld004.Model.Shoot_result;

public interface MyBroadcastListener {
    public void connectStatus(String value);
    public void showTarget(String value,String time,String lane);
    public void shootResults(Shoot_result shoot_result);
    public void show_temper(String T);
}
