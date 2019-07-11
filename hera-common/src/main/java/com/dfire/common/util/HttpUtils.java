package com.dfire.common.util;

import com.dfire.logs.ErrorLog;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

/**
 * desc:
 *
 * @author scx
 * @create 2019/04/26
 */
public class HttpUtils {


    public static String doGet(String urlStr, List<BasicHeader> headers) {
        URI uri = null;
        try {
            URL url = new URL(urlStr);
            uri = new URI(url.getProtocol(), url.getHost() + ":" + url.getPort(), url.getPath(), url.getQuery(), null);
        } catch (URISyntaxException | MalformedURLException e) {
            ErrorLog.error("url格式错误", e);

        }

        HttpGet httpGet = new HttpGet(uri);
        if (headers != null && headers.size() > 0) {
            for (BasicHeader header : headers) {
                httpGet.setHeader(header);
            }
        }
        return doExecute(urlStr, httpGet);

    }

    private static String doExecute(String url, HttpUriRequest request) {
        try {
            HttpClient httpClient = HttpClients.createMinimal();
            HttpResponse response = httpClient.execute(request);
            int code = response.getStatusLine().getStatusCode();
            if (code == 200) {
                return EntityUtils.toString(response.getEntity());
            } else {
                ErrorLog.error(url + " http请求异常:" + response.getStatusLine().getStatusCode() + response.getEntity().toString());
            }
        } catch (IOException e) {
            ErrorLog.error("发送http请求失败", e);
        }
        return null;
    }


}