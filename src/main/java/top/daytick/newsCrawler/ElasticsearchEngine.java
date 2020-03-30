package top.daytick.newsCrawler;

import org.apache.http.HttpHost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

public class ElasticsearchEngine {

    public static void main(String[] args) {
        while (true) {
            System.out.println("Please input search keyword：");
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in, Charset.defaultCharset()));
            String keyword = null;
            try {
                keyword = reader.readLine();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            search(keyword);
        }
    }

    private static void search(String keyword) {
        try (RestHighLevelClient client = new RestHighLevelClient(RestClient.builder(new HttpHost("localhost", 9200, "http")))) {
            SearchRequest searchRequest = new SearchRequest("news");
            searchRequest.source(new SearchSourceBuilder().query(new MultiMatchQueryBuilder(keyword, "title", "content")));

            SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
            SearchHits hits = searchResponse.getHits();
            hits.forEach(hit -> System.out.println(hit.getSourceAsString()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
