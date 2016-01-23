package com.fkzhang.wechatunrecalled;

import android.content.ContentValues;
import android.database.Cursor;

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
    protected Object mSQLDB;
    protected WechatPackageNames mP;
    protected boolean mInit;
    protected Object mObject;

    public WechatUnrecalledHook(WechatPackageNames packageNames) {
        this.mP = packageNames;
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
            hookSns(loader);
        } catch (Exception e) {
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
                        initDatabase(loader);
                        preventMsgRecall(param);
                    }
                });
    }

    protected void hookSns(ClassLoader loader) {
        findAndHookConstructor(mP.snsClass, loader,
                mP.snsMethod, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
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
//        if (mSQLDB == null)
        mSQLDB = getObjectField(mObject, mP.dbField);
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
        if (replacemsg.contains("撤回")) {
            replacemsg = replacemsg.replaceAll("撤回了", "尝试撤回") + " (已阻止)";
        } else {
            replacemsg = replacemsg.replaceAll("recalled", "tried to recall") + " (Prevented)";
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

    protected Cursor rawQuery(String query) {
        if (mSQLDB == null)
            return null;
        return (Cursor) XposedHelpers.callMethod(mSQLDB, "rawQuery", query, null);
    }


}
