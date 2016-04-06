package com.brioal.imageloadertest;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;
import android.widget.TextView;

import com.brioal.imageloadertest.database.DataBaseHelper;
import com.brioal.imageloadertest.model.MainHeadItem;
import com.brioal.imageloadertest.util.Constants;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.FindListener;

public class MainActivity extends AppCompatActivity {

    private static final int INITSUCCESS = 1;
    @Bind(R.id.main_image)
    ImageView mainImage;
    @Bind(R.id.main_text)
    TextView mainText;

    private DataBaseHelper helper;

    private MainHeadItem item;
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            initData();
        }
    };

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == INITSUCCESS) {
                initView();
            }
        }
    };

    private void initView() {
        ImageLoader.getInstance().displayImage(item.getmImageUrl(), mainImage, Constants.options);
        mainText.setText(item.getmDesc());

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        Bmob.initialize(this, Constants.appID);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        helper = new DataBaseHelper(this, "Test.db3", null, 1);
        new Thread(runnable).start();

    }

    public void initData() {
        if (isNetworkConnected(MainActivity.this)) {
            BmobQuery<MainHeadItem> query = new BmobQuery<>();
            query.findObjects(this, new FindListener<MainHeadItem>() {
                @Override
                public void onSuccess(List<MainHeadItem> list) {
                    item = new MainHeadItem(MainActivity.this);
                    item.setmImage(list.get(0).getmImage());
//                    item.setmDesc(list.get(0).getmDesc());
                    handler.sendEmptyMessage(INITSUCCESS);
                    SaveData();
                }

                @Override
                public void onError(int i, String s) {

                }
            });
        } else {
            ReadDateBase();
        }


    }

    //保存本地数据
    private void SaveData() {
        SQLiteDatabase db = helper.getReadableDatabase();
        db.execSQL("insert into MainHeadItem values(null,?,?)", new String[]{item.getmImage().getFileUrl(MainActivity.this), item.getmDesc()});
    }

    //    读取本地数据
    private void ReadDateBase() {
        Cursor cursor = helper.getReadableDatabase().rawQuery("select * from MainHeadItem ", null);//TODO记录
        while (cursor.moveToNext()) {
            item = new MainHeadItem(MainActivity.this, cursor.getString(1), cursor.getString(2));

        }

        handler.sendEmptyMessage(INITSUCCESS);

    }

    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
