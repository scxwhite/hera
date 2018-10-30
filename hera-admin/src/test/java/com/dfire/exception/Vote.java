package com.dfire.exception;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
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

    private static long millis = System.currentTimeMillis();

    private static Random random = new Random();

    private static Integer solt;

    private static Integer cnt = 200;

    public static void main(String[] args) throws URISyntaxException, IOException {

        solt = cnt * 1200;

        millis -= solt * 1000;

        for (int i = 0; i < cnt; i++) {
            vote(millis + random.nextInt(solt));
        }
    }


    public static void vote(long time) throws URISyntaxException, IOException {
        CloseableHttpClient clients = HttpClients.createDefault();
        URIBuilder uriBuilder = new URIBuilder("http://support.finance.sina.com.cn/service/api/openapi.php/VoteService.setVote");
        List<NameValuePair> list = new LinkedList<>();
        list.add(new BasicNameValuePair("appid", "chaoliurenwu18"));
        list.add(new BasicNameValuePair("id", "3018"));
        list.add(new BasicNameValuePair("wxflag", "0"));
        list.add(new BasicNameValuePair("uuid", UUID.randomUUID().toString().replace("-", "")));
        list.add(new BasicNameValuePair("callback", "jsonp_" + time));
        list.add(new BasicNameValuePair("_", String.valueOf(time)));
        uriBuilder.setParameters(list);
        HttpGet get = new HttpGet(uriBuilder.build());
        get.addHeader("Refer", "http://finance.sina.cn/zt_d/2018sdcljjrw?from=singlemessage&isappinstalled=0");
        get.addHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100 Safari/537.36");
        CloseableHttpResponse response = clients.execute(get);
        HttpEntity entity = response.getEntity();
        System.out.println(EntityUtils.toString(entity, "UTF-8"));
    }

}
