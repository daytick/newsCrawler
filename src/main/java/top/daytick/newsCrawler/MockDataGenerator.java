package top.daytick.newsCrawler;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import java.util.Random;

public class MockDataGenerator {
    private static final int TARGET_LINK_COUNT = 5_0000;

    public static void main(String[] args) {
        SqlSessionFactory sqlSessionFactory;
        try {
            String resource = "db/mybatis/mybatis-config.xml";
            InputStream inputStream = Resources.getResourceAsStream(resource);
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        insertMockData(sqlSessionFactory);
    }

    private static void insertMockData(SqlSessionFactory sqlSessionFactory) {
        try (SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH)) {
            List<News> baseNews = sqlSession.selectList("top.daytick.mockMapper.selectNews");

            int count = TARGET_LINK_COUNT - baseNews.size();
            Random random = new Random();
            try {
                while (count-- > 0) {
                    int index = random.nextInt(baseNews.size());
                    News newsToBeInserted = new News(baseNews.get(index));

                    Instant setTime = newsToBeInserted.getCreatedAt().minusSeconds(random.nextInt(3600 * 24 * 365));
                    newsToBeInserted.setCreatedAt(setTime);
                    newsToBeInserted.setModifiedAt(setTime);

                    sqlSession.insert("top.daytick.mockMapper.insertNews", newsToBeInserted);

                    System.out.println("Left: " + count);
                    if (count % 2000 == 0) {
                        sqlSession.flushStatements();
                    }
                }
                sqlSession.commit();
            } catch (Exception e) {
                sqlSession.rollback();
                throw new RuntimeException(e);
            }
        }
    }

}
