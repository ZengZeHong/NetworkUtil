package com.rdc.zzh.networkutil;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.rdc.zzh.networkutil.util.NetworkUtil;
import com.rdc.zzh.networkutil.util.PostBody;
import com.rdc.zzh.networkutil.util.callback.ByteListener;
import com.rdc.zzh.networkutil.util.callback.ProgressListener;
import com.rdc.zzh.networkutil.util.callback.ResultListener;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private Bitmap bitmap;
    private ImageView imageView;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0x123) {
                if (bitmap != null) {
                    imageView.setImageBitmap(bitmap);
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = (ImageView) findViewById(R.id.imageView);
    }

    public void get(View view) {
        NetworkUtil.getInstance().get("http://www.baidu.com", new ResultListener() {
            @Override
            public void onResultSuccess(String success) {
                Log.e(TAG, "onResultSuccess: " + success);
            }

            @Override
            public void onResultFail(String fail) {
                Log.e(TAG, "onResultFail: " + fail);
            }
        });
    }

    public void post(View view) {
        PostBody body = new PostBody.Builder().addParams("userId", "1").addParams("password", "111111").build();
        NetworkUtil.getInstance().post("http://115.28.64.167/spg/user/login", body, new ResultListener() {
            @Override
            public void onResultSuccess(String success) {
                Log.e(TAG, "onResultSuccess: " + success);
            }

            @Override
            public void onResultFail(String fail) {
                Log.e(TAG, "onResultFail: " + fail);
            }
        });
    }

    public void image(View view) {
        NetworkUtil.getInstance().get("http://c.hiphotos.baidu.com/image/h%3D200/sign=a280d7a0ed24b899c13c7e385e071d59/2934349b033b5bb54352dd5e32d3d539b700bc8d.jpg", new ByteListener() {
            @Override
            public void setBytesSuccess(byte[] bytes) {
                bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                handler.sendEmptyMessage(0x123);
            }

            @Override
            public void setBytesFail(String fail) {
                Log.e(TAG, "setBytesFail: " + fail);
            }
            //进度可选
        }, new ProgressListener() {
            @Override
            public void onUpdate(long bytesRead, long contentLength, boolean done) {
                Log.e(TAG, "onUpdate: " + bytesRead + ">>" + contentLength + ">>" + done);
            }
        });
    }
}
