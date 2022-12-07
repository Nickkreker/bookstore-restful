package main;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import models.Book;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PersistentStorage {
    private Connection conn;
    private Logger log = LoggerFactory.getLogger(Server.class);

    public PersistentStorage() throws SQLException {
        Properties property = new Properties();
        try {
            property.load(new FileInputStream("src/main/resources/config.properties"));
            String host = property.getProperty("db.host");
            String login = property.getProperty("db.login");
            String password = property.getProperty("db.password");

            String url = String.format("jdbc:%s?user=%s&password=%s", host, login, password);
            conn = DriverManager.getConnection(url);
        } catch (IOException e) {
            log.error("Failed to read connection properties from config.properties");
            throw new SQLException(e);
        }
    }

    /**
     * Gets a book from a database with a given id.
     *
     * @param id id of a book to retrieve
     * @return an instance of a book created from retrieved data
     * @throws IllegalArgumentException if book with a given id was not found
     */
    public Book getBook(int id) throws SQLException {
        PreparedStatement st = conn.prepareStatement("select title, author, published from book where id = ?");
        st.setInt(1, id);   
        ResultSet rs = st.executeQuery();
        if (!rs.isBeforeFirst())
            throw new IllegalArgumentException(String.format("No such book with id %d", id));
        rs.next();

        String title = rs.getString("title");
        String author = rs.getString("author");
        int published = rs.getInt("published"); 

        return new Book(title, author, published, id);
    }

    /**
     * Gets list of all book ids present in the database
     *
     * @return list of present ids
     */
    public List<Integer> getBooksIds() throws SQLException {
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery("select id from book");

        List<Integer> list = new ArrayList<>();
        while (rs.next()) {
            list.add(rs.getInt("id"));
        }
        rs.close();
        st.close();
        return list;
    }
}
