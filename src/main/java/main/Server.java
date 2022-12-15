package main;

import models.Book;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public class Server {
    private final int port;
    private PersistentStorage ps;
    private ServerSocket server;
    private Logger log = LoggerFactory.getLogger(Server.class);


    /**
     * Creates a server.
     * 
     * @param port Port on which server will listen for upcoming requests
     */
    public Server(int port) {
        this.port = port;
        try {
            ps = new PersistentStorage();
            server = new ServerSocket(port);
        } catch (SQLException e) {
            log.error("Failed to connect to a database");
            throw new RuntimeException(e);
        } catch (IOException e) {
            log.error(String.format("Failed to initialize server on port %d", port));
            throw new RuntimeException(e);
        }
    }

    /**
     * Starts server so it can accept connections.
     */
    public void start() throws IOException {
        log.info("Server started listening on port {}", port);
        while (true) {
            Socket socket = server.accept();
            log.info("Received connection from {}", socket.getRemoteSocketAddress().toString());
            handleRequest(socket);
            socket.close();
        }
    }

    private void handleRequest(Socket socket) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.equals(""))
                    break;
                String[] words = line.split(" ");
                switch (words[0]) {
                    case "GET":
                        handleGetRequest(words[1], socket);
                }
            }
        } catch (IOException e) {
            log.error("Failed to read from socket {}", socket.getRemoteSocketAddress().toString());
            throw new IOException(e);
        }
    }

    private void handleGetRequest(String route, Socket socket) {
        String[] routeWords = route.split("/");
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {
            if (routeWords.length == 3 && routeWords[1].equals("books")) {
                Book book;
                try {
                    book = ps.getBook(Integer.valueOf(routeWords[2]));
                    writer.write(String.format(
                            "HTTP/1.1 200 OK\ncontent-type: application/json\r\n\r\n%s\n",
                            book.toString()
                    ));
                } catch (SQLException e) {
                    log.warn("Failed to prepare answer to client because of sql exception");
                    writer.write("HTTP/1.1 500 Internal Server Error\r\n\r\n");
                } catch (IllegalArgumentException e) {
                    writer.write("HTTP/1.1 404 Internal Server Error\r\n\r\n");
                }
            } else if (routeWords.length == 2 && routeWords[1].equals("books")) {
                try {
                    List<Book> books = ps.getBooks();
                    StringBuilder sb = new StringBuilder();
                    for (Book book : books) {
                        sb.append(String.format("{\"title\": \"%s\", \"id\": %d},", book.getTitle(), book.getId()));
                    }
                    if (!sb.isEmpty()) {
                        sb.deleteCharAt(sb.length() - 1);
                        sb.insert(0, "[");
                        sb.append("]");
                    }

                    writer.write(String.format(
                            "HTTP/1.1 200 OK\ncontent-type: application/json\r\n\r\n%s\n",
                            sb.toString()
                    ));
                } catch (SQLException e) {
                    log.warn("Failed to prepare answer to client because of sql exception");
                    writer.write("HTTP/1.1 500 Internal Server Error\r\n\r\n");
                }
            } else {
                writer.write("HTTP/1.1 404 Not Found\r\n\r\n");
            }
        } catch (IOException e) {
            log.error("Failed to write into socket %s", socket.getRemoteSocketAddress().toString());
        }
    }
}
