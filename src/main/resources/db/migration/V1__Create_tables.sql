-- 新闻表
CREATE TABLE news(
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(100),
    content MEDIUMTEXT,
    link TEXT,
    created_at TIMESTAMP DEFAULT NOW(),
    modified_at TIMESTAMP DEFAULT NOW()
)ENGINE=INNODB DEFAULT CHARSET=UTF8;

-- 待处理的链接表
CREATE TABLE links_to_be_processed(
    link TEXT
)ENGINE=INNODB DEFAULT CHARSET=UTF8;

-- 已经处理的链接表
CREATE TABLE links_already_processed(
    link TEXT
)ENGINE=INNODB DEFAULT CHARSET=UTF8;