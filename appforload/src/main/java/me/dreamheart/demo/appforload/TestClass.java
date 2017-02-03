package me.dreamheart.demo.appforload;

import android.util.Log;

/**
 * Created by Junhua Lv on 2017/2/1.
 * 测试非public的类调用的问题
 */

class TestClass {
    static class TestSubClass {
        static class TestSubSubClass {
            void test () {
                Log.v("TestClass", "TestSubSubClass ok");
            }
        }
        static String testStr = "TestSubClass ok";
        void test (String str) {
            Log.v("TestClass", str);
        }
    }

    void test() {
        Log.v("TestClass", "test ok");
    }
}
