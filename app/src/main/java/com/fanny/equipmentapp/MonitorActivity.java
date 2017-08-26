package com.fanny.equipmentapp;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.hyphenate.EMConnectionListener;
import com.hyphenate.EMError;
import com.hyphenate.chat.EMCallManager;
import com.hyphenate.chat.EMCallStateChangeListener;
import com.hyphenate.chat.EMClient;
import com.hyphenate.exceptions.EMNoActiveCallException;
import com.hyphenate.exceptions.EMServiceNotReadyException;
import com.hyphenate.media.EMLocalSurfaceView;
import com.hyphenate.media.EMOppositeSurfaceView;
import com.hyphenate.util.NetUtils;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MonitorActivity extends Activity implements View.OnClickListener {
    FrameLayout flContent;
    Button callReceive;
    Button callEnd;
    Button callCall;
    ImageView switchcamera;

    private LinearLayout lllarge;
    private LinearLayout llsmall;
    private EMOppositeSurfaceView remoteSurfaceView;  // 对方视频
    private EMLocalSurfaceView localSurfaceView;
    private View itemview;
    private EditText callnum;
    private Ringtone rt;
    private Uri uri;
    private Uri newUri;
    private CallReceiver myCallReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitor);
        lllarge = (LinearLayout) findViewById(R.id.ll1);
        llsmall = (LinearLayout) findViewById(R.id.ll2);

        remoteSurfaceView = (EMOppositeSurfaceView) findViewById(R.id.oppositesurface);
        localSurfaceView = (EMLocalSurfaceView) findViewById(R.id.localsurfaceview);
        localSurfaceView.setZOrderOnTop(true);
        switchcamera = (ImageView) findViewById(R.id.switchcamera);

        callCall = (Button) findViewById(R.id.call_call);
        callReceive = (Button) findViewById(R.id.call_receive);
        callEnd = (Button) findViewById(R.id.call_end);
        callCall.setOnClickListener(this);
        callReceive.setOnClickListener(this);
        callEnd.setOnClickListener(this);
        switchcamera.setOnClickListener(this);

        setRingtone("a.mp3");

        initSurface();
        initReceiveVideo();
        initcalllistener();

        //注册一个监听连接状态的listener
        EMClient.getInstance().addConnectionListener(new MyConnectionListener());
    }

    /**
     * 设置视频通话铃声
     */
    private void setRingtone(String path) {

        File sdfile = new File(path);
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DATA, sdfile.getAbsolutePath());
        values.put(MediaStore.MediaColumns.TITLE, sdfile.getName());
        values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/*");
        values.put(MediaStore.Audio.Media.IS_RINGTONE, true);
        values.put(MediaStore.Audio.Media.IS_NOTIFICATION, false);
        values.put(MediaStore.Audio.Media.IS_ALARM, false);
        values.put(MediaStore.Audio.Media.IS_MUSIC, false);

//        uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        uri = MediaStore.Audio.Media.getContentUriForPath(sdfile.getAbsolutePath());
        newUri = this.getContentResolver().insert(uri, values);
        RingtoneManager.setActualDefaultRingtoneUri(this, RingtoneManager.TYPE_RINGTONE, newUri);
        rt = RingtoneManager.getRingtone(this, newUri);

    }


    //实现ConnectionListener接口
    private class MyConnectionListener implements EMConnectionListener {
        @Override
        public void onConnected() {
        }

        @Override
        public void onDisconnected(final int error) {
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    if (error == EMError.USER_REMOVED) {
                        // 显示帐号已经被移除
                    } else if (error == EMError.USER_LOGIN_ANOTHER_DEVICE) {
                        // 显示帐号在其他设备登录
                    } else {
                        if (NetUtils.hasNetwork(MonitorActivity.this)) {
                            //连接不到聊天服务器
                        } else {
                            //当前网络不可用，请检查网络设置
                        }

                    }
                }
            });
        }
    }

    private void initcalllistener() {
        EMClient.getInstance().callManager().addCallStateChangeListener(new EMCallStateChangeListener() {
            @Override
            public void onCallStateChanged(CallState callState, CallError error) {
                switch (callState) {
                    case CONNECTING: // 正在连接对方

            new Thread(new Runnable() {
                @Override
                public void run() {
                    rt.play();
                }
            }).start();
                        Log.e("MainActivity", "正在连接对方");
                        break;
                    case CONNECTED: // 双方已经建立连接

                        Log.e("MainActivity", "双方已经建立连接");
                        break;

                    case ACCEPTED: // 电话接通成功
                        //开始纪录通话时间

                        //获取到来电通知后，自动接通来电
//                        new Thread(new Runnable() {
//                            @Override
//                            public void run() {
//                                rt.stop();
//                            }
//                        }).start();

                        Log.e("MainActivity", "电话接通成功");
                        break;
                    case DISCONNECTED: // 电话断了
                        //结束纪录通话时间
                        Log.e("MainActivity", "电话断了");
                        //释放资源
                        releaseData();

//                        finish();
                        break;
                    case NETWORK_UNSTABLE: //网络不稳定
                        if (error == CallError.ERROR_NO_DATA) {
                            //无通话数据
                            Toast.makeText(MonitorActivity.this, "网络不稳定", Toast.LENGTH_LONG).show();
                        } else {
                        }
                        break;
                    case NETWORK_NORMAL: //网络恢复正常

                        break;
                    default:
                        break;
                }

            }
        });
    }

    /**
     * 挂断电话后释放所有通话资源
     */
    private void releaseData() {

        /**
         * 释放完毕，初始化视频界面
         */
        initSurface();
    }


    private void initSurface() {

        EMClient.getInstance().callManager().setSurfaceView(localSurfaceView, remoteSurfaceView);
        EMClient.getInstance().callManager().getCallOptions().setMaxVideoFrameRate(30);
        EMCallManager.EMVideoCallHelper callHelper = EMClient.getInstance().callManager().getVideoCallHelper();


    }


    //注册监听来电广播
    private void initReceiveVideo() {
        IntentFilter callFilter = new
                IntentFilter(EMClient.getInstance().callManager().getIncomingCallBroadcastAction());
        myCallReceiver = new CallReceiver();
        registerReceiver(myCallReceiver, callFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(myCallReceiver);
    }





    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.call_call:

                break;
            case R.id.call_receive:
                //接听视频通话
                ReceiveVideo();
                break;
            case R.id.call_end:
                //挂断结束视频通话
                EndVedio();
                break;
            case R.id.switchcamera:
                //切换前置和后置摄像头
                EMClient.getInstance().callManager().switchCamera();
                break;
        }

    }

    private void EndVedio() {
        /**
         * 挂断通话
         */
        try {
            EMClient.getInstance().callManager().endCall();
            Toast.makeText(MonitorActivity.this, "通话结束", Toast.LENGTH_LONG).show();
        } catch (EMNoActiveCallException e) {
            e.printStackTrace();
        }
    }

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

    int delayTime = 8000;

    private class CallReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String from = intent.getStringExtra("from");
            String type = intent.getStringExtra("type");

            //自定义toast显示来电状态
            Toast.makeText(MonitorActivity.this, "用户" + from + "来电", Toast.LENGTH_LONG).show();

            new Handler().postDelayed(new Runnable() {

                public void run() {
                    ReceiveVideo();
//                    rt.stop();

                }

            }, delayTime);



        }
    }

    //拨打视频通话
    private void SendVideo(String username) {
        try {
            EMClient.getInstance().callManager().makeVideoCall(username);
        } catch (EMServiceNotReadyException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
//        EventBus.getDefault().register(this);
    }




}
