package me.yuqirong.plugin;

import android.app.ActivityThread;
import android.app.Instrumentation;
import android.content.Context;
import android.text.TextUtils;

import java.io.File;

import me.yuqirong.plugin.constant.PluginConstant;
import me.yuqirong.plugin.constant.PluginConstant.*;
import me.yuqirong.plugin.hook.PluginInstrumentation;
import me.yuqirong.plugin.util.ReflectUtil;

/**
 * @author Zhouyu
 * @date 2018/8/13
 */
public class PluginManager {

    private static PluginManager instance;

    private Context context;

    private PluginManager(Context context) {
        //no instance
        this.context = context;
    }

    public static PluginManager getInstance(Context context) {
        if (instance == null) {
            instance = new PluginManager(context);
        }
        return instance;
    }

    /**
     * 加载插件apk
     *
     * @param pluginApkPath
     * @return
     */
    public boolean loadPluginApk(String pluginApkPath) {
        if (TextUtils.isEmpty(pluginApkPath)) {
            return false;
        }

        File pluginApk = new File(pluginApkPath);

//        if (!pluginApk.exists() || !pluginApk.isFile()) {
//            return false;
//        }

        initPluginEnvironmentIfNeed(context);

        // load apk

        return true;
    }


    private void initPluginEnvironmentIfNeed(Context context) {
        ActivityThread activityThread = getActivityThread();
        Instrumentation instrumentation = getInstrumentation(activityThread);
        if (instrumentation != null && instrumentation instanceof PluginInstrumentation) {
            //already init plugin environment
            return;
        }
        PluginInstrumentation pluginInstrumentation = new PluginInstrumentation(instrumentation);
        setInstrumentation(activityThread, pluginInstrumentation);
    }

    private void setInstrumentation(ActivityThread activityThread, PluginInstrumentation pluginInstrumentation) {
        ReflectUtil.setField(activityThread.getClass(), activityThread, FieldName.mInstrumentation, pluginInstrumentation);
    }

    private Instrumentation getInstrumentation(ActivityThread activityThread) {
        return (Instrumentation) ReflectUtil.invokeMethod(ClassName.ActivityThread, activityThread, MethodName.getInstrumentation, null, null);
    }

    private ActivityThread getActivityThread() {
        return (ActivityThread) ReflectUtil.invokeMethod(ClassName.ActivityThread, null, MethodName.currentActivityThread, null, null);
    }



}
