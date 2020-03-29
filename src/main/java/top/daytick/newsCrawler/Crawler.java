package top.daytick.newsCrawler;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Crawler {
    private static final Pattern PATTERN = Pattern.compile("https?://(news\\.)?sina\\.cn.*");
    private static final String USERAGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.149 Safari/537.36";

    @SuppressFBWarnings("DMI_CONSTANT_DB_PASSWORD")
    public static void main(String[] args) throws IOException, SQLException {
        Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/news?characterEncoding=utf-8", "root", "root");

        String link;
        while ((link = getNextLinkThenDelete(connection)) != null) {
            if (isLinkProcessed(connection, link)) {
                continue;
            }

            System.out.println("link=" + link);
            Matcher matcher = PATTERN.matcher(link);
            if (matcher.matches()) {

                Document doc = httpGetAndParseHtml(link);

                parseLinksFromPageAndStoreIntoDatabase(connection, doc);

                storeIntoDatabaseIfItIsNews(connection, doc, link);
            }

            updateDatabase(connection, link, "INSERT INTO links_already_processed(link) VALUES(?)");
        }

    }

    private static void parseLinksFromPageAndStoreIntoDatabase(Connection connection, Document doc) throws SQLException {
        List<Element> aTags = doc.select("a");
        if (!aTags.isEmpty()) {
            for (Element aTag : aTags) {
                String link = aTag.attr("href");

                if (link.startsWith("//")) {
                    link = "https:" + link;
                }

                if (link.startsWith("http")) {
                    updateDatabase(connection, link, "INSERT INTO links_to_be_processed(link) VALUES(?)");
                }
            }
        }
    }

    private static boolean isLinkProcessed(Connection connection, String link) throws SQLException {
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

    private static String getNextLinkThenDelete(Connection connection) throws SQLException {
        String link = getNextLink(connection);
        if (link != null) {
            updateDatabase(connection, link, "DELETE FROM links_to_be_processed WHERE link = ?");
        }

        return link;
    }

    private static void updateDatabase(Connection connection, String link, String sql) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, link);
            preparedStatement.executeUpdate();
        }
    }

    private static String getNextLink(Connection connection) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT link FROM links_to_be_processed LIMIT 1"); ResultSet resultSet = preparedStatement.executeQuery()) {
            if (resultSet.next()) {
                return resultSet.getString(1);
            }
        }
        return null;
    }

    private static void storeIntoDatabaseIfItIsNews(Connection connection, Document doc, String link) throws SQLException {
        List<Element> articleTags = doc.select("article");
        if (!articleTags.isEmpty()) {
            for (Element articleTag : articleTags) {
                String title = articleTag.child(0).text();
                List<Element> paragraphs = articleTag.select("p");
                String content = paragraphs.stream().map(Element::text).collect(Collectors.joining("\n"));

                try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO news(title, content, link, created_at, modified_at) VALUES(?, ?, ?, NOW(), NOW())")) {
                    preparedStatement.setString(1, title);
                    preparedStatement.setString(2, content);
                    preparedStatement.setString(3, link);
                    preparedStatement.executeUpdate();
                }
            }
        }
    }

    private static Document httpGetAndParseHtml(String link) throws IOException {
        CloseableHttpClient httpclient = HttpClients.custom().setDefaultRequestConfig(RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build()).build();
        HttpGet httpGet = new HttpGet(link);
        httpGet.setHeader("user-agent", USERAGENT);
        try (CloseableHttpResponse response = httpclient.execute(httpGet)) {
            HttpEntity entity = response.getEntity();
            return Jsoup.parse(EntityUtils.toString(entity));
        }
    }

}
