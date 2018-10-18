package de.betzen.wordclock;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPReceiver implements Runnable {

    // see solution @ https://stackoverflow.com/questions/17308729/send-broadcast-udp-but-not-receive-it-on-other-android-devices

    // for broadcast management, compare https://gist.github.com/Antarix/8131277

    DatagramSocket socket;
    int port;
    Context mContext;

    UDPReceiver(int port, Context mContext) {
        this.port = port;
        this.mContext = mContext;
    }

    @Override
    public void run() {
        try {
            //Keep a socket open to listen to all the UDP traffic that is destined for this port
            socket = new DatagramSocket(port, InetAddress.getByName("0.0.0.0"));
            socket.setBroadcast(true);

            while (true) {
                Log.i("UDPReceiver", "Ready to receive broadcast packets!");

                //Receive a packet
                byte[] recvBuf = new byte[15];
                DatagramPacket packet = new DatagramPacket(recvBuf, recvBuf.length);
                socket.receive(packet);

                //Packet received
                Log.i("UDPReceiver", "Packet received from: " + packet.getAddress().getHostAddress());
                String data = new String(packet.getData()).trim();
                Log.i("UDPReceiver", "Packet received; data: " + data);

                // Send the packet data back to the UI thread  (marked "UDPReceiver-Event")
                Intent localIntent = new Intent("UDPReceiver-Event")
                        // Puts the data into the Intent
                        .putExtra(Constants.UDP_RECEIVER_STRING, data);
                // Broadcasts the Intent to receivers in this app.
                LocalBroadcastManager.getInstance(this.mContext).sendBroadcast(localIntent);
            }
        } catch (IOException ex) {
            Log.i("UDPReceiver", "IOException " + ex.getMessage());
        }
    }

}
