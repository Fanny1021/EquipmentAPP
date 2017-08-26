package com.fanny.equipmentapp.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.fanny.equipmentapp.MainActivity;
import com.fanny.equipmentapp.MyApp;
import com.fanny.equipmentapp.SpUtil;
import com.fanny.equipmentapp.receiver.LanuchReciever;
import com.hyphenate.EMCallBack;
import com.hyphenate.EMConnectionListener;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMOptions;
import com.hyphenate.exceptions.EMNoActiveCallException;
import com.hyphenate.exceptions.HyphenateException;

import cn.bmob.v3.Bmob;

/**
 * Created by Fanny on 17/6/3.
 */

public class LoginService extends Service{

    private static final String TAG = "LoginService";
    public static final String username = "woman";
    public static final String password = "123456";
    public static boolean isConnect=false;
    public static boolean isLogin=false;

//    public static EMClient instance;

//    public static EMClient getInstance() {
//        return instance;
//    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //初始化bmob
        Bmob.initialize(this, "5737aead2c235755f21e71e3630359df");

        //初始化环信
        EMOptions options = new EMOptions();
        //默认添加好友时，是否验证
        options.setAcceptInvitationAlways(false);
        //设置是否自动登录
        options.setAutoLogin(true);
        //初始化
        EMClient.getInstance().init(this, options);
//        instance.init(this,options);
        //在做打包混淆时，关闭debug模式，避免消耗不必要的资源
        EMClient.getInstance().setDebugMode(true);
//        instance.setDebugMode(true);
        Log.e(TAG,"第一次启动service");

        /**
         * 后台登录环信服务器
         */
//        initlogin();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        /**
         * 后台登录环信服务器
         */
        initlogin();

//        //注册一个监听连接状态的listener
//        EMClient.getInstance().addConnectionListener(new MyConnectionListener());
//        return super.onStartCommand(intent, flags, startId);
        /**
         * 保持用户登录在环信服务器
         */
//        Toast.makeText(this, "Service Started", Toast.LENGTH_LONG).show();
//        initlogin();
        Log.e(TAG,"后台服务");
        Log.e(TAG,"用户是否与服务器连接："+isConnect);
        return START_STICKY;


    }
    private class MyConnectionListener implements EMConnectionListener {

        @Override
        public void onConnected() {

//            Log.e("service","islogin:"+ MyApp.islogin);

            isConnect = true;
            MyApp.isConnect=true;

            Log.e(TAG,"监听连接结果"+isConnect);


        }

        @Override
        public void onDisconnected(final int errorCode) {
            isConnect = false;
            MyApp.isConnect=false;

            Log.e(TAG,"监听连接结果"+isConnect);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG,"service被kil，服务已停止");
    }

    private void initlogin() {
        // 登陆到环信服务器(前提是：产品对应的用户登录名和密码已经在本地和远程服务器注册过，直接给用户提供账号和密码即可)
        if (!TextUtils.isEmpty(username) && !TextUtils.isEmpty(password)) {

            EMClient.getInstance().login(username, password, new EMCallBack() {
                @Override
                public void onSuccess() {
                    EMClient.getInstance().groupManager().loadAllGroups();
                    EMClient.getInstance().chatManager().loadAllConversations();
                    Log.e(TAG, "登录聊天服务器成功！");

                    isLogin=true;
                    MyApp.islogin=true;

                    //保存用户信息
                    SpUtil.putString(getApplicationContext(), SpUtil.USERNAME, username);
                    SpUtil.putString(getApplicationContext(), SpUtil.PASSWORD, password);

                    EMClient.getInstance().addConnectionListener(new MyConnectionListener());

                }

                @Override
                public void onError(int code, String error) {

                    isLogin=false;
                    MyApp.islogin=false;

                    Log.e("TAG", "登录聊天服务器失败！");
                    Log.e("TAG", error);

                }

                @Override
                public void onProgress(int progress, String status) {

                }
            });
        }
    }

    private void initbroadcast() {
        IntentFilter callFilter = new
                IntentFilter(EMClient.getInstance().callManager().getIncomingCallBroadcastAction());
        registerReceiver(new CallReceiver(), callFilter);

    }

    private class CallReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String from = intent.getStringExtra("from");
            String type = intent.getStringExtra("type");

            //更新ui显示来电状态
//            Toast.makeText(MonitorActivity.this, "用户" + from + "来电", Toast.LENGTH_LONG).show();

            Log.e(TAG,"用户" + from + "来电");
//            来电铃声设置
//            setMyRingtone("/RingTone/a.mp3");
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    rt.play();
//                }
//            }).start();

//            new Handler().postDelayed(new Runnable() {
//
//                public void run() {
//                    ReceiveVideo();
////                    rt.stop();
//
//                }
//
//            }, delayTime);

        }
    }


    /**
     * 自动接听来电调用以下方法
     */
    private void ReceiveVideo() {

        /**
         * 接听通话
         * @throws EMNoActiveCallException
         * @throws EMNetworkUnconnectedException
         */
        try {
            EMClient.getInstance().callManager().answerCall();
        } catch (EMNoActiveCallException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}
