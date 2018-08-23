package com.niki.amap.locate;

import android.Manifest;
import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.tbruyelle.rxpermissions2.RxPermissions;

/**
 * Created by Niki on 8/22/18 11:32
 * E-Mail Address：m13296644326@163.com
 */

public class LocateNikiClient {

    //持续定位
    public static final int LOCATE_MODE_CONSTANT = 1;
    //单次定位(默认)
    public static final int LOCATE_MODE_ONCE = 2;
    public static final String EVENT_CONSTANT_LOCATE = "event_constant_locate";

    private static final LocateNikiClient ourInstance = new LocateNikiClient();
    private AMapLocationClient mAMapLocationClient;
    private ReactApplicationContext mContext;
    private Promise mPromise;
    private LocateConfig mLocateConfig;

    public static LocateNikiClient getInstance() {
        return ourInstance;
    }

    private LocateNikiClient() {
    }

    private AMapLocation mAMapLocation;

    public void init(ReactApplicationContext context, Activity activity, LocateConfig config) {
        mContext = context;
        mLocateConfig = config;
        RxPermissions rxPermissions = new RxPermissions(activity);
        rxPermissions.request(
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        ).subscribe(granted -> {
            if (granted) {
                executeInit(activity, config);
            } else {
                Toast.makeText(context, "定位权限不足", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void executeInit(Activity context, LocateConfig config) {
        mAMapLocationClient = new AMapLocationClient(context);
        mAMapLocationClient.setLocationListener(new AMapLocationListener() {
            @Override
            public void onLocationChanged(AMapLocation aMapLocation) {
                if (aMapLocation != null && aMapLocation.getErrorCode() == AMapLocation.LOCATION_SUCCESS) {
                    mAMapLocation = aMapLocation;
                    onGetLocation(aMapLocation, AMapLocation.LOCATION_SUCCESS);
                    emmitLocateEvent(AMapLocation.LOCATION_SUCCESS, aMapLocation);
                    Log.i("com.niki.rndemo.log", aMapLocation.getAddress());
                }else {
                    mAMapLocation = null;
                    int errCode = aMapLocation == null ? -1 : aMapLocation.getErrorCode();
                    onGetLocation(null,errCode);
                    emmitLocateEvent(errCode, null);
                }
            }
        });
        AMapLocationClientOption option = new AMapLocationClientOption();
        option.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        option.setNeedAddress(false);
        //设置定位间隔,单位毫秒,默认为2000ms. 这里如果是单次模式, 那么interval值默认会很长, 节约性能
        Log.i("com.niki.rndemo.log","interval: " + config.interval);

        option.setInterval(config.interval);
        //默认为true, 是否获取3s内最精确的定位
        option.setOnceLocationLatest(true);
        //设置需要拿到位置信息,但是只有internet定位才能拿到,使用GPS并不能拿到
        option.setNeedAddress(true);
        //设置定位参数
        mAMapLocationClient.setLocationOption(option);
        // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
        // 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用stopLocation()方法来取消定位请求
        // 在定位结束后，在合适的生命周期调用onDestroy()方法
        // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
        //启动定位3s之内, 会马上获取一次精确定位. 便是js端单次定位的原理
        //启动定位
        mAMapLocationClient.startLocation();
    }

    private void emmitLocateEvent(int errorCode, AMapLocation aMapLocation) {
        mContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(EVENT_CONSTANT_LOCATE, getParamsMap(errorCode, aMapLocation));
    }

    public void getLocation(Promise promise) {
        mPromise = promise;
        if (mAMapLocation != null){
            onGetLocation(mAMapLocation, AMapLocation.LOCATION_SUCCESS);
        }
    }

    private void onGetLocation(AMapLocation aMapLocation, int errCode) {
        if (mPromise == null){
            return;
        }
        if (errCode == AMapLocation.LOCATION_SUCCESS){
            WritableMap map = getParamsMap(AMapLocation.LOCATION_SUCCESS, aMapLocation);
            mPromise.resolve(map);
        }else {
            //为了js端解析方便, 这里也使用resolve, 不使用reject方法
            mPromise.resolve(getParamsMap(errCode, null));
        }
        mPromise = null;
    }

    private WritableMap getParamsMap(int errCode, AMapLocation aMapLocation){
        WritableMap map = new WritableNativeMap();
        map.putString("location", JSON.toJSONString(aMapLocation));
        map.putInt("errorCode", errCode);
        return map;
    }


    // TODO: 8/22/18 如果为空, 那么等待成功, 发起一个计时器, timeOut

    public void destroy() {
        Log.i("com.niki.rndemo.log","interval: " + "function destroy()");
        if (mAMapLocationClient != null) {
            mAMapLocationClient.stopLocation();
            mAMapLocationClient.onDestroy();
        }
    }

    public static class LocateConfig{
        public int locateMode;
        public long interval;
    }

}


