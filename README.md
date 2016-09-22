# AlxPicassoProgress
为Android Picasso库添加了图片下载进度实时显示的功能

效果展示<br>：

新功能：仿Instagram效果模糊图占位

![demo](https://github.com/AlexZhuo/AlxPicassoProgress/blob/master/GifDemo/demo3.gif)
![demo](https://github.com/AlexZhuo/AlxPicassoProgress/blob/master/GifDemo/demo4.gif)

普通用法，使用默认图片占位
![demo](https://github.com/AlexZhuo/AlxPicassoProgress/blob/master/GifDemo/demo1.gif)
![demo](https://github.com/AlexZhuo/AlxPicassoProgress/blob/master/GifDemo/demo2.gif)

使用方法：

如果想添加模糊图占位功能

AlxPicassoUtils.displayImageProgress(url,imageView,progressWheel,textView,base64Str);  

参数分别为：图片url地址，ImageView控件，圆形进度条控件，进度显示TextView,经过Base64压缩的图片缩略图

AlxPicassoUtils.displayImageProgress(url,imageView,progressWheel,textView);  

参数分别为：图片url地址，ImageView控件，圆形进度条控件，进度显示TextView

相关细节和用法、思路请看我的博客：http://blog.csdn.net/lvshaorong/article/details/52606943
