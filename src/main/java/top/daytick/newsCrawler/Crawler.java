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
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Crawler {
    private static final Pattern PATTERN = Pattern.compile("https?://\\w*\\.?sina\\.cn.*");
    private static final String USERAGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.149 Safari/537.36";

    public static void main(String[] args) throws IOException {
        Queue<String> linkPool = new LinkedList<>();
        Set<String> processedPool = new HashSet<>();
        linkPool.offer("https://sina.cn");

        while (true) {
            if (linkPool.isEmpty()) {
                break;
            }

            String link = linkPool.poll();
            if (link.startsWith("//")) {
                link = "https:" + link;
            }
            System.out.println("link=" + link);

            if (processedPool.contains(link)) {
                continue;
            }

            Matcher matcher = PATTERN.matcher(link);
            if (matcher.matches()) {
                Document doc = httpGetAndParseHtml(link);

                doc.select("a").stream().map(aTag -> aTag.attr("href")).filter(mayBeUsefulLink -> mayBeUsefulLink.startsWith("http")).forEach(linkPool::offer);

                storeIntoDatabaseIfItIsNews(doc);
            }

            processedPool.add(link);
            System.out.print("\n");
        }

    }

    private static void storeIntoDatabaseIfItIsNews(Document doc) {
        List<Element> articleTags = doc.select("article");
        if (!articleTags.isEmpty()) {
            for (Element articleTag : articleTags) {
                String title = articleTag.child(0).text();
                System.out.println("title=" + title + "\"");
            }
        }
    }

    private static Document httpGetAndParseHtml(String link) throws IOException {
        CloseableHttpClient httpclient = HttpClients.custom().setDefaultRequestConfig(RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build()).build();
        HttpGet httpGet = new HttpGet(link);
        httpGet.setHeader("user-agent", USERAGENT);
        try (CloseableHttpResponse response = httpclient.execute(httpGet)) {
            System.out.println(response.getStatusLine());
            HttpEntity entity = response.getEntity();
            return Jsoup.parse(EntityUtils.toString(entity));
        }
    }

}
