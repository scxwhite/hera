package com.dfire.exception;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * Created by xiaosuda on 2018/10/29.
 */
public class Vote {


    private static Random random = new Random();

    private static Integer solt;

    private static Integer cnt = 100;

    public static void main(String[] args) throws URISyntaxException, IOException {


        for (int i = 0; i < cnt; i++) {
            vote();
        }
    }


    public static void vote() throws URISyntaxException, IOException {
        CloseableHttpClient clients = HttpClients.createDefault();
        URIBuilder uriBuilder = new URIBuilder("http://support.finance.sina.com.cn/service/api/openapi.php/VoteService.setVote");
        List<NameValuePair> list = new LinkedList<>();
        long millis = System.currentTimeMillis();
        list.add(new BasicNameValuePair("appid", "chaoliurenwu18"));
        list.add(new BasicNameValuePair("id", "1012"));
        list.add(new BasicNameValuePair("wxflag", "0"));
        list.add(new BasicNameValuePair("uuid", UUID.randomUUID().toString().replace("-", "")));
        list.add(new BasicNameValuePair("callback", "jsonp_" + millis));
        list.add(new BasicNameValuePair("_", String.valueOf(millis)));
        uriBuilder.setParameters(list);
        HttpGet get = new HttpGet(uriBuilder.build());
        get.addHeader("Refer", "http://finance.sina.cn/zt_d/2018sdcljjrw?from=singlemessage&isappinstalled=0");
        get.addHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100 Safari/537.36");
        HttpHost proxy = new HttpHost("116.226.217.54", 9999);
        RequestConfig requestConfig = RequestConfig.custom().setProxy(proxy).build();
        get.setConfig(requestConfig);
        CloseableHttpResponse response = clients.execute(get);
        HttpEntity entity = response.getEntity();
        System.out.println(EntityUtils.toString(entity, "UTF-8"));
    }

}
