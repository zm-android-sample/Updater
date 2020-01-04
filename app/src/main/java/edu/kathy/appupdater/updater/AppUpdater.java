package edu.kathy.appupdater.updater;

import edu.kathy.appupdater.updater.net.INetManager;
import edu.kathy.appupdater.updater.net.OkHttpNetManager;

public class AppUpdater {
    private static AppUpdater sAppUpdater = new AppUpdater();

    private INetManager mNetManager = new OkHttpNetManager();

    // 网络请求 下载的能力
    // okhttp volley htpclient httpurlconnection
    public static AppUpdater getInstance() {
        return sAppUpdater;
    }

    public void setNetManager(INetManager netManager) {
        mNetManager = netManager;
    }

    public INetManager getNetManager() {
        return mNetManager;
    }
}
