package me.dreamheart.chameleon.dex;

/**
 * Created by Junhua Lv on 2017/2/2.
 */

public class AccessFlagUtils {

    static public boolean isDefault (int accessFlag) {
        return (accessFlag & 0x07) == 0;
    }

    static public int changeToPublic (int accessFlag) {
        return accessFlag | 0x01;
    }
}
