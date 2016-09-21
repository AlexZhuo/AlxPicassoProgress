package vc.zz.qduxsh.picassoprogress;

import android.net.Uri;
import android.support.annotation.IntRange;
import android.support.annotation.WorkerThread;
import android.util.Log;

import com.squareup.picasso.Downloader;
import com.squareup.picasso.NetworkPolicy;

import java.io.IOException;

import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.Call;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import okio.Source;

/**
 * Created by Alex on 2016/9/20.
 */

public final class AlxPicassoOk3Downloader implements Downloader {

    private final Call.Factory client;
    private final Cache cache;


    public AlxPicassoOk3Downloader(OkHttpClient client,final ProgressListener listener) {
        this.client = client.newBuilder().addNetworkInterceptor(new Interceptor() {
            @Override public okhttp3.Response intercept(Chain chain) throws IOException {
                okhttp3.Response originalResponse = chain.proceed(chain.request());
                return originalResponse.newBuilder().body(
                        new AlxPicassoOk3Downloader.ProgressResponseBody(originalResponse.body(),originalResponse.request().url().url().toString(), listener))
                        .build();
            }
        }).build();
        this.cache = ((OkHttpClient)this.client).cache();
    }

    public AlxPicassoOk3Downloader(Call.Factory client) {
        this.client = client;
        this.cache = null;
    }
    public AlxPicassoOk3Downloader(OkHttpClient client) {
        this.client = client;
        this.cache = client.cache();
    }

    /**
     * 如果根据url请求到的不是图片而是字符串的话，支持自动重定向
     * @param uri
     * @param networkPolicy
     * @return
     * @throws IOException
     */
    @Override public Response load(Uri uri, int networkPolicy) throws IOException {
        Log.i("AlexImage","准备从php拿url去cdn要图片的uri是->"+uri+"    缓存策略是"+networkPolicy);
        CacheControl cacheControl = null;
        if (networkPolicy != 0) {
            if (NetworkPolicy.isOfflineOnly(networkPolicy)) {
                cacheControl = CacheControl.FORCE_CACHE;
            } else {
                CacheControl.Builder builder = new CacheControl.Builder();
                if (!NetworkPolicy.shouldReadFromDiskCache(networkPolicy)) {
                    builder.noCache();
                }
                if (!NetworkPolicy.shouldWriteToDiskCache(networkPolicy)) {
                    builder.noStore();
                }
                cacheControl = builder.build();
            }
        }

        Request.Builder php_builder = new Request.Builder().url(uri.toString());
        if (cacheControl != null) {
            php_builder.cacheControl(cacheControl);
        }
        long startTime = System.currentTimeMillis();
        okhttp3.Response php_response = client.newCall(php_builder.build()).execute();
        int php_responseCode = php_response.code();
        Log.i("AlexImage","php响应码是"+php_responseCode);
        if (php_responseCode >= 300) {
            php_response.body().close();
            throw new ResponseException(php_responseCode + " " + php_response.message(), networkPolicy, php_responseCode);
        }
        boolean fromPhpCache = php_response.cacheResponse() != null;
        Log.i("AlexImage","php的响应是不是从缓存拿的呀？"+fromPhpCache);
        Log.i("AlexImage","全部的header是"+php_response.headers());
        if("text/html".equals(php_response.header("Content-Type")) || "text/plain".equals(php_response.header("Content-Type"))){//如果php发来的是cdn的图片url
            Log.i("AlexImage","现在是从php取得的url字符串而不是jpg");
            String cdnUrl = php_response.body().string();
            Log.i("AlexImage","php服务器响应时间"+(System.currentTimeMillis() - startTime));
            Log.i("AlexImage","CDN的imageUrl是->"+cdnUrl);
            Request.Builder cdn_builder = new Request.Builder().url(cdnUrl);
            if (cacheControl != null) {
                cdn_builder.cacheControl(cacheControl);
            }
            long cdnStartTime = System.currentTimeMillis();
            okhttp3.Response cdn_response = client.newCall(cdn_builder.build()).execute();
            int cdn_responseCode = cdn_response.code();
            Log.i("AlexImage","cdn的响应码是"+cdn_responseCode);
            if (cdn_responseCode >= 300) {
                cdn_response.body().close();
                throw new ResponseException(cdn_responseCode + " " + cdn_response.message(), networkPolicy,
                        cdn_responseCode);
            }
            Log.i("AlexImage","cdn响应时间"+(System.currentTimeMillis() - cdnStartTime));
            boolean fromCache = cdn_response.cacheResponse() != null;
            ResponseBody cdn_responseBody = cdn_response.body();
            Log.i("AlexImage","cdn的图片是不是从缓存拿的呀？fromCache = "+fromCache);
            return new Response(cdn_responseBody.byteStream(), fromCache, cdn_responseBody.contentLength());
        }else {//如果php发来的不是图片的URL，那就直接用php发来的图片
            Log.i("AlexImage","准备直接用PHP的图片！！！");
            boolean fromCache = php_response.cacheResponse() != null;
            ResponseBody responseBody = php_response.body();
            return new Response(responseBody.byteStream(), fromCache, responseBody.contentLength());
        }
    }

    @Override public void shutdown() {
        if (cache != null) {
            try {
                cache.close();
            } catch (IOException ignored) {
            }
        }
    }
    public interface ProgressListener {
        @WorkerThread
        void update(@IntRange(from = 0, to = 100) int percent,String url);
    }
    public static class ProgressResponseBody extends ResponseBody {

        private final ResponseBody responseBody;
        private final ProgressListener progressListener;
        private BufferedSource bufferedSource;
        private String url;

        public ProgressResponseBody(ResponseBody responseBody,String url, ProgressListener progressListener) {
            this.responseBody = responseBody;
            this.progressListener = progressListener;
            this.url = url;
            Log.i("Alex","当前图片是：："+url);
        }

        @Override
        public MediaType contentType() {
            Log.i("Alex","contentType是"+responseBody.contentType());
            return responseBody.contentType();
        }

        @Override
        public long contentLength() {
            Log.i("Alex","contentLength"+responseBody.contentLength());
            return responseBody.contentLength();
        }

        @Override
        public BufferedSource source() {
            if (bufferedSource == null) {
                bufferedSource = Okio.buffer(source(responseBody.source()));
            }
            return bufferedSource;
        }

        private Source source(Source source) {

            return new ForwardingSource(source) {
                long totalBytesRead = 0L;

                @Override
                public long read(Buffer sink, long byteCount) throws IOException {
                    long bytesRead = super.read(sink, byteCount);
                    // read() returns the number of bytes read, or -1 if this source is exhausted.
                    totalBytesRead += bytesRead != -1 ? bytesRead : 0;
                    if (progressListener != null) {
                        progressListener.update(
                                ((int) ((100 * totalBytesRead) / responseBody.contentLength())),url);
                    }
                    return bytesRead;
                }
            };
        }
    }
}