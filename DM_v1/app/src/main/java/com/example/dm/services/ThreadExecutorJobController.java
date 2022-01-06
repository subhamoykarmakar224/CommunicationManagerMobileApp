package com.example.dm.services;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadExecutorJobController {
    ThreadPoolExecutor threadPool = new ThreadPoolExecutor(
            10, 10, 10, TimeUnit.SECONDS, new LinkedBlockingQueue<>()
    );

    public void startDBPushToServer() {

    }

    public void stopDBPushToServer() {
    }
}
