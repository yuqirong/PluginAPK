package me.yuqirong.plugin.entity;

import android.app.Application;
import android.content.pm.PackageParser;
import android.content.res.AssetManager;
import android.content.res.Resources;

import me.yuqirong.plugin.hook.PluginClassLoader;

/**
 * @author Zhouyu
 * @date 2018/8/15
 */
public class Plugin {

    public String mFilePath;

    public PackageParser.Package mPackage;

    public AssetManager mAssetManager;

    public Resources mResources;

    public PluginClassLoader mClassLoader;

    public Application mApplication;

}
