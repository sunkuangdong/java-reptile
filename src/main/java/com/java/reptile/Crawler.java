package com.java.reptile;

import java.sql.*;
import java.util.*;

import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.stream.Collectors;

public class Crawler {
    MyBatisCrawlerDao dao = new MyBatisCrawlerDao();
    ;

    public void run() throws SQLException {
        String link;
        while ((link = dao.getNextLinkThenDelete()) != null) {
            // 从数据库中判断 是否正在处理这个连接池
            if (dao.isLinkProcessed(link)) {
                continue;
            }
            if (isInterestingLink(link)) {
                // 我们需要处理
                startHttp(link);
            }
        }
    }

    public static void main(String[] args) throws SQLException {
        new Crawler().run();
    }

    public void startHttp(String link) {
        try {
            // 使用 Jsoup 解析 html 字符串
            Document doc = httpGetAndParseHtml(link);
            parseUrlsFrompageAndStoreIntoDatabase(doc);
            // 假如这是一个新闻的页面，就存入数据库，不然不管
            storeIntoDatabaseIfItIsNwesPage(doc, link);
            // 处理完成之后放进数据库中
            dao.insertProcessedLink(link);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void parseUrlsFrompageAndStoreIntoDatabase(Document doc) {
        // 获取想要的结果
        for (Element aTag : doc.select("a")) {
            String href = aTag.attr("href");
            dao.insertLinkToBeProcessed(href);
        }
    }


    private void storeIntoDatabaseIfItIsNwesPage(Document doc, String link) throws SQLException {
        ArrayList<Element> articleTags = doc.select("article");
        if (!articleTags.isEmpty()) {
            for (Element articleTag : articleTags) {
                // 拿到了 title
                String title = articleTags.get(0).child(0).text();
                ArrayList<Element> elements = articleTag.select("p");
                String content = elements.stream().map(Element::text).collect(Collectors.joining("\n"));
                System.out.println(title);
                System.out.println(content);
                dao.insertNewsIntoDatabase(link, title, content);
            }
        }
    }

    // 给我一个 link，我转换成 String html
    private static Document httpGetAndParseHtml(String link) throws IOException {
        final CloseableHttpClient httpClient = HttpClients.createDefault();
        if (link.startsWith("//")) {
            link = "https:" + link;
        }
        HttpGet httpGet = new HttpGet(link);
        httpGet.addHeader("user-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/111.0.0.0 Safari/537.36");
        try (
                CloseableHttpResponse response = httpClient.execute(httpGet)
        ) {
            HttpEntity responseGetEntity = response.getEntity();
            String html = EntityUtils.toString(responseGetEntity);
            return Jsoup.parse(html);
        }
    }

    public static boolean isInterestingLink(String link) {
        return (isNewsPage(link) || isIndexPage(link)) && isNotLingPage(link);
    }

    private static boolean isIndexPage(String link) {
        return "https://sina.cn".equals(link);
    }

    private static boolean isNewsPage(String link) {
        return link.contains("news.sina.cn");
    }

    private static boolean isNotLingPage(String link) {
        return !link.contains("passport.sina.cn");
    }
}
