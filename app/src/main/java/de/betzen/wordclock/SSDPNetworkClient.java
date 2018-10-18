package de.betzen.wordclock;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.content.LocalBroadcastManager;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

public class SSDPNetworkClient {
    private final Context mContext;

    //https://www.kompf.de/java/multicast.html

    SSDPNetworkClient(Context mContext) {
        this.mContext = mContext;
    }

    private String dump = null;

    /**
     * UPNP/SSDP client to demonstrate the usage of UDP multicast sockets.
     * @throws IOException
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void multicast() throws IOException {
        try {
            InetAddress multicastAddress = InetAddress.getByName("239.255.255.250"); // multicast address for SSDP
            final int port = 1900; // standard port for SSDP
            MulticastSocket socket = new MulticastSocket(port);
            socket.setReuseAddress(true);
            socket.setSoTimeout(15000);  //breaks try after 15 sec and throws SocketTimeoutException
            socket.joinGroup(multicastAddress);

            // send discover
            byte[] txbuf = DISCOVER_MESSAGE_ROOTDEVICE.getBytes("UTF-8");
            DatagramPacket hi = new DatagramPacket(txbuf, txbuf.length,
                    multicastAddress, port);
            socket.send(hi);
            System.out.println("SSDP discover sent");

            do {
                byte[] rxbuf = new byte[8192];
                DatagramPacket packet = new DatagramPacket(rxbuf, rxbuf.length);
                socket.receive(packet);
                dump = dumpPacket(packet);
            } while (true); // should leave loop by SocketTimeoutException
        } catch (SocketTimeoutException e) {
            System.out.println("Timeout");
            // Send the packet data back to the UI thread  (marked "UDPReceiver-Event")
            Intent localIntent = new Intent("UDPReceiver-Event")
                    // Puts the data into the Intent
                    .putExtra(Constants.UDP_RECEIVER_STRING, dump);
            // Broadcasts the Intent to receivers in this app.
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(localIntent);
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private String dumpPacket(DatagramPacket packet) throws IOException {
        InetAddress addr = packet.getAddress();
        System.out.println("Response from: " + addr);
        ByteArrayInputStream in = new ByteArrayInputStream(packet.getData(), 0, packet.getLength());
        copyStream(in, System.out);
        return StreamToString(in);
    }

    private void copyStream(InputStream in, OutputStream out) throws IOException {
        BufferedInputStream bin = new BufferedInputStream(in);
        BufferedOutputStream bout = new BufferedOutputStream(out);
        int c = bin.read();
        while (c != -1) {
            out.write((char) c);
            c = bin.read();
        }
        bout.flush();
    }

    private final static String DISCOVER_MESSAGE_ROOTDEVICE =
            "M-SEARCH * HTTP/1.1\r\n" +
                    "ST: upnp:rootdevice\r\n" +
                    "MX: 3\r\n" +
                    "MAN: `ssdp:discover`\r\n".replace('`', '"') +
                    "HOST: 239.255.255.250:1900\r\n\r\n";

    /**
     * MAIN
     */
    /*@RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static void main(String[] args) throws Exception {
        SSDPNetworkClient client = new SSDPNetworkClient();
        client.multicast();
    }
    */

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static String StreamToString(InputStream inputStream) throws IOException {
            //https://www.baeldung.com/convert-input-stream-to-string (Section 4)
            StringBuilder textBuilder = new StringBuilder();
            try (Reader reader = new BufferedReader(new InputStreamReader
                    (inputStream, Charset.forName(StandardCharsets.UTF_8.name())))) {
                int c = 0;
                while ((c = reader.read()) != -1) {
                    textBuilder.append((char) c);
                }
            }
            return textBuilder.toString();

    }
}


