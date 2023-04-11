package com.java.reptile;

import java.sql.SQLException;

public interface CrawlerDao {
    String getNextLink(String sql) throws SQLException;
    void insertOrDeleteLinkIntoDatabase(String sql, String link);
    void insertNewsIntoDatabase(String link, String title, String content);

    boolean isLinkProcessed(String link) throws SQLException;
}
