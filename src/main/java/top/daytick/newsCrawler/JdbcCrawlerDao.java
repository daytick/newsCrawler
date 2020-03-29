package top.daytick.newsCrawler;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.sql.*;

public class JdbcCrawlerDao implements CrawlerDao {
    private static final String USERNAME = "root";
    private static final String PASSWORD = "root";
    private final Connection connection;

    @SuppressFBWarnings("DMI_CONSTANT_DB_PASSWORD")
    public JdbcCrawlerDao() {
        try {
            this.connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/news?characterEncoding=utf-8", USERNAME, PASSWORD);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public String getNextLinkThenDelete() throws SQLException {
        String link = getNextLink();
        if (link != null) {
            insertProcessedLink(link);
        }

        return link;
    }

    private String getNextLink() throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT link FROM links_to_be_processed LIMIT 1"); ResultSet resultSet = preparedStatement.executeQuery()) {
            if (resultSet.next()) {
                return resultSet.getString(1);
            }
        }
        return null;
    }

    public void insertProcessedLink(String link) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO links_already_processed(link) VALUES(?)")) {
            preparedStatement.setString(1, link);
            preparedStatement.executeUpdate();
        }
    }

    @Override
    public void insertToBeProcessedLink(String link) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO links_to_be_processed(link) VALUES(?)")) {
            preparedStatement.setString(1, link);
            preparedStatement.executeUpdate();
        }
    }

    public void insertNewsIntoDatabase(String title, String content, String link) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO news(title, content, link, created_at, modified_at) VALUES(?, ?, ?, NOW(), NOW())")) {
            preparedStatement.setString(1, title);
            preparedStatement.setString(2, content);
            preparedStatement.setString(3, link);
            preparedStatement.executeUpdate();
        }
    }

    public boolean isLinkProcessed(String link) throws SQLException {
        ResultSet resultSet = null;
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT link FROM links_already_processed WHERE link = ?")) {
            preparedStatement.setString(1, link);
            resultSet = preparedStatement.executeQuery();
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
