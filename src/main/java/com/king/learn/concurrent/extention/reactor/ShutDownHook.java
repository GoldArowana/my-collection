package com.king.learn.concurrent.extention.reactor;

import java.io.IOException;
import java.net.ServerSocket;

public class ShutDownHook extends Thread {
    private ServerSocket serverSocket;

    public ShutDownHook(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    @Override
    public void run() {
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            serverSocket = null;
            System.out.println("Server has been closed!");
        }

    }
}
