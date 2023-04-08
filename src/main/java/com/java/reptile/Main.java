package com.java.reptile;

import java.util.Arrays;

import org.apache.hc.client5.http.classic.methods.HttpGet;
//import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;
//import org.apache.hc.core5.http.message.BasicNameValuePair;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        final CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet("https://sina.cn");
        try (
                CloseableHttpResponse response = httpClient.execute(httpGet)
        ) {
            System.out.println(response.getEntity());
            HttpEntity responseGetEntity = response.getEntity();
            System.out.println(EntityUtils.toString(responseGetEntity));
        } catch (ParseException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
//        try (final CloseableHttpClient httpclient = HttpClients.createDefault()) {
//            final ClassicHttpRequest httpGet = ClassicRequestBuilder.get("https://sina.cn").build();
//            httpclient.execute(httpGet, response -> {
//                System.out.println(response.getCode() + " " + response.getReasonPhrase());
//                final HttpEntity entity1 = response.getEntity();
//                EntityUtils.consume(entity1);
//                System.out.println(entity1.toString());
//                return null;
//            });
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
    }
}
