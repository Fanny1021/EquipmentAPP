package com.fanny.equipmentapp.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.fanny.equipmentapp.MainActivity;
import com.fanny.equipmentapp.service.LoginService;

/**
 * Created by Fanny on 17/6/3.
 */

public class LanuchReciever extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent tIntent = new Intent(context , LoginService.class);
        // 启动指定Service
        context.startService(tIntent);
    }
}
