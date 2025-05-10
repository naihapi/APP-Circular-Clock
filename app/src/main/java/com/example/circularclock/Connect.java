package com.example.circularclock;

import android.content.Context;
import android.location.OnNmeaMessageListener;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.lang.annotation.Target;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

public class Connect extends AppCompatActivity {
    private static final int UDP_PORT = 8266;//UDP端口
    private static final String TARGET_IP = "192.168.4.1";//IP地址
    private DatagramSocket socket;
    public String Rec_Buffer;
    private boolean RecFunRUNNING = false;
    public boolean Rec_Flag = false;

    public void Connect_InitPro() {
        try {
            Connect_Close();

            socket = new DatagramSocket(null);//仅新建数据socket

            socket.setReuseAddress(true);//允许新socket地址重用(应对重新进入页面的情况)
            socket.bind(new InetSocketAddress(UDP_PORT));//绑定端口
        } catch (SocketException se) {
            Log.e("Socket", "" + se);
        }

        Connect_RecStart();
        Connect_ReceiveFunction();
        RecFunRUNNING = true;
    }

    public void Connect_SendString(final String message) {
        try {
            byte[] data = message.getBytes();
            InetAddress address = InetAddress.getByName(TARGET_IP);

            DatagramPacket packet = new DatagramPacket(data, data.length, address, UDP_PORT);
            socket.send(packet);
        } catch (Exception error) {
            Log.e("UDP发送失败", "" + error);
        }
    }

    public void Connect_RecStart() {
        RecFunRUNNING = true;
    }

    public void Connect_RecStop() {
        RecFunRUNNING = false;
    }

    public void Connect_Close() {
        if (socket != null) {
            socket.close();
        }
    }

    public void Connect_ReceiveFunction() {
        try {
            new Thread(() -> {
                byte[] buffer = new byte[1024];
                while (RecFunRUNNING) {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

                    try {
                        if (!Rec_Flag) {
                            socket.receive(packet);
                            Rec_Buffer = new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8);
                            Rec_Flag = true;
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                }
            }).start();
        } catch (Exception e) {
            Log.e("UDP接收失败", "" + e);
        }
    }
}