package com.king.learn.concurrent.reactor;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private static Demultiplexer demultiplexer = new Demultiplexer();

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(4000);
        Runtime.getRuntime().addShutdownHook(new ShutDownHook(serverSocket));

        long listenTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - listenTime < 60000) {
            Socket socket = serverSocket.accept();
            int requestID = socket.getInputStream().read();
            if (requestID != -1) {
                System.out.println("No. " + requestID + " request has came!");
                demultiplexer.accept(requestID);
            }
        }
        serverSocket.close();
    }
}
