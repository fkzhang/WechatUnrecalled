package com.fkzhang.wechatunrecalled.Hooks;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;

import com.fkzhang.wechatunrecalled.Util.WechatMainDBHelper;
import com.fkzhang.wechatunrecalled.WechatPackageNames;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.callStaticMethod;
import static de.robv.android.xposed.XposedHelpers.findConstructorExact;

/**
 * Created by fkzhang on 1/16/2016.
 */
public class WechatUnrecalledHook600 extends WechatUnrecalledHook {

    protected Object mStorageObject;

    public WechatUnrecalledHook600(WechatPackageNames packageNames) {
        super(packageNames);
    }

    public void hook(ClassLoader loader) {
        try {
            hookRecall(loader);
        } catch (Exception e) {
            XposedBridge.log(e);
        }
        try {
            hookDatabase(loader);
        } catch (Exception e) {
            XposedBridge.log(e);
        }
        try {
            hookDbObject(loader);
        } catch (Exception e) {
            XposedBridge.log(e);
        }
        try {
            findAndHookConstructor("dalvik.system.DexClassLoader", loader, String.class, String.class,
                    String.class, ClassLoader.class, new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            String dexPath = (String) param.args[0];
                            ClassLoader classLoader = (ClassLoader) param.thisObject;
                            if (dexPath.contains(w.packageName + ".plugin.mutidex")) {
                                try {
                                    hookSns(classLoader);
                                } catch (Exception e) {
                                    XposedBridge.log(e);
                                }
                            }
                        }
                    });
        } catch (Exception e) {
            XposedBridge.log(e);
        }
        try {
            hookApplicationPackageManager(loader);
        } catch (Throwable t) {
            XposedBridge.log(t);
        }
    }

    protected void hookNotification(final ClassLoader loader) {
        // notification
        XposedHelpers.findAndHookMethod(w.notificationClass, loader,
                "a", w.packageName + ".booter.u", String.class, String.class, int.class,
                int.class, boolean.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        super.beforeHookedMethod(param);

                        String talker = (String) param.args[1];
                        String content = (String) param.args[2];
                        int type = (int) param.args[3];

                        displayNotification(param, talker, content, type);

                    }
                });
    }

    protected void hookDbObject(final ClassLoader loader) {
        XposedBridge.hookMethod(findConstructorExact(w.storageClass1, loader, w.storageMethod1),
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

//        findAndHookConstructor(w.storageClass1, loader, w.storageMethod1, new XC_MethodHook() {
//            @Override
//            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                mStorageObject = param.thisObject;
//                init(loader);
//                if (mDb == null) {
//                    try {
//                        mDb = new WechatMainDBHelper(param.args[0]);
//                        mNotificationHelper.setDB(mDb);
//                    } catch (Throwable t) {
//                        log(t);
//                    }
//                }
//            }
//        });
    }

    protected void updateMessageCount() {
        callMethod(callMethod(mStorageObject, w.msgCountMethod1, "message"), w.msgCountMethod2);
    }

    protected Bitmap getImage(String path) {
        String str = null;
        try {
            str = (String) callMethod(callStaticMethod(mImgClss, w.imageMethod1),
                    w.imageMethod2, path, "", "");
        } catch (Exception e) {
            XposedBridge.log(e);
        }

        if (TextUtils.isEmpty(str))
            return null;

        return BitmapFactory.decodeFile(str);
    }

}
