package de.betzen.wordclock;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HandshakeUDP {
    private int port;
    private String targetIP;

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getTargetIP() {
        return targetIP;
    }

    public void setTargetIP(String targetIP) {
        this.targetIP = targetIP;
    }

    public HandshakeUDP(int port) {
        //constructor without additional parameters: setup for general broadcast on 0.0.0.0
        this.port=port;
        this.targetIP = "0.0.0.0";
        Log.i("HandshakeUDP","IP automatically set to broadcast @ 0.0.0.0");
    }

    public HandshakeUDP(int port, String targetIP) {
        this.port=port;
        this.targetIP = targetIP;
        if(!isValidIP(targetIP)) {      //check if IP is in valid format
            Log.e("HandshakeUDP","Warning: IP " + targetIP + " is not a valid IP address [0-255].[0-255].[0-255].[0-255]");
        }

    }

    private boolean isValidIP(String ip) {
        //check if IP is in valid format
        String ipPattern = "((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)";
        Pattern pattern = Pattern.compile(ipPattern);
        Matcher matcher = pattern.matcher(ip);
        return matcher.matches();
    }

    public void sendPackage(String packageStr) {
        //sends String packageStr to IP:port address via UDP
        int server_port = port;
            DatagramSocket s = null;
            try {
                s = new DatagramSocket();
            } catch (SocketException e) {
                Log.e("HandshakeUDP", "sendPackage: couldn't initialize DatagramSocket");
                e.printStackTrace();
            }
            InetAddress local = null;
            try {
                local = InetAddress.getByName(targetIP);
            } catch (UnknownHostException e) {
                Log.e("HandshakeUDP", "sendPackage: couldn't resolve local IP");
                e.printStackTrace();
            }
            int msg_length=packageStr.length();
        byte[] message = packageStr.getBytes();
        DatagramPacket p = new DatagramPacket(message, msg_length,local,server_port);
        try {
            s.send(p);
        } catch (IOException e) {
            Log.e("HandshakeUDP", "sendPackage: IOException trying to send DatagramPacket");
            e.printStackTrace();
        } catch (NullPointerException f) {
            Log.e("HandshakeUDP", "sendPackage: NullPointerException trying to send DatagramPacket");
            f.printStackTrace();
        }
    }

    public String receivePackage() {
        //receives and returns String text from port via UDP
        String text;
        byte[] message = new byte[1];            //incoming message size set to 1500 byte, change if needed
        DatagramPacket p = new DatagramPacket(message, message.length);
        DatagramSocket s = null;
        try {
            s = new DatagramSocket(port);
        } catch (SocketException e) {
            Log.e("HandshakeUDP", "receivePackage: couldn't initialize DatagramSocket");
            e.printStackTrace();
        }
        try {
            if (s != null) {
                s.receive(p);
            }
        } catch (IOException e) {
            Log.e("HandshakeUDP", "receivePackage: IOError trying to receive package");
            e.printStackTrace();
        }
        text = new String(message, 0, p.getLength());
        Log.d("HandshakeUDP","receivePackage / received message from port:" + text);
        if (s != null) {
            s.close();
        }
        return text;
    }
}
