package com.feigdev.utils;

import android.os.Build;

/**
 * Created by ejf3 on 2/14/14.
 */
public class PlatformVersion {

    public static boolean isAboveKitKat(){
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT);
    }
}
