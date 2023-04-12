package com.java.reptile;

public class Main {
    public static void main(String[] args) {
        CrawlerDao dao = new MyBatisCrawlerDao();
        // 准备开四个线程
        for (int i = 0; i < 8; i++) {
            new Crawler(dao).start();
        }
    }
}
