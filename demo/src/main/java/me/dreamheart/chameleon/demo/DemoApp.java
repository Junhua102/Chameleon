package me.dreamheart.chameleon.demo;

import android.app.Application;

import me.dreamheart.chameleon.loader.PluginLoader;

/**
 * Created by Junhua Lv on 2017/1/26.
 * demo app 用于初始化
 */
public class DemoApp extends Application {

    PluginLoader mPluginLoader;

    @Override
    public void onCreate() {
        super.onCreate();
        mPluginLoader = new PluginLoader();
        mPluginLoader.init(this);
    }

    public PluginLoader getPluginLoader() {
        return mPluginLoader;
    }
}
