package vc.zz.qduxsh.picassoprogress;


import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Build;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;


/**
 * Created by Alex on 2016/4/6.
 */
public class AlxBitmapUtils {

    /**
     * 传入一个bitmap，根据传入比例进行大小缩放
     * @param bitmap
     * @param widthRatio 宽度比例，缩小就比1小，放大就比1大
     * @param heightRatio
     * @return
     */
    public static Bitmap scaleBitmap(Bitmap bitmap, float widthRatio, float heightRatio) {
        Matrix matrix = new Matrix();
        matrix.postScale(widthRatio,heightRatio);
        return Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true);
    }


    /**
     * 获取一个bitmap在内存中所占的大小
     * @param image
     * @return
     */
    public static int getSize(Bitmap image){
        int size=0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {    //API 19
            size = image.getAllocationByteCount();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {//API 12
            size = image.getByteCount();
        } else {
            size = image.getRowBytes() * image.getHeight();
        }
        return size;
    }



    /**
     * 从assets文件夹中根据文件名得到一个Bitmap
     * @param fileName
     * @return
     */
    public static Bitmap getDataFromAssets(Context context,String fileName){
        Log.i("Alex","准备从assets文件夹中读取文件"+fileName);
        try {
            //可以直接使用context.getResources().getAssets().open(fileName);得到一个InputStream再用BufferedInputStream通过缓冲区获得字符数组
            AssetFileDescriptor descriptor = context.getResources().getAssets().openFd(fileName);//此处获得文件描述之后可以得到FileInputStream，然后使用NIO得到Channel
            long fileSize = descriptor.getLength();
            Log.i("Alex","要读取的文件的长度是"+fileSize);//注意这个地方如果文件大小太大，在decodeStream需要设置参数进行裁剪
            Bitmap bitmap = BitmapFactory.decodeStream(context.getResources().getAssets().open(fileName));
//            Bitmap bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor);//注意，AssetFileDescriptor只能用来获取文件的大小，不能用来获取inputStream，用FileDescriptor获取的输入流BitmapFactory.decodeStream不能识别
            if(bitmap==null)Log.i("Alex","decode bitmap失败");
            return bitmap;
        } catch (Exception e) {
            Log.i("Alex","读取文件出现异常",e);
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 获取系统总内存,返回单位为kb
     * @return
     */
    public static long getPhoneTotalMemory() {
        String str1 = "/proc/meminfo";// 系统内存信息文件
        String str2;
        String[] arrayOfString;
        long initial_memory = 0;
        try {
            FileReader localFileReader = new FileReader(str1);
            BufferedReader localBufferedReader = new BufferedReader(localFileReader, 8192);
            str2 = localBufferedReader.readLine();// 读取meminfo第一行，系统总内存大小
            arrayOfString = str2.split("\\s+");
            for (String num : arrayOfString) {
                Log.i("Alex",str2+"内存是::::"+num + "\t");
            }
            initial_memory = Integer.valueOf(arrayOfString[1]).intValue();// 获得系统总内存，单位是KB，乘以1024转换为Byte
            localBufferedReader.close();
        } catch (IOException e) {
            Log.i("Alex","获取系统总内存出现异常",e);
        } catch (Exception e){
            Log.i("Alex","获取系统总内存出现异常2",e);
        }
        return initial_memory;
    }

    /**
     * 获取系统当前可用内存
     * @return
     */
    private long getAvailMemory(Context context) {
        // 获取android当前可用内存大小
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(mi);
        return mi.availMem;// 将获取的内存大小规格化
    }




    public static Bitmap base64ToBitmap(String sourceBase64){
        if(TextUtils.isEmpty(sourceBase64)) sourceBase64 = "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";
        byte[] sourceBytes = Base64.decode(sourceBase64.getBytes(),Base64.DEFAULT);
        if(sourceBytes == null || sourceBytes.length == 0)return null;
        Bitmap bitmap = BitmapFactory.decodeByteArray(sourceBytes,0,sourceBytes.length);
        if(bitmap == null)return null;
        if(bitmap.getHeight()<2)return null;
        return bitmap;
    }

    public static Bitmap tiny2BlurBitmap(Context context,Bitmap tinyBitmap){
        if(tinyBitmap == null || tinyBitmap.getWidth()<2)return null;
        //将25*25的小bitmap先放大，再进行高斯模糊,放大后的宽度是300像素
        int scaleRatio = 300 / tinyBitmap.getWidth();
        Bitmap bigBitmap = AlxBitmapUtils.scaleBitmap(tinyBitmap, scaleRatio, scaleRatio);
        tinyBitmap.recycle();//微型图片已经没用了，回收掉
        //将300宽度的大图进行高斯模糊，模糊半径为8
        Bitmap blurBitmap = AlxBitmapUtils.fastblur(context, bigBitmap, 8);
        bigBitmap.recycle();//扩大到300像素宽图片已经没用了，回收掉
        return blurBitmap;
    }

    public static Bitmap base64ToBlurBitmap(Context context,String base64){
        if(context == null || TextUtils.isEmpty(base64))return null;
        Bitmap tinyBitmap = base64ToBitmap(base64);
        Bitmap blurBitmap = tiny2BlurBitmap(context,tinyBitmap);
        return blurBitmap;
    }

    @SuppressLint("NewApi")
    public static Bitmap fastblur(Context context, Bitmap sentBitmap, int radius) {

        if (Build.VERSION.SDK_INT > 16) {
            Bitmap bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);

            final RenderScript rs = RenderScript.create(context);
            final Allocation input = Allocation.createFromBitmap(rs, sentBitmap, Allocation.MipmapControl.MIPMAP_NONE,
                    Allocation.USAGE_SCRIPT);
            final Allocation output = Allocation.createTyped(rs, input.getType());
            final ScriptIntrinsicBlur script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
            script.setRadius(radius /* e.g. 3.f */);
            script.setInput(input);
            script.forEach(output);
            output.copyTo(bitmap);
            return bitmap;
        }

        // Stack Blur v1.0 from
        // http://www.quasimondo.com/StackBlurForCanvas/StackBlurDemo.html
        //
        // Java Author: Mario Klingemann <mario at quasimondo.com>
        // http://incubator.quasimondo.com
        // created Feburary 29, 2004
        // Android port : Yahel Bouaziz <yahel at kayenko.com>
        // http://www.kayenko.com
        // ported april 5th, 2012

        // This is a compromise between Gaussian Blur and Box blur
        // It creates much better looking blurs than Box Blur, but is
        // 7x faster than my Gaussian Blur implementation.
        //
        // I called it Stack Blur because this describes best how this
        // filter works internally: it creates a kind of moving stack
        // of colors whilst scanning through the image. Thereby it
        // just has to add one new block of color to the right side
        // of the stack and remove the leftmost color. The remaining
        // colors on the topmost layer of the stack are either added on
        // or reduced by one, depending on if they are on the right or
        // on the left side of the stack.
        //
        // If you are using this algorithm in your code please add
        // the following line:
        //
        // Stack Blur Algorithm by Mario Klingemann <mario@quasimondo.com>

        Bitmap bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);

        if (radius < 1) {
            return (null);
        }

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        int[] pix = new int[w * h];
        Log.e("pix", w + " " + h + " " + pix.length);
        bitmap.getPixels(pix, 0, w, 0, 0, w, h);

        int wm = w - 1;
        int hm = h - 1;
        int wh = w * h;
        int div = radius + radius + 1;

        int r[] = new int[wh];
        int g[] = new int[wh];
        int b[] = new int[wh];
        int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
        int vmin[] = new int[Math.max(w, h)];

        int divsum = (div + 1) >> 1;
        divsum *= divsum;
        int dv[] = new int[256 * divsum];
        for (i = 0; i < 256 * divsum; i++) {
            dv[i] = (i / divsum);
        }

        yw = yi = 0;

        int[][] stack = new int[div][3];
        int stackpointer;
        int stackstart;
        int[] sir;
        int rbs;
        int r1 = radius + 1;
        int routsum, goutsum, boutsum;
        int rinsum, ginsum, binsum;

        for (y = 0; y < h; y++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            for (i = -radius; i <= radius; i++) {
                p = pix[yi + Math.min(wm, Math.max(i, 0))];
                sir = stack[i + radius];
                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);
                rbs = r1 - Math.abs(i);
                rsum += sir[0] * rbs;
                gsum += sir[1] * rbs;
                bsum += sir[2] * rbs;
                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }
            }
            stackpointer = radius;

            for (x = 0; x < w; x++) {

                r[yi] = dv[rsum];
                g[yi] = dv[gsum];
                b[yi] = dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (y == 0) {
                    vmin[x] = Math.min(x + radius + 1, wm);
                }
                p = pix[yw + vmin[x]];

                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[(stackpointer) % div];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi++;
            }
            yw += w;
        }
        for (x = 0; x < w; x++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            yp = -radius * w;
            for (i = -radius; i <= radius; i++) {
                yi = Math.max(0, yp) + x;

                sir = stack[i + radius];

                sir[0] = r[yi];
                sir[1] = g[yi];
                sir[2] = b[yi];

                rbs = r1 - Math.abs(i);

                rsum += r[yi] * rbs;
                gsum += g[yi] * rbs;
                bsum += b[yi] * rbs;

                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }

                if (i < hm) {
                    yp += w;
                }
            }
            yi = x;
            stackpointer = radius;
            for (y = 0; y < h; y++) {
                // Preserve alpha channel: ( 0xff000000 & pix[yi] )
                pix[yi] = (0xff000000 & pix[yi]) | (dv[rsum] << 16) | (dv[gsum] << 8) | dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (x == 0) {
                    vmin[y] = Math.min(y + r1, hm) * w;
                }
                p = x + vmin[y];

                sir[0] = r[p];
                sir[1] = g[p];
                sir[2] = b[p];

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[stackpointer];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi += w;
            }
        }

        Log.e("pix", w + " " + h + " " + pix.length);
        bitmap.setPixels(pix, 0, w, 0, 0, w, h);
        return (bitmap);
    }
}
