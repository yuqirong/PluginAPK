package me.yuqirong.plugin.hook;

import android.app.Instrumentation;

/**
 * @author Zhouyu
 * @date 2018/8/13
 */
public class PluginInstrumentation extends Instrumentation {

    private final Instrumentation instrumentation;

    public PluginInstrumentation(Instrumentation instrumentation) {
        this.instrumentation = instrumentation;
    }


}
