#ImageLoader的基本使用
##ImageLoader是一款强大的图片加载缓存库,功能非常强大,算是几款图片加载开源库中相对比较成熟和稳定的.
###GitHub地址:[Android-Universal-Image-Loader](https://github.com/nostra13/Android-Universal-Image-Loader.git)
###下面就用一个小demo来演示一下这款功能强大的开源库的使用,首先看一下效果:
###有网的时候从服务器加载数据,无网的时候读取本地缓存显示,如下:
![这里写图片描述](http://img.my.csdn.net/uploads/201604/08/1460081080_9030.gif)

###要实现如上的效果主要有以下几个步骤:

 1. 配置`Application`,初始化imageloader的配置
 2. 设置全局配置`DisplayImageOptions`
 3. 获取图片URl,调用`ImageLoader.getInstance().displayImage(url, ImageView, UILApplication.options);`来显示图片
 4. 保存图片`url`到本地,无网的时候读取,即可与有网情况下一样显示图片


###`Application`的配置
####直接上代码,解释都在注释当中:(全局的Options也定义在此类中)
```

import android.annotation.TargetApi;
import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.StrictMode;

import com.brioal.imageloadertest.R;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

public class UILApplication extends Application {
	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	@SuppressWarnings("unused")
	@Override
	public void onCreate() {
		if (Constants.Config.DEVELOPER_MODE && Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
			StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll().penaltyDialog().build());
			StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectAll().penaltyDeath().build());
		}
		super.onCreate();
		initImageLoader(getApplicationContext());
	}

	public static DisplayImageOptions options  = new DisplayImageOptions.Builder()
			.showImageOnLoading(R.mipmap.ic_launcher) //设置图片在下载期间显示的图片
			.showImageForEmptyUri(R.mipmap.ic_launcher)//设置图片Uri为空或是错误的时候显示的图片
			.showImageOnFail(R.mipmap.ic_launcher)  //设置图片加载/解码过程中错误时候显示的图片
			.cacheInMemory(true)//设置下载的图片是否缓存在内存中
			.cacheOnDisc(true)//设置下载的图片是否缓存在SD卡中
			.considerExifParams(true)  //是否考虑JPEG图像EXIF参数（旋转，翻转）
			.imageScaleType(ImageScaleType.EXACTLY_STRETCHED)//设置图片以如何的编码方式显示
			.bitmapConfig(Bitmap.Config.RGB_565)//设置图片的解码类型//
			.resetViewBeforeLoading(true)//设置图片在下载前是否重置，复位
			.displayer(new RoundedBitmapDisplayer(20))//是否设置为圆角，弧度为多少
			.displayer(new FadeInBitmapDisplayer(100))//是否图片加载好后渐入的动画时间
			.build();//构建完成

	public static void initImageLoader(Context context) {
		ImageLoaderConfiguration.Builder config = new ImageLoaderConfiguration.Builder(context);
		config.threadPriority(Thread.NORM_PRIORITY - 2); // 设置线程数量,Thread.NORM_PRIORITY - 2能保证读取图片最大性能并且不影响主线程性能
		config.denyCacheImageMultipleSizesInMemory();
		config.diskCacheFileNameGenerator(new Md5FileNameGenerator()); // 设置磁盘保存缓存的加密方式
		config.diskCacheSize(50 * 1024 * 1024); // 设置磁盘缓存大小
		config.tasksProcessingOrder(QueueProcessingType.LIFO);
		config.writeDebugLogs(); // 图片加载情况打印到log
		ImageLoader.getInstance().init(config.build());
	}
}
```
###定义好了`Application`别忘了在`Manifest`中使用,即设置`application`的name属性,如下:
```
<application
        android:name=".util.UILApplication"
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:supportsRtl="true"
        android:theme="@style/AppTheme">
```

###从服务器读取图片地址的操作就忽略了,接下来直接调用方法显示图片:
```
//参数分别为图片Url地址 , 要显示的ImageView组件 ,全局Option配置
ImageLoader.getInstance().displayImage(item.getmImageUrl(), mainImage, UILApplication.options);
```

###无网读取缓存的方法:
####其他地方不用更改,需要更改的只是获取图片`Url`的方法,一般建议将图片`Url`地址保存到本地数据库,启动的时候判断是否存在网络,如果不存在即读取本地数据,当然最好的方式是默认读取本地数据,如果有网,则进行刷新操作,示例`Demo`中使用的是`Url`保存在本地数据库,那么就顺便理一下示例`Demo`的实现过程吧

####1.需要两个方法,`initData()`和`initView()`,前一个用于获取数据,后一个用于显示图片到界面,当然为了提升性能一般讲`initData()`放到子线程中进行,读取数据完成后通过`Handler`通知`UI`线程调用`IinitView()`方法,那么就还需要一个`Runnable`和`Handler`对象,下面贴一下代码:
```
private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            //调用数据更新
            initData();
        }
    };

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == INITSUCCESS) { //INITSUCCESS定义的全局变量,int
                initView();
            }
        }
    };
```
####`initData()`代码(本次Demo使用Bmob来获取服务器数据)
```
if (isNetworkConnected(MainActivity.this)) {
            BmobQuery<MainHeadItem> query = new BmobQuery<>();
            query.findObjects(this, new FindListener<MainHeadItem>() {
                @Override
                public void onSuccess(List<MainHeadItem> list) {
                    item = new MainHeadItem(MainActivity.this);
                    item.setmImage(list.get(0).getmImage());
                    item.setmDesc(list.get(0).getmDesc());
                    handler.sendEmptyMessage(INITSUCCESS);
                    SaveData();
                }

                @Override
                public void onError(int i, String s) {

                }
            });
        } else {
            ReadDateBase();
            handler.sendEmptyMessage(INITSUCCESS);
        }
```
####判断是否有网络的方法(需要在`Manifest`中申请权限):
```
//权限申请
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```
####具体方法
```
public static boolean isNetworkConnected(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
```
####读取本地数据库的方法:(本次测试只保存了三个属性,`id` ,`mDesc` , `MimageUrl`)
```
//    读取本地数据
    private void ReadDateBase() {
        Cursor cursor = helper.getReadableDatabase().rawQuery("select * from MainHeadItem ", null);//TODO记录
        while (cursor.moveToNext()) {
            item = new MainHeadItem(MainActivity.this, cursor.getString(1), cursor.getString(2));
        }
        handler.sendEmptyMessage(INITSUCCESS);

    }
```
####可以看到从网络加载数据之后就调用了保存本地数据,也一起贴出来:
```
//保存本地数据
    private void SaveData() {
        SQLiteDatabase db = helper.getReadableDatabase();
        db.execSQL("insert into MainHeadItem values(null,?,?)", new String[]{item.getmImage().getFileUrl(MainActivity.this), item.getmDesc()});
    }
```
####注:`helper`为自定义的` SQLiteOpenHelper`对象,只包含建表操作,就不贴了.

####经过如上的操作之后,即使无网,也能获取到跟有网状态下一样的数据,即实现了无网缓存

####本次只是使用了`ImageLoader`的一些基本的使用,在实际使用中可以根据项目的实际情况来进行配置.
