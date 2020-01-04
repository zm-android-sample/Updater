package edu.kathy.appupdater.updater.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import edu.kathy.appupdater.BuildConfig;

public class AppUtils {

    public static long getVersionCode(Context context) {
        PackageManager packageManager = context.getPackageManager();
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                return packageInfo.getLongVersionCode();
            } else {
                return packageInfo.versionCode;
            }
        } catch (PackageManager.NameNotFoundException ex) {
            ex.printStackTrace();
        }
        return -1;
    }

    public static void installApk(Activity activity, File apkFile) {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Intent.ACTION_VIEW);

        Uri uri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            uri = FileProvider.getUriForFile(activity, BuildConfig.APPLICATION_ID + ".fileprovider", apkFile);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            Log.e("AppUtil", uri.toString());
        } else {
            uri = Uri.fromFile(apkFile);
        }
        intent.setDataAndType(uri, "application/vnd.android.package-archive");
        activity.startActivity(intent);
        // TODO N FileProvider
        // TODO O Install Permission适配
    }

//    我们可以根据以下的数据类型选择
//    {
//        //{后缀名，MIME类型}
//        {".3gp",    "video/3gpp"},
//        {".apk",    "application/vnd.Android.package-archive"},
//        {".asf",    "video/x-ms-asf"},
//        {".avi",    "video/x-msvideo"},
//        {".bin",    "application/octet-stream"},
//        {".bmp",    "image/bmp"},
//        {".c",  "text/plain"},
//        {".class",  "application/octet-stream"},
//        {".conf",   "text/plain"},
//        {".cpp",    "text/plain"},
//        {".doc",    "application/msword"},
//        {".docx",   "application/vnd.openxmlformats-officedocument.wordprocessingml.document"},
//        {".xls",    "application/vnd.ms-excel"},
//        {".xlsx",   "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"},
//        {".exe",    "application/octet-stream"},
//        {".gif",    "image/gif"},
//        {".gtar",   "application/x-gtar"},
//        {".gz", "application/x-gzip"},
//        {".h",  "text/plain"},
//        {".htm",    "text/html"},
//        {".html",   "text/html"},
//        {".jar",    "application/java-archive"},
//        {".java",   "text/plain"},
//        {".jpeg",   "image/jpeg"},
//        {".jpg",    "image/jpeg"},
//        {".js", "application/x-JavaScript"},
//        {".log",    "text/plain"},
//        {".m3u",    "audio/x-mpegurl"},
//        {".m4a",    "audio/mp4a-latm"},
//        {".m4b",    "audio/mp4a-latm"},
//        {".m4p",    "audio/mp4a-latm"},
//        {".m4u",    "video/vnd.mpegurl"},
//        {".m4v",    "video/x-m4v"},
//        {".mov",    "video/quicktime"},
//        {".mp2",    "audio/x-mpeg"},
//        {".mp3",    "audio/x-mpeg"},
//        {".mp4",    "video/mp4"},
//        {".mpc",    "application/vnd.mpohun.certificate"},
//        {".mpe",    "video/mpeg"},
//        {".mpeg",   "video/mpeg"},
//        {".mpg",    "video/mpeg"},
//        {".mpg4",   "video/mp4"},
//        {".mpga",   "audio/mpeg"},
//        {".msg",    "application/vnd.ms-outlook"},
//        {".ogg",    "audio/ogg"},
//        {".pdf",    "application/pdf"},
//        {".png",    "image/png"},
//        {".pps",    "application/vnd.ms-powerpoint"},
//        {".ppt",    "application/vnd.ms-powerpoint"},
//        {".pptx",   "application/vnd.openxmlformats-officedocument.presentationml.presentation"},
//        {".prop",   "text/plain"},
//        {".rc", "text/plain"},
//        {".rmvb",   "audio/x-pn-realaudio"},
//        {".rtf",    "application/rtf"},
//        {".sh", "text/plain"},
//        {".tar",    "application/x-tar"},
//        {".tgz",    "application/x-compressed"},
//        {".txt",    "text/plain"},
//        {".wav",    "audio/x-wav"},
//        {".wma",    "audio/x-ms-wma"},
//        {".wmv",    "audio/x-ms-wmv"},
//        {".wps",    "application/vnd.ms-works"},
//        {".xml",    "text/plain"},
//        {".z",  "application/x-compress"},
//        {".zip",    "application/x-zip-compressed"},
//        {"",        "*/*"}
//    };

    public static String getFileMD5(File targetFile) {
        if (targetFile == null || !targetFile.isFile()) {
            return null;
        }

        MessageDigest digest = null;
        FileInputStream in = null;
        byte[] buffer = new byte[1024];
        int len = 0;

        try {
            digest = MessageDigest.getInstance("MD5");
            in = new FileInputStream(targetFile);
            while ((len = in.read(buffer)) != -1) {
                digest.update(buffer, 0, len);
            }
        } catch (NoSuchAlgorithmException | IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        byte[] result = digest.digest();
        BigInteger bigint = new BigInteger(1, result);
        return bigint.toString(16);
    }
}
