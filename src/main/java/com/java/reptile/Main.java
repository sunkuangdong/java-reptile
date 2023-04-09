package com.java.reptile;

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

public class Main {
    public static void main(String[] args) {
        Integer i = null;
        if (i == 1) {
            return;
        }
        // 待处理的连接池
        List<String> linkPool = new ArrayList<>();
        // 正在处理的连接池
        Set<String> processedLinks = new HashSet<>();
        linkPool.add("https://sina.cn");
        while (true) {
            // 是否存在在连接池中
            if (linkPool.isEmpty()) {
                break;
            }
            // 从最后一个拿最有效率，不需要挪动元素
            // remove 会返回拿掉的元素
            String link = linkPool.remove(linkPool.size() - 1);
            // 是否正在处理这个连接池
            if (processedLinks.contains(linkPool)) {
                continue;
            }
            // 是否是我们要处理的
            if (!link.contains(link)) {
                continue;
            }
            if (isInterestingLink(link)) {
                // 我们需要处理
                startHttp(link, linkPool, processedLinks);
            }
        }

    }

    public static void startHttp(String link, List<String> linkPool, Set<String> processedLinks) {
        try {
            // 使用 Jsoup 解析 html 字符串
            Document doc = httpGetAndParseHtml(link);
            // 获取想要的结果
            doc.select("a")
                    .stream()
                    .map(aTag -> aTag.attr("href"))
                    .forEach(linkPool::add);
            // 假如这是一个新闻的页面，就存入数据库，不然不管
            storeIntoDatabaseIfItIsNwesPage(doc);
            // 处理完成之后 重新放回连接池
            processedLinks.add(link);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void storeIntoDatabaseIfItIsNwesPage(Document doc) {
        ArrayList<Element> articleTags = doc.select("m_f_a_r");
        if (!articleTags.isEmpty()) {
            for (Element articleTag : articleTags) {
                // 拿到了 title
                String title = articleTags.get(0).child(0).text();
                System.out.println(title);
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
            System.out.println(link);
            HttpEntity responseGetEntity = response.getEntity();
            System.out.println(response.getStatusLine());
            String html = EntityUtils.toString(responseGetEntity);
//            System.out.println("-------------" +Jsoup.parse(html));
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
