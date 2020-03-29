package top.daytick.newsCrawler;

import java.sql.SQLException;

public interface CrawlerDao {

    String getNextLinkThenDelete() throws SQLException;

    void insertProcessedLink(String link) throws SQLException;

    void insertToBeProcessedLink(String link) throws SQLException;

    void insertNewsIntoDatabase(String title, String content, String link) throws SQLException;

    boolean isLinkProcessed(String link) throws SQLException;
}
