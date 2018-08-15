package me.yuqirong.myapplication;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import me.yuqirong.plugin.PluginManager;
import me.yuqirong.plugin.entity.Plugin;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pluginFilePath = Environment.getExternalStorageDirectory().getAbsolutePath()
                        + "/plugin-app-debug.apk";
                Plugin plugin = PluginManager.getInstance(getApplication()).loadPluginApk(pluginFilePath);
                Log.i(TAG, plugin.toString());
            }
        });
    }

}
