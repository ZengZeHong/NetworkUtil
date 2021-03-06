package com.rdc.zzh.networkutil.util;

import android.util.Log;

import java.net.CookieManager;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

/**
 * Created by ZengZeHong on 2016/9/23.
 * 抽象CookieJar，二次封装CookieManager，用来保存Cookie
 */

public abstract class CookieJar extends CookieManager {
    private static final String TAG = "CookieJar";

    //设置Cookie
    abstract void setCookies(String url, HttpURLConnection httpURLConnection);

    //获取Cookie
    abstract List<HttpCookie> getCookies(String url);

    /**
     * 判断当前指定的url已经获取过Cookie
     *
     * @param url
     * @return
     */
    public boolean isCookieStoreNull(String url) {
        CookieStore cookieStore = getCookieStore();
        try {
            Log.e(TAG, "isCookieStoreNull: " + cookieStore.get(new URI(url)).size());
            if (cookieStore.get(new URI(url)).size() == 0)
                return true;
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return false;
    }
}
