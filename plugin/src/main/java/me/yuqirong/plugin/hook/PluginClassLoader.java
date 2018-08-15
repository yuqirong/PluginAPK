package me.yuqirong.plugin.hook;

import dalvik.system.DexClassLoader;

/**
 * @author Zhouyu
 * @date 2018/8/15
 */
public class PluginClassLoader extends DexClassLoader {

    public PluginClassLoader(String dexPath, String optimizedDirectory, String librarySearchPath, ClassLoader parent) {
        super(dexPath, optimizedDirectory, librarySearchPath, parent);
    }

}
