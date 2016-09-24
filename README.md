# NetworkUtil
简单封装HttpUrlConnection
* 封装了get和post方法的请求
* 设置了Cookie的自动保存
* 获取过程中可以选择是否带进度监听

eg:

get  

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

post

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
