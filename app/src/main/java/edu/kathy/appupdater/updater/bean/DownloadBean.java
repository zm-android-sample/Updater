package edu.kathy.appupdater.updater.bean;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

// android平台用Parceable
public class DownloadBean implements Serializable {
    private String title;
    private String content;
    private String url;
    private String md5;
    private String versionCode;

    public DownloadBean(String title, String content, String url, String md5, String versionCode) {
        this.title = title;
        this.content = content;
        this.url = url;
        this.md5 = md5;
        this.versionCode = versionCode;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public String getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(String versionCode) {
        this.versionCode = versionCode;
    }

    public static DownloadBean parse(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            String title = jsonObject.optString("title");
            String content = jsonObject.optString("content");
            String url = jsonObject.optString("url");
            String md5 = jsonObject.optString("md5");
            String versionCode = jsonObject.optString("versionCode");
            DownloadBean downloadBean = new DownloadBean(title, content, url, md5,versionCode);
            return downloadBean;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
