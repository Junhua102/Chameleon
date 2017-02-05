package me.dreamheart.demo.appforload;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.activity_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, Main2Activity.class));
            }
        });
        new TestClass().test();
        new TestClass.TestSubClass().test(TestClass.TestSubClass.testStr);
        new TestClass.TestSubClass.TestSubSubClass().test();
//        ApplicationInfo appInfo = this.getPackageManager().getApplicationInfo(this.getPackageName(), PackageManager.GET_META_DATA);
//        appInfo.metaData.getInt("DH_APP_CHANNEL");
    }

}
