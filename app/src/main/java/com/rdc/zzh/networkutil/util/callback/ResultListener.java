package com.rdc.zzh.networkutil.util.callback;

/**
 * Created by ZengZeHong on 2016/9/22.
 * 下载回调接口
 */

public interface ResultListener {
    void onResultSuccess(String success);

    void onResultFail(String fail);
}
