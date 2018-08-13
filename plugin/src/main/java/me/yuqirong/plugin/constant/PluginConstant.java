package me.yuqirong.plugin.constant;

/**
 * @author Zhouyu
 * @date 2018/8/13
 */
public final class PluginConstant {

    public interface ClassName {
        String ActivityThread = "android.app.ActivityThread";
    }

    public interface MethodName {
        String currentActivityThread = "currentActivityThread";
        String getInstrumentation = "getInstrumentation";
    }

    public interface FieldName {
        String mInstrumentation = "mInstrumentation";
    }
}
