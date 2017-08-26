package com.fanny.equipmentapp;

import android.app.Activity;
import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.fanny.equipmentapp.bean.UserBean;
import com.fanny.equipmentapp.service.LoginService;
import com.hyphenate.EMCallBack;
import com.hyphenate.EMConnectionListener;
import com.hyphenate.EMError;
import com.hyphenate.chat.EMClient;
import com.hyphenate.exceptions.HyphenateException;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;

public class MainActivity extends Activity implements View.OnClickListener{

    EditText etLoginUsername;
    EditText etLoginPwd;
    Button btLogin;
    Button btregist;
    private LinearLayout ll_login;
    private LinearLayout ll_entry;
    private Button btn_entry;
    private Intent loginIntent;
    private Intent loginService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e("MainActivity","create");
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);
        ll_login = (LinearLayout) findViewById(R.id.ll_login);
        etLoginUsername = (EditText) findViewById(R.id.et_login_username);
        etLoginPwd = (EditText) findViewById(R.id.et_login_pwd);
        btLogin = (Button) findViewById(R.id.bt_login);
        btLogin.setOnClickListener(this);
        btregist = (Button) findViewById(R.id.bt_regist);
        btregist.setOnClickListener(this);

        ll_entry = (LinearLayout) findViewById(R.id.ll_entry);
        btn_entry = (Button) findViewById(R.id.btn_entry);
        btn_entry.setOnClickListener(this);



        loginIntent = new Intent(getApplicationContext(), MonitorActivity.class);
        loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);


        if(loginService==null) {
            loginService = new Intent(getBaseContext(), LoginService.class);
            startService(loginService);
            if (LoginService.isConnect == true) {
                EMClient.getInstance().addConnectionListener(new MyConnectionListener());
            }
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        //注册一个监听连接状态的listener
//        instance.addConnectionListener(new MyConnectionListener());

//        EMClient instance=LoginService.getInstance();
//        instance.addConnectionListener(new MyConnectionListener());
//        LoginService.instance.addConnectionListener(new MyConnectionListener());

//        EMClient.getInstance().addConnectionListener(new MyConnectionListener());
        initview();
    }

    private class MyConnectionListener implements EMConnectionListener {

        @Override
        public void onConnected() {

            Log.e("mainactivity","islogin:"+LoginService.isLogin);
            Log.e("mainactivity","isconnect:"+LoginService.isConnect);

            MyApp.isConnect = true;

        }

        @Override
        public void onDisconnected(final int errorCode) {

            MyApp.isConnect = false;
        }
    }


    private void initview() {
        if(MyApp.islogin==true && MyApp.isConnect==true){
            ll_login.setVisibility(View.GONE);
            ll_entry.setVisibility(View.VISIBLE);
        }else {
            ll_entry.setVisibility(View.GONE);
            ll_login.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_entry:
                startActivity(loginIntent);
                finish();
                break;
            case R.id.bt_login:
                if(MyApp.isConnect==false || MyApp.islogin==false){
                    // 登陆到环信服务器
                    final String username = etLoginUsername.getText().toString();
                    final String password = etLoginPwd.getText().toString();
                    if (!TextUtils.isEmpty(username) && !TextUtils.isEmpty(password)) {

                        EMClient.getInstance().login(username, password, new EMCallBack() {
                            @Override
                            public void onSuccess() {
                                MyApp.isConnect=true;
                                EMClient.getInstance().groupManager().loadAllGroups();
                                EMClient.getInstance().chatManager().loadAllConversations();
                                Log.d("login", "登录聊天服务器成功！");
                                ThreadUtil.executeMainThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(MainActivity.this,"成功登录到远程监控系统！",Toast.LENGTH_SHORT).show();
                                    }
                                });
                                //保存用户信息
                                SpUtil.putString(getApplicationContext(),SpUtil.USERNAME,username);
                                SpUtil.putString(getApplicationContext(),SpUtil.PASSWORD,password);

                                MyApp.islogin=true;

                                /**
                                 * 登录成功后，开启后台服务，保持与服务端的长连接
                                 */


                                Log.e("MAIN","islogin:"+MyApp.islogin);
                                Log.e("MAIN","isconnect:"+MyApp.isConnect);

                                startActivity(loginIntent);
                                startService(loginService);
                                finish();
                            }

                            @Override
                            public void onError(int code, String error) {

                                MyApp.islogin=false;

                                Log.e("MAIN","islogin:"+MyApp.islogin);
                                Log.e("MAIN","isconnect:"+MyApp.isConnect);

                                Log.d("login", "登录聊天服务器失败！");
                                Log.d("login", error);
                                ThreadUtil.executeMainThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(MainActivity.this,"未成功登录到远程监控系统！请确保用户是否注册和密码是否正确",Toast.LENGTH_SHORT).show();
                                    }
                                });
                                finish();
                            }

                            @Override
                            public void onProgress(int progress, String status) {

                                Log.e("MAIN_progress","islogin:"+MyApp.islogin);
                                Log.e("MAIN_progress","isconnect:"+MyApp.isConnect);
                            }
                        });

                    }
                }else {

                }

                break;
            case R.id.bt_regist:

//                loginService= new Intent(getBaseContext(),LoginService.class);
//                startService(loginService);

//                final String username1 = etLoginUsername.getText().toString();
//                final String password1 = etLoginPwd.getText().toString();
//                if (!TextUtils.isEmpty(username1) && !TextUtils.isEmpty(password1)) {
//                    final UserBean bu=new UserBean();
//                    bu.setUsername(username1);
//                    bu.setPassword(password1);
//                    bu.signUp(new SaveListener<UserBean>() {
//
//                        @Override
//                        public void done(UserBean userBean, BmobException e) {
//                            if(e==null){
//                                Log.d("main", "注册后台服务器成功！");
//                                //注册成功后即可到环信注册
//                                ThreadUtil.executeSubThread(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        try {
//                                            EMClient.getInstance().createAccount(username1,password1);
//                                            ThreadUtil.executeMainThread(new Runnable() {
//                                                @Override
//                                                public void run() {
//                                                    SpUtil.putString(getApplicationContext(),SpUtil.USERNAME,username1);
//                                                    SpUtil.putString(getApplicationContext(),SpUtil.PASSWORD,password1);
//                                                    Log.d("main", "注册聊天服务器成功！");
//                                                    Toast.makeText(MainActivity.this, "注册成功", Toast.LENGTH_SHORT).show();
//
//                                                    /**
//                                                     * 注册成功之后将数据保存在输入框内
//                                                     */
//                                                    etLoginUsername.setText(username1);
//                                                    etLoginPwd.setText(password1);
//                                                }
//                                            });
//                                        } catch (HyphenateException e1) {
//                                            e1.printStackTrace();
//                                            bu.delete();
//                                        }
//                                    }
//                                });
//                            }else {
//                                e.printStackTrace();
//                                Log.d("main", "注册后台服务器失败！");
//                            }
//                        }
//                    });
//                }
                break;
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        String username = SpUtil.getString(this, SpUtil.USERNAME, "");
        String password = SpUtil.getString(this, SpUtil.PASSWORD, "");
        if(!TextUtils.isEmpty(username)&&!TextUtils.isEmpty(password)){
            etLoginUsername.setText(username);
            etLoginPwd.setText(password);
        }
    }
}
