package de.betzen.wordclock;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class EchoClient {
    private DatagramSocket socket;
    //private InetAddress address;

    private byte[] buf;

    public EchoClient() {
        try {
            socket = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
        }
        //address = InetAddress.getByName("localhost");
    }

    public String sendEcho(String msg) throws Throwable {           //test method with standard address and port
        String received = sendEcho(msg, InetAddress.getByName("localhost"), 4445);
        return received;
    }

    public String sendEcho(String msg, InetAddress address, int port) throws Throwable {
        buf = msg.getBytes();
        DatagramPacket packet
                = new DatagramPacket(buf, buf.length, address, port);
        socket.send(packet);
        packet = new DatagramPacket(buf, buf.length);
        socket.receive(packet);
        String received = new String(
                packet.getData(), 0, packet.getLength());
        return received;
    }

    public String broadcastEcho(Context mContext, String msg, int port) throws Throwable {           //test method with standard address and port
        socket.setBroadcast(true);
        InetAddress address = getBroadcastAddress(mContext);
        String received = sendEcho(msg, address, port);
        return received;
    }

    public void close() {
        socket.close();
    }

    InetAddress getBroadcastAddress(Context mContext) throws IOException {
        WifiManager wifi = (WifiManager) mContext.getApplicationContext().getSystemService(Context.WIFI_SERVICE); //mContext.getSystemService(Context.WIFI_SERVICE);
        DhcpInfo dhcp = wifi.getDhcpInfo();
        // handle null somehow

        int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
        byte[] quads = new byte[4];
        for (int k = 0; k < 4; k++)
            quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
        return InetAddress.getByAddress(quads);
    }
}
