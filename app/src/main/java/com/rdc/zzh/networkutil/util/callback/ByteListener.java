package com.rdc.zzh.networkutil.util.callback;

/**
 * Created by ZengZeHong on 2016/9/22.
 * 下载字节接口
 */

public interface ByteListener {
    void setBytesSuccess(byte[] bytes);

    void setBytesFail(String fail);
}
