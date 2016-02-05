package com.fkzhang.wechatunrecalled;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;

import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.callStaticMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookConstructor;

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
            hookStorageObject(loader);
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
                            if (dexPath.contains(mP.packageName + ".plugin.mutidex")) {
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

    protected void hookStorageObject(final ClassLoader loader) {
        findAndHookConstructor(mP.storageClass1, loader, mP.storageMethod1, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                mStorageObject = param.thisObject;
            }
        });
    }

    protected void updateMessageCount() {
        callMethod(callMethod(mStorageObject, mP.msgCountMethod1, "message"), mP.msgCountMethod2);
    }

    protected Bitmap getImage(String path) {
        String str = null;
        try {
            str = (String) callMethod(callStaticMethod(mImgClss, mP.imageMethod1),
                    mP.imageMethod2, path, "", "");
        } catch (Exception e) {
        }

        if (TextUtils.isEmpty(str))
            return null;

        return BitmapFactory.decodeFile(str);
    }

}
