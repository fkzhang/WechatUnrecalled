package com.fkzhang.wechatunrecalled;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v4.app.TaskStackBuilder;
import android.text.TextUtils;

import java.util.Map;
import java.util.Random;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.callStaticMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookConstructor;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getObjectField;

/**
 * Created by fkzhang on 1/16/2016.
 */
public class WechatUnrecalledHook {
    private final SettingsHelper mSettings;
    protected Object mSQLDB;
    protected WechatPackageNames mP;
    protected boolean mInit;
    protected Object mObject;
    protected boolean mNotificationInit;
    protected Context mNotificationContext;
    protected Class<?> mNotificationClass;
    protected int mNotificationIcon;
    protected Bitmap mNotificationLargeIcon;
    protected Class<?> mImgClss;
    protected boolean mAvatarInit;
    protected Class<?> mAvatarLoader;
    private boolean isChinese = true;

    public WechatUnrecalledHook(WechatPackageNames packageNames) {
        this.mP = packageNames;
        mSettings = new SettingsHelper("com.fkzhang.wechatunrecalled");
    }

    public void hook(ClassLoader loader) {
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
                        unRecallSnsComments(param.args[0]);
                    }
                });
    }

    protected void initDatabase(ClassLoader loader) {
        if (mInit)
            return;

        mInit = true;
        Class<?> c = findClass(mP.dbClass1, loader);
        mObject = callMethod(callStaticMethod(c, mP.dbMethod1), mP.dbMethod2);
        mSQLDB = getObjectField(mObject, mP.dbField);

        // look for: field_imgPath in pluginsdk/model/app
        // .startsWith("THUMBNAIL_DIRPATH://")
        mImgClss = findClass(mP.imageClass, loader);
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
                    }
                });
    }

    protected void preventMsgRecall(XC_MethodHook.MethodHookParam param) {
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
        if (replacemsg.contains("撤回")) {
            replacemsg = replacemsg.replaceAll("撤回了", "尝试撤回") + " (已阻止)";
        } else {
            replacemsg = replacemsg.replaceAll("recalled", "tried to recall") + " (Prevented)";
            isChinese = false;
        }

        if (!mSettings.getBoolean("disable_notification", false)) {
            Cursor cursor = getMessage(msgsvrid);
            if (cursor == null || !cursor.moveToFirst())
                return;

            String content = cursor.getString(cursor.getColumnIndex("content"));
            Bitmap icon = getAccountAvatar(talker);
            int t = cursor.getInt(cursor.getColumnIndex("type"));
            switch (t) {
                case 1: // text
                    showTextNotification(replacemsg, content, icon);
                    if (mSettings.getBoolean("show_content", false)) {
                        if (isChinese) {
                            replacemsg = content + " (已阻止撤回)";
                        } else {
                            replacemsg = content + " (Recall prevented)";
                        }
                    }
                    break;
                case 3: // image
                    String imgPath = cursor.getString(cursor.getColumnIndex("imgPath"));
                    if (!TextUtils.isEmpty(imgPath)) {
                        Bitmap bitmap = getImage(imgPath);
                        Intent intent = new Intent();
                        intent.putExtra("img_gallery_talker", talker);
                        intent.putExtra("img_gallery_msg_svr_id", Long.parseLong(msgsvrid));
                        String summary;
                        if (isChinese) {
                            summary = "点击查看";
                        } else {
                            summary = "View in full screen mode";
                        }
                        showImageNotification(replacemsg, bitmap, intent, summary, icon);
                    }
                    break;
                case 62: // video
                    Intent intent = new Intent();
                    intent.putExtra("img_gallery_talker", talker);
                    intent.putExtra("img_gallery_msg_svr_id", Long.parseLong(msgsvrid));
                    String summary;
                    if (isChinese) {
                        summary = "[小视频] 点击查看";
                    } else {
                        summary = "[Video] View in full screen mode";
                    }
                    showVideoNotification(replacemsg, summary, intent, icon);
                    break;
            }
            cursor.close();
        }

        insertMessage(talker, replacemsg);
    }

    protected void unRecallSnsComments(Object SQL) {
        String query = "SELECT rowid FROM SnsComment WHERE commentflag=1";
        Cursor cursor = (Cursor) callMethod(SQL, "rawQuery", query, null);

        if (cursor == null || !cursor.moveToFirst())
            return;

        ContentValues v = new ContentValues();
        v.put("commentflag", 0);

        do {
            String rowid = cursor.getString(cursor.getColumnIndex("rowid"));
            callMethod(SQL, "update", "SnsComment", v, "rowid = ?", new String[]{rowid});
        } while (cursor.moveToNext());
        cursor.close();
    }

    public void preventCommentRecall(XC_MethodHook.MethodHookParam param) {
        String table = (String) param.args[0];
        if (!table.equalsIgnoreCase("snscomment"))
            return;

        ContentValues v = (ContentValues) param.args[1];
        if (v.containsKey("commentflag") && v.getAsInteger("commentflag") == 1) {
            param.setResult(null); // prevent call
            if (mSettings.getBoolean("disable_notification", false))
                return;

            String talker = v.getAsString("talker");
            Bitmap icon = getAccountAvatar(talker);
            Cursor cursor = getContact(talker);
            String name = talker;
            if (cursor != null && cursor.moveToFirst()) {
                name = cursor.getString(cursor.getColumnIndex("conRemark"));
                if (TextUtils.isEmpty(name)) {
                    name = cursor.getString(cursor.getColumnIndex("nickname"));
                }
            }
            String content;
            if (isChinese) {
                content = "尝试删除一条评论";
            } else {
                content = "tried to delete a comment";
            }
            showTextNotification(name, content, icon);

        }
    }

    protected void insertMessage(String talker, String msg) {
        int contactId = getTalkerId(talker);
        if (contactId == -1)
            return;

        int type = 10000;
        int status = 3;
        long createTime = System.currentTimeMillis();
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
        v.put("talkerid", contactId);
        insertSQL("message", "", v);
        updateMessageCount();
    }

    protected long getNextMsgId() {
        Cursor cursor = rawQuery("SELECT max(msgId) FROM message");
        if (cursor == null || !cursor.moveToFirst())
            return -1;
        return cursor.getInt(0) + 1;
    }

    protected int getTalkerId(String talker) {
        String query = "SELECT rowid FROM rcontact WHERE username = '" + talker + "'";
        Cursor cursor = rawQuery(query);

        int contactId = -1;
        if (cursor != null && cursor.moveToFirst()) {
            contactId = cursor.getInt(cursor.getColumnIndex("rowid"));
            cursor.close();
        }
        return contactId;
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

    protected void showTextNotification(String title, String content, Bitmap icon) {
        Notification.Builder builder = new Notification.Builder(mNotificationContext)
                .setLargeIcon(icon)
                .setSmallIcon(mNotificationIcon)
                .setContentTitle(title)
                .setAutoCancel(true);

        if (content != null) {
            builder.setContentText(content);
        }

        Intent resultIntent = new Intent();
        resultIntent.setClassName(mNotificationContext.getPackageName(), mNotificationClass.getName());

        showNotification(builder, resultIntent);
    }

    protected void showVideoNotification(String title, String content, Intent resultIntent, Bitmap icon) {
        Notification.Builder builder = new Notification.Builder(mNotificationContext)
                .setLargeIcon(icon)
                .setSmallIcon(mNotificationIcon)
                .setContentTitle(title)
                .setContentText(content)
                .setAutoCancel(true);

        resultIntent.setClassName(mNotificationContext.getPackageName(),
                mP.packageName + ".ui.chatting.gallery.ImageGalleryUI");

        showNotification(builder, resultIntent);
    }

    protected void showImageNotification(String title, Bitmap bitmap, Intent resultIntent,
                                         String summary, Bitmap icon) {
        Notification.Builder builder = new Notification.Builder(mNotificationContext)
                .setLargeIcon(icon)
                .setSmallIcon(mNotificationIcon)
                .setContentTitle(title)
                .setAutoCancel(true);

        if (bitmap != null) {
            builder.setStyle(new Notification.BigPictureStyle()
                    .bigPicture(bitmap).setSummaryText(summary));
        }

        resultIntent.setClassName(mNotificationContext.getPackageName(),
                mP.packageName + ".ui.chatting.gallery.ImageGalleryUI");

        showNotification(builder, resultIntent);
    }

    protected void showNotification(Notification.Builder builder, Intent intent) {
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

    public void initNotification(ClassLoader loader) {
        if (mNotificationInit)
            return;

        // look for: notification_icon
        mNotificationInit = true;
        mNotificationContext = (Context) callStaticMethod(findClass(mP.contextGetter, loader),
                "getContext");
        mNotificationClass = findClass(mP.packageName + ".ui.LauncherUI", loader);
        mNotificationIcon = (int) callStaticMethod(
                findClass(mP.iconClass, loader), mP.iconMethod);
        mNotificationLargeIcon = BitmapFactory.decodeResource(mNotificationContext
                .getResources(), mNotificationContext.getApplicationInfo().icon);
    }

    protected Bitmap getImage(String path) {
        String str = (String) callMethod(callStaticMethod(mImgClss, mP.imageMethod1),
                mP.imageMethod2, path);
        if (TextUtils.isEmpty(str))
            return null;

        return BitmapFactory.decodeFile(str);
    }

    protected void initAvatarLoader(ClassLoader loader) {
        if (mAvatarInit)
            return;
        mAvatarInit = true;
        mAvatarLoader = findClass(mP.avatarClass, loader);
    }

    protected Bitmap getAccountAvatar(String accountName) {
        if (mAvatarLoader == null)
            return null;

        Bitmap avatar = null;
        try {
            avatar = (Bitmap) callMethod(callStaticMethod(mAvatarLoader, mP.avatarMethod1),
                    mP.avatarMethod2, accountName, false, -1);
        } catch (Throwable t) {
//            XposedBridge.log(t);
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
        initDatabase(loader);
        initNotification(loader);
        initAvatarLoader(loader);
    }
}
