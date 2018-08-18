package me.yuqirong.myapplication;

import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import me.yuqirong.plugin.PluginManager;
import me.yuqirong.plugin.entity.Plugin;

/**
 * @author Zhouyu
 * @date 2018/8/14
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private Plugin plugin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String pluginFilePath = Environment.getExternalStorageDirectory().getAbsolutePath()
                        + "/app-debug.apk";
                plugin = PluginManager.getInstance(getApplication()).loadPluginApk(pluginFilePath);
                Log.i(TAG, plugin.toString());
            }
        });

        findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                try {
                    if (plugin != null) {
                        Intent intent = new Intent();
                        intent.setClassName("me.yuqirong.pluginapp", "me.yuqirong.pluginapp.PluginMainActivity");
                        PluginManager.getInstance(getApplication()).startActivity(MainActivity.this, plugin, intent);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

}
