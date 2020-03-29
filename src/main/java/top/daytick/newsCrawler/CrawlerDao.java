package top.daytick.newsCrawler;

import java.sql.SQLException;

public interface CrawlerDao {

    String getNextLinkThenDelete() throws SQLException;
    void updateDatabase(String link, String sql) throws SQLException;
    void insertNewsIntoDatabase(String title, String content, String link) throws SQLException;
    boolean isLinkProcessed(String link) throws SQLException;

}
