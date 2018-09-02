# PluginAPK
介绍
===
插件化 demo ，目前支持的特性：

* 构建插件环境；
* 实现动态加载插件 apk ；
* 简单实现加载插件 Activity ；
* 实现加载插件 Service ；
* 实现加载插件 BoardCastReceiver ；

此插件化 demo 仅作为公司内部分享演示使用，功能支持并不完善，请勿在实际项目中使用！

插件 apk 原理
============
构造插件环境
---------
``` java
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

```

1. 通过反射获取当前 ActivityThread 的对象；
2. 利用 ActivityThread 对象得到当前 ActivityThread 中的 Instrumentation 对象；
3. 创建一个自定义 PluginInstrumentation 的包装类，并 hook 其中的 newActivity 和 callActivityOnCreate 方法，为后面加载插件 Activity 作基础；
4. 利用反射将创建出来的 PluginInstrumentation 对象替代之前的 Instrumentation ，这样系统到时候调用的就是我们自定义的 PluginInstrumentation 了；

动态加载插件 apk
--------------
### 解析插件 apk 包的信息
``` java
private void parsePluginPackage(Plugin plugin) {
    try {
        File pluginApk = new File(plugin.mFilePath);
        PackageParser packageParser = new PackageParser();
        PackageParser.Package pluginPackage = packageParser.parsePackage(pluginApk, PackageParser.PARSE_MUST_BE_APK);
        ReflectUtil.invokeMethod(PluginConstant.ClassName.PackageParser, null, PluginConstant.MethodName.collectCertificates,
                new Class[]{PackageParser.Package.class, int.class}, new Object[]{pluginPackage, PackageParser.PARSE_MUST_BE_APK});
        plugin.mPackage = pluginPackage;
    } catch (Exception e) {
        e.printStackTrace();
    }
}
```

解析 apk 的方案是 copy 了 VirtualAPK 的代码，利用安卓原生的 PackageParser ，反射进行解析 apk 包，返回的 pluginPackage 中包含了 Application 、四大组件等的信息。

### 解析插件 apk 的资源

``` java
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
```

AssetManager 类中有一个 @hide 的方法：addAssetPath ，支持从外部 apk 加载资源。

### 创建插件的 ClassLoader

``` java
private void createPluginClassLoader(Plugin plugin) {
    try {
        String dexPath = context.getCacheDir().getAbsolutePath() + "/plugin-dir/dex";
        File dexDir = new File(dexPath);
        if (!dexDir.exists()) {
            dexDir.mkdirs();
        }
        String libPath = context.getCacheDir().getAbsolutePath() + "/plugin-dir/lib";
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
```

自定义创建了一个类 PluginClassLoader ，继承自 DexClassLoader ，将插件 apk 的所在的路径传入，即可加载插件 apk 的 dex 。

创建插件的 Application
---------------------
``` java
private void createPluginApplication(Plugin plugin) {
    try {
        ApplicationInfo applicationInfo = plugin.mPackage.applicationInfo;
        String appClassName = applicationInfo.className;
        if (appClassName == null) {
            appClassName = Application.class.getName();
        }
        Application application = (Application) plugin.mClassLoader.loadClass(appClassName).newInstance();
        plugin.mApplication = application;

        // attach base context
        Field mBase = ReflectUtil.getField(ContextWrapper.class, FieldName.mBase);
        if (mBase != null) {
            mBase.set(application, new PluginContext(context.getApplicationContext(), plugin));
        }
    } catch (Throwable e) {
        e.printStackTrace();
    }
}
```

之前讲过利用 PackageParser 解析出来的 apk 信息中，是包含了 Application 信息的。所以我们只需要将 PluginClassLoader 去加载插件的 Application 类，然后反射创建出 Application 的实例即可。

加载插件 Activity
----------------
### 利用壳 Activity 来欺骗系统

``` java
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
```

在 startActivity 中，先假装我们要启动的是 PluginShellActivity ，PluginShellActivity 是我们在 AndroidManifest.xml 中预注册的壳。把真正要启动的插件 Activity 类保存在 PluginDataStorage 中，作为参数隐藏在 Intent 中。这样就欺骗了安卓系统，绕过了 Activity 是否在 AndroidManifest.xml 中注册的道。

### 创建插件 Activity 对象

``` java
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
        if (plugin != null) {
            classLoader = plugin.mClassLoader;
            className = pluginClassName;
        }
    }
    return super.newActivity(classLoader, className, intent);
}
```

Instrumentation 的 newActivity 方法是创建出 Activity 实例的，所以我们在这 hook 了该方法，我们把真正要加载的插件 Activity 类从 PluginDataStorage 取出来。然后替换对应的 classLoader 和 className 即可，这样就实现了创建出插件 Activity 的目的了。

### 填充插件 Activity
``` java
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
```

在 Instrumentation 又 hook 了 callActivityOnCreate 方法，这个方法接下来会去调用 Activity 的 onCreate 方法。所以在这之前我们把插件 Activity 的一些属性填充好，比如插件资源、插件 Application 、插件 Activity 的主题等。

加载插件的 BoardCastReceiver
--------------------------

``` java
private void registerBroadCastReceiver(Plugin plugin) {
    try {
        ArrayList<PackageParser.Activity> receivers = plugin.mPackage.receivers;
        for (PackageParser.Activity receiver : receivers) {
            Object obj = Class.forName(receiver.getComponentName().getClassName(), false, plugin.mClassLoader).newInstance();
            BroadcastReceiver broadcastReceiver = BroadcastReceiver.class.cast(obj);
            // 注册广播
            for (PackageParser.ActivityIntentInfo intentInfo : receiver.intents) {
                context.registerReceiver(broadcastReceiver, intentInfo);
            }
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
}
```

PackageParser 解析出来的 receivers 是插件的 BoardCastReceiver 。所以可以遍历 receivers ，然后一一调用 registerReceiver 进行注册即可。



