package me.yuqirong.plugin.hook;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.AssetManager;
import android.content.res.Resources;

import me.yuqirong.plugin.entity.Plugin;

/**
 * @author Zhouyu
 * @date 2018/8/15
 */
public class PluginContext extends ContextWrapper {

    private final Plugin mPlugin;

    public PluginContext(Context context, Plugin plugin) {
        super(context);
        this.mPlugin = plugin;
    }

    @Override
    public Resources getResources() {
        return mPlugin.mResources;
    }

    @Override
    public ClassLoader getClassLoader() {
        return mPlugin.mClassLoader;
    }

    @Override
    public AssetManager getAssets() {
        return mPlugin.mAssetManager;
    }

}
