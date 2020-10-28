package com.example.vns_handheld004.Broadcast;

import com.example.vns_handheld004.Model.Shoot_result;

public interface MyBroadcastListener {
    public void connectStatus(String value);
    public void showTarget(String value,String time);
    public void shootResults(Shoot_result shoot_result);
    public void show_temper(int T);
    public void show_lane(int L);
}
