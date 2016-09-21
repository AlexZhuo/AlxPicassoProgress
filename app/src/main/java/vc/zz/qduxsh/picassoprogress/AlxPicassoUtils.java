package vc.zz.qduxsh.picassoprogress;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.IntRange;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.pnikosis.materialishprogress.ProgressWheel;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

import okhttp3.OkHttpClient;

/**
 * Created by Alex on 2016/9/20.
 */
public class AlxPicassoUtils {
    private static Picasso picasso;
    private static AlxPicassoOk3Downloader.ProgressListener progressListener;
    private static final WeakHashMap<String,ProgressWheel> progressWheelHashMap = new WeakHashMap<>();//用于管理进度条的map,使用弱引用可以防止OOM
    private static final WeakHashMap<String,TextView> textViewHashMap = new WeakHashMap<>();//用于管理进度条的map,使用弱引用可以防止OOM
    private static final Handler handler = new Handler();
    private static final int PROGRESS_SIZE = 200;//圆形进度条的大小
    private static final float PROGRESS_SPIN_SPEED = 0.2f;//圆形进度条的旋转速度
    private static final ConcurrentHashMap<String,Integer> progressHashMap = new ConcurrentHashMap<>();//用于记录某个url的下载进度
    /**
     * 获得单例的Picasso，如果不单例那么Lru缓存就会失效
     * @param context
     * @param listener
     * @return
     */
    static private Picasso getPicasso(Context context, AlxPicassoOk3Downloader.ProgressListener listener) {
        OkHttpClient client = AlxOk3ClientManager.getDefaultClient(context);
        AlxPicassoOk3Downloader downloader = new AlxPicassoOk3Downloader(client,listener);
        if(picasso == null) picasso = new Picasso.Builder(context).downloader(downloader).build();
        return picasso;
    }

    private static AlxPicassoOk3Downloader.ProgressListener getListener(){
        if(progressListener == null)progressListener = new AlxPicassoOk3Downloader.ProgressListener() {
            @Override
            public void update(@IntRange(from = 0, to = 100) final int percent, final String url) {
                Log.i("Alex","当前下载的百分比是=="+percent+"   url::"+url);
                if(percent > 100 || percent <1)return;
                final ProgressWheel progressWheel = progressWheelHashMap.get(url);
                final TextView textView = textViewHashMap.get(url);
                progressHashMap.put(url,percent);
                if(textView == null || progressWheel == null)return;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        //防止listView的控件复用
                        if(!url.equals(progressWheel.getTag(R.id.progress_wheel)) ||! url.equals(textView.getTag(R.id.tv1))){
                            progressWheelHashMap.remove(url);
                            textViewHashMap.remove(url);
                            return;
                        }
                        if(percent == 100 || progressHashMap.get(url)==100){
                            textView.setVisibility(View.GONE);
                            progressWheel.setVisibility(View.GONE);
                            return;
                        }
                        //如果百分比突然不正常了，说明重新下载了一遍，这时候之前那一遍就应该干掉
                        if(progressHashMap.get(url) > percent){//正常情况下，每次的percent都应该比上次大
                            Log.i("Alex","注意：：图片被下载了两次!!!!!!  "+url);
                            if(progressHashMap.get(url) == 100) {
                                Log.i("Alex","事情变得更加棘手了！！！");
                                textView.setVisibility(View.GONE);
                                progressWheel.setVisibility(View.GONE);
                            }
                            return;
                        }
                        if(progressWheel.getVisibility() == View.GONE)progressWheel.setVisibility(View.VISIBLE);
                        if(textView.getVisibility() == View.GONE)textView.setVisibility(View.VISIBLE);
                        textView.setText(percent+"%");
                        progressWheel.setProgress(percent/100f);//设置进度条的进度
                    }
                });
            }
        };
        return progressListener;
    }

    public static void displayImageProgress(final String url, ImageView imageView , final ProgressWheel progressWheel, final TextView textView){
        if(progressListener == null)getListener();
        if(picasso == null)getPicasso(imageView.getContext(),progressListener);
        if(progressWheel.getCircleRadius() != PROGRESS_SIZE)progressWheel.setCircleRadius(PROGRESS_SIZE);
        //控制进度显示的控件
        if(progressWheel.getSpinSpeed() != PROGRESS_SPIN_SPEED)progressWheel.setSpinSpeed(PROGRESS_SPIN_SPEED);
        //设置tag防止复用引起的错乱,将url和空间绑定起来
        progressWheel.setTag(R.id.progress_wheel,url);
        textView.setTag(R.id.tv1,url);
        //默认不显示，有读数的时候才显示
        if(progressHashMap.get(url) == null || progressHashMap.get(url) == 100) {
            progressWheel.setVisibility(View.GONE);
            textView.setVisibility(View.GONE);
            if(progressHashMap.get(url)==null)progressHashMap.put(url,0);
        }else{
            progressWheel.setVisibility(View.VISIBLE);
            progressWheel.setProgress(progressHashMap.get(url)/100f);
            textView.setVisibility(View.VISIBLE);
            textView.setText(progressHashMap.get(url)+"%");
        }
        progressWheelHashMap.put(url,progressWheel);
        textViewHashMap.put(url,textView);
        //同一张图会开两个线程同时下载，如果你滑下去又划上来
        picasso.load(url).placeholder(R.drawable.qraved_bg_default).into(imageView, new Callback() {
            @Override
            public void onSuccess() {
                Log.i("Alex","图片加载成功::"+url);
                //防止listView的控件复用
                if(url.equals(progressWheel.getTag(R.id.progress_wheel)) && url.equals(textView.getTag(R.id.tv1))){
                    progressWheel.setVisibility(View.GONE);
                    textView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onError() {
                Log.i("Alex","图片加载失败");
            }
        });

    }
}
