package com.rdc.zzh.networkutil.util;

import android.util.Log;

import com.rdc.zzh.networkutil.util.callback.ByteListener;
import com.rdc.zzh.networkutil.util.callback.Callback;
import com.rdc.zzh.networkutil.util.callback.ProgressListener;
import com.rdc.zzh.networkutil.util.callback.ResultListener;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ZengZeHong on 2016/9/22.
 * 下载接口的实现类
 */

public class NetworkConnection implements Callback {
    private static final String TAG = "NetworkConnection";
    private static final String TAG_GET_COOKIE = "Set-Cookie";
    private static final String TAG_SET_COOKIE = "Cookie";
    private static final String ERROR_EXCEPTION = "Exception Error";
    private static final String ERROR_NETWORK = "网络连接失败，请稍后再试";
    private static final String ERROR_SERVICE = "服务器异常，请稍后再试";
    private static final String ERROR_OVER_TIME = "网络连接超时，请检查您的网络";
    //只列出一些常用的功能
    private int connectTimeOut;
    private int readTimeOut;
    private boolean doInput;
    private boolean doOutput;
    private boolean useCaches;
    private List<String> values = new ArrayList<>();
    //请求头
    private List<String> keys = new ArrayList<>();
    //Cookie的管理
    private CookieJar cookieJar;
    private ProgressListener progressListener;

    public NetworkConnection(int connectTimeOut, int readTimeOut, boolean doInput, boolean doOutput, boolean useCaches, List<String> keys, List<String> values, CookieJar cookieJar) {
        this.connectTimeOut = connectTimeOut;
        this.readTimeOut = readTimeOut;
        this.doInput = doInput;
        this.doOutput = doOutput;
        this.useCaches = useCaches;
        this.keys = keys;
        this.values = values;
        this.cookieJar = cookieJar;
    }

    /**
     * 创建一个通用的HttpURLConnection
     *
     * @param imgUrl 请求地址
     * @param method Get,Post方法
     * @return
     * @throws IOException
     */
    private HttpURLConnection createConnection(String imgUrl, String method) throws IOException {
        URL url = new URL(imgUrl);
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setRequestMethod(method);
        if (connectTimeOut != -1)
            httpURLConnection.setConnectTimeout(connectTimeOut);
        if (readTimeOut != -1)
            httpURLConnection.setReadTimeout(readTimeOut);
        //默认要true,false,false
        httpURLConnection.setDoInput(doInput);
        httpURLConnection.setDoOutput(doOutput);
        httpURLConnection.setUseCaches(useCaches);
        //添加请求头
        for (int i = 0; i < keys.size(); i++) {
            httpURLConnection.setRequestProperty(keys.get(i), values.get(i));
        }
        return httpURLConnection;
    }

    /**
     * 下载过程中的进度监听
     *
     * @param progressListener
     */
    public void setProgressListener(ProgressListener progressListener) {
        this.progressListener = progressListener;
    }

    /**
     * 普通的文本请求
     *
     * @param url            请求地址
     * @param resultListener 接口监听
     */
    @Override
    public void get(final String url, final ResultListener resultListener) {

        new Thread() {
            @Override
            public void run() {
                super.run();
                InputStream in = null;
                HttpURLConnection httpURLConnection = null;
                try {
                    httpURLConnection = createConnection(url, "GET");
                    //添加Cookie
                    getCookie(url, httpURLConnection);
                    httpURLConnection.connect();
                    Log.e(TAG, "get: getResponseCode " + httpURLConnection.getResponseCode());
                    Log.e(TAG, "get: getResponseMessage " + httpURLConnection.getResponseMessage());
                    if (httpURLConnection.getResponseCode() == 200) {
                        in = httpURLConnection.getInputStream();
                        BufferedReader br = new BufferedReader(new InputStreamReader(in));
                        String inputLine;
                        StringBuffer sb = new StringBuffer();
                        while ((inputLine = br.readLine()) != null) {
                            sb.append(inputLine).append("\n");
                        }
                        //保存Cookie
                        setCookie(url, httpURLConnection);
                        resultListener.onResultSuccess(sb.toString());
                    } else
                        resultListener.onResultFail(httpURLConnection.getResponseCode() + ">>" + httpURLConnection.getResponseMessage());
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    resultListener.onResultFail(ERROR_EXCEPTION);
                    Log.e(TAG, "throw MalformedURLException");
                } catch (IOException e) {
                    e.printStackTrace();
                    resultListener.onResultFail(ERROR_NETWORK);
                    Log.e(TAG, "throw IOException");
                } finally {
                    try {
                        if (in != null)
                            in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (httpURLConnection != null)
                        httpURLConnection.disconnect();
                }
            }
        }.start();
    }


    /**
     * 请求数据流，比如获取图片，以数组形式传递
     *
     * @param url          请求地址
     * @param byteListener 数据监听
     */
    @Override
    public void get(final String url, final ByteListener byteListener) {

        new Thread() {
            @Override
            public void run() {
                super.run();
                InputStream in = null;
                HttpURLConnection httpURLConnection = null;
                try {
                    httpURLConnection = createConnection(url, "GET");
                    getCookie(url, httpURLConnection);
                    httpURLConnection.connect();
                    Log.e(TAG, "get: getResponseCode " + httpURLConnection.getResponseCode());
                    Log.e(TAG, "get: getResponseMessage " + httpURLConnection.getResponseMessage());
                    Log.e(TAG, "get: getConnectLenght " + httpURLConnection.getContentLength());
                    //下载成功
                    if (httpURLConnection.getResponseCode() == 200) {
                        in = httpURLConnection.getInputStream();
                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        byte[] buff = new byte[1024];
                        int len;
                        //初始化进度
                        if (progressListener != null)
                            progressListener.onUpdate(0, httpURLConnection.getContentLength(), false);
                        while ((len = in.read(buff)) != -1) {
                            out.write(buff, 0, len);
                            //下载过程中的进度
                            if (progressListener != null)
                                progressListener.onUpdate(out.size(), httpURLConnection.getContentLength(), false);
                        }
                        //下载完成
                        if (progressListener != null)
                            progressListener.onUpdate(out.size(), httpURLConnection.getContentLength(), true);

                        out.flush();
                        out.close();
                        //保存Cookie
                        setCookie(url, httpURLConnection);
                        byteListener.setBytesSuccess(out.toByteArray());
                    } else
                        //下载失败
                        byteListener.setBytesFail(httpURLConnection.getResponseCode() + ">>" + httpURLConnection.getResponseMessage());
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e(TAG, "IOException 一般是断网的情况去请求");
                    byteListener.setBytesFail(ERROR_NETWORK);
                } finally {
                    try {
                        if (in != null)
                            in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                        byteListener.setBytesFail(ERROR_EXCEPTION);
                    }
                    if (httpURLConnection != null)
                        httpURLConnection.disconnect();
                }
            }
        }.start();
    }

    /**
     * 普通post请求，只支持存文本
     *
     * @param url            请求地址
     * @param postBody       请求参数体
     * @param resultListener 返回监听
     */
    @Override
    public void post(final String url, final PostBody postBody, final ResultListener resultListener) {

        new Thread() {
            @Override
            public void run() {
                super.run();
                InputStream in = null;
                HttpURLConnection httpURLConnection = null;
                try {
                    httpURLConnection = createConnection(url, "POST");
                    httpURLConnection.setChunkedStreamingMode(0);
                    //Cookie处理
                    getCookie(url, httpURLConnection);
                    Log.e(TAG, "run: size" + postBody.size());
                    if (postBody.size() != 0) {
                        //写入请求参数
                        OutputStream out = httpURLConnection.getOutputStream();
                        StringBuffer sbParams = new StringBuffer();
                        sbParams.append(postBody.getKeys().get(0) + "=").append(postBody.getValues().get(0));
                        for (int i = 1; i < postBody.size(); i++) {
                            sbParams.append("&" + postBody.getKeys().get(i) + "=").append(postBody.getValues().get(i));
                        }
                        out.write(sbParams.toString().getBytes());
                        out.flush();
                        out.close();
                    }

                    //Cookie的管理
                    setCookie(url, httpURLConnection);
                    Log.e(TAG, "post: getResponseCode " + httpURLConnection.getResponseCode());
                    Log.e(TAG, "post: getResponseMessage " + httpURLConnection.getResponseMessage());
                    //成功响应
                    if (httpURLConnection.getResponseCode() == 200) {
                        in = httpURLConnection.getInputStream();
                        BufferedReader br = new BufferedReader(new InputStreamReader(in));
                        String inputLine = "";
                        StringBuffer sb = new StringBuffer();
                        while ((inputLine = br.readLine()) != null) {
                            sb.append(inputLine).append("\n");
                        }
                        resultListener.onResultSuccess(sb.toString());
                    } else {
                        resultListener.onResultFail(httpURLConnection.getContentEncoding() + ">>" + httpURLConnection.getResponseMessage());
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    resultListener.onResultFail(ERROR_EXCEPTION);
                    Log.e(TAG, "throw MalformedURLException");
                } catch (IOException e) {
                    e.printStackTrace();
                    resultListener.onResultFail(ERROR_NETWORK);
                } finally {
                    try {
                        if (in != null)
                            in.close();
                    } catch (IOException e) {
                        resultListener.onResultFail(ERROR_EXCEPTION);
                        e.printStackTrace();
                    }
                    if (httpURLConnection != null)
                        httpURLConnection.disconnect();
                }
            }
        }.start();
    }

    /**
     * 请求数据流，比如获取图片，以数组形式传递
     *
     * @param url          请求地址
     * @param postBody     请求参数提
     * @param byteListener 返回监听
     */
    @Override
    public void post(final String url, final PostBody postBody, final ByteListener byteListener) {

        new Thread() {
            @Override
            public void run() {
                super.run();
                Log.e(TAG, "run: ");
                InputStream in = null;
                HttpURLConnection httpURLConnection = null;
                try {
                    httpURLConnection = createConnection(url, "POST");
                    httpURLConnection.setChunkedStreamingMode(0);
                    getCookie(url, httpURLConnection);
                    //写入请求参数
                    if (postBody.size() != 0) {
                        OutputStream out = httpURLConnection.getOutputStream();
                        StringBuffer sbParams = new StringBuffer();
                        sbParams.append(postBody.getKeys().get(0) + "=").append(postBody.getValues().get(0));
                        for (int i = 1; i < postBody.size(); i++) {
                            sbParams.append("&" + postBody.getKeys().get(i) + "=").append(postBody.getValues().get(i));
                        }
                        out.write(sbParams.toString().getBytes());
                        out.flush();
                        out.close();
                    }
                    //Cookie的管理
                    setCookie(url, httpURLConnection);
                    Log.e(TAG, "get: getResponseCode " + httpURLConnection.getResponseCode());
                    Log.e(TAG, "get: getResponseMessage " + httpURLConnection.getResponseMessage());
                    if (httpURLConnection.getResponseCode() == 200) {
                        in = httpURLConnection.getInputStream();
                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        byte[] buff = new byte[1024];
                        int len;
                        //初始化进度
                        if (progressListener != null)
                            progressListener.onUpdate(0, httpURLConnection.getContentLength(), false);
                        while ((len = in.read(buff)) != -1) {
                            out.write(buff, 0, len);
                            if (progressListener != null)
                                progressListener.onUpdate(out.size(), httpURLConnection.getContentLength(), false);
                        }
                        if (progressListener != null)
                            progressListener.onUpdate(out.size(), httpURLConnection.getContentLength(), true);

                        out.flush();
                        out.close();
                        byteListener.setBytesSuccess(out.toByteArray());
                    } else
                        //失败
                        byteListener.setBytesFail(httpURLConnection.getResponseCode() + ">>" + httpURLConnection.getResponseMessage());
                } catch (IOException e) {
                    e.printStackTrace();
                    byteListener.setBytesFail(ERROR_NETWORK);
                } finally {
                    try {
                        if (in != null)
                            in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                        byteListener.setBytesFail(ERROR_EXCEPTION);
                    }
                    if (httpURLConnection != null)
                        httpURLConnection.disconnect();
                }
            }
        }.start();
    }

    /**
     * 添加Cookie
     *
     * @param url
     * @param httpURLConnection
     */
    private void getCookie(String url, HttpURLConnection httpURLConnection) {
        if (!cookieJar.isCookieStoreNull(url)) {
            List<HttpCookie> lists = cookieJar.getCookies(url);
            if (lists != null && lists.size() != 0) {
                Log.e(TAG, "run: cookie " + lists.get(0).toString());
                //添加请求头
                httpURLConnection.setRequestProperty(TAG_SET_COOKIE, lists.get(0).toString());
            }
        }
    }

    /**
     * 保存Cookie
     *
     * @param url
     * @param httpURLConnection
     */
    private void setCookie(String url, HttpURLConnection httpURLConnection) {
        if (httpURLConnection.getHeaderField(TAG_GET_COOKIE) != null) {
            cookieJar.setCookies(url, httpURLConnection);
        }
    }

    public static final class Builder {
        //只列出一些常用的功能，默认如下
        private int connectTimeOut = -1;
        private int readTimeOut = -1;
        private boolean doInput = true;
        private boolean doOutput = false;
        private boolean useCaches = false;
        //请求头
        private final List<String> keys = new ArrayList<>();
        private final List<String> values = new ArrayList<>();
        private CookieJar cookieJar;

        public Builder() {
        }

        public Builder cookieJar(CookieJar cookieJar) {
            this.cookieJar = cookieJar;
            return this;
        }

        public Builder addHeader(String key, String value) {
            if (key == null || value == null)
                throw new NullPointerException();
            if (key != null && value != null) {
                keys.add(key);
                values.add(value);
            }
            return this;
        }

        public Builder connectTimeOut(int connectTimeOut) {
            this.connectTimeOut = connectTimeOut;
            return this;
        }

        public Builder readTimeOut(int readTimeOut) {
            this.readTimeOut = readTimeOut;
            return this;
        }

        public Builder doInput(boolean doInput) {
            this.doInput = doInput;
            return this;
        }

        public Builder doOutput(boolean doOutput) {
            this.doOutput = doOutput;
            return this;
        }

        public Builder useCaches(boolean useCaches) {
            this.useCaches = useCaches;
            return this;
        }

        public NetworkConnection build() {
            return new NetworkConnection(connectTimeOut, readTimeOut, doInput, doOutput, useCaches, keys, values, cookieJar);
        }
    }


}
