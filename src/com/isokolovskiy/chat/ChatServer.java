package com.isokolovskiy.chat;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

@SuppressWarnings("WeakerAccess")
public class ChatServer {

    public ChatServer(int port) throws IOException {

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            //noinspection InfiniteLoopStatement
            while (true) {
                Socket s = serverSocket.accept();
                System.out.println("Accepted from " + s.getInetAddress());
                ChatHandler handler = new ChatHandler(s);
                handler.start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String port = "8082";
        try {
            new ChatServer(Integer.parseInt(port));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
