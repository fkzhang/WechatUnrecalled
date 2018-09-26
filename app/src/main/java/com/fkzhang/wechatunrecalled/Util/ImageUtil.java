package com.fkzhang.wechatunrecalled.Util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

/**
 * Created by fkzhang on 2/6/2016.
 */
public class ImageUtil {
    public static void getBitmapFromURL(String src, OnImageProcessListener listener) {
        try {
            URL url = new URL(src);
            getBitmapFromURL(url, listener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void getBitmapFromURL(URL url, OnImageProcessListener listener) {
        new ImageWebLoader(listener).execute(url);
    }

    public static void getBitmapFromFile(File file, OnImageProcessListener listener) {
        new ImageFileLoader(listener).execute(file);
    }

    public static void saveBitmapToFile(File file, Bitmap bitmap) {
        new ImageFileSaver(file, bitmap).execute();
    }

    public static void saveBitmapToFile(File file, Bitmap bitmap, OnImageProcessListener listener) {
        new ImageFileSaver(file, bitmap, listener).execute();
    }

    private static Bitmap getBitmapFromURL(URL url) {
        if (url == null) {
            return null;
        }

        try {
            return BitmapFactory.decodeStream(url.openConnection().getInputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void saveBitmapFile(File file, Bitmap bitmap) {
        try {
            if (file.exists()) {
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
                fileOutputStream.flush();
                fileOutputStream.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void saveBitmapFile(File file, Bitmap bitmap, OnImageProcessListener listener) {
        try {
            if (file.exists()) {
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
                fileOutputStream.flush();
                fileOutputStream.close();
                listener.onComplete(bitmap);
                return;
            }
            listener.onComplete(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void saveToStream(Bitmap bitmap, OutputStream stream) {
        if (stream == null)
            return;
        try {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            stream.flush();
            stream.close();
            bitmap.recycle();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Bitmap getBitmapFromStream(InputStream stream) {
        if (stream == null)
            return null;

        Bitmap bitmap = BitmapFactory.decodeStream(stream);
        try {
            stream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    private static Bitmap getBitmapFromFile(File file) {
        if (!file.exists())
            return null;

        try {
            return BitmapFactory.decodeStream(new FileInputStream(file));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public interface OnImageProcessListener {
        void onComplete(Bitmap bitmap);
    }

    private static class ImageWebLoader extends AsyncTask<URL, Void, Bitmap> {
        private OnImageProcessListener listener;

        public ImageWebLoader(OnImageProcessListener listener) {
            this.listener = listener;
        }

        @Override
        protected Bitmap doInBackground(URL... params) {
            return getBitmapFromURL(params[0]);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            if (listener != null) {
                listener.onComplete(bitmap);
            }
        }
    }

    private static class ImageFileLoader extends AsyncTask<File, Void, Bitmap> {
        private OnImageProcessListener listener;

        public ImageFileLoader(OnImageProcessListener listener) {
            this.listener = listener;
        }

        @Override
        protected Bitmap doInBackground(File... params) {
            return getBitmapFromFile(params[0]);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            if (listener != null) {
                listener.onComplete(bitmap);
            }
        }
    }

    private static class ImageFileSaver extends AsyncTask<Void, Void, Void> {
        private Bitmap mBitmap;
        private File mFile;
        private OnImageProcessListener listener;

        public ImageFileSaver(File file, Bitmap bitmap, OnImageProcessListener listener) {
            mFile = file;
            mBitmap = bitmap;
            this.listener = listener;
        }

        public ImageFileSaver(File file, Bitmap bitmap) {
            mFile = file;
            mBitmap = bitmap;
        }

        @Override
        protected Void doInBackground(Void... params) {
            if (this.listener != null) {
                saveBitmapFile(mFile, mBitmap, listener);
            } else {
                saveBitmapFile(mFile, mBitmap);
            }
            return null;
        }

    }
}
