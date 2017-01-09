package com.iagocanalejas.retrofit;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import java.util.concurrent.Executor;

/**
 * Created by Canalejas on 09/01/2017.
 */

class AndroidExecutor implements Executor {
    private final Handler mHandler = new Handler(Looper.getMainLooper());

    @Override
    public void execute(@NonNull Runnable runnable) {
        mHandler.post(runnable);
    }
}
