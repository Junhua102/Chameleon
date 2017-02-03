package me.dreamheart.chameleon.common;

import android.annotation.TargetApi;
import android.os.Build;

import dalvik.system.DexClassLoader;
import me.dreamheart.chameleon.Hook;

/**
 * @author Lody
 * @version 1.0
 */
@TargetApi(Build.VERSION_CODES.CUPCAKE)
public class PluginClassLoader extends DexClassLoader {

    protected PlugInfo plugInfo;

    public PluginClassLoader(PlugInfo plugInfo, String dexPath, String optimizedDirectory, String libraryPath, ClassLoader parent) {
        super(dexPath, optimizedDirectory, libraryPath, parent);
        this.plugInfo = plugInfo;
    }

    public PlugInfo getPlugInfo() {
        return plugInfo;
    }

    protected Class<?> loadClass(String className, boolean resolv)
            throws ClassNotFoundException {
//        Log.i("cl", "loadClass: " + className);
        if (className.equals("me.dreamheart.chameleon.Hook")) {
            return Hook.class;
        }
        return super.loadClass(className, resolv);
    }
}
