package com.fkzhang.wechatunrecalled.Util;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.v4.util.SparseArrayCompat;

import com.fkzhang.wechatunrecalled.WechatPackageNames;

import java.util.LinkedList;

import de.robv.android.xposed.XposedBridge;

import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static de.robv.android.xposed.XposedHelpers.newInstance;

/**
 * Created by fkzhang on 2/6/2016.
 */
public class WechatSnsDBHelper {
    private Object mSnsSQL;
    private WechatPackageNames w;

    public WechatSnsDBHelper(WechatPackageNames packageNames, Object dbObject) {
        mSnsSQL = dbObject;
        w = packageNames;
    }

    public static byte[] encodeContentBlob(Object o) {
        if (o == null)
            return null;

        try {
            return (byte[]) callMethod(o, "toByteArray");
        } catch (Throwable t) {
            XposedBridge.log(t);
        }
        return null;
    }

    public static String removeDeletedTag(String content) {
        if (content.contains("[已删除]") || content.contains("[Deleted]")) {
            content = content.replaceAll("\\[已删除\\]", "").replaceAll("\\[Deleted\\]", "").trim();
        }
        return content;
    }

    public Cursor rawQuery(String query) {
        return rawQuery(query, null);
    }

    public Cursor rawQuery(String query, String[] whereClause) {
        return (Cursor) callMethod(mSnsSQL, "rawQuery", query, whereClause);
    }

    public void update(String table, ContentValues contentValues, String selection, String[] whereClause) {
        callMethod(mSnsSQL, "update", table, contentValues, selection, whereClause);
    }

    public Object getSnsCommentBlob(String snsId, Class<?> cls) {
        String query = "SELECT attrBuf FROM SnsInfo WHERE snsId = " + snsId;
        Cursor cursor = rawQuery(query);

        if (cursor == null || !cursor.moveToFirst())
            return null;

        byte[] blob = cursor.getBlob(cursor.getColumnIndex("attrBuf"));
        cursor.close();
        return decodeBlob(cls, blob);
    }

    public String getCommentContent(Object o) {
        if (o == null)
            return null;
        return (String) getObjectField(o, w.commentContentField);
    }

    public Object decodeBlob(Class<?> cls, byte[] blob) {
        if (cls == null || blob == null)
            return null;

        try {
            return callMethod(newInstance(cls), w.blobDecodeMethod,
                    new Class[]{byte[].class}, blob);
        } catch (Throwable t) {
            XposedBridge.log(t);
        }
        return null;
    }

    public String getSnsContent(Object o) {
        if (o == null)
            return null;
        return (String) getObjectField(o, w.snsContentField);
    }

    public SparseArrayCompat<Object> getSnsCommentContent(Object o) {
        if (o == null)
            return null;
        LinkedList linkedList = (LinkedList) getObjectField(o, w.commentsListField);
        SparseArrayCompat<Object> comments = new SparseArrayCompat<>(linkedList.size());
        for (Object item : linkedList) {
            int time = (int) getObjectField(item, w.commentTimeField);
            comments.put(time, item);
        }
        return comments;
    }
}