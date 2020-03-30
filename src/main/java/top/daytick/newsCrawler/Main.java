package top.daytick.newsCrawler;

public class Main {
    public static void main(String[] args) {

        CrawlerDao mybatisCrawlerDao = new MybatisCrawlerDao();
        for (int i = 0; i < 10; i++) {
            new Crawler(mybatisCrawlerDao).start();
        }

    }
}
