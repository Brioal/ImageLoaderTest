package com.brioal.imageloadertest.model;

import android.content.Context;

import cn.bmob.v3.BmobObject;
import cn.bmob.v3.datatype.BmobFile;

/**
 * Created by Brioal on 2016/4/6.
 */
public class MainHeadItem extends BmobObject {

    private BmobFile mImage;
    private String mDesc;
    private String mImageUrl;
    private Context context ;

    public MainHeadItem(Context context) {
        this.context = context;
    }

    public MainHeadItem(Context context, String mImageUrl, String mDesc) {
        this.context = context;
        this.mImageUrl = mImageUrl;
        this.mDesc = mDesc;
    }

    public void setmDesc(String mDesc) {
        this.mDesc = mDesc;
    }

    public void setmImage(BmobFile mImage) {
        this.mImage = mImage;
    }

    public String getmImageUrl() {
        if (mImageUrl == null) {
            return mImage.getFileUrl(context);
        }
        return mImageUrl;
    }

    public MainHeadItem(String tableName) {
        super(tableName);
    }

    public BmobFile getmImage() {
        return mImage;
    }

    public String getmDesc() {
        return mDesc;
    }
}
