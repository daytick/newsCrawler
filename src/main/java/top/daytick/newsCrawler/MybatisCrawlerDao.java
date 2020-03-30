package top.daytick.newsCrawler;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class MybatisCrawlerDao implements CrawlerDao {
    private SqlSessionFactory sqlSessionFactory;

    public MybatisCrawlerDao() {
        try {
            String resource = "db/mybatis/mybatis-config.xml";
            InputStream inputStream = Resources.getResourceAsStream(resource);
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public synchronized String getNextLinkThenDelete() {
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            String link = session.selectOne("top.daytick.myMapper.getNextLink");
            if (link != null) {
                session.delete("top.daytick.myMapper.deleteLink", link);
            }
            return link;
        }
    }

    @Override
    public void insertNewsIntoDatabase(String title, String content, String link) {
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            session.insert("top.daytick.myMapper.insertNews", new News(title, content, link));
        }
    }

    @Override
    public boolean isLinkProcessed(String link) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            int count = session.selectOne("top.daytick.myMapper.countLink", link);
            return count != 0;
        }
    }

    @Override
    public void insertProcessedLink(String link) {
        insertLink(link, "links_already_processed");
    }

    @Override
    public void insertToBeProcessedLink(String link) {
        insertLink(link, "links_to_be_processed");
    }

    private void insertLink(String link, String tableName) {
        Map<String, String> param = new HashMap<>();
        param.put("tableName", tableName);
        param.put("link", link);
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            session.insert("top.daytick.myMapper.insertLink", param);
        }
    }
}
