package models;

public class Book {
    private final int id;
    private final String title;
    private final String author;
    private final int published;

    public Book(String title, String author, int published, int id) {
        this.title = title;
        this.author = author;
        this.published = published;
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public int getYearPublished() {
        return published;
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return String.format(
                "{\"id\":%d,\"title\":\"%s\",\"author\":\"%s\",\"published\":%d}",
                id, title, author, published);
    }
}
