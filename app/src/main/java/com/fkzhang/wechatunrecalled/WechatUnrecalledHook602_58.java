package com.fkzhang.wechatunrecalled;

import de.robv.android.xposed.XC_MethodHook;

import static de.robv.android.xposed.XposedHelpers.findAndHookConstructor;

/**
 * Created by fkzhang on 1/16/2016.
 */
public class WechatUnrecalledHook602_58 extends WechatUnrecalledHook600 {

    public WechatUnrecalledHook602_58(WechatPackageNames packageNames) {
        super(packageNames);
    }

    protected void hookStorageObject(final ClassLoader loader) {
        findAndHookConstructor(mP.storageClass1, loader, mP.storageMethod1,
                mP.packageName + ".storage.am", mP.packageName + ".storage.an", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        mStorageObject = param.thisObject;
                    }
                });
    }

}
