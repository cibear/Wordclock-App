package de.betzen.wordclock;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.StrictMode;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPSender {

    // see solution @ https://stackoverflow.com/questions/17308729/send-broadcast-udp-but-not-receive-it-on-other-android-devices
    Context mContext;

    UDPSender(Context mContext) {
        this.mContext = mContext;
    }

    public int sendBroadcast(String messageStr, int target_port) {
        // Hack Prevent crash (sending should be done using an async task)
        StrictMode.ThreadPolicy policy = new   StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        try {
            //Open a random port to send the package
            DatagramSocket socket = new DatagramSocket();
            int openedPort = socket.getLocalPort();
            socket.setBroadcast(true);
            byte[] sendData = messageStr.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, getBroadcastAddress(), target_port);
            socket.send(sendPacket);
            System.out.println(getClass().getName() + "Broadcast packet sent to: " + getBroadcastAddress().getHostAddress());
            socket.close();
            //return the port opened by DatagramSocket for the UDP receiver to listen for Wordclock replies
            return openedPort;
        } catch (IOException e) {
            Log.e("UDPSender", "IOException: " + e.getMessage());
            return -1;
        }
    }

    InetAddress getBroadcastAddress() throws IOException {
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
