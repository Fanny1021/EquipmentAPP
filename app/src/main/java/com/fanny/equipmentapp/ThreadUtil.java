package com.fanny.equipmentapp;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by huang on 2017/3/16.
 */

public class ThreadUtil {

    private static final Executor EXECUTOR = Executors.newSingleThreadExecutor();
    private static final Handler HANDLER = new Handler(Looper.getMainLooper());

    // 提供任务执行在子线程
    public static void executeSubThread(Runnable runnable){
        EXECUTOR.execute(runnable);
    }

    // 提供任务执行在主线程
    public static void executeMainThread(Runnable runnable){
        HANDLER.post(runnable);
    }

}
