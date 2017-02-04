package me.dreamheart.chameleon;

/**
 * 注入到插件dex中的钩子
 */
public class Hook {
    public interface HookListener {
        Object onAttachBaseContext(Object activity, Object orgContext);
        void onCreate(Object activity, Object savedInstanceState);
    }

    static public HookListener sHookListener;

    static public Object attachBaseContext(Object activity, Object orgContext) {
        if (null != sHookListener) {
            return sHookListener.onAttachBaseContext(activity, orgContext);
        }
        return orgContext;
    }

    static public void onCreate(Object activity, Object savedInstanceState) {
        if (null != sHookListener) {
            sHookListener.onCreate(activity, savedInstanceState);
        }
    }
}
