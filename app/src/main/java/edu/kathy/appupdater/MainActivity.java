package edu.kathy.appupdater;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import edu.kathy.appupdater.updater.AppUpdater;
import edu.kathy.appupdater.updater.bean.DownloadBean;
import edu.kathy.appupdater.updater.net.INetCallback;
import edu.kathy.appupdater.updater.ui.UpdateVersionShowDialog;
import edu.kathy.appupdater.updater.utils.AppUtils;

public class MainActivity extends AppCompatActivity {

    private Button mUpdaterBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mUpdaterBtn = findViewById(R.id.updater_btn);
        mUpdaterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                 http://59.110.162.30/app_updater_version.json
//                {
//                    "title":"4.5.0更新啦！",
//                        "content":"1. 优化了阅读体验；\n2. 上线了 hyman 的课程；\n3. 修复了一些已知问题。",
//                        "url":"http://59.110.162.30/v450_imooc_updater.apk",
//                        "md5":"14480fc08932105d55b9217c6d2fb90b",
//                        "versionCode":"450"
//                }

                AppUpdater.getInstance().getNetManager().get("http://59.110.162.30/app_updater_version.json", new INetCallback() {
                    @Override
                    public void success(String response) {
                        // 解析json
                        // 做版本匹配

                        DownloadBean downloadBean = DownloadBean.parse(response);
                        if (downloadBean == null) {
                            Toast.makeText(MainActivity.this, "版本检测接口返回数据异常", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        long versionCode = Long.parseLong(downloadBean.getVersionCode());

                        if (versionCode <= AppUtils.getVersionCode(MainActivity.this)) {
                            Toast.makeText(MainActivity.this, "已经是最新版本", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        UpdateVersionShowDialog.show(MainActivity.this, downloadBean);
                    }

                    @Override
                    public void failed(Throwable throwable) {
                        Toast.makeText(MainActivity.this, "获取版本信息失败", Toast.LENGTH_SHORT).show();
                    }
                }, MainActivity.this);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AppUpdater.getInstance().getNetManager().cancel(this);
    }
}
