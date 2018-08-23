package com.niki.amap.locate;


import android.app.Activity;
import android.util.Log;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

/**
 * Created by Niki on 8/22/18 14:24
 * E-Mail Address：m13296644326@163.com
 */

public class LocateNikiModule extends ReactContextBaseJavaModule {

    public static final String KEY_LOCATE_MODE = "locateMode";
    public static final String KEY_INTERVAL = "interval";
    public static final String KEY_MODE_ONCE = "MODE_ONCE";
    public static final String KEY_MODE_CONSTANT = "MODE_CONSTANT";
    public static final String EVENT_NAME_CONSTANT_LOCATE = "EVENT_NAME_LOCATE_CONSTANT";

    public LocateNikiModule(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @Override
    public String getName() {
        return "LocateAMap";
    }

    @Nullable
    @Override
    public Map<String, Object> getConstants() {
        //可以不重写, 定义一些可以供JS端同步使用的常量
        HashMap<String, Object> map = new HashMap<>();
        map.put(KEY_MODE_ONCE, LocateNikiClient.LOCATE_MODE_ONCE);
        map.put(KEY_MODE_CONSTANT,  LocateNikiClient.LOCATE_MODE_CONSTANT);
        map.put(EVENT_NAME_CONSTANT_LOCATE,  LocateNikiClient.EVENT_CONSTANT_LOCATE);
        return map;
    }

    @ReactMethod
    public void getLocation(Promise promise) {
        LocateNikiClient.getInstance().getLocation(promise);
    }

    @ReactMethod
    public void init(ReadableMap map) {
        Log.i("com.niki.rndemo.log","interval: " + "function init()");

        LocateNikiClient.LocateConfig locateConfig = new LocateNikiClient.LocateConfig();
        locateConfig.locateMode = map.getInt(KEY_LOCATE_MODE);
        locateConfig.interval = map.getInt(KEY_INTERVAL);
        Activity activity = getCurrentActivity();
        //当前在子线程, 需要转到主线程
        activity.runOnUiThread(() -> LocateNikiClient.getInstance().init(getReactApplicationContext(), activity, locateConfig));
    }
}
