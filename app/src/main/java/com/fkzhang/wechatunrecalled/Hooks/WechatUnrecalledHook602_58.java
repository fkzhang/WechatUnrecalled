package com.fkzhang.wechatunrecalled.Hooks;

import com.fkzhang.wechatunrecalled.Util.WechatMainDBHelper;
import com.fkzhang.wechatunrecalled.WechatPackageNames;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;

import static de.robv.android.xposed.XposedHelpers.findConstructorExact;

/**
 * Created by fkzhang on 1/16/2016.
 */
public class WechatUnrecalledHook602_58 extends WechatUnrecalledHook600 {

    public WechatUnrecalledHook602_58(WechatPackageNames packageNames) {
        super(packageNames);
    }

    protected void hookDbObject(final ClassLoader loader) {
        XposedBridge.hookMethod(findConstructorExact(w.storageClass1, loader, w.storageMethod1,
                w.packageName + ".storage.am", w.packageName + ".storage.an"),
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        mStorageObject = param.thisObject;
                        init(loader);
                        if (mDb == null) {
                            try {
                                mDb = new WechatMainDBHelper(param.args[0]);
                                mNotificationHelper.setDB(mDb);
                            } catch (Throwable t) {
                                log(t);
                            }
                        }
                    }
                });

//        findAndHookConstructor(w.storageClass1, loader, w.storageMethod1,
//                w.packageName + ".storage.am", w.packageName + ".storage.an", new XC_MethodHook() {
//                    @Override
//                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                        mStorageObject = param.thisObject;
//                        init(loader);
//                        if (mDb == null) {
//                            try {
//                                mDb = new WechatMainDBHelper(param.args[0]);
//                                mNotificationHelper.setDB(mDb);
//                            } catch (Throwable t) {
//                                log(t);
//                            }
//                        }
//                    }
//                });
    }

}
