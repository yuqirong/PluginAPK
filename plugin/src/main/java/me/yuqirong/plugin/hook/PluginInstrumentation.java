package me.yuqirong.plugin.hook;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageParser;
import android.os.Bundle;
import android.view.ContextThemeWrapper;

import me.yuqirong.plugin.PluginManager;
import me.yuqirong.plugin.constant.PluginConstant;
import me.yuqirong.plugin.entity.Plugin;
import me.yuqirong.plugin.entity.PluginDataStorage;
import me.yuqirong.plugin.util.ReflectUtil;

/**
 * @author Zhouyu
 * @date 2018/8/13
 */
public class PluginInstrumentation extends Instrumentation {

    private final Instrumentation mInstrumentation;

    public PluginInstrumentation(Instrumentation instrumentation) {
        this.mInstrumentation = instrumentation;
    }

    @Override
    public Activity newActivity(ClassLoader classLoader, String className, Intent intent)
            throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        intent.setExtrasClassLoader(PluginDataStorage.class.getClassLoader());
        PluginDataStorage dataStorage = intent.getParcelableExtra(PluginConstant.PLUGIN_DATA);
        if (dataStorage != null) {
            String pluginClassName = dataStorage.className;
            String pluginPackageName = dataStorage.packageName;
            Plugin plugin = PluginManager.getInstance(getContext()).getPlugin(pluginPackageName);
            // replace
            classLoader = plugin.mClassLoader;
            className = pluginClassName;
        }
        return super.newActivity(classLoader, className, intent);
    }

    @Override
    public void callActivityOnCreate(Activity activity, Bundle icicle) {
        Intent intent = activity.getIntent();
        intent.setExtrasClassLoader(PluginDataStorage.class.getClassLoader());
        PluginDataStorage dataStorage = intent.getParcelableExtra(PluginConstant.PLUGIN_DATA);

        if (dataStorage != null) {
            String pluginPackageName = dataStorage.packageName;
            Plugin plugin = PluginManager.getInstance(getContext()).getPlugin(pluginPackageName);

            if (plugin != null) {
                Context baseContext = activity.getBaseContext();
                PluginContext pluginContext = new PluginContext(baseContext, plugin);
                try {
                    ReflectUtil.setField(ContextThemeWrapper.class, activity, PluginConstant.FieldName.mResources, pluginContext.getResources());
                    ReflectUtil.setField(ContextWrapper.class, activity, PluginConstant.FieldName.mBase, pluginContext);
                    ReflectUtil.setField(Activity.class, activity, PluginConstant.FieldName.mApplication, plugin.mApplication);
                    // init theme
                    ActivityInfo activityInfo = findActivityInfo(activity, plugin);
                    if (activityInfo != null) {
                        int themeResource = activityInfo.getThemeResource();
                        if (themeResource == 0) {
                            ApplicationInfo applicationInfo = plugin.mPackage.applicationInfo;
                            int theme = applicationInfo.theme;
                            activity.setTheme(theme);
                        } else {
                            ReflectUtil.setField(Activity.class, activity, PluginConstant.FieldName.mActivityInfo, activityInfo);
                            activity.setTheme(themeResource);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        super.callActivityOnCreate(activity, icicle);
    }

    private ActivityInfo findActivityInfo(Activity activity, Plugin plugin) {
        for (PackageParser.Activity act : plugin.mPackage.activities) {
            if (act.className.equals(activity.getClass().getName())) {
                ActivityInfo info = act.info;
                return info;
            }
        }
        return null;
    }

    @Override
    public Context getContext() {
        return mInstrumentation.getContext();
    }

    @Override
    public Context getTargetContext() {
        return mInstrumentation.getTargetContext();
    }

    @Override
    public ComponentName getComponentName() {
        return mInstrumentation.getComponentName();
    }


}
