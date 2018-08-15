package me.yuqirong.plugin.hook;

import android.app.ActivityThread;
import android.app.Instrumentation;

import me.yuqirong.plugin.constant.PluginConstant.ClassName;
import me.yuqirong.plugin.constant.PluginConstant.FieldName;
import me.yuqirong.plugin.constant.PluginConstant.MethodName;
import me.yuqirong.plugin.util.ReflectUtil;

/**
 * @author Zhouyu
 * @date 2018/8/15
 */
public class PluginEnvironment {

    /**
     * 初始化插件化环境
     */
    public static void initPluginEnvironmentIfNeed() {
        ActivityThread activityThread = getActivityThread();
        Instrumentation instrumentation = getInstrumentation(activityThread);
        if (instrumentation != null && instrumentation instanceof PluginInstrumentation) {
            //already init plugin environment
            return;
        }
        PluginInstrumentation pluginInstrumentation = new PluginInstrumentation(instrumentation);
        setInstrumentation(activityThread, pluginInstrumentation);
    }

    private static void setInstrumentation(ActivityThread activityThread, PluginInstrumentation pluginInstrumentation) {
        ReflectUtil.setField(activityThread.getClass(), activityThread, FieldName.mInstrumentation, pluginInstrumentation);
    }

    private static Instrumentation getInstrumentation(ActivityThread activityThread) {
        return (Instrumentation) ReflectUtil.invokeMethod(ClassName.ActivityThread, activityThread, MethodName.getInstrumentation, null, null);
    }

    private static ActivityThread getActivityThread() {
        return (ActivityThread) ReflectUtil.invokeMethod(ClassName.ActivityThread, null, MethodName.currentActivityThread, null, null);
    }


}
