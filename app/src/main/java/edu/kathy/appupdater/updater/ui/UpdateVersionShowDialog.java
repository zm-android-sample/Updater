package edu.kathy.appupdater.updater.ui;

import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

import java.io.File;

import edu.kathy.appupdater.MainActivity;
import edu.kathy.appupdater.R;
import edu.kathy.appupdater.updater.AppUpdater;
import edu.kathy.appupdater.updater.bean.DownloadBean;
import edu.kathy.appupdater.updater.net.INetDownloadCallback;
import edu.kathy.appupdater.updater.utils.AppUtils;

public class UpdateVersionShowDialog extends DialogFragment {

    private static final String KEY_DOWNLOAD_BEAN = "download_bean";

    private DownloadBean mDownloadBean;

    public static void show(FragmentActivity activity, DownloadBean downloadBean) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(KEY_DOWNLOAD_BEAN, downloadBean);
        UpdateVersionShowDialog updateVersionShowDialog = new UpdateVersionShowDialog();
        updateVersionShowDialog.setArguments(bundle);
        updateVersionShowDialog.show(activity.getSupportFragmentManager(), "updateVersionShowDialog");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDownloadBean = (DownloadBean) getArguments().getSerializable(KEY_DOWNLOAD_BEAN);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_updater, container, false);
        bindEvents(view);
        return view;
    }

    private void bindEvents(View view) {
        TextView tvTitle = view.findViewById(R.id.title_textview);
        TextView tvContent = view.findViewById(R.id.content_textview);
        final TextView tvUpdate = view.findViewById(R.id.update_textview);
        tvTitle.setText(mDownloadBean.getTitle());
        tvContent.setText(mDownloadBean.getContent());
        tvUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                v.setEnabled(false);

                final File targetFile = new File(getActivity().getCacheDir(), "target.apk");
                AppUpdater.getInstance().getNetManager().download(mDownloadBean.getUrl(), targetFile, new INetDownloadCallback() {
                    @Override
                    public void success(File apkFile) {
                        // 安装的代码
                        v.setEnabled(true);
                        dismiss();

                        String fileMD5 = AppUtils.getFileMD5(targetFile);
                        Log.e(UpdateVersionShowDialog.class.getName(), "MD5:" + fileMD5);

                        if (!TextUtils.isEmpty(fileMD5) && fileMD5.equals(mDownloadBean.getMd5())) {
                            // 检测MD5 信息摘要 判断文件有没有被修改过(是否完整下载)
                            AppUtils.installApk(getActivity(), targetFile);
                        } else {
                            Toast.makeText(getActivity(), "MD5 检测失败", Toast.LENGTH_SHORT).show();
                        }

                        Log.e(MainActivity.class.getName(), "success " + apkFile.getAbsolutePath());
                    }

                    @Override
                    public void progress(int progress) {
                        // 更新界面的代码
                        Log.e(MainActivity.class.getName(), "Progress " + progress);
                        tvUpdate.setText(progress + "");
                    }

                    @Override
                    public void failed(Throwable throwable) {
                        v.setEnabled(true);
                        Toast.makeText(getActivity(), "文件下载失败", Toast.LENGTH_SHORT).show();
                    }
                }, UpdateVersionShowDialog.this);
            }
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        Log.d(UpdateVersionShowDialog.class.getName(), "onDismiss");
        AppUpdater.getInstance().getNetManager().cancel(this);
    }
}
