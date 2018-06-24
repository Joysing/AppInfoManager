package cn.appinfo.tools;

import java.util.concurrent.TimeUnit;

import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * Created by gumuyun on 2018/6/8.
 */
public class OkHttpUtil {

    /**
     * 发送HTTP请求
     * @param url 请求的地址
     * @param requestBody 请求头
     * @param callback 回调函数
     */
    public static void sendOkHttpRequest(String url, RequestBody requestBody, Callback callback){
        OkHttpClient client=new OkHttpClient.Builder()
                .connectTimeout(Constants.CONNECTION_TIMEOUT, TimeUnit.SECONDS)//连接超时
                .readTimeout(Constants.READ_TIMEOUT,TimeUnit.SECONDS)//读取数据超时
                .build();
        Request.Builder builder = new Request.Builder().url(url);
        if (requestBody!=null){
            builder.post(requestBody);
        }
        Request request = builder.build();
        client.newCall(request).enqueue(callback);
    }
}
