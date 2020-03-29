package top.daytick.newsCrawler;

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
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class Crawler {
    private CrawlerDao newsDao = new JdbcCrawlerDao();

    public static void main(String[] args) {
        try {
            new Crawler().run();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void run() throws SQLException, IOException {
        String link;
        while ((link = newsDao.getNextLinkThenDelete()) != null) {
            if (newsDao.isLinkProcessed(link)) {
                continue;
            }

            System.out.println("link=" + link);
            if (isNeededLink(link)) {
                Document doc = httpGetAndParseHtml(link);

                parseLinksFromPageAndStoreIntoDatabase(doc);

                storeIntoDatabaseIfItIsNews(doc, link);
            }

            newsDao.updateDatabase(link, "INSERT INTO links_already_processed(link) VALUES(?)");
        }
    }

    private boolean isNeededLink(String link) {
        return link.contains("news.sina.cn") || "https://sina.cn".equals(link);
    }

    private void parseLinksFromPageAndStoreIntoDatabase(Document doc) throws SQLException {
        List<Element> aTags = doc.select("a");
        if (!aTags.isEmpty()) {
            for (Element aTag : aTags) {
                String link = aTag.attr("href");

                if (link.startsWith("//")) {
                    link = "https:" + link;
                }

                if (link.startsWith("http")) {
                    newsDao.updateDatabase(link, "INSERT INTO links_to_be_processed(link) VALUES(?)");
                }
            }
        }
    }

    private void storeIntoDatabaseIfItIsNews(Document doc, String link) throws SQLException {
        List<Element> articleTags = doc.select("article");
        if (!articleTags.isEmpty()) {
            for (Element articleTag : articleTags) {
                String title = articleTag.child(0).text();
                List<Element> paragraphs = articleTag.select("p");
                String content = paragraphs.stream().map(Element::text).collect(Collectors.joining("\n"));

                newsDao.insertNewsIntoDatabase(title, content, link);
            }
        }
    }

    private Document httpGetAndParseHtml(String link) throws IOException {
        CloseableHttpClient httpclient = HttpClients.custom().setDefaultRequestConfig(RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build()).build();
        HttpGet httpGet = new HttpGet(link);
        try (CloseableHttpResponse response = httpclient.execute(httpGet)) {
            HttpEntity entity = response.getEntity();
            return Jsoup.parse(EntityUtils.toString(entity));
        }
    }

}
