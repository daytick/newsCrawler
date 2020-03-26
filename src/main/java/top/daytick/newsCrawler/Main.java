package top.daytick.newsCrawler;

import org.apache.http.HttpEntity;
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

public class Main {

    public static void main(String[] args) throws IOException {
        // 链接池
        Queue<String> linkPool = new LinkedList<>();
        // 已处理的链接池
        Set<String> processedPool = new HashSet<>();

        linkPool.offer("https://news.sina.cn");

        while (true) {

            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (linkPool.isEmpty()) {
                break;
            }

            String link = linkPool.poll();
            if (link.startsWith("//")) {
                link = "https:" + link;
            }

            if (processedPool.contains(link)) {
                continue;
            }

            if (link.contains("sina.cn")) {

                Document doc = httpGetAndParseHtml(link);
                List<Element> aTags = doc.select("a");
                for (Element aTag : aTags) {
                    String mayBeUsefulLink = aTag.attr("href");
                    if (!(mayBeUsefulLink.isEmpty() || mayBeUsefulLink.startsWith("javascript") || mayBeUsefulLink.contains("photo.sina.cn"))) {
                        linkPool.offer(mayBeUsefulLink);
                    }
                }

                // 如果是新闻页，就存入数据库
                List<Element> articleTags = doc.select("article");
                if (!articleTags.isEmpty()) {
                    for (Element articleTag : articleTags) {
                        String title = articleTag.child(0).text();
                        System.out.println("title=" + title + "\"");
                    }
                }

                processedPool.add(link);
            }

        }

    }

    private static Document httpGetAndParseHtml(String link) throws IOException {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(link);
        try (CloseableHttpResponse response = httpclient.execute(httpGet)) {
            System.out.println(response.getStatusLine());
            HttpEntity entity = response.getEntity();
            return Jsoup.parse(EntityUtils.toString(entity));
        }
    }

}
