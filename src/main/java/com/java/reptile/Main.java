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

public class Main {
    public static void main(String[] args) throws SQLException {
        Connection connection = DriverManager.getConnection("jdbc:h2:file:/Users/sunkuangdong/Desktop/javaStudy/java-reptile/news");
        String link;
        while ((link = getNextLinkThenDelete(connection)) != null) {
            // 从数据库中判断 是否正在处理这个连接池
            if (isLinkProcessed(connection, link)) {
                continue;
            }
            if (isInterestingLink(link)) {
                // 我们需要处理
                startHttp(link, connection);
            }
        }
    }

    private static String getNextLinkThenDelete(Connection connection) throws SQLException {
        String link = getNextLink(connection, "select link from LINKS_TO_BE_PROCESSED");
        // 是否存在在连接池中
        if (link == null) {
            return null;
        }
        insertOrDeleteLinkIntoDatabase(connection, "DELETE from LINKS_TO_BE_PROCESSED where link = ?", link);
        return link;
    }

    private static boolean isLinkProcessed(Connection connection, String link) throws SQLException {
        ResultSet resultSet = null;
        try (PreparedStatement statement = connection.prepareStatement("select link from LINKS_ALREADY_PROCESSED where link = ?")) {
            statement.setString(1, link);
            resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return true;
            }
        } finally {
            if (resultSet != null) {
                resultSet.close();
            }
        }
        return false;
    }

    private static String getNextLink(Connection connection, String sqlUrl) throws SQLException {
        // 待处理的连接池
        ResultSet resultSet = null;
        try (PreparedStatement statement = connection.prepareStatement(sqlUrl)) {
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                return resultSet.getString(1);
            }
        } finally {
            if (resultSet != null) {
                resultSet.close();
            }
        }
        return null;
    }

    public static void startHttp(String link, Connection connection) {
        try {
            // 使用 Jsoup 解析 html 字符串
            Document doc = httpGetAndParseHtml(link);
            parseUrlsFrompageAndStoreIntoDatabase(connection, doc);
            // 假如这是一个新闻的页面，就存入数据库，不然不管
            storeIntoDatabaseIfItIsNwesPage(doc);
            // 处理完成之后放进数据库中
            insertOrDeleteLinkIntoDatabase(connection, "insert into LINKS_ALREADY_PROCESSED(link) values (?)", link);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void parseUrlsFrompageAndStoreIntoDatabase(Connection connection, Document doc) {
        // 获取想要的结果
        for (Element aTag : doc.select("a")) {
            String href = aTag.attr("href");
            insertOrDeleteLinkIntoDatabase(connection, "insert into LINKS_TO_BE_PROCESSED(link) values (?)", href);
        }
    }

    private static void insertOrDeleteLinkIntoDatabase(Connection connection, String sql, String link) {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, link);
            statement.executeUpdate();
        } catch (SQLException e) {
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
