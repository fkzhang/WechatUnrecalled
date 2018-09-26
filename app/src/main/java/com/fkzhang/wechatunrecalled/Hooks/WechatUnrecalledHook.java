package com.fkzhang.wechatunrecalled.Hooks;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Message;
import android.support.v4.util.SparseArrayCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.fkzhang.wechatunrecalled.Util.ImageUtil;
import com.fkzhang.wechatunrecalled.Util.NotificationHelper;
import com.fkzhang.wechatunrecalled.Util.SettingsHelper;
import com.fkzhang.wechatunrecalled.Util.WechatMainDBHelper;
import com.fkzhang.wechatunrecalled.Util.WechatSnsDBHelper;
import com.fkzhang.wechatunrecalled.WechatPackageNames;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.callStaticMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static de.robv.android.xposed.XposedHelpers.setObjectField;

/**
 * Created by fkzhang on 1/16/2016.
 */
public class WechatUnrecalledHook {
    protected final SettingsHelper mSettings;
    protected final NotificationHelper mNotificationHelper;
    protected final String TAG_DELETED = "[已删除]";
    protected WechatPackageNames w;
    protected Object mObject;
    protected Context mNotificationContext;
    protected Class<?> mImgClss;
    protected Class<?> mAvatarLoader;
    protected HashMap<String, Bitmap> mAvatarCache;
    protected Class<?> mCommentClass;
    protected Class<?> mDbClass1;
    protected Class<?> mSnsContentClass;
    protected Class<?> mSnsAttrClass;
    protected boolean mDebug = false;
    protected Class<?> mConversationClass;
    protected WechatMainDBHelper mDb;
    protected WechatSnsDBHelper mSnsDb;
    protected Class<?> SnsLuckyMoneyWantSeePhotoUI;
    protected Class<?> snsLuckyMoneyDataClass;

    public WechatUnrecalledHook(WechatPackageNames packageNames) {
        this.w = packageNames;
        mSettings = new SettingsHelper("com.fkzhang.wechatunrecalled");
        mAvatarCache = new HashMap<>();
        mNotificationHelper = new NotificationHelper(packageNames);
    }

    public static void findAndHookConstructor(String className, ClassLoader classLoader, Object... parameters) {
        Class<?> cls = findClass(className, classLoader);
        Class<?>[] parameterTypes = new Class[parameters.length - 1];
        for (int i = 0; i < parameters.length - 1; i++) {
            if (parameters[i] instanceof String) {
                parameterTypes[i] = findClass((String) parameters[i], classLoader);
            } else if (parameters[i] instanceof Class) {
                parameterTypes[i] = (Class<?>) parameters[i];
            }
        }
        try {
            Constructor<?> constructor = cls.getDeclaredConstructor(parameterTypes);
            constructor.setAccessible(true);
            XC_MethodHook callback = (XC_MethodHook) parameters[parameters.length - 1];
            XposedBridge.hookMethod(constructor, callback);
        } catch (Throwable t) {
            XposedBridge.log(t);
        }
    }

    public void hook(final ClassLoader loader) {
        try {
            XposedBridge.log("chengongle0");
            hookRecall(loader);
            XposedBridge.log("chengongle1");
        } catch (Throwable e) {
            XposedBridge.log(e);
        }
        try {
            hookDatabase(loader);
        } catch (Throwable e) {
            XposedBridge.log(e);
        }
        try {
            hookDbObject(loader);
        } catch (Throwable t) {
            XposedBridge.log(t);
        }
        try {
            hookSns(loader);
        } catch (Throwable e) {
            XposedBridge.log(e);
        }
        try {
            hookApplicationPackageManager(loader);
        } catch (Throwable t) {
            XposedBridge.log(t);
        }
        try {
            hookLauncherUI(loader);
        } catch (Throwable t) {
            XposedBridge.log(t);
        }
        try {
            hookNotification(loader);
        } catch (Throwable t) {
            XposedBridge.log(t);
        }
        try {
            hookSnsLucky(loader);
        } catch (Throwable t) {
            XposedBridge.log(t);
        }

    }

    protected void hookRecall(final ClassLoader loader) {
        XposedHelpers.findAndHookMethod(w.recallClass, loader,
                w.recallMethod, String.class, String.class, String.class,
                new XC_MethodHook() {
                    @SuppressWarnings("unchecked")
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        preventMsgRecall(param);
                    }
                });
    }

    protected void hookSns(final ClassLoader loader) {
        findAndHookConstructor(w.snsClass, loader,
                w.snsMethod, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        init(loader);
                        if (mSnsDb == null) {
                            mSnsDb = new WechatSnsDBHelper(w, param.args[0]);
                        }

                        mSettings.reload();
                        unRecallSnsComments();
                        unRecallSnsMoments();
                    }
                });
    }

    protected void hookSnsLucky(final ClassLoader loader) {
        if (!mSettings.getBoolean("snslucky", true))
            return;

        if (!TextUtils.isEmpty(w.snsLuckyMoneyClass1)) {
            try {
                if (snsLuckyMoneyDataClass == null) {
                    snsLuckyMoneyDataClass = findClass(w.snsLuckyMoneyClass1, loader);
                }
                // replace detailed
                findAndHookMethod(snsLuckyMoneyDataClass, w.snsLuckyMoneyBlur,
                        w.snsLuckyMoneyClass2,
                        new XC_MethodHook() {
                            @Override
                            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                                if (mSettings.getBoolean("snslucky", true)) {
                                    param.setResult(callStaticMethod(snsLuckyMoneyDataClass,
                                            w.snsLuckyMoneyOrignal, param.args[0]));
                                }

                            }
                        });

            } catch (Throwable t) {
                XposedBridge.log(t);
            }
            try {
                // replace thumbnail
                findAndHookMethod(snsLuckyMoneyDataClass, w.snsLuckyMoneyBlur2,
                        w.snsLuckyMoneyClass2,
                        new XC_MethodHook() {
                            @Override
                            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                                if (mSettings.getBoolean("snslucky", true)) {
                                    param.setResult(callStaticMethod(snsLuckyMoneyDataClass,
                                            w.snsLuckyMoneyOrignal, param.args[0]));
                                }
                            }
                        });

            } catch (Throwable t) {
                XposedBridge.log(t);
            }
        }
        try {
            findAndHookConstructor(w.snsLuckyMoneyClass3, loader, int.class, Activity.class,
                    w.snsLuckyMoneyClass4, new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            final Activity activity = (Activity) param.args[1];
                            if (SnsLuckyMoneyWantSeePhotoUI != null)
                                return;

                            ClassLoader classLoader = activity.getClassLoader();
                            SnsLuckyMoneyWantSeePhotoUI = findClass(w.snsLuckyMoneyWantSeePhotoUI, classLoader);

                            findAndHookMethod(SnsLuckyMoneyWantSeePhotoUI,
                                    w.snsLuckyMoneyWantSeePhotoUIConstructor,
                                    SnsLuckyMoneyWantSeePhotoUI, new XC_MethodHook() {
                                        @Override
                                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                                            Object snsLuckyMoneyWantSeePhotoUI = param.args[0];
                                            ImageView banner = (ImageView) getObjectField(snsLuckyMoneyWantSeePhotoUI, w.snsLuckyMoneyBanner);
                                            Button button = (Button) getObjectField(snsLuckyMoneyWantSeePhotoUI, w.snsLuckyMoneyButton);
                                            ImageView imageView = (ImageView) getObjectField(snsLuckyMoneyWantSeePhotoUI, w.snsLuckyMoneyBannerImageView);
                                            TextView textView = (TextView) getObjectField(snsLuckyMoneyWantSeePhotoUI, w.snsLuckyMoneyBannerTextView);
                                            View view = (View) getObjectField(snsLuckyMoneyWantSeePhotoUI, w.snsLuckyMoneyBannerView1);
                                            View view2 = (View) getObjectField(snsLuckyMoneyWantSeePhotoUI, w.snsLuckyMoneyBannerView2);

                                            view.setVisibility(View.GONE);
                                            view2.setVisibility(View.GONE);
                                            textView.setVisibility(View.GONE);
                                            button.setVisibility(View.GONE);
                                            banner.setVisibility(View.GONE);
                                            imageView.setVisibility(View.GONE);
                                        }
                                    });

                            if (!TextUtils.isEmpty(w.luckyRevealImageView)) {
                                findAndHookMethod(w.luckyRevealImageView, loader,
                                        w.snsLuckyMoneyMethod1, w.luckyRevealImageView, new XC_MethodHook() {
                                            @Override
                                            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                                                param.setResult(null);
                                            }
                                        });
                                findAndHookMethod(w.luckyRevealImageView, loader,
                                        w.snsLuckyMoneySetBitmapMethod, new XC_MethodHook() {
                                            @Override
                                            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                                                final Bitmap bitmap = (Bitmap) callStaticMethod(snsLuckyMoneyDataClass,
                                                        w.snsLuckyMoneyBitmapDecoder,
                                                        callMethod(param.thisObject, w.snsLuckyMoneyRevealBigpicture));
                                                callMethod(param.thisObject, "setImageBitmap", bitmap);

                                                ImageView imageView = (ImageView) param.thisObject;
                                                imageView.setOnLongClickListener(new View.OnLongClickListener() {
                                                    @Override
                                                    public boolean onLongClick(View v) {
                                                        saveBitmapFile("temp", bitmap, new ImageUtil.OnImageProcessListener() {
                                                            @Override
                                                            public void onComplete(Bitmap bitmap) {
                                                                Intent shareIntent = new Intent();
                                                                shareIntent.setAction(Intent.ACTION_SEND);
                                                                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // temp permission for receiving app to read this file
                                                                File imageFile = new File(mNotificationContext.getCacheDir(), "temp.png");
                                                                shareIntent.setType("image/*");
                                                                shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(imageFile));
                                                                activity.startActivity(Intent.createChooser(shareIntent, mSettings.getString("share_image", "分享图片")));
                                                            }
                                                        });

                                                        return false;
                                                    }
                                                });
                                            }
                                        });
                            }

                        }
                    });
        } catch (Throwable t) {
            log(t);
        }
    }

    protected void hookLauncherUI(final ClassLoader loader) {
        findAndHookMethod(w.launcherUI, loader, "onResume", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                mSettings.reload();
                if (mNotificationHelper.isInit()) {
                    mNotificationHelper.cancelNotifications();
                }
            }
        });
    }

    protected void hookDatabase(ClassLoader loader) {
        findAndHookMethod(w.SQLiteDatabaseClass, loader,
                "updateWithOnConflict", String.class, ContentValues.class, String.class,
                String[].class, int.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        preventCommentRecall(param);
                        preventMomentRecall(param);
                    }
                });
        findAndHookMethod(w.SQLiteDatabaseClass, loader,
                "insertWithOnConflict", String.class, String.class, ContentValues.class, int.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        mSettings.reload();
                        if (!mSettings.getBoolean("new_comment_notification_enable", true))
                            return;

                        String s = (String) param.args[0];

                        if (!s.equalsIgnoreCase("snscomment"))
                            return;

                        ContentValues v = (ContentValues) param.args[2];
                        String talker = (String) v.get("talker");
                        Bitmap icon = getAccountAvatar(talker);
                        String title = mDb.getNickname(talker) + " "
                                + mSettings.getString("new_comment", "(新评论)");
                        String content = mSnsDb.getCommentContent(mSnsDb.decodeBlob(mCommentClass,
                                v.getAsByteArray("curActionBuf")));

                        Intent intent = new Intent();
                        intent.setClassName(mNotificationContext.getPackageName(),
                                w.snsCommentDetailUI);

                        intent.putExtra("INTENT_SNSID", "sns_table_" + v.getAsString("snsID"));
                        intent.putExtra("INTENT_FROMSUI", true);
                        intent.putExtra("INTENT_FROMSUI_COMMENTID", v.getAsLong("commentSvrID"));

                        mNotificationHelper.showCommentNotification(title, content, icon,
                                intent, "new_comment");
                    }
                });

        findAndHookMethod(w.SQLiteDatabaseClass, loader,
                "executeSql", String.class, Object[].class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        String query = (String) param.args[0];
                        if (mSettings.getBoolean("prevent_moments_recall", true) &&
                                query.toLowerCase().contains("snsinfo set sourcetype")) {
                            param.setResult(null);
                        }
                    }
                });


    }

    protected void hookDbObject(final ClassLoader loader) {
        // get database object
        findAndHookConstructor(w.storageClass1, loader,
                w.storageMethod1, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        // look for: LinkedBlockingQueue
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
    }

    protected void hookNotification(final ClassLoader loader) {
        // notification
        XposedHelpers.findAndHookMethod(w.notificationClass, loader,
                "handleMessage", Message.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        Message message = (Message) param.args[0];
                        String talker = message.getData().getString("notification.show.talker");
                        String content = message.getData().getString("notification.show.message.content");
                        int type = message.getData().getInt("notification.show.message.type");
                        displayNotification(param, talker, content, type);
                    }
                });
    }

    protected void hookApplicationPackageManager(ClassLoader loader) {
        findAndHookMethod("android.app.ApplicationPackageManager", loader,
                "getInstalledApplications", int.class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        @SuppressWarnings("unchecked") List<ApplicationInfo> applicationInfoList
                                = (List<ApplicationInfo>) param.getResult();
                        ArrayList<ApplicationInfo> to_remove = new ArrayList<>();
                        for (ApplicationInfo info : applicationInfoList) {
                            if (info.packageName.contains("com.fkzhang") ||
                                    info.packageName.contains("de.robv.android.xposed.installer")) {
                                to_remove.add(info);
                            }
                        }
                        if (to_remove.isEmpty())
                            return;

                        applicationInfoList.removeAll(to_remove);
                    }
                });
        findAndHookMethod("android.app.ApplicationPackageManager", loader,
                "getInstalledPackages", int.class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        @SuppressWarnings("unchecked") List<PackageInfo> packageInfoList
                                = (List<PackageInfo>) param.getResult();
                        ArrayList<PackageInfo> to_remove = new ArrayList<>();
                        for (PackageInfo info : packageInfoList) {
                            if (info.packageName.contains("com.fkzhang") ||
                                    info.packageName.contains("de.robv.android.xposed.installer")) {
                                to_remove.add(info);
                            }
                        }
                        if (to_remove.isEmpty())
                            return;

                        packageInfoList.removeAll(to_remove);
                    }
                });
    }

    protected void updateMessageCount() {
        callMethod(mObject, w.updateMsgId);
    }

    protected void preventMsgRecall(XC_MethodHook.MethodHookParam param) {
        String xml = (String) param.args[0];
        String tag = (String) param.args[1];
        if (TextUtils.isEmpty(xml) || TextUtils.isEmpty(tag) ||
                !tag.equals("sysmsg") || !xml.contains("revokemsg")) {
            return;
        }

        @SuppressWarnings("unchecked") Map<String, String> map =
                (Map<String, String>) param.getResult();
        if (map == null)
            return;

        String key = ".sysmsg.$type";
        if (!map.containsKey(key))
            return;

        String type = map.get(key);
        if (type == null || !type.equals("revokemsg"))
            return;

        final String talker = map.get(".sysmsg.revokemsg.session");
        String replacemsg = map.get(".sysmsg.revokemsg.replacemsg");
        String msgsvrid = map.get(".sysmsg.revokemsg.newmsgid");

        if (replacemsg.startsWith("你") || replacemsg.toLowerCase().startsWith("you")) {
            return;
        }

        mSettings.reload();
        String[] strings = replacemsg.split("\"");
        replacemsg = "\"" + strings[1] + "\" " + mSettings.getString("recalled",
                "尝试撤回上一条消息 （已阻止)");

        map.put(key, null);
        param.setResult(map);

        try {
            Cursor cursor = mDb.getMessageBySvrId(msgsvrid);
            if (cursor == null || !cursor.moveToFirst())
                return;

            if (mSettings.getBoolean("msg_recall_notification_enable", true)) {
                final Bitmap icon = getAccountAvatar(talker);
                int t = cursor.getInt(cursor.getColumnIndex("type"));
                String content;
                switch (t) {
                    case 1: // text
                        content = cursor.getString(cursor.getColumnIndex("content")).trim();
                        if (talker.contains("@chatroom")) {
                            int idx = content.indexOf(":");
                            if (idx != -1) {
                                content = content.substring(idx + 1, content.length()).trim();
                            }
                        }
                        mNotificationHelper.showTextNotification(talker, replacemsg, content, icon);
                        if (mSettings.getBoolean("show_content", false)
                                && !TextUtils.isEmpty(content)) {
                            replacemsg += ": " + content;
                        }
                        break;
                    case 3: // image
                        String imgPath = cursor.getString(cursor.getColumnIndex("imgPath"));
                        if (!TextUtils.isEmpty(imgPath)) {
                            Bitmap bitmap = getImage(imgPath);
                            Intent intent = new Intent();
                            intent.putExtra("img_gallery_talker", talker);
                            intent.putExtra("img_gallery_msg_svr_id", Long.parseLong(msgsvrid));
                            String summary = mSettings.getString("img_summary",
                                    "[图片] 点击查看");
                            mNotificationHelper.showImageNotification(talker, replacemsg, bitmap, intent, summary, icon);
                        }
                        break;
                    case 34:// audio
                        mNotificationHelper.showTextNotification(talker, replacemsg, mSettings.getString("audio", "[语音]"),
                                icon);
                        break;
                    case 47:// emoji
                        if (TextUtils.isEmpty(w.emojiMethod1))
                            break;
                        content = cursor.getString(cursor.getColumnIndex("content")).trim();
                        String cdnurl = content.substring(content.indexOf("cdnurl"), content.length());
                        cdnurl = "http:" + cdnurl.substring(cdnurl.indexOf("//"), cdnurl.indexOf("designerid"))
                                .replaceAll("\"", "").trim();

                        final String md5 = cursor.getString(cursor.getColumnIndex("imgPath"));
                        String nickname = null;
                        if (talker.contains("@chatroom")) {
                            nickname = mDb.getChatroomMemberName(content.substring(0, content.indexOf(":")).trim());
                        }
                        final String name = nickname;

                        final String finalReplacemsg = replacemsg;
                        ImageUtil.getBitmapFromURL(cdnurl, new ImageUtil.OnImageProcessListener() {
                            @Override
                            public void onComplete(Bitmap bitmap) {
                                mNotificationHelper.showEmojiNotification(finalReplacemsg, bitmap,
                                        mNotificationHelper.getChatModeIntent(talker),
                                        getEmojiText(name, md5), icon);
                            }
                        });
                        break;
                    case 62: // video
                        Intent intent = new Intent();
                        intent.putExtra("img_gallery_talker", talker);
                        intent.putExtra("img_gallery_msg_svr_id", Long.parseLong(msgsvrid));
                        String summary = mSettings.getString("video_summary",
                                "[小视频] 点击查看");
                        mNotificationHelper.showImageNotification(talker, replacemsg, null, intent, summary, icon);
                        break;
                }
            }

            long createTime = cursor.getLong(cursor.getColumnIndex("createTime"));
            int idx = cursor.getColumnIndex("talkerId");
            int talkerId = -1;
            if (idx != -1) {
                talkerId = cursor.getInt(cursor.getColumnIndex("talkerId"));
            }
            cursor.close();
            mDb.insertSystemMessage(talker, talkerId, replacemsg, createTime + 1);
            updateMessageCount();
        } catch (Throwable t) {
            XposedBridge.log(t);
        }

    }

    protected void unRecallSnsComments() {
        if (!mSettings.getBoolean("prevent_comments_recall", true))
            return;
        String query = "SELECT commentSvrID,curActionBuf FROM SnsComment WHERE commentflag=1";
        Cursor cursor = mSnsDb.rawQuery(query);

        if (cursor == null || !cursor.moveToFirst())
            return;

        do {
            ContentValues v = new ContentValues();
            v.put("commentSvrID", cursor.getString(cursor.getColumnIndex("commentSvrID")));
            v.put("curActionBuf", cursor.getBlob(cursor.getColumnIndex("curActionBuf")));
            setCommentDeleteFlag(v);
        } while (cursor.moveToNext());
        cursor.close();
    }

    protected void unRecallSnsMoments() {
        if (!mSettings.getBoolean("prevent_moments_recall", true))
            return;

        String query = "SELECT snsId,content,sourceType FROM SnsInfo WHERE sourceType=0";
        Cursor cursor = mSnsDb.rawQuery(query);
        if (cursor == null || !cursor.moveToFirst())
            return;

        do {
            ContentValues v = new ContentValues();
//            v.put("sourceType", 10);
            v.put("snsId", cursor.getString(cursor.getColumnIndex("snsId")));
            v.put("content", cursor.getBlob(cursor.getColumnIndex("content")));
//            if (cursor.getInt(cursor.getColumnIndex("sourceType")) == 0) {
            setSnsDeleteFlag(v);
//            } else {
//                unsetSnsDeleteFlag(v);
//            }
        } while (cursor.moveToNext());
        cursor.close();
    }

    public void preventCommentRecall(XC_MethodHook.MethodHookParam param) {
        String table = (String) param.args[0];
        if (!table.equalsIgnoreCase("snscomment"))
            return;
        mSettings.reload();

        ContentValues v = (ContentValues) param.args[1];
        if (v.containsKey("commentflag") && v.getAsInteger("commentflag") == 1 &&
                mSettings.getBoolean("prevent_comments_recall", true)) {
            param.setResult(null); // prevent call

            setCommentDeleteFlag(v);

            if (!mSettings.getBoolean("comment_recall_notification_enable", true))
                return;

            String talker = v.getAsString("talker");
            Bitmap icon = getAccountAvatar(talker);
            String title = mDb.getNickname(talker) + " " +
                    mSettings.getString("comment_recall_content", "");

            String content = mSnsDb.getCommentContent(mSnsDb.decodeBlob(mCommentClass,
                    v.getAsByteArray("curActionBuf")));

            Intent resultIntent = new Intent();
            resultIntent.setClassName(mNotificationContext.getPackageName(), w.SnsMsgUI);

            mNotificationHelper.showCommentNotification(title, content, icon, resultIntent,
                    "comment_recall");
        }
    }

    public void preventMomentRecall(XC_MethodHook.MethodHookParam param) {
        String table = (String) param.args[0];
        if (!table.equalsIgnoreCase("snsinfo"))
            return;
        mSettings.reload();

        ContentValues v = (ContentValues) param.args[1];
        if (mSettings.getBoolean("prevent_moments_recall", true) &&
                v.containsKey("sourceType") && v.containsKey("type")) {
            int sourceType = v.getAsInteger("sourceType");
            int type = v.getAsInteger("type");
            //type: 2: text, 21 luckymoneyphoto,
            if (sourceType == 0 || (type != 2 && sourceType == 8/*set to private*/)) {
                param.setResult(null); // prevent call
                setSnsDeleteFlag(v);
            } else if (type == 2 || type == 21 || sourceType != 10) {
                if (isDeletedMarked(v.getAsString("snsId"))) {
                    param.setResult(null); // prevent call
                }
            }
        }

        if (mSettings.getBoolean("prevent_comments_recall", true)) {
            updateSnsCommentChange(param);
        }
    }

    protected void displayNotification(XC_MethodHook.MethodHookParam param, final String talker, String content, final int type) {

        if (!mSettings.getBoolean("custom_notification_enable", false))
            return;

        if (!(type == 1 || type == 3 || type == 34 || type == 47 || type == 62))
            return;

        try {
            Cursor cursor = mDb.getLastMsg(talker);
            if (cursor == null || !cursor.moveToFirst())
                return;

            final Bitmap icon = getAccountAvatar(talker);
            final String title = mDb.getChatroomName(talker);
            switch (type) {
                case 1://text
                    content = cursor.getString(cursor.getColumnIndex("content"));
                    if (talker.contains("@chatroom")) { // from chatroom
                        int idx = content.indexOf(":");
                        if (idx != -1) {
                            String n = mDb.getChatroomMemberName(content.substring(0, idx));
                            content = content.substring(idx + 1, content.length()).trim();
                            content = TextUtils.isEmpty(n) ? content : n + ":" + content;
                        }
                    }
                    mNotificationHelper.displayCustomNotification(talker,
                            title, content, icon, null, type,
                            mNotificationHelper.getChatModeIntent(talker));
                    param.setResult(null); // prevent call
                    break;
                case 3://image
                    final String imgPath = cursor.getString(cursor.getColumnIndex("imgPath"));
                    if (!TextUtils.isEmpty(imgPath)) {
                        Bitmap bitmap = getImage(imgPath);
                        Intent intent = mNotificationHelper.getImageIntent();
                        intent.putExtra("img_gallery_talker", talker);
                        intent.putExtra("img_gallery_msg_svr_id",
                                cursor.getLong(cursor.getColumnIndex("msgSvrId")));
                        String summary = mSettings.getString("img_summary", "[图片] 点击查看");
                        mNotificationHelper.displayCustomNotification(talker,
                                title, summary, icon, bitmap, type, intent);
                        param.setResult(null); // prevent call
                    }
                    break;
                case 34: // audio
                    mNotificationHelper.displayCustomNotification(talker,
                            title, mSettings.getString("audio", "[语音]"), icon, null, type,
                            mNotificationHelper.getChatModeIntent(talker));
                    param.setResult(null); // prevent call
                    break;
                case 47: // emoji
                    if (TextUtils.isEmpty(w.emojiMethod1))
                        break;

                    String cdnurl = content.substring(content.indexOf("cdnurl"), content.length());
                    cdnurl = "http:" + cdnurl.substring(cdnurl.indexOf("//"), cdnurl.indexOf("designerid"))
                            .replaceAll("\"", "").trim();

                    final String md5 = cursor.getString(cursor.getColumnIndex("imgPath"));
                    String nickname = null;
                    if (talker.contains("@chatroom")) {
                        nickname = mDb.getChatroomMemberName(content.substring(0, content.indexOf(":")).trim());
                    }
                    final String name = nickname;

                    ImageUtil.getBitmapFromURL(cdnurl, new ImageUtil.OnImageProcessListener() {
                        @Override
                        public void onComplete(Bitmap bitmap) {
                            mNotificationHelper.displayCustomNotification(talker,
                                    title, getEmojiText(name, md5), icon, bitmap, type,
                                    mNotificationHelper.getChatModeIntent(talker));
                        }
                    });
                    param.setResult(null); // prevent call
                    break;
                case 62://video
                    Intent intent = mNotificationHelper.getImageIntent();
                    intent.putExtra("img_gallery_talker", talker);
                    intent.putExtra("img_gallery_msg_svr_id",
                            cursor.getLong(cursor.getColumnIndex("msgSvrId")));
                    String summary = mSettings.getString("video_summary", "[小视频] 点击查看");
                    mNotificationHelper.displayCustomNotification(talker,
                            title, summary, icon, null, type, intent);
                    param.setResult(null); // prevent call
                    break;
            }
            cursor.close();
        } catch (Throwable t) {
            log(t);
        }
    }

    protected Bitmap getImage(String path) {
        String str = (String) callMethod(callStaticMethod(mImgClss, w.imageMethod1),
                w.imageMethod2, path);
        if (TextUtils.isEmpty(str))
            return null;

        return BitmapFactory.decodeFile(str);
    }

    protected Bitmap getAccountAvatar(String accountName) {
        if (mAvatarCache.containsKey(accountName)) {
            Bitmap bitmap = mAvatarCache.get(accountName);
            if (!bitmap.isRecycled())
                return bitmap;
        }
        if (mAvatarLoader == null)
            return null;

        Bitmap avatar = null;
        try {
            avatar = (Bitmap) callMethod(callStaticMethod(mAvatarLoader, w.avatarMethod1),
                    w.avatarMethod2, accountName, false, -1);
            if (avatar != null) {
                mAvatarCache.put(accountName, avatar);
            }
        } catch (Throwable t) {
            XposedBridge.log(t);
        }
        if (avatar == null) {
            avatar = mNotificationHelper.getLargeIcon();
        }
        return avatar;
    }

    protected void init(ClassLoader loader) {
        if (mDbClass1 == null) {
            try {
                mDbClass1 = findClass(w.dbClass1, loader);
            } catch (Throwable t) {
                log(t);
            }
        }
        if (mObject == null && mDbClass1 != null) {
            try {
                mObject = callMethod(callStaticMethod(mDbClass1, w.dbMethod1), w.dbMethod2);
            } catch (Throwable t) {
                log(t);
            }
        }

        // look for: field_imgPath in pluginsdk/model/app
        if (mImgClss == null) {
            try {
                mImgClss = findClass(w.imageClass, loader);
            } catch (Throwable t) {
                log(t);
            }
        }

        // look for: notification_icon
        if (mNotificationContext == null) {
            try {
                mNotificationContext = (Context) callStaticMethod(findClass(w.contextGetter, loader),
                        "getContext");
            } catch (Throwable t) {
                log(t);
            }
        }
        if (!mNotificationHelper.isInit()) {
            // look for: notification_icon
            try {
                mNotificationHelper.init((Context) callStaticMethod(findClass(w.contextGetter, loader),
                        "getContext"), findClass(w.iconClass, loader), findClass(w.launcherUI, loader));
                mNotificationHelper.setSettings(mSettings);
            } catch (Throwable t) {
                log(t);
            }
        }

        // look for: field_curActionBuf
        if (mCommentClass == null) {
            try {
                mCommentClass = findClass(w.commentClass, loader);
            } catch (Throwable t) {
                log(t);
            }
        }
        if (mAvatarLoader == null) {
            try {
                mAvatarLoader = findClass(w.avatarClass, loader);
            } catch (Throwable t) {
                log(t);
            }
        }
        if (mSnsContentClass == null) {
            // look for in sns: field_content
            try {
                mSnsContentClass = findClass(w.snsContentClass, loader);
            } catch (Throwable t) {
                log(t);
            }
        }
        if (mSnsAttrClass == null) {
            // look for field_attrBuf
            try {
                mSnsAttrClass = findClass(w.snsAttrClass, loader);
            } catch (Throwable t) {
                log(t);
            }
        }

        try {
            if (mConversationClass == null) {
                mConversationClass = findClass(w.emojiClass, loader);
            }
        } catch (Throwable t) {
            log(t);
        }

    }

    protected void setSnsDeleteFlag(ContentValues v) {
        Object contentObject = mSnsDb.decodeBlob(mSnsContentClass, v.getAsByteArray("content"));
        if (contentObject == null)
            return;

        String content = WechatSnsDBHelper.removeDeletedTag(mSnsDb.getSnsContent(contentObject));

        content = mSettings.getString("deleted", TAG_DELETED) + "\n" + content;
        setObjectField(contentObject, w.snsContentField, content);

        ContentValues c = new ContentValues();
        c.put("content", WechatSnsDBHelper.encodeContentBlob(contentObject));
        c.put("sourceType", 10);

        mSnsDb.update("SnsInfo", c, "snsId = ?", new String[]{v.getAsString("snsId")});
    }

    protected void setCommentDeleteFlag(ContentValues v) {
        Object commentObject = mSnsDb.decodeBlob(mCommentClass, v.getAsByteArray("curActionBuf"));
        String content = WechatSnsDBHelper.removeDeletedTag(mSnsDb.getCommentContent(commentObject));
        content += " " + mSettings.getString("deleted", TAG_DELETED);
        setObjectField(commentObject, w.commentContentField, content);
        ContentValues c = new ContentValues();
        c.put("curActionBuf", WechatSnsDBHelper.encodeContentBlob(commentObject));
        c.put("commentflag", 0);
        mSnsDb.update("SnsComment", c, "commentSvrID = ?", new String[]{v.getAsString("commentSvrID")});
    }

    private boolean isDeletedMarked(String snsId) {
        String query = "SELECT content FROM SnsInfo WHERE snsId=" + snsId;
        Cursor cursor = mSnsDb.rawQuery(query);
        if (cursor == null || !cursor.moveToFirst())
            return false;

        Object contentObject = mSnsDb.decodeBlob(mSnsContentClass,
                cursor.getBlob(cursor.getColumnIndex("content")));
        cursor.close();
        if (contentObject == null)
            return false;

        String content = mSnsDb.getSnsContent(contentObject);
        return !TextUtils.isEmpty(content) && (content.contains(TAG_DELETED) || content.contains("[Deleted]"));
    }

    protected void updateSnsCommentChange(XC_MethodHook.MethodHookParam param) {
        ContentValues v = (ContentValues) param.args[1];
        if (!v.containsKey("attrBuf"))
            return;

        Object attrObject = mSnsDb.decodeBlob(mSnsAttrClass, v.getAsByteArray("attrBuf"));
        SparseArrayCompat<Object> comments = mSnsDb.getSnsCommentContent(attrObject);

        if (comments == null || comments.size() == 0)
            return;

        Object oldAttrObject = mSnsDb.getSnsCommentBlob(v.getAsString("snsId"), mSnsAttrClass);
        SparseArrayCompat<Object> oldComments = mSnsDb.getSnsCommentContent(oldAttrObject);

        if (oldComments == null || oldComments.size() == 0)
            return;

        // tag deleted comments
        for (int i = 0; i < oldComments.size(); i++) {
            int key = oldComments.keyAt(i);
            if (comments.indexOfKey(key) < 0) { // not found
                Object item = oldComments.get(key);
                String content = WechatSnsDBHelper.removeDeletedTag(mSnsDb.getCommentContent(item)) + " "
                        + mSettings.getString("deleted", TAG_DELETED);
                setObjectField(item, w.commentContentField, content);
            }
        }

        // add new comments
        for (int i = 0; i < comments.size(); i++) {
            int key = comments.keyAt(i);
            if (oldComments.indexOfKey(key) < 0) { // not found
                oldComments.put(key, comments.get(key));
            }
        }

        // replace original
        LinkedList<Object> linkedList = new LinkedList<>();
        for (int i = 0; i < oldComments.size(); i++) {
            linkedList.add(oldComments.get(oldComments.keyAt(i)));
        }
        setObjectField(attrObject, w.commentsListField, linkedList);
        v.put("attrBuf", WechatSnsDBHelper.encodeContentBlob(attrObject));
    }

    protected void log(String msg) {
        if (mDebug) {
            XposedBridge.log(msg);
        }
    }

    protected void log(Throwable t) {
        if (mDebug) {
            XposedBridge.log(t);
        }
    }

    public String getEmojiText(String nickname, String imgPath) {
        String emoji = mSettings.getString("emoji", "[动画表情]");

        if (TextUtils.isEmpty(imgPath))
            return emoji;

        String emojiTxt = null;
        try {
            emojiTxt = (String) callStaticMethod(mConversationClass, w.emojiMethod1, imgPath);
        } catch (Throwable t) {
            log(t);
        }
        if (TextUtils.isEmpty(emojiTxt))
            return emoji;

        emojiTxt = "[" + emojiTxt + "]";
        return TextUtils.isEmpty(nickname) ? emojiTxt : nickname + ": " + emojiTxt;
    }

    public void saveBitmapFile(String fileName, Bitmap bitmap, ImageUtil.OnImageProcessListener listener) {
        File cacheFile = new File(mNotificationContext.getCacheDir(), fileName + ".png");
        if (!cacheFile.exists()) {
            try {
                cacheFile.createNewFile();
            } catch (Exception e) {
                log(e);
            }
        }

        if (!cacheFile.exists())
            return;
        ImageUtil.saveBitmapToFile(cacheFile, bitmap, listener);
    }
}
