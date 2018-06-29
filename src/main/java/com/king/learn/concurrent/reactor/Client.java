package com.king.learn.concurrent.reactor;

import java.io.*;
import java.net.*;

public class Client extends Thread {
    private static final int PORT = 4000;
    private static final String ADDRESS = "localhost";
    private int requestID;

    public Client(int requestID) {
        this.requestID = requestID;
    }

    @Override
    public void run() {
        try {
            Socket socket = new Socket(ADDRESS, PORT);
            socket.getOutputStream().write(requestID);
            sleep(5000);
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("No." + requestID + " request was sent ...");
    }

    public static void main(String[] args) {
        int i = 0;
        while (i < 10) {
            new Client(i++).start();
        }
    }
}
