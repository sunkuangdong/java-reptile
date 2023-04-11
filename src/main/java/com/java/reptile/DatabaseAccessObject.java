package com.java.reptile;

import java.sql.*;

public class DatabaseAccessObject implements CrawlerDao {
    final Connection connection;

    public DatabaseAccessObject() throws SQLException {
        this.connection = DriverManager.getConnection("jdbc:h2:file:/Users/sunkuangdong/Desktop/javaStudy/java-reptile/news");
    }

    public String getNextLink(String sqlUrl) throws SQLException {
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

    public void insertOrDeleteLinkIntoDatabase(String sql, String link) {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, link);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void insertNewsIntoDatabase(String link, String title, String content) {
        try (PreparedStatement statement = connection.prepareStatement("insert into news(url, title, content, created_at, MODIFIED_AT)values(?,?,?,now(),now())")) {
            statement.setString(1, link);
            statement.setString(2, title);
            statement.setString(3, content);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isLinkProcessed(String link) throws SQLException {
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
}
