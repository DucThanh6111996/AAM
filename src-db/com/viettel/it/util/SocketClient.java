/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.it.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;

/**
 *
 * @author hienhv4
 */
public class SocketClient {

    private Socket socket;
    PrintStream out;
    BufferedReader in;

    public SocketClient(String serverIP, int port) throws IOException, Exception {
        this.socket = null;
        int count = 0;
        while (null == this.socket && count < 3) {
            try {
                InetAddress address = InetAddress.getByName(serverIP);
                this.socket = new Socket(address, port);
                this.socket.setSoTimeout(300000);// Timeout 5phut
            } catch (Exception ex) {
                count++;
                if (count == 2) {
                    throw ex;
                }
                System.out.println("Create socket error: " + ex.getMessage());
            }
        }
        creatConnection();
    }

    public final void creatConnection() throws IOException {
        try {
            out = new PrintStream(socket.getOutputStream());
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException ex) {
            throw ex;
        }
    }

    public void assureConnection() throws IOException {
        if ((null != out) || (null != in)) {
            creatConnection();
        }
    }

    public void sendMsg(String msg) throws IOException {
        assureConnection();
        out.println(msg);
        out.flush();
    }
    
    public String receiveResult() throws Exception {
        String msg = in.readLine();
        return msg;
    }
}
