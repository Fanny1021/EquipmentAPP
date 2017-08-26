package com.fanny.equipmentapp;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.OnNmeaMessageListener;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.fanny.equipmentapp.receiver.LanuchReciever;
import com.hyphenate.EMCallBack;
import com.hyphenate.EMConnectionListener;
import com.hyphenate.EMError;
import com.hyphenate.EMMessageListener;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMOptions;
import com.hyphenate.chat.EMTextMessageBody;
import com.hyphenate.chat.adapter.EMACallConference;
import com.hyphenate.chat.adapter.EMACallManagerListenerInterface;
import com.hyphenate.chat.adapter.EMACallRtcImpl;
import com.hyphenate.chat.adapter.EMACallSession;
import com.hyphenate.chat.adapter.EMACallStream;
import com.hyphenate.chat.adapter.EMAError;
import com.hyphenate.exceptions.EMNoActiveCallException;
import com.superrtc.sdk.RtcConnection;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.Bmob;

/**
 * Created by Fanny on 17/4/25.
 */

public class MyApp extends Application implements EMMessageListener, EMACallManagerListenerInterface {

    private static final String TAG = "MyApp";
    private static final String username = "user";
    private static final String password = "123456";
    private SoundPool soundPool;
    private int duan;
    private int yulu;
    private CallReceiver myreceiver;
    public static boolean isConnect = false;
    public static boolean islogin=false;



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
        //在做打包混淆时，关闭debug模式，避免消耗不必要的资源
        EMClient.getInstance().setDebugMode(true);


        //接受消息
        EMClient.getInstance().chatManager().addMessageListener(this);

        //关于提示音
        soundPool = new SoundPool(2, AudioManager.STREAM_MUSIC, 1);
        //加载声音文件
        duan = soundPool.load(this, R.raw.duan, 1);
        yulu = soundPool.load(this, R.raw.yulu, 1);

        //注册一个监听连接状态的listener
//        EMClient.getInstance().addConnectionListener(new MyConnectionListener());

        /**
         * 自动登录本地和远程服务器
         */
//        initlogin();

        /**
         * 注册来电监听广播
         */
        initbroadcast();

        /**
         * 开启后台运行服务
         */
//        initservice();
    }

    private void initlogin() {
        // 登陆到环信服务器
        final String username = "woman";
        final String password = "123456";
        if (!TextUtils.isEmpty(username) && !TextUtils.isEmpty(password)) {

            EMClient.getInstance().login(username, password, new EMCallBack() {

                @Override
                public void onSuccess() {
                    EMClient.getInstance().groupManager().loadAllGroups();
                    EMClient.getInstance().chatManager().loadAllConversations();
                    Log.d("login", "登录聊天服务器成功！");

                    islogin = true;
                    ThreadUtil.executeMainThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "成功登录到远程监控系统！", Toast.LENGTH_SHORT).show();
                        }
                    });
                    //保存用户信息
                    SpUtil.putString(getApplicationContext(), SpUtil.USERNAME, username);
                    SpUtil.putString(getApplicationContext(), SpUtil.PASSWORD, password);



                }

                @Override
                public void onError(int code, String error) {

                    Log.d("login", "登录聊天服务器失败！");
                    Log.d("login", error);
                    ThreadUtil.executeMainThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "未成功登录,请用户手动登录", Toast.LENGTH_SHORT).show();
                        }
                    });

                }

                @Override
                public void onProgress(int progress, String status) {
                    Log.e("myapp", "正在登录");
                }
            });

        }
    }

    private void initbroadcast() {
        IntentFilter callFilter = new IntentFilter(EMClient.getInstance().callManager().getIncomingCallBroadcastAction());
        myreceiver = new CallReceiver();
        registerReceiver(myreceiver, callFilter);
    }


    private class CallReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // 拨打方username
            final String from = intent.getStringExtra("from");
            // call type
            final String type = intent.getStringExtra("type");

            //提示通知消息
//        final EMTextMessageBody body= (EMTextMessageBody) emMessage.getBody();
            ThreadUtil.executeMainThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "来自" + from + "的" + type + "消息", Toast.LENGTH_SHORT).show();
                }
            });

            //使用EventBus将消息发送给聊天界面monitoractivity
//            EventBus.getDefault().post(from);

            //收到消息后播放提示音
            //判断应用是否运行在后台
            if (isRunningBack()) {
                soundPool.play(yulu, 1, 1, 0, 0, 1);
                //弹出通知栏，点击打开聊天界面

                //先打开主界面，再打开聊天界面

                Intent mainIntent = new Intent(getApplicationContext(), MainActivity.class);
                mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

//                exit();

//                Intent chatIntent = new Intent(getApplicationContext(), MonitorActivity.class);
//                chatIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                chatIntent.putExtra("username", from);

//                Intent[] intents = new Intent[]{mainIntent, chatIntent};
//                Intent[] intents = new Intent[]{mainIntent};
                PendingIntent pendingIntent=PendingIntent.getActivity(getApplicationContext(),0,mainIntent,PendingIntent.FLAG_UPDATE_CURRENT);
//                PendingIntent pendingIntent = PendingIntent.getActivities(getApplicationContext(), 0, intents, PendingIntent.FLAG_UPDATE_CURRENT);

                //Notification
                Notification builder = new Notification.Builder(getApplicationContext())
                        .setSmallIcon(R.mipmap.conversation_selected_2)
                        .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.avatar3))
                        .setContentTitle("您有一条新消息")
                        .setContentText(from)
                        .setContentInfo("来自" + from)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)
                        .build();

                NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                notificationManager.notify(0, builder);

            } else {
                soundPool.play(duan, 1, 1, 0, 0, 1);
            }

        }
    }


    private class MyConnectionListener implements EMConnectionListener {

        @Override
        public void onConnected() {

            isConnect = true;
            ThreadUtil.executeMainThread(new Runnable() {
                @Override
                public void run() {
                        // 显示帐号在其他设备登录
                        // 关闭所有界面，打开登陆界面
                        exit();
                        Toast.makeText(MyApp.this, "进入通话界面", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getApplicationContext(), MonitorActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);

                }
            });

        }

        @Override
        public void onDisconnected(final int errorCode) {

            isConnect = false;
            ThreadUtil.executeMainThread(new Runnable() {
                @Override
                public void run() {
                    if (errorCode == EMError.USER_LOGIN_ANOTHER_DEVICE) {
                        // 显示帐号在其他设备登录
                        // 关闭所有界面，打开登陆界面
                        exit();
                        Toast.makeText(MyApp.this, "账户在其他设备登录，请重新登录", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                }
            });
        }
    }

    //处理app界面的所有acticity
    private List<Activity> activities = new ArrayList<>();

    public void addActivity(Activity activity) {
        activities.add(activity);
    }

    public void removeActivity(Activity activity) {
        activities.remove(activity);
    }

    private void exit() {
        for (int i = 0; i < activities.size(); i++) {
            activities.get(i).finish();
        }
        activities.clear();
    }



    /**
     * msg监听接口
     *
     * @param messages
     */
    //监听消息通知的重写方法
    @Override
    public void onMessageReceived(List<EMMessage> messages) {
        EMMessage emMessage = messages.get(0);
        final EMTextMessageBody body = (EMTextMessageBody) emMessage.getBody();
        ThreadUtil.executeMainThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), body.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        //使用EventBus将消息发送给聊天界面monitoractivity
        EventBus.getDefault().post(emMessage);

        //收到消息后播放提示音
        //判断应用是否运行在后台
        if (isRunningBack()) {
            soundPool.play(yulu, 1, 1, 0, 0, 1);
            //弹出通知栏，点击打开聊天界面
            //先打开主界面，再打开聊天界面

            Intent mainIntent = new Intent(this, MainActivity.class);
            mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            Intent chatIntent = new Intent(this, MonitorActivity.class);
            chatIntent.putExtra("username", emMessage.getUserName());

            Intent[] intents = new Intent[]{mainIntent, chatIntent};
            PendingIntent pendingIntent = PendingIntent.getActivities(this, 0, intents, PendingIntent.FLAG_UPDATE_CURRENT);

            //Notification
            Notification builder = new Notification.Builder(this)
                    .setSmallIcon(R.mipmap.conversation_selected_2)
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.avatar3))
                    .setContentTitle("您有一条新消息")
                    .setContentText(body.getMessage())
                    .setContentInfo("来自" + emMessage.getUserName())
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .build();

            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.notify(0, builder);

        } else {
            soundPool.play(duan, 1, 1, 0, 0, 1);
        }

    }

    private boolean isRunningBack() {
        ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> runningTasks = activityManager.getRunningTasks(100);
        return !runningTasks.get(0).topActivity.getPackageName().equals(getPackageName());
    }

    @Override
    public void onCmdMessageReceived(List<EMMessage> messages) {

    }

    @Override
    public void onMessageRead(List<EMMessage> messages) {

    }

    @Override
    public void onMessageDelivered(List<EMMessage> messages) {

    }

    @Override
    public void onMessageChanged(EMMessage message, Object change) {

    }


    /**
     * 视频通话监听接口
     *
     * @param from
     * @param to
     */

    @Override
    public void onSendPushMessage(String from, String to) {

    }

    @Override
    public void onRecvCallFeatureUnsupported(EMACallSession session, EMAError error) {

    }

    @Override
    public void onRecvCallIncoming(EMACallSession callSession) {


    }

    @Override
    public void onRecvCallConnected(EMACallSession callSession) {

    }

    @Override
    public void onRecvCallAccepted(EMACallSession callSession) {

    }

    @Override
    public void onRecvCallEnded(EMACallSession callSession, int endReasonOrdinal, EMAError error) {

    }

    @Override
    public void onRecvCallNetworkStatusChanged(EMACallSession callSession, int toStatus) {

    }

    @Override
    public void onRecvCallStateChanged(EMACallSession callSession, int StreamControlType) {

    }

    @Override
    public void onNewRtcConnection(String callId, int mode, EMACallStream callStream, String to, RtcConnection.Listener listener, EMACallRtcImpl rtcImpl) {

    }

    @Override
    public void onConferenceMemberJoined(EMACallConference callConference, String enteredName) {

    }

    @Override
    public void onConferenceMemberLeaved(EMACallConference callConference, String exitedName) {

    }

    @Override
    public void onConferenceMemberPublished(EMACallConference callConference, String pubedName) {

    }

    @Override
    public void onConferenceMembersUpdated(EMACallConference callConference) {

    }

    @Override
    public void onConferenceStreamConnected(EMACallConference callSession, EMACallStream subedStream) {

    }

    @Override
    public void onConferenceStreamClosed(EMACallConference callSession, EMACallStream subedStream, EMAError error) {

    }

    @Override
    public void onConferenceClosed(EMACallConference callSession) {

    }


}
