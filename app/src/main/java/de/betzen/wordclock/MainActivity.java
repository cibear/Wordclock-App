package de.betzen.wordclock;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.app.Activity;

import static android.os.SystemClock.sleep;
import static de.betzen.wordclock.IPActivity.IP_ADDRESS;

public class MainActivity extends Activity {

    EditText etResponse;
    EditText etUDPResponse;
    TextView tvIsConnected;
    String custom_ip_address;

    //TODO: trying to fix DatagramSocket crashes
    //DatagramSocket socket = null;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //setup IP button
        Button btIP = findViewById(R.id.buttonIP);
        btIP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoIPActivity();
            }
        });

        //if IP address was chosen, receive intent and extract IP
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            custom_ip_address = (String) extras.get(IP_ADDRESS);
        } else {
            custom_ip_address = null;
        }

        //TODO: just a test sequence for UDP communciation:
        // send and receive a package @ port 1234
        //final HandshakeUDP sendNreceive = new HandshakeUDP(1234,"0.0.0.0");
        //sendNreceive.sendPackage("Dies ist ein Test");
        //String receivedPackage = sendNreceive.receivePackage();
        //Toast.makeText(this, receivedPackage, Toast.LENGTH_SHORT).show();
        //////////////////

        //Register MessageService in Manifest to work (to receive messages from UDPReceiver-Thread
        startService(new Intent(MainActivity.this, MessageService.class));


        //TODO: test UDP receiver
        /*
        UDPReceiver udpReceiver = new UDPReceiver(1234, MainActivity.this);
        Thread udpReceiverThread = new Thread(udpReceiver,"UDP-Receiver");
        udpReceiverThread.setDaemon(true);
        udpReceiverThread.start();
        */

        //sleep(1000);

        /*
        UDPSenderAsyncTask udpSenderAsyncTask = new UDPSenderAsyncTask() {
            @Override
            protected String doInBackground(String... strings) {
                return null;
            }

        };

        */
        ///TODO: delete these lines after testing
        /*
        try {
            socket = new DatagramSocket(1234, InetAddress.getByName("0.0.0.0"));
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        startReceiver(1234);
        */

        //TODO: this part is working!

        //udpSenderAsyncTask.sendUDPBroadcast("UDP broadcast successful - check this out, Dude!",Constants.WORDCLOCK_UDP_PORT);

        /*//TODO: implement Multicasting
        SSDPNetworkClient client = new SSDPNetworkClient(this);
        try {
            client.multicast();
        } catch (IOException e) {
            e.printStackTrace();
        }
        */




        // get reference to the views
        etResponse = (EditText) findViewById(R.id.etResponse);
        etUDPResponse = findViewById(R.id.etUDPResponse);
        tvIsConnected = (TextView) findViewById(R.id.tvIsConnected);

        //TODO: EchoClient/EchoServer UDP Client

        new EchoUDPAsyncTask().execute();


        //TODO: implement UDP receiver
        //new UDPReceiverAsyncTask().execute(String.valueOf(openedPort));         //listen on port opened by UDP sender for a reply by the Wordclock

        //TODO: Testbutton
        /*
        Button buttonRx = findViewById(R.id.buttonReceive);
        buttonRx.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String receivedPackage = sendNreceive.receivePackage();
                Toast.makeText(v.getContext(), receivedPackage, Toast.LENGTH_SHORT).show();
            }
        });
        */

        // check if you are connected or not
        if(isConnected()){
            tvIsConnected.setBackgroundColor(0xFF00CC00);
            tvIsConnected.setText("You are connected");
        }
        else{
            tvIsConnected.setText("You are NOT connected");
        }

        //REST API read-out of workdclock parameters
        // call AsyncTask to perform network operation on separate thread
        if (!(custom_ip_address == null))
            new HttpAsyncTask().execute("http://"+custom_ip_address+"/api/plugin");
        //TODO: remove sample address
        else
            new HttpAsyncTask().execute("http://hmkcode.appspot.com/rest/controller/get.json");
    }

    private void gotoIPActivity() {
        Intent intent = new Intent(this,IPActivity.class);
        startActivity(intent);
    }

    public static String GET(String url){
        InputStream inputStream = null;
        String result = "";
        try {

            // create HttpClient
            HttpClient httpclient = new DefaultHttpClient();

            // make GET request to the given URL
            HttpResponse httpResponse = httpclient.execute(new HttpGet(url));

            // receive response as inputStream
            inputStream = httpResponse.getEntity().getContent();

            // convert inputstream to string
            if(inputStream != null)
                result = convertInputStreamToString(inputStream);
            else
                result = "Did not work!";

        } catch (Exception e) {
            Log.d("InputStream", e.getLocalizedMessage());
        }

        return result;
    }

    //EchoClient to receive UDP handshake from Wordclock
    private class EchoUDPAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            EchoClient client = new EchoClient();
            String echo = "UDP Echo not received";
            try {
                echo = client.broadcastEcho(MainActivity.this, Constants.WORDCLOCK_ECHO_MESSAGE,Constants.WORDCLOCK_UDP_PORT);
            } catch(Throwable e) {
                e.printStackTrace();
            }
            client.close();

            return echo;
        }

        @Override
        protected void onPostExecute(String result) {
            etUDPResponse.setText(result);
        }
    }


    private static String convertInputStreamToString(InputStream inputStream) throws IOException{
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;

    }

    public boolean isConnected(){
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Activity.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected())
            return true;
        else
            return false;
    }

    private class HttpAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {

            return GET(urls[0]);
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            //Toast.makeText(getBaseContext(), "Received!", Toast.LENGTH_LONG).show();
            etResponse.setText(result);
            if(custom_ip_address==null) return;
            //parse JSON object
            JSONObject json = null;
            JSONObject jsonPlugins = null;
            JSONArray jsonArray[][] = null; // get articles array
            try {
                json = new JSONObject(result);
                jsonPlugins = json.getJSONObject("plugin");
                //Toast.makeText(getBaseContext(), jsonPlugins.getString("pretty_name"), Toast.LENGTH_LONG).show();

                /*
                articles.length(); // --> 2
                articles.getJSONObject(0); // get first article in the array
                articles.getJSONObject(0).names(); // get first article keys [title,url,categories,tags]
                articles.getJSONObject(0).getString("url"); // return an article url
                */
                //print JSON elements
                String jsonMessage = "Das aktuelle Plugin heißt:  " + jsonPlugins.getString("pretty_name") + "\nSeine Funktion:  "+ jsonPlugins.getString("description");
                etResponse.setText(jsonMessage);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    /*
    //TODO: trying to pass received UDP broadcast by invoking an AsyncTask
    private class UDPReceiverAsyncTask extends AsyncTask<String, Void, String> {
        // see solution @ https://stackoverflow.com/questions/17308729/send-broadcast-udp-but-not-receive-it-on-other-android-devices

        //DatagramSocket socket;
        public int port;

//        public void UDPReceiver(int port) {
//            this.port = port;
//        }
        @Override
        protected String doInBackground(String... strings) {  //receives port in String format as single argument
            try {
                //this.port = Integer.valueOf(strings[0]);
                //Keep a socket open to listen to all the UDP traffic that is destined for this port
                socket = new DatagramSocket(1234, InetAddress.getByName("0.0.0.0"));
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

                    socket.disconnect();

                    //return received UDP message
                    return data;
                }
            } catch (IOException ex) {
                Log.i("UDPReceiver", "IOException " + ex.getMessage());
            }
            socket.disconnect();
            return null;
        }

        @Override
        protected void onPostExecute(String results) {
            if(results!=null) {
                etUDPResponse.setText(results);
            }
            else {
                etUDPResponse.setText("No UDP Broadcast received until timeout.");
            }

        }
    }

    */


    // compare broadcast handling example @ https://gist.github.com/Antarix/8131277
    @Override
    protected void onPause() {
        // Unregister since the activity is paused.
        LocalBroadcastManager.getInstance(this).unregisterReceiver(
                mMessageReceiver);
        super.onPause();
    }

    @Override
    protected void onResume() {
        // Register to receive messages.
        // We are registering an observer (mMessageReceiver) to receive Intents
        // with actions named "UDPReceiver-Event".
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mMessageReceiver, new IntentFilter("UDPReceiver-Event"));
        Log.i("LocalBroadcastManager","Listening to UDPReceiver-Events");
        super.onResume();
    }

    // Our handler for received Intents. This will be called whenever an Intent
    // with an action named "UDPReceiver-Event" is broadcasted.
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            // Get extra data included in the Intent
            String message = intent.getStringExtra(Constants.UDP_RECEIVER_STRING);   //gets the Extra tagged with UDP_RECEIVER_STRING
            Log.d("receiver", "Got message: " + message);
            //Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
            EditText editText = findViewById(R.id.etUDPResponse);
            editText.setText(message);
        }
    };

    //Messaging service to receive UDP receiver detection calls from thread
    public class MessageService extends Service {

        @Override
        public IBinder onBind(Intent intent) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            // TODO Auto-generated method stub
            sendMessage();
            return super.onStartCommand(intent, flags, startId);
        }

        // Send an Intent with an action named "UDPReceiver-Event". The Intent
        // sent should
        // be received by the ReceiverActivity.
        //TODO: currently not in use, just kept as a handy example
        private void sendMessage() {
            Log.d("sender", "Broadcasting message");
            Intent intent = new Intent("UDPReceiver-Event");
            // You can also include some extra data.
            intent.putExtra("message", "This is my message!");  //TODO: make sure the [name] "message" is replicated in the getStringExtra() command of the receiver
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }

    }
/*
    private class UDPSenderAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {  //receives port in String format as single argument
            try {
                String udp_broadcast_message = strings[0];  //message to send
                int target_port = Integer.parseInt(strings[1]);
                UDPSender udpSender = new UDPSender(MainActivity.this);
                int openedPort = udpSender.sendBroadcast(udp_broadcast_message, target_port);
                //return Integer.toString(openedPort);
                //here comes the receiver part
                return startReceiver(openedPort);


            } catch(Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        public void sendUDPBroadcast(String udp_broadcast_message, int target_port) {       //constructor-type front for the task to allow calling with string/int combination
            new UDPSenderAsyncTask().execute(udp_broadcast_message,String.valueOf(target_port));
        }

        @Override
        protected void onPostExecute(String result) {
            //after sending a discovery beacon, start to listen for a reply by the wordclock
            if(Integer.parseInt(result) != -1)
                startReceiver(Integer.parseInt(result));         //listen on port opened by UDP sender for a reply by the Wordclock
            else
                Log.e("UDPReceiver","UDP Receiver can not be initialized - UDPSender returned no valid openedPort (-1)");
        }

    }
*/
 /*
    private String startReceiver(int openedPort) {
        //DatagramSocket socket = null;
        try {
            //this.port = Integer.valueOf(strings[0]);
            //Keep a socket open to listen to all the UDP traffic that is destined for this port
            socket = new DatagramSocket(1234, InetAddress.getByName("0.0.0.0"));

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

                socket.close();

                //return received UDP message
                return data;
            }
        } catch (IOException ex) {
            Log.i("UDPReceiver", "IOException " + ex.getMessage());
        }
        return null;
    }
 */

    //TODO: implement Multicast support:  https://www.kompf.de/java/multicast.html

}
