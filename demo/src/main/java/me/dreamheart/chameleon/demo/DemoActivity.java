package me.dreamheart.chameleon.demo;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;

import me.dreamheart.chameleon.common.PlugInfo;
import me.dreamheart.chameleon.loader.PluginLoader;
import me.dreamheart.chameleon.tools.Tools;


public class DemoActivity extends Activity {

    PluginLoader mPluginLoader;
    Button mLoadButton;
    PlugInfo mPlugInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);

        mPluginLoader = ((DemoApp)getApplicationContext()).getPluginLoader();

        mLoadButton = (Button) findViewById(R.id.load_btn);
        mLoadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadApk();
            }
        });

        findViewById(R.id.start_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPluginLoader.startPlugin(DemoActivity.this, mPlugInfo);
            }
        });
    }

    private void loadApk () {
        final ProgressDialog pd = ProgressDialog.show(this, "", "加载中...", false, false);
        new AsyncTask<String, String, Long>(){

            @Override
            protected Long doInBackground(String... params) {
                long startTime = System.currentTimeMillis();
                mPlugInfo = mPluginLoader.installPlugin(new File(Environment.getExternalStorageDirectory(), "appforload.apk"));
                return  System.currentTimeMillis() - startTime;
            }

            @Override
            protected void onProgressUpdate(String... values) {
                super.onProgressUpdate(values);
            }

            @Override
            protected void onPostExecute(Long consume) {
                super.onPostExecute(consume);
                Toast.makeText(DemoActivity.this, "Consume: " + (consume / 1000f) + "s", Toast.LENGTH_LONG).show();
                pd.cancel();
            }

            @Override
            protected void onCancelled() {
                super.onCancelled();
                pd.cancel();
            }
        }.execute("");
    }
}
