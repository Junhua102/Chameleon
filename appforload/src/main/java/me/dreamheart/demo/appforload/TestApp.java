package me.dreamheart.demo.appforload;

import android.app.Application;
import android.util.Log;

/**
 * Created by Junhua Lv on 2017/1/26.
 */

public class TestApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Log.v("TestApp", "TestApp onCreate");
    }
}
