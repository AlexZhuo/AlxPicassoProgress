package vc.zz.qduxsh.picassoprogress;

import android.content.Context;
import android.os.StatFs;

import java.io.File;
import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;

/**
 * Created by Administrator on 2016/9/20.
 */
public class AlxOk3ClientManager {
    private static final String PICASSO_CACHE = "picasso-cache";//缓存图片的存放文件夹名
    private static final int MIN_DISK_CACHE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final int MAX_DISK_CACHE_SIZE = 50 * 1024 * 1024; // 50MB最大SD卡占用空间
    public static File createDefaultCacheDir(Context context) {
        File cache = new File(context.getApplicationContext().getCacheDir(), PICASSO_CACHE);
        if (!cache.exists()) {
            //noinspection ResultOfMethodCallIgnored
            cache.mkdirs();
        }
        return cache;
    }

    private static long calculateDiskCacheSize(File dir) {
        long size = MIN_DISK_CACHE_SIZE;

        try {
            StatFs statFs = new StatFs(dir.getAbsolutePath());
            long available = ((long) statFs.getBlockCount()) * statFs.getBlockSize();
            // Target 2% of the total space.
            size = available / 50;
        } catch (IllegalArgumentException ignored) {
        }

        // Bound inside min/max size for disk cache.
        return Math.max(Math.min(size, MAX_DISK_CACHE_SIZE), MIN_DISK_CACHE_SIZE);
    }

    private static OkHttpClient defaultOkHttpClient(File cacheDir, long maxSize) {
        Interceptor REWRITE_CACHE_CONTROL_INTERCEPTOR = new Interceptor() {
            @Override
            public okhttp3.Response intercept(Chain chain) throws IOException {
                okhttp3.Response originalResponse = chain.proceed(chain.request());
                return originalResponse.newBuilder()
                        .removeHeader("Pragma")
                        .header("Cache-Control", String.format("max-age=%d", 604800))//本地sd卡缓存7天
                        .build();
            }
        };

        return new OkHttpClient.Builder()
                .cache(new okhttp3.Cache(cacheDir, maxSize))
                .addNetworkInterceptor(REWRITE_CACHE_CONTROL_INTERCEPTOR)
                .build();
    }


    /**
     * Create new downloader that uses OkHttp. This will install an image cache into your application
     * cache directory.
     */
    public static OkHttpClient getDefaultClient(Context context) {
        return getDefaultClient(createDefaultCacheDir(context));
    }


    /**
     * Create new downloader that uses OkHttp. This will install an image cache into the specified
     * directory.
     *
     * @param cacheDir The directory in which the cache should be stored
     */
    public static OkHttpClient getDefaultClient(File cacheDir) {
        return getDefaultClient(cacheDir, calculateDiskCacheSize(cacheDir));
    }

    /**
     * Create new downloader that uses OkHttp. This will install an image cache into your application
     * cache directory.
     *
     * @param maxSize The size limit for the cache.
     */
    public static OkHttpClient getDefaultClient(final Context context, final long maxSize) {
        return getDefaultClient(createDefaultCacheDir(context), maxSize);
    }

    /**
     * Create new downloader that uses OkHttp. This will install an image cache into the specified
     * directory.
     *
     * @param cacheDir The directory in which the cache should be stored
     * @param maxSize The size limit for the cache.
     */
    public static OkHttpClient getDefaultClient(File cacheDir, long maxSize) {
        return defaultOkHttpClient(cacheDir, maxSize);
    }

}
