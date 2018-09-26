package com.fkzhang.wechatunrecalled.Util;

import android.content.ContentValues;
import android.database.Cursor;
import android.text.TextUtils;

import java.util.HashMap;
import java.util.Random;

import static de.robv.android.xposed.XposedHelpers.callMethod;

/**
 * Created by fkzhang on 2/6/2016.
 */
public class WechatMainDBHelper {
    private Object SQLDB;
    private HashMap<String, String> mNicknameCache;
    private HashMap<String, String> mChatroomMemberMap;

    public WechatMainDBHelper(Object dbObject) {
        SQLDB = dbObject;
//        XposedBridge.log("" + dbObject);
        mNicknameCache = new HashMap<>();
    }

    public void insertSQL(String table, String selection, ContentValues contentValues) {
        callMethod(SQLDB, "insert", table, selection, contentValues);
    }

    public Cursor rawQuery(String query) {
        return rawQuery(query, null);
    }

    public Cursor rawQuery(String query, String[] args) {
        return (Cursor) callMethod(SQLDB, "rawQuery", query, args);
    }

    public void SQLUpdate(String table, ContentValues contentValues, String selection, String[] args) {
        callMethod(SQLDB, "update", table, contentValues, selection, args);
    }

    public Cursor getMessageBySvrId(String msgSrvId) {
        String sql = "select * from message where msgsvrid=?";
        String[] sqlArgs = {msgSrvId};

        return rawQuery(sql, sqlArgs);
    }

    public void insertMessage(String talker, int talkerId, String msg) {
        insertMessage(talker, talkerId, msg, 1, System.currentTimeMillis());
    }

    public void insertSystemMessage(String talker, int talkerId, String msg) {
        insertMessage(talker, talkerId, msg, 10000, System.currentTimeMillis());
    }

    public void insertSystemMessage(String talker, int talkerId, String msg, long createTime) {
        insertMessage(talker, talkerId, msg, 10000, createTime);
    }

    public void insertMessage(String talker, int talkerId, String msg, int type, long createTime) {
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
    }

    public long getNextMsgId() {
        Cursor cursor = rawQuery("SELECT max(msgId) FROM message");
        if (cursor == null || !cursor.moveToFirst())
            return -1;

        long id = cursor.getInt(0) + 1;
        cursor.close();
        return id;
    }

    public Cursor getLastMsg(String username) {
        String query = "SELECT * FROM message WHERE msgId = (SELECT max(msgId) FROM message WHERE talker='" +
                username + "')";
        return rawQuery(query);
    }

    public int getUnreadCount(String username) {
        Cursor cursor = rawQuery("select unReadCount from rconversation where " +
                "username = '" + username
                + "' and ( parentref is null or parentref = '' ) ");

        if (cursor == null || !cursor.moveToFirst())
            return 0;

        int cnt = cursor.getInt(cursor.getColumnIndex("unReadCount"));
        cursor.close();
        return cnt;
    }

    public String getNickname(String username) {
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
        name = name.trim();
        cursor.close();
        mNicknameCache.put(username, name);
        return name;
    }

    public HashMap<String, String> getChatRoomMembers() {
        String query = "SELECT * FROM chatroom";
        Cursor cursor = rawQuery(query);

        HashMap<String, String> map = new HashMap<>();

        if (cursor == null || !cursor.moveToFirst())
            return map;

        do {
            String memberlist = cursor.getString(cursor.getColumnIndex("memberlist"));
            String displayname = cursor.getString(cursor.getColumnIndex("displayname"));

            String[] members = memberlist.split(";");
            String[] names;
            if (displayname.contains("、")) {
                names = displayname.split("、");
            } else {
                names = displayname.split(",");
            }

            for (int i = 0; i < members.length; i++) {
                map.put(members[i].trim(), names[i].trim());
            }

        } while (cursor.moveToNext());
        cursor.close();

        return map;
    }

    public String getChatroomName(String username) {
        String name = getNickname(username);
        if (!TextUtils.isEmpty(name))
            return name;

        String query = "SELECT * FROM chatroom WHERE chatroomname = ?";
        Cursor cursor = rawQuery(query, new String[]{username});
        if (cursor == null || !cursor.moveToFirst())
            return null;

        name = cursor.getString(cursor.getColumnIndex("displayname"));
        cursor.close();
        return name;
    }

    public String getChatroomMemberName(String username) {
        if (mChatroomMemberMap == null) {
            mChatroomMemberMap = getChatRoomMembers();
        }

        if (mChatroomMemberMap.containsKey(username)) {
            return mChatroomMemberMap.get(username);
        }

        // reload
        mChatroomMemberMap = getChatRoomMembers();
        if (mChatroomMemberMap.containsKey(username)) {
            return mChatroomMemberMap.get(username);
        }
        return null;
    }

    public Cursor getContact(String username) {
        String query = "SELECT * FROM rcontact WHERE username = ?";
        return rawQuery(query, new String[]{username});
    }

}