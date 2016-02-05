package com.fkzhang.wechatunrecalled;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.util.SparseArrayCompat;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.callStaticMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookConstructor;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static de.robv.android.xposed.XposedHelpers.newInstance;
import static de.robv.android.xposed.XposedHelpers.setObjectField;

/**
 * Created by fkzhang on 1/16/2016.
 */
public class WechatUnrecalledHook {
    private final SettingsHelper mSettings;
    protected Object mSQLDB;
    protected WechatPackageNames mP;
    protected Object mObject;
    protected Context mNotificationContext;
    protected Class<?> mNotificationClass;
    protected int mNotificationIcon = -1;
    protected Bitmap mNotificationLargeIcon;
    protected Class<?> mImgClss;
    protected Class<?> mAvatarLoader;
    protected HashMap<String, Bitmap> mAvatarCache;
    protected HashMap<String, String> mNicknameCache;
    protected Class<?> mCommentClass;
    protected Object mSnsSQL;
    protected Class<?> mDbClass1;
    protected Class<?> mSnsContentClass;
    protected Class<?> mSnsAttrClass;
    private boolean mDebug = false;

    public WechatUnrecalledHook(WechatPackageNames packageNames) {
        this.mP = packageNames;
        mSettings = new SettingsHelper("com.fkzhang.wechatunrecalled");
        mAvatarCache = new HashMap<>();
        mNicknameCache = new HashMap<>();
    }

    public void hook(final ClassLoader loader) {
        try {
            hookRecall(loader);
        } catch (Throwable e) {
            XposedBridge.log(e);
        }
        try {
            hookDatabase(loader);
        } catch (Throwable e) {
            XposedBridge.log(e);
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
    }

    protected void hookRecall(final ClassLoader loader) {
        XposedHelpers.findAndHookMethod(mP.recallClass, loader,
                mP.recallMethod, String.class, String.class, String.class,
                new XC_MethodHook() {
                    @SuppressWarnings("unchecked")
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        init(loader);
                        preventMsgRecall(param);
                    }
                });
    }

    protected void hookSns(final ClassLoader loader) {
        findAndHookConstructor(mP.snsClass, loader,
                mP.snsMethod, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        init(loader);
                        if (mSnsSQL == null) {
                            mSnsSQL = param.args[0];
                        }
                        mSettings.reload();
                        unRecallSnsComments();
                        unRecallSnsMoments();
                    }
                });
        try {
            if (!TextUtils.isEmpty(mP.luckyRevealImageView)) {
                findAndHookMethod(mP.luckyRevealImageView, loader, "getBlurBitmapFilePath",
                        new XC_MethodReplacement() {
                            @Override
                            protected Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                                return callMethod(methodHookParam.thisObject, "getOriginBitmapFilePath");
                            }
                        });
            }
        } catch (Throwable t) {
            XposedBridge.log(t);
        }

        try {
            if (!TextUtils.isEmpty(mP.snsLuckyMoneyClass1)) {
                findAndHookMethod(mP.snsLuckyMoneyClass1, loader, mP.snsLuckyMoneyBlur,
                        mP.snsLuckyMoneyClass2,
                        new XC_MethodReplacement() {
                            @Override
                            protected Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                                return callMethod(methodHookParam.thisObject,
                                        mP.snsLuckyMoneyOrignal, methodHookParam.args[0]);
                            }
                        });
            }
        } catch (Throwable t) {
            XposedBridge.log(t);
        }
    }


    protected void updateMessageCount() {
        callMethod(mObject, mP.updateMsgId);
    }

    protected void hookDatabase(ClassLoader loader) {
        findAndHookMethod(mP.packageNameBase + ".kingkong.database.SQLiteDatabase", loader,
                "updateWithOnConflict", String.class, ContentValues.class, String.class,
                String[].class, int.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        preventCommentRecall(param);
                        preventMomentRecall(param);
                    }
                });
        findAndHookMethod(mP.packageNameBase + ".kingkong.database.SQLiteDatabase", loader,
                "insertWithOnConflict", String.class, String.class, ContentValues.class, int.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        mSettings.reload();
                        if (!mSettings.getBoolean("enable_new_comment_notification", false))
                            return;

                        String s = ((String) param.args[0]).toLowerCase();

                        if (!s.equalsIgnoreCase("snscomment"))
                            return;

                        ContentValues v = (ContentValues) param.args[2];
                        String talker = (String) v.get("talker");
                        Bitmap icon = getAccountAvatar(talker);
                        String name = getNickname(talker) + " "
                                + mSettings.getString("new_comment", "(新评论)");
                        String content = getCommentContent(decodeBlob(mCommentClass,
                                v.getAsByteArray("curActionBuf")));

                        Intent intent = new Intent();
                        intent.setClassName(mNotificationContext.getPackageName(),
                                mP.packageName + ".plugin.sns.ui.SnsCommentDetailUI");
                        intent.putExtra("INTENT_SNSID", "sns_table_" + v.getAsString("snsID"));
                        intent.putExtra("INTENT_FROMSUI", true);
                        intent.putExtra("INTENT_FROMSUI_COMMENTID", v.getAsLong("commentSvrID"));

                        showCommentNotification(name, content, icon, intent);
                    }
                });

        findAndHookMethod(mP.packageNameBase + ".kingkong.database.SQLiteDatabase", loader,
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


        map.put(key, null);
        param.setResult(map);

        String talker = map.get(".sysmsg.revokemsg.session");
        String replacemsg = map.get(".sysmsg.revokemsg.replacemsg");
        String msgsvrid = map.get(".sysmsg.revokemsg.newmsgid");
        replacemsg = replacemsg.replaceAll("撤回了", "尝试撤回")
                .replaceAll("recalled", "tried to recall");
        mSettings.reload();
        replacemsg += " " + mSettings.getString("recalled", "(Prevented)");

        Cursor cursor = getMessage(msgsvrid);
        if (cursor == null || !cursor.moveToFirst())
            return;

        if (mSettings.getBoolean("enable_recall_notification", true)) {
            Bitmap icon = getAccountAvatar(talker);
            int t = cursor.getInt(cursor.getColumnIndex("type"));
            switch (t) {
                case 1: // text
                    String content = cursor.getString(cursor.getColumnIndex("content")).trim();
                    if (talker.contains("@chatroom")) {
                        int idx = content.indexOf(":");
                        if (idx != -1) {
                            content = content.substring(idx + 1, content.length()).trim();
                        }
                    }
                    showTextNotification(talker, replacemsg, content, icon);
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
                        String summary = mSettings.getString("recalled_img_summary",
                                "[图片] 点击查看");
                        showImageNotification(talker, replacemsg, bitmap, intent, summary, icon);
                    }
                    break;
                case 62: // video
                    Intent intent = new Intent();
                    intent.putExtra("img_gallery_talker", talker);
                    intent.putExtra("img_gallery_msg_svr_id", Long.parseLong(msgsvrid));
                    String summary = mSettings.getString("recalled_video_summary",
                            "[小视频] 点击查看");
                    showImageNotification(talker, replacemsg, null, intent, summary, icon);
                    break;
            }
        }

        long createTime = cursor.getLong(cursor.getColumnIndex("createTime"));
        int idx = cursor.getColumnIndexOrThrow("talkerId");
        int talkerId = -1;
        if (idx != -1) {
            talkerId = cursor.getInt(cursor.getColumnIndex("talkerId"));
        }
        cursor.close();
        insertMessage(talker, talkerId, replacemsg, createTime + 1);
    }

    protected void unRecallSnsComments() {
        if (!mSettings.getBoolean("prevent_comments_recall", true))
            return;
        String query = "SELECT commentSvrID,curActionBuf FROM SnsComment WHERE commentflag=1";
        Cursor cursor = rawQuerySns(query);

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
        Cursor cursor = rawQuerySns(query);
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

            if (!mSettings.getBoolean("enable_comment_recall_notification", true))
                return;

            String talker = v.getAsString("talker");
            Bitmap icon = getAccountAvatar(talker);
            String name = getNickname(talker) + " " +
                    mSettings.getString("comment_recall_content", "");

            String content = getCommentContent(decodeBlob(mCommentClass,
                    v.getAsByteArray("curActionBuf")));

            Intent resultIntent = new Intent();
            resultIntent.setClassName(mNotificationContext.getPackageName(),
                    mP.packageName + ".plugin.sns.ui.SnsMsgUI");

            showCommentNotification(name, content, icon, resultIntent);
        }
    }

    public void preventMomentRecall(XC_MethodHook.MethodHookParam param) {
        String table = (String) param.args[0];
        if (!table.equalsIgnoreCase("snsinfo"))
            return;
        mSettings.reload();

        ContentValues v = (ContentValues) param.args[1];
        if (v.containsKey("sourceType")) {
            int sourceType = v.getAsInteger("sourceType");
            if (mSettings.getBoolean("prevent_moments_recall", true) &&
                    (sourceType == 0 || sourceType == 2 || sourceType == 8)) {
                param.setResult(null); // prevent call
                setSnsDeleteFlag(v);
                return;
            }
        }

        if (mSettings.getBoolean("prevent_comments_recall", true)) {
            updateSnsCommentChange(param);
        }
    }

    protected void insertMessage(String talker, int talkerId, String msg, long createTime) {
        int type = 10000;
        int status = 3;
        long msgSvrId = createTime + (new Random().nextInt());
        long msgId = getNextMsgId();
        ContentValues v = new ContentValues();
        v.put("msgid", msgId);
        v.put("msgSvrid", msgSvrId);
        v.put("type", type);
        v.put("status", status);
        v.put("createTime", createTime);
        v.put("talker", talker);
        v.put("content", msg);
        if (talkerId != -1) {
            v.put("talkerid", talkerId);
        }
        insertSQL("message", "", v);
        updateMessageCount();
    }

    protected long getNextMsgId() {
        Cursor cursor = rawQuery("SELECT max(msgId) FROM message");
        if (cursor == null || !cursor.moveToFirst())
            return -1;
        int id = cursor.getInt(0) + 1;
        cursor.close();
        return id;
    }

    protected void insertSQL(String table, String selection, ContentValues contentValues) {
        if (mSQLDB == null)
            return;
        callMethod(mSQLDB, "insert", table, selection, contentValues);
    }

    protected Cursor rawQuery(String query, String[] where) {
        if (mSQLDB == null)
            return null;
        return (Cursor) XposedHelpers.callMethod(mSQLDB, "rawQuery", query, where);
    }

    protected Cursor rawQuery(String query) {
        return rawQuery(query, null);
    }

    protected Cursor getMessage(String msgsvrid) {
        String query = "SELECT * FROM message WHERE msgsvrid = ?";
        return rawQuery(query, new String[]{msgsvrid});
    }

    protected Cursor getContact(String username) {
        String query = "SELECT * FROM rcontact WHERE username = ?";
        return rawQuery(query, new String[]{username});
    }

    protected Object getSnsCommentBlob(String snsId) {
        String query = "SELECT attrBuf FROM SnsInfo WHERE snsId = " + snsId;
        Cursor cursor = rawQuerySns(query);

        if (cursor == null || !cursor.moveToFirst())
            return null;

        byte[] blob = cursor.getBlob(cursor.getColumnIndex("attrBuf"));
        return decodeBlob(mSnsAttrClass, blob);
    }

    protected Cursor rawQuerySns(String query) {
        if (mSnsSQL == null)
            return null;
        return (Cursor) callMethod(mSnsSQL, "rawQuery", query, null);
    }

    protected void updateSns(String table, ContentValues contentValues, String selection, String[] whereClause) {
        if (mSnsSQL == null)
            return;
        callMethod(mSnsSQL, "update", table, contentValues, selection, whereClause);
    }

    protected void showTextNotification(String username, String title, String content, Bitmap icon) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mNotificationContext)
                .setLargeIcon(icon)
                .setSmallIcon(mNotificationIcon)
                .setContentTitle(title)
                .setAutoCancel(true);

        if (!TextUtils.isEmpty(content)) {
            builder.setContentText(content);
        }

        Intent intent = new Intent();
        intent.setClassName(mNotificationContext.getPackageName(), mP.packageName + ".ui.chatting.ChattingUI");

        intent.putExtra("Chat_User", username);
        intent.putExtra("Chat_Mode", 1);

        showNotification(builder, intent);
    }

    protected void showCommentNotification(String title, String content, Bitmap icon, Intent resultIntent) {
        if (TextUtils.isEmpty(content))
            return;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(mNotificationContext)
                .setLargeIcon(icon)
                .setSmallIcon(mNotificationIcon)
                .setContentTitle(title)
                .setAutoCancel(true)
                .setContentText(content);

        showNotification(builder, resultIntent);
    }

    protected void showImageNotification(String username, String title, Bitmap bitmap, Intent resultIntent,
                                         String summary, Bitmap icon) {
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(mNotificationContext)
                        .setLargeIcon(icon)
                        .setSmallIcon(mNotificationIcon)
                        .setContentTitle(title)
                        .setAutoCancel(true);

        if (bitmap != null) {
            builder.setStyle(new NotificationCompat.BigPictureStyle()
                    .bigPicture(bitmap).setSummaryText(summary));
        } else {
            builder.setContentText(summary);
        }

        Intent intent = new Intent();
        intent.setClassName(mNotificationContext.getPackageName(), mP.packageName + ".ui.chatting.ChattingUI");
        intent.putExtra("Chat_User", username);
        intent.putExtra("Chat_Mode", 1);
        PendingIntent pendingIntent = PendingIntent.getActivity(mNotificationContext, 0, intent, 0);
        builder.addAction(android.R.drawable.sym_action_chat, mSettings.getString("reply", "回复"), pendingIntent);

        resultIntent.setClassName(mNotificationContext.getPackageName(),
                mP.packageName + ".ui.chatting.gallery.ImageGalleryUI");

        showNotification(builder, resultIntent);
    }

    protected void showNotification(NotificationCompat.Builder builder, Intent intent) {
        TaskStackBuilder stackBuilder;
        stackBuilder = TaskStackBuilder.create(mNotificationContext);
        stackBuilder.addParentStack(mNotificationClass);

        stackBuilder.addNextIntent(intent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(resultPendingIntent);

        Notification notification = builder.build();
        notification.flags = Notification.FLAG_ONLY_ALERT_ONCE | Notification.FLAG_SHOW_LIGHTS
                | Notification.FLAG_AUTO_CANCEL;

        notification.ledOnMS = 300;
        notification.ledOffMS = 1000;
        notification.ledARGB = Color.GREEN;

        NotificationManager mNotificationManager =
                (NotificationManager) mNotificationContext
                        .getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(getNotificationId(), notification);
    }

    protected Bitmap getImage(String path) {
        String str = (String) callMethod(callStaticMethod(mImgClss, mP.imageMethod1),
                mP.imageMethod2, path);
        if (TextUtils.isEmpty(str))
            return null;

        return BitmapFactory.decodeFile(str);
    }

    protected Bitmap getAccountAvatar(String accountName) {
        if (mAvatarCache.containsKey(accountName)) {
            return mAvatarCache.get(accountName);
        }
        if (mAvatarLoader == null)
            return null;

        Bitmap avatar = null;
        try {
            avatar = (Bitmap) callMethod(callStaticMethod(mAvatarLoader, mP.avatarMethod1),
                    mP.avatarMethod2, accountName, false, -1);
            if (avatar != null) {
                mAvatarCache.put(accountName, avatar);
            }
        } catch (Throwable t) {
            XposedBridge.log(t);
        }
        if (avatar == null) {
            avatar = mNotificationLargeIcon;
        }
        return avatar;
    }

    protected int getNotificationId() {
        return (int) (System.currentTimeMillis() & 0xfffffff);
    }

    protected void init(ClassLoader loader) {
        if (mDbClass1 == null) {
            try {
                mDbClass1 = findClass(mP.dbClass1, loader);
            } catch (Throwable t) {
                log(t);
            }
        }
        if (mObject == null && mDbClass1 != null) {
            try {
                mObject = callMethod(callStaticMethod(mDbClass1, mP.dbMethod1), mP.dbMethod2);
            } catch (Throwable t) {
                log(t);
            }
        }
        if (mSQLDB == null && mObject != null) {
            try {
                mSQLDB = getObjectField(mObject, mP.dbField);
            } catch (Throwable t) {
                log(t);
            }
        }

        // look for: field_imgPath in pluginsdk/model/app
        if (mImgClss == null) {
            try {
                mImgClss = findClass(mP.imageClass, loader);
            } catch (Throwable t) {
                log(t);
            }
        }

        // look for: notification_icon
        if (mNotificationContext == null) {
            try {
                mNotificationContext = (Context) callStaticMethod(findClass(mP.contextGetter, loader),
                        "getContext");
            } catch (Throwable t) {
                log(t);
            }
        }
        if (mNotificationClass == null) {
            try {
                mNotificationClass = findClass(mP.packageName + ".ui.LauncherUI", loader);
            } catch (Throwable t) {
                log(t);
            }
        }
        if (mNotificationIcon == -1) {
            try {
                mNotificationIcon = (int) callStaticMethod(
                        findClass(mP.iconClass, loader), mP.iconMethod);
            } catch (Throwable t) {
                log(t);
            }
        }
        if (mNotificationLargeIcon == null && mNotificationContext != null) {
            try {
                mNotificationLargeIcon = BitmapFactory.decodeResource(mNotificationContext
                        .getResources(), mNotificationContext.getApplicationInfo().icon);
            } catch (Throwable t) {
                log(t);
            }
        }

        // look for: field_curActionBuf
        if (mCommentClass == null) {
            try {
                mCommentClass = findClass(mP.commentClass, loader);
            } catch (Throwable t) {
                log(t);
            }
        }
        if (mAvatarLoader == null) {
            try {
                mAvatarLoader = findClass(mP.avatarClass, loader);
            } catch (Throwable t) {
                log(t);
            }
        }
        if (mSnsContentClass == null) {
            // look for in sns: field_content
            try {
                mSnsContentClass = findClass(mP.snsContentClass, loader);
            } catch (Throwable t) {
                log(t);
            }
        }
        if (mSnsAttrClass == null) {
            // look for field_attrBuf
            try {
                mSnsAttrClass = findClass(mP.snsAttrClass, loader);
            } catch (Throwable t) {
                log(t);
            }
        }

    }

    protected String getNickname(String username) {
        if (mNicknameCache.containsKey(username)) {
            return mNicknameCache.get(username);
        }

        Cursor cursor = getContact(username);
        if (cursor == null || !cursor.moveToFirst())
            return username;

        String name = cursor.getString(cursor.getColumnIndex("conRemark"));
        if (TextUtils.isEmpty(name)) {
            name = cursor.getString(cursor.getColumnIndex("nickname"));
        }
        cursor.close();
        mNicknameCache.put(username, name);
        return name;
    }


    protected String getCommentContent(Object o) {
        if (o == null)
            return null;
        return (String) getObjectField(o, mP.commentContentField);
    }

    protected Object decodeBlob(Class<?> cls, byte[] blob) {
        if (cls == null || blob == null)
            return null;

        try {
            return callMethod(newInstance(cls), mP.blobDecodeMethod,
                    new Class[]{byte[].class}, blob);
        } catch (Throwable t) {
            XposedBridge.log(t);
        }
        return null;
    }

    protected String getSnsContent(Object o) {
        if (o == null)
            return null;
        return (String) getObjectField(o, mP.snsContentField);
    }

    protected SparseArrayCompat<Object> getSnsCommentContent(Object o) {
        if (o == null)
            return null;
        LinkedList linkedList = (LinkedList) getObjectField(o, mP.snsAttrField);
        SparseArrayCompat<Object> comments = new SparseArrayCompat<>(linkedList.size());
        for (Object item : linkedList) {
            int time = (int) getObjectField(item, mP.commentTimeField);
            comments.put(time, item);
        }
        return comments;
    }

    protected byte[] encodeContentBlob(Object o) {
        if (o == null)
            return null;

        try {
            return (byte[]) callMethod(o, "toByteArray");
        } catch (Throwable t) {
            XposedBridge.log(t);
        }
        return null;
    }

    protected void unsetSnsDeleteFlag(ContentValues v) {
        Object contentObject = decodeBlob(mSnsContentClass, v.getAsByteArray("content"));
        if (contentObject == null)
            return;

        String content = removeDeletedTag(getSnsContent(contentObject));
        setObjectField(contentObject, mP.snsContentField, content);
        v.put("content", encodeContentBlob(contentObject));
        updateSns("SnsInfo", v, "snsId = ?", new String[]{v.getAsString("snsId")});
    }

    protected void setSnsDeleteFlag(ContentValues v) {
        Object contentObject = decodeBlob(mSnsContentClass, v.getAsByteArray("content"));
        if (contentObject == null)
            return;

        String content = removeDeletedTag(getSnsContent(contentObject));

        content = mSettings.getString("deleted", "[已删除]") + "\n" + content;
        setObjectField(contentObject, mP.snsContentField, content);

        ContentValues c = new ContentValues();
        c.put("content", encodeContentBlob(contentObject));
        c.put("sourceType", 10);

        updateSns("SnsInfo", c, "snsId = ?", new String[]{v.getAsString("snsId")});
    }

    protected void setCommentDeleteFlag(ContentValues v) {
        Object commentObject = decodeBlob(mCommentClass, v.getAsByteArray("curActionBuf"));
        String content = removeDeletedTag(getCommentContent(commentObject));
        content += " " + mSettings.getString("deleted", "[已删除]");
        setObjectField(commentObject, mP.commentContentField, content);
        ContentValues c = new ContentValues();
        c.put("curActionBuf", encodeContentBlob(commentObject));
        c.put("commentflag", 0);
        updateSns("SnsComment", c, "commentSvrID = ?", new String[]{v.getAsString("commentSvrID")});
    }

    private boolean isDeletedMarked(String snsId) {
        String query = "SELECT content FROM SnsInfo WHERE snsId=" + snsId;
        Cursor cursor = rawQuerySns(query);
        if (cursor == null || !cursor.moveToFirst())
            return false;

        Object contentObject = decodeBlob(mSnsContentClass,
                cursor.getBlob(cursor.getColumnIndex("content")));
        cursor.close();
        if (contentObject == null)
            return false;

        String content = getSnsContent(contentObject);
        return !TextUtils.isEmpty(content) && (content.contains("[已删除]") || content.contains("[Deleted]"));
    }

    protected static String removeDeletedTag(String content) {
        if (content.contains("[已删除]") || content.contains("[Deleted]")) {
            content = content.replaceAll("\\[已删除\\]", "").replaceAll("\\[Deleted\\]", "").trim();
        }
        return content;
    }

    protected void updateSnsCommentChange(XC_MethodHook.MethodHookParam param) {
        ContentValues v = (ContentValues) param.args[1];
        if (!v.containsKey("attrBuf"))
            return;

        Object attrObject = decodeBlob(mSnsAttrClass, v.getAsByteArray("attrBuf"));
        SparseArrayCompat<Object> comments = getSnsCommentContent(attrObject);

        if (comments == null || comments.size() == 0)
            return;

        Object oldAttrObject = getSnsCommentBlob(v.getAsString("snsId"));
        SparseArrayCompat<Object> oldComments = getSnsCommentContent(oldAttrObject);

        if (oldComments == null || oldComments.size() == 0)
            return;

        // tag deleted comments
        for (int i = 0; i < oldComments.size(); i++) {
            int key = oldComments.keyAt(i);
            if (comments.indexOfKey(key) < 0) { // not found
                Object item = oldComments.get(key);
                String content = removeDeletedTag(getCommentContent(item)) + " "
                        + mSettings.getString("deleted", "[已删除]");
                setObjectField(item, mP.commentContentField, content);
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
        setObjectField(attrObject, mP.snsAttrField, linkedList);
        v.put("attrBuf", encodeContentBlob(attrObject));
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
}
