package me.dreamheart.chameleon.loader;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;

import java.io.File;

import me.dreamheart.chameleon.Hook;
import me.dreamheart.chameleon.common.FrameworkClassLoader;
import me.dreamheart.chameleon.common.Globals;
import me.dreamheart.chameleon.common.PlugInfo;
import me.dreamheart.chameleon.installer.PluginInstaller;
import me.dreamheart.chameleon.utils.ReflectionUtils;

/**
 * Created by Junhua Lv on 2017/1/26.
 * 插件加载器
 */

public class PluginLoader {

    /**
     * 插件dex opt输出路径
     */
    private String odexOutputPath;
    /**
     * 私有目录中存储插件的路径
     */
    private File pluginInternalStoragePath;

    private ClassLoader pluginParentClassLoader;
    private Context mContext;
    private FrameworkClassLoader mFrameworkClassLoader;

    /**
     * 初始化
     * @param context    宿主application context
     */
    public void init (Context context) {
        mContext = context;
        context.getClassLoader();
        pluginParentClassLoader = ClassLoader.getSystemClassLoader().getParent();
        File optimizedDexPath = context.getDir(Globals.PRIVATE_PLUGIN_ODEX_OUTPUT_DIR_NAME, Context.MODE_PRIVATE);
        odexOutputPath = optimizedDexPath.getAbsolutePath();
        pluginInternalStoragePath = context.getDir(
                Globals.PRIVATE_PLUGIN_OUTPUT_DIR_NAME, Context.MODE_PRIVATE
        );

        // 替换ClassLoader
        try {
            Object mPackageInfo = ReflectionUtils.getFieldValue(context,
                    "mBase.mPackageInfo", true);
            mFrameworkClassLoader = new FrameworkClassLoader(
                    context.getClassLoader());
            // set Application's classLoader to FrameworkClassLoader
            ReflectionUtils.setFieldValue(mPackageInfo, "mClassLoader",
                    mFrameworkClassLoader, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
//        Tools.inject(context);
    }

    /**
     * 安装插件
     * @param apkFile    apk文件
     * @return 插件信息，用作启动插件时的输入参数
     */
    public PlugInfo installPlugin (File apkFile) {
        if (!apkFile.isFile()) {
            return null;
        }

        PlugInfo plugInfo = PluginInstaller.install(mContext, apkFile, pluginInternalStoragePath, odexOutputPath, pluginParentClassLoader);
        return plugInfo;
    }

    /**
     * 启动插件
     * start插件的默认activiy
     * @param plugInfo    安装插件时生成的插件信息
     */
    public void startPlugin (Context context, PlugInfo plugInfo) {
        if (null == plugInfo)
            return;

        ActivityInfo activityInfo = plugInfo.getMainActivity().activityInfo;
        if (activityInfo == null) {
            throw new ActivityNotFoundException("Cannot find Main Activity from plugin.");
        }

        mFrameworkClassLoader.setPlugin(plugInfo);
//        PluginInstrumentation.currentPlugin = plugInfo;
        Hook.sHookListener = new PluginHook(mContext, plugInfo);
        plugInfo.ensureApplicationCreated();

        Intent intent = new Intent();
        try {
            intent.setClass(context, plugInfo.getClassLoader().loadClass(activityInfo.name));
            context.startActivity(intent);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
