package me.dreamheart.chameleon.installer;

import android.app.Application;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.ApplicationInfo;
import android.content.pm.ResolveInfo;
import android.content.res.AssetManager;
import android.content.res.Resources;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.dreamheart.chameleon.common.PlugInfo;
import me.dreamheart.chameleon.common.PluginClassLoader;
import me.dreamheart.chameleon.common.PluginContext;
import me.dreamheart.chameleon.dex.ApkRefactor;
import me.dreamheart.chameleon.utils.FileUtil;
import me.dreamheart.chameleon.utils.PluginManifestUtil;
import me.dreamheart.chameleon.utils.Trace;

/**
 * Created by Junhua Lv on 2017/1/26.
 * 插件安装器
 */
public class PluginInstaller {

    static public PlugInfo install (Context context, File pluginApk, File pluginInternalStoragePath, String odexOutputPath, ClassLoader pluginParentClassLoader) {
        PlugInfo info = new PlugInfo();
        info.setId(pluginApk.getName());

        String pluginApkPath = pluginApk.getAbsolutePath();
        //Load Plugin Manifest
        try {
            PluginManifestUtil.setManifestInfo(context, pluginApkPath, info, getPluginLibPath(info, pluginInternalStoragePath));
        } catch (Exception e) {
            throw new RuntimeException("Unable to create ManifestInfo for "
                    + pluginApkPath + " : " + e.getMessage());
        }

        // 搜索所有activity，并设置对应的替换名称
        int activityIndex = 0;
        List<ApkRefactor.RefactorItem> refactorItems = new ArrayList<>();
        Collection<ResolveInfo> activities = info.getActivities();
        info.resetActivityMap();
        for (ResolveInfo resolveInfo : activities) {
            String pluginActivityName = "me.dreamheart.demo.PluginActivity" + activityIndex;
            refactorItems.add(new ApkRefactor.RefactorItem(resolveInfo.activityInfo.name, pluginActivityName));
            resolveInfo.activityInfo.name = pluginActivityName;
            info.addActivity(resolveInfo);
            activityIndex ++;
        }
        ApkRefactor.ApkInfo apkInfo = new ApkRefactor.ApkInfo(info.getPackageInfo().packageName);
        // application可能会受到重构的影响
        info.getPackageInfo().applicationInfo.className = ApkRefactor.getNewApplicationName(info.getPackageInfo().applicationInfo.className, refactorItems);
        // 重构apk的保存路径
        File newApk = new File(pluginInternalStoragePath, info.getPackageName() + ".apk");
        // 删除以前生成的旧apk
        newApk.delete();
        String newApkFile = newApk.getAbsolutePath();
        Trace.store("generate " + newApkFile);
        // 开始重构
        ApkRefactor.refactoring(pluginApkPath, apkInfo, refactorItems, newApkFile, pluginInternalStoragePath.getAbsolutePath());

        //Load Plugin Res
        try {
            AssetManager am = AssetManager.class.newInstance();
            am.getClass().getMethod("addAssetPath", String.class)
                    .invoke(am, newApkFile);
            info.setAssetManager(am);
            Resources hotRes = context.getResources();
            Resources res = new Resources(am, hotRes.getDisplayMetrics(),
                    hotRes.getConfiguration());
            info.setResources(res);
        } catch (Exception e) {
            throw new RuntimeException("Unable to create Resources&Assets for "
                    + info.getPackageName() + " : " + e.getMessage());
        }
        //Load  classLoader for Plugin
        PluginClassLoader pluginClassLoader = new PluginClassLoader(info, newApkFile, odexOutputPath
                , getPluginLibPath(info, pluginInternalStoragePath).getAbsolutePath(), pluginParentClassLoader);
        info.setClassLoader(pluginClassLoader);
        ApplicationInfo appInfo = info.getPackageInfo().applicationInfo;
        // 创建插件的Application
        Application app = makeApplication(info, appInfo);
        attachBaseContext(context, info, app);
        info.setApplication(app);
        Trace.store("Build pluginInfo => " + info);
        return info;
    }

    static private File getPluginLibPath(PlugInfo plugInfo, File dexInternalStoragePath) {
        return new File(dexInternalStoragePath, plugInfo.getId() + "-dir/lib/");
    }

    /**
     * 构造插件的Application
     *
     * @param plugInfo 插件信息
     * @param appInfo 插件ApplicationInfo
     * @return 插件App
     */
    static private Application makeApplication(PlugInfo plugInfo, ApplicationInfo appInfo) {
        String appClassName = appInfo.className;
        if (appClassName == null) {
            //Default Application
            appClassName = Application.class.getName();
        }
        try {
            return (Application) plugInfo.getClassLoader().loadClass(appClassName).newInstance();
        } catch (Throwable e) {
            throw new RuntimeException("Unable to create Application for "
                    + plugInfo.getPackageName() + ": "
                    + e.getMessage());
        }
    }

    static private void attachBaseContext(Context context, PlugInfo info, Application app) {
        try {
            Field mBase = ContextWrapper.class.getDeclaredField("mBase");
            mBase.setAccessible(true);
            mBase.set(app, new PluginContext(context.getApplicationContext(), info));
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
