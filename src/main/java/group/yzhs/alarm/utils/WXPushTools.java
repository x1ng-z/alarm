package group.yzhs.alarm.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * create by yjq on 2019-9-21
 */
@Slf4j
@UtilityClass
public class WXPushTools {

    public    <T> T postForEntity(String url, Object myclass, Class<T> returnClass) {
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        HttpPost posturl = new HttpPost(url);
        RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(15 * 1000)
                .setConnectTimeout(10 * 1000)
                .setConnectionRequestTimeout(5 * 1000)
                .build();
        posturl.setConfig(requestConfig);
        String jsonSting = JSON.toJSONString(myclass);
        StringEntity entity = new StringEntity(jsonSting, "UTF-8");
        posturl.setEntity(entity);
        posturl.setHeader("Content-Type", "application/json;charset=utf8");
        // 响应模型
        CloseableHttpResponse response = null;
        try {
            // 由客户端执行(发送)Post请求
            response = httpClient.execute(posturl);
            // 从响应模型中获取响应实体
            HttpEntity responseEntity = response.getEntity();
            if (responseEntity != null) {
                String stringValue=EntityUtils.toString(responseEntity);
                return JSONObject.parseObject(stringValue,returnClass);
            }
        } catch (Exception e) {
            log.error(e.getMessage(),e);
        } finally {
            try {
                // 释放资源
                if (httpClient != null) {
                    httpClient.close();
                }
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public  void sendwx(String url, String msg, String department) {
        Map<String, String> map = new HashMap<>();
        map.put("message", msg);
        map.put("department", department);
        log.info("微信推送内容：{}", map);
        String result = WXPushTools.doget(url, map);
        log.debug("微信推送结果：" + result);
    }

    public  String doget(String url, Map<String, String> map) {
        String result = null;
        CloseableHttpClient httpclient = HttpClients.createDefault();
        // 创建参数队列
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            String mapKey = entry.getKey();
            String mapValue = entry.getValue();
            params.add(new BasicNameValuePair(mapKey, mapValue));
        }
        try {
            //参数转换为字符串
            String paramsStr = EntityUtils.toString(new UrlEncodedFormEntity(params, "UTF-8"));
            url = url + "?" + paramsStr;
            // 创建httpget.
            HttpGet httpget = new HttpGet(url);
            // 执行get请求.
            CloseableHttpResponse response = httpclient.execute(httpget);
            try {
                // 获取响应实体
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    result = EntityUtils.toString(entity);
                }
            } finally {
                response.close();
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            // 关闭连接,释放资源
            try {
                httpclient.close();
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
        return result;
    }

    public  String dopost_file(String url, Map<String, String> map, String filepath) {
//        if (true) {
//            return "";
//        }
        String result = null;
        CloseableHttpClient httpclient = HttpClients.createDefault();
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            String mapKey = entry.getKey();
            String mapValue = entry.getValue();
            params.add(new BasicNameValuePair(mapKey, mapValue));
        }
        try {
            String paramsStr = EntityUtils.toString(new UrlEncodedFormEntity(params, "UTF-8"));
            url = url + "?" + paramsStr;
            HttpPost httppost = new HttpPost(url);
            FileBody fileBody = new FileBody(new File(filepath));
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            builder.addPart("image", fileBody);//文件参数
            HttpEntity entity = builder.build();
            httppost.setEntity(entity);
            CloseableHttpResponse response = httpclient.execute(httppost);
            try {
                HttpEntity resEntity = response.getEntity();
                if (resEntity != null) {
                    result = EntityUtils.toString(resEntity, "UTF-8");
                }
            } finally {
                response.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                httpclient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public  String doPostjson(String url, Object myclass, Map<String, String> map) {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        if (null != map) {
            for (Map.Entry<String, String> entry : map.entrySet()) {
                String mapKey = entry.getKey();
                String mapValue = entry.getValue();
                params.add(new BasicNameValuePair(mapKey, mapValue));
            }
        }
        String paramsStr = null;
        try {
            paramsStr = EntityUtils.toString(new UrlEncodedFormEntity(params, "UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        url = url + "?" + paramsStr;
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        HttpPost posturl = new HttpPost(url);

        String jsonSting = JSON.toJSONString(myclass);
        StringEntity entity = new StringEntity(jsonSting, "UTF-8");
        posturl.setEntity(entity);
        posturl.setHeader("Content-Type", "application/json;charset=utf8");
        // 响应模型
        CloseableHttpResponse response = null;
        try {
            // 由客户端执行(发送)Post请求
            response = httpClient.execute(posturl);
            // 从响应模型中获取响应实体
            HttpEntity responseEntity = response.getEntity();
            if (responseEntity != null) {
                return EntityUtils.toString(responseEntity);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                // 释放资源
                if (httpClient != null) {
                    httpClient.close();
                }
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
