package me.yuqirong.plugin.constant;

/**
 * @author Zhouyu
 * @date 2018/8/13
 */
public final class PluginConstant {

    public interface ClassName {
        String ActivityThread = "android.app.ActivityThread";
        String AssetManager = "android.content.res.AssetManager";
    }

    public interface MethodName {
        String currentActivityThread = "currentActivityThread";
        String getInstrumentation = "getInstrumentation";
        String addAssetPath = "addAssetPath";
    }

    public interface FieldName {
        String mInstrumentation = "mInstrumentation";
        String mBase = "mBase";
    }
}
