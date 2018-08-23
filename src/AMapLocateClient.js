/**
 * Created by Niki on 8/22/18 7:56 PM.
 * Email: m13296644326@163.com
 */

import React from 'react';
import {
    NativeModules,
    Platform,
    DeviceEventEmitter
} from 'react-native';

const isIos = Platform.OS === 'ios';
const {LocateAMap} = NativeModules;
const {MODE_ONCE, MODE_CONSTANT, EVENT_NAME_LOCATE_CONSTANT} = LocateAMap;
export default {

    LOCATE_MODE_ONCE: MODE_ONCE,
    LOCATE_MODE_CONSTANT: MODE_CONSTANT,

    init: ({locateMode, interval}) => {
        //ios平台不需要init
        if (isIos) {
            return;
        }
        //默认模式为单次定位
        if (locateMode !== MODE_ONCE && locateMode !== MODE_CONSTANT) {
            locateMode = MODE_ONCE;
        }
        //如果没指定间隔时间, 那么单次默认为20s, 持续默认为3s
        if (!interval) {
            if (locateMode === MODE_ONCE) {
                interval = 20 * 1000;
            } else {
                interval = 3 * 1000;
            }
        }
        LocateAMap.init({locateMode, interval});
    },

    //data为{errorCode, location}, errorCode为0代表正常
    getLocation: () => {
        return LocateAMap.getLocation()
            .then(data => {
                return new Promise((res, rej) => {
                    if (data.errorCode === 0){
                        res( {
                            errorCode: 0,
                            location: JSON.parse(data.location),
                        });
                    }else {
                        rej({
                            errorCode: data.errorCode,
                            location: null,
                        })
                    }
                })
            });
    },

    //返回subscription对象, 可以用来解绑, 可在页面销毁时调用
    //errorCode, location
    addLocateListener: (listener) => {
        return DeviceEventEmitter.addListener(EVENT_NAME_LOCATE_CONSTANT, data => {
            if (data) {
                let {errorCode, location} = data;
                if (errorCode === 0 && location) {
                    listener(errorCode, JSON.parse(location));
                } else {
                    listener(errorCode, location)
                }
            } else {
                listener(-1, null);
            }
        });
    },

    removeAllListener: () => {
        DeviceEventEmitter.removeAllListeners(EVENT_NAME_LOCATE_CONSTANT);
    },

    removeListener: (subscription) => {
        if (!subscription){
            return;
        }
        DeviceEventEmitter.removeSubscription(subscription)
    },

    getLocationIOS: () => {

    }

}