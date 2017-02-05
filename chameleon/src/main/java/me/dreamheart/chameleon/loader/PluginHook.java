package me.dreamheart.chameleon.loader;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;

import me.dreamheart.chameleon.Hook;
import me.dreamheart.chameleon.common.PlugInfo;
import me.dreamheart.chameleon.common.PluginContext;
import me.dreamheart.chameleon.utils.Trace;

/**
 * Created by Junhua Lv on 2017/1/28.
 * 插件钩子
 */
public class PluginHook implements Hook.HookListener {

    private PlugInfo plugInfo;
    private Context context;

    public PluginHook(Context appContext, PlugInfo plugInfo) {
        this.plugInfo = plugInfo;
        context = appContext;
    }

    /**
     * 插件activity调用onAttachBaseContext时，调用此函数
     * @param activity      插件activity
     * @param orgContext    原context
     * @return PluginContext
     */
    @Override
    public Object onAttachBaseContext(Object activity, Object orgContext) {
        return attachBaseContext((Activity)activity, (Context)orgContext);
    }

    /**
     * 将插件的baseContext替换成PluginContext
     * @param activity      插件activity
     * @param orgContext    原context
     * @return PluginContext
     */
    private Context attachBaseContext(Activity activity, Context orgContext) {
        return new PluginContext(context, plugInfo);
    }

    @Override
    public void onCreate(Object activity, Object savedInstanceState) {
        Trace.store("Hook onCreate " + activity.getClass().getName());
        ActivityInfo activityInfo = plugInfo.findActivityByClassName(activity.getClass().getName());
        int themeResource = activityInfo.getThemeResource();
        if (themeResource != 0) {
            ((Activity) activity).setTheme(themeResource);
        }
    }

}
