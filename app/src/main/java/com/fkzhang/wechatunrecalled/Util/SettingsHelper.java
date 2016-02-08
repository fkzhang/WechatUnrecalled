package com.fkzhang.wechatunrecalled.Util;

import android.content.Context;
import android.content.SharedPreferences;

import de.robv.android.xposed.XSharedPreferences;

/**
 * Created by fkzhang on 1/24/2016.
 */
public class SettingsHelper {
    private SharedPreferences mPreferences = null;
    private XSharedPreferences mXPreferences = null;

    public SettingsHelper(String name) {
        mXPreferences = new XSharedPreferences(name);
        mXPreferences.makeWorldReadable();
        this.reload();
    }

    public SettingsHelper(Context context, String packageName) {
        this.mPreferences = context.getSharedPreferences(
                packageName+"_preferences", 1);
    }

    public String getString(String key, String defaultValue) {
        if (mPreferences != null) {
            return mPreferences.getString(key, defaultValue);
        } else if (mXPreferences != null) {
            return mXPreferences.getString(key, defaultValue);
        }

        return defaultValue;
    }

    public int getInt(String key, int defaultValue) {
        if (mPreferences != null) {
            return mPreferences.getInt(key, defaultValue);
        } else if (mXPreferences != null) {
            return mXPreferences.getInt(key, defaultValue);
        }

        return defaultValue;
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        if (mPreferences != null) {
            return mPreferences.getBoolean(key, defaultValue);
        } else if (mXPreferences != null) {
            return mXPreferences.getBoolean(key, defaultValue);
        }

        return defaultValue;
    }

    public void setString(String key, String value) {
        SharedPreferences.Editor editor = null;
        if (mPreferences != null) {
            editor = mPreferences.edit();
        } else if (mXPreferences != null) {
            editor = mXPreferences.edit();
        }

        if (editor != null) {
            editor.putString(key, value);
            editor.commit();
        }
    }

    public void setBoolean(String key, boolean value) {
        SharedPreferences.Editor editor = null;
        if (mPreferences != null) {
            editor = mPreferences.edit();
        } else if (mXPreferences != null) {
            editor = mXPreferences.edit();
        }

        if (editor != null) {
            editor.putBoolean(key, value);
            editor.commit();
        }
    }

    public void setInt(String key, int value) {
        SharedPreferences.Editor editor = null;
        if (mPreferences != null) {
            editor = mPreferences.edit();
        } else if (mXPreferences != null) {
            editor = mXPreferences.edit();
        }

        if (editor != null) {
            editor.putInt(key, value);
            editor.commit();
        }
    }

    public void reload() {
        if (mXPreferences != null) {
            mXPreferences.reload();
        }
    }
}