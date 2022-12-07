package main;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.Arrays;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import models.Book;

public class App {
    public static void main(String[] args) throws IOException {
        Server server = new Server(5000);
        server.start();
    }
}
