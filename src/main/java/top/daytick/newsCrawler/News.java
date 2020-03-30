package top.daytick.newsCrawler;

import java.time.Instant;

public class News {
    private Long id;
    private String title;
    private String content;
    private String link;
    private Instant createdAt;
    private Instant modifiedAt;

    public News() {
    }

    public News(String title, String content, String link) {
        this.title = title;
        this.content = content;
        this.link = link;
    }

    public News(News old) {
        this.id = old.id;
        this.title = old.title;
        this.content = old.content;
        this.link = old.link;
        this.createdAt = old.createdAt;
        this.modifiedAt = old.modifiedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getModifiedAt() {
        return modifiedAt;
    }

    public void setModifiedAt(Instant modifiedAt) {
        this.modifiedAt = modifiedAt;
    }
}
