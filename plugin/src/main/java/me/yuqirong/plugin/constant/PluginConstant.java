package me.yuqirong.plugin.constant;

/**
 * @author Zhouyu
 * @date 2018/8/13
 */
public final class PluginConstant {

    public static final String PLUGIN_DATA = "PLUGIN_DATA";

    public interface ClassName {
        String ActivityThread = "android.app.ActivityThread";
        String AssetManager = "android.content.res.AssetManager";
        String PackageParser = "android.content.pm.PackageParser";
    }

    public interface MethodName {
        String currentActivityThread = "currentActivityThread";
        String getInstrumentation = "getInstrumentation";
        String addAssetPath = "addAssetPath";
        String collectCertificates = "collectCertificates";
    }

    public interface FieldName {
        String mInstrumentation = "mInstrumentation";
        String mBase = "mBase";
        String mResources = "mResources";
        String mApplication = "mApplication";
        String mComponent = "mComponent";
        String mActivityInfo = "mActivityInfo";
    }
}
