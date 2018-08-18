package me.yuqirong.plugin;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageParser;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.text.TextUtils;

import java.io.File;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import me.yuqirong.plugin.constant.PluginConstant;
import me.yuqirong.plugin.constant.PluginConstant.*;
import me.yuqirong.plugin.entity.Plugin;
import me.yuqirong.plugin.entity.PluginDataStorage;
import me.yuqirong.plugin.hook.PluginClassLoader;
import me.yuqirong.plugin.hook.PluginContext;
import me.yuqirong.plugin.hook.PluginEnvironment;
import me.yuqirong.plugin.hook.PluginShellActivity;
import me.yuqirong.plugin.util.ReflectUtil;

/**
 * @author Zhouyu
 * @date 2018/8/13
 */
public class PluginManager {

    private static PluginManager instance;

    private Context context;

    // key = plugin package name
    private Map<String, Plugin> pluginMap = new HashMap<>();

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

    public Plugin getPlugin(String packageName) {
        return pluginMap.get(packageName);
    }

    /**
     * 加载插件apk
     *
     * @param pluginApkPath
     * @return
     */
    public Plugin loadPluginApk(String pluginApkPath) {
        if (TextUtils.isEmpty(pluginApkPath)) {
            return null;
        }
        File pluginApk = new File(pluginApkPath);

        if (!pluginApk.exists() || !pluginApk.isFile()) {
            return null;
        }

        Plugin plugin = new Plugin();
        plugin.mFilePath = pluginApkPath;
        // init plugin environment
        PluginEnvironment.initPluginEnvironmentIfNeed();

        // load apk
        parsePluginPackage(plugin);
        loadPluginResource(plugin);
        createPluginClassLoader(plugin);
        createPluginApplication(plugin);
        // cache plugin
        pluginMap.put(plugin.mPackage.packageName, plugin);
        return plugin;
    }

    public void startActivity(Context context, Plugin plugin, Intent intent) throws Exception {
        if (plugin == null || intent == null) {
            throw new Exception("plugin or intent cannot be null");
        }
        ComponentName component = intent.getComponent();
        if (component == null) {
            throw new Exception("component cannot be null");
        }

        PluginDataStorage pluginDataStorage = new PluginDataStorage();
        pluginDataStorage.packageName = component.getPackageName();
        pluginDataStorage.className = component.getClassName();

        // replace activity
        intent.putExtra(PluginConstant.PLUGIN_DATA, pluginDataStorage);
        intent.setClass(context, PluginShellActivity.class);
        intent.setExtrasClassLoader(PluginDataStorage.class.getClassLoader());

        context.startActivity(intent);
    }

    private void createPluginApplication(Plugin plugin) {
        try {
            ApplicationInfo applicationInfo = plugin.mPackage.applicationInfo;
            String appClassName = applicationInfo.className;
            if (appClassName == null) {
                appClassName = Application.class.getName();
            }
            Application application = (Application) plugin.mClassLoader.loadClass(appClassName).newInstance();
            plugin.mApplication = application;

            // attach base context  TODO why ?
            Field mBase = ReflectUtil.getField(ContextWrapper.class, FieldName.mBase);
            if (mBase != null) {
                mBase.set(application, new PluginContext(context.getApplicationContext(), plugin));
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void createPluginClassLoader(Plugin plugin) {
        try {
            File pluginApk = new File(plugin.mFilePath);
            String dexPath = pluginApk.getParent() + "/dex";
            File dexDir = new File(dexPath);
            if (!dexDir.exists()) {
                dexDir.mkdirs();
            }
            String libPath = pluginApk.getParent() + "/lib";
            File libDir = new File(libPath);
            if (!libDir.exists()) {
                libDir.mkdirs();
            }
            PluginClassLoader pluginClassLoader = new PluginClassLoader(plugin.mFilePath,
                    dexDir.getAbsolutePath(), libDir.getAbsolutePath(),
                    ClassLoader.getSystemClassLoader().getParent());
            plugin.mClassLoader = pluginClassLoader;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadPluginResource(Plugin plugin) {
        try {
            AssetManager am = AssetManager.class.newInstance();
            ReflectUtil.invokeMethod(ClassName.AssetManager, am, MethodName.addAssetPath,
                    new Class[]{String.class}, new Object[]{plugin.mFilePath});
            plugin.mAssetManager = am;
            Resources hostRes = context.getResources();
            Resources res = new Resources(am, hostRes.getDisplayMetrics(),
                    hostRes.getConfiguration());
            plugin.mResources = res;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void parsePluginPackage(Plugin plugin) {
        // 这里直接copy VirtualAPK 解析插件的方法
        try {
            File pluginApk = new File(plugin.mFilePath);
            PackageParser packageParser = new PackageParser();
            PackageParser.Package pluginPackage = packageParser.parsePackage(pluginApk, PackageParser.PARSE_MUST_BE_APK);
            plugin.mPackage = pluginPackage;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
