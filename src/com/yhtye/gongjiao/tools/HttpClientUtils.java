package com.yhtye.gongjiao.tools;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.util.Log;

public class HttpClientUtils {
    /**
     * post请求
     * 
     * @param url 请求url
     * @param paramsMap  请求参数
     * @return
     */
    public static String postResponse(String url, Map<String, Object> paramsMap) {
        try {
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httpost = new HttpPost(url);
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            if (paramsMap != null) {
                for (String key : paramsMap.keySet()) {
                    params.add(new BasicNameValuePair(key,
                            paramsMap.get(key).toString()));
                }
            }
            httpclient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 60000); 
            
            httpost.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
            HttpResponse response = httpclient.execute(httpost);
            HttpEntity entity = response.getEntity();
            String htmlStr = null;
            if (entity != null) {
                entity = new BufferedHttpEntity(entity);
                htmlStr = EntityUtils.toString(entity);
                entity.consumeContent();
            }
            return htmlStr;

        } catch (Exception e) {
            Log.i(url, e.getMessage());
        }
        return null;
    }
    
    /**
     * get请求
     * 
     * @param url 请求url
     * @return
     */
    public static String getResponse(String url) {
        try {
            HttpClient httpclient = new DefaultHttpClient();
            URI uri = new URI(url);
            HttpGet httpGet = new HttpGet(uri);
            httpclient.getParams().setParameter(ClientPNames.COOKIE_POLICY,
                    CookiePolicy.BROWSER_COMPATIBILITY);
            httpclient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 60000); 
            
            httpGet.setHeader("Referer", "http://www.bjbus.com");
            HttpResponse response = httpclient.execute(httpGet);
            if (response == null) {
                return null;
            }
            HttpEntity entity = response.getEntity();
            String htmlStr = null;
            if (entity != null) {
                entity = new BufferedHttpEntity(entity);
                htmlStr = EntityUtils.toString(entity, "UTF-8");
                entity.consumeContent();
            }
            
            return htmlStr;

        } catch (Exception e) {
            Log.i(url, e.getMessage());
        }
        return null;
    }
}
