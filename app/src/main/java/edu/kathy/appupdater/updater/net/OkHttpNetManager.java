package edu.kathy.appupdater.updater.net;

import android.os.Handler;
import android.os.Looper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class OkHttpNetManager implements INetManager {

    private static OkHttpClient sOkHttpClient;
    private static Handler sHandler = new Handler(Looper.getMainLooper());

    static {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(10, TimeUnit.SECONDS);
        sOkHttpClient = builder.build();
    }

    @Override
    public void get(String url, final INetCallback callback, Object tag) {
        Request.Builder builder = new Request.Builder();
        Request request = builder
                        .url(url)
                        .get()
                        .tag(tag)
                        .build();
        Call call = sOkHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                // 非ui线程
                sHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.failed(e);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) {
                try {
                    final String resString = response.body().string();
                    sHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.success(resString);
                        }
                    });
                } catch (Throwable e) {
                    e.printStackTrace();
                    callback.failed(e);
                }
            }
        });

    }

    // 断点续传(断点续下) 通过HTTP的RANGE指定下载一个文件的起始字节和终止字节
    // 大文件可以通过多线程分段下载 然后RandomAccessFile来合并
    // 增量更新 本地apk和server apk 通过diff(bsdiff 开源算法)算法生成patch => download patch
    @Override
    public void download(String url, final File targetFile, final INetDownloadCallback callback, Object tag) {
        if (!targetFile.exists()) {
            targetFile.getParentFile().mkdirs();
        }

        Request.Builder builder = new Request.Builder();
        Request request = builder.url(url).get().tag(tag).build();
        Call call = sOkHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                sHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.failed(e);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                InputStream is = null;
                OutputStream os = null;

                try {
                    final long totalLength = response.body().contentLength();

                    is = response.body().byteStream();
                    os = new FileOutputStream(targetFile);

                    byte[] buffer = new byte[8 * 1024];
                    long curLength = 0;
                    int bufferLen = 0;

                    while (!call.isCanceled() && (bufferLen = is.read(buffer)) != -1) {
                        os.write(buffer, 0, bufferLen);
                        os.flush();
                        curLength += bufferLen;

                        final long finalCurLength = curLength;

                        sHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                callback.progress((int) (finalCurLength * 1.0F / totalLength * 100));
                            }
                        });
                    }

                    if (call.isCanceled()) {
                        return;
                    }

                    try {
                        targetFile.setExecutable(true, false);
                        targetFile.setReadable(true, false);
                        targetFile.setWritable(true, false);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    sHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.success(targetFile);
                        }
                    });
                } catch (final Throwable e) {
                    e.printStackTrace();

                    if (call.isCanceled()) {
                        return;
                    }

                    sHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.failed(e);
                        }
                    });
                } finally {
                    if (is != null) {
                        is.close();
                    }

                    if (os != null) {
                        os.close();
                    }
                }
            }
        });
    }

    @Override
    public void cancel(Object tag) {
        List<Call> queuedCalls = sOkHttpClient.dispatcher().queuedCalls();
        if (queuedCalls != null) {
            for (Call call : queuedCalls) {
                if (tag.equals(call.request().tag())) {
                    call.cancel();
                }
            }
        }

        List<Call> runningCalls = sOkHttpClient.dispatcher().runningCalls();
        if (runningCalls != null) {
            for (Call call : runningCalls) {
                if (tag.equals(call.request().tag())) {
                    call.cancel();
                }
            }
        }
    }
}
