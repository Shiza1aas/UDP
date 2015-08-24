package com.example.shahjahan.udp;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "NETWORK_INFO";
    String ip_address;
    String subnet_mask;
    String broadcast_address;
    Integer PORT = 8888;
    private static final int DISCOVERY_PORT = 8889;
    byte[] data = ("Md. Shahjahan").getBytes();
    EditText name_of_the_device;
    ListView all_contacts;
    ArrayList<String> All_Contacts = new ArrayList<>();
    ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        name_of_the_device = (EditText) findViewById(R.id.name_of_the_device);
        all_contacts = (ListView) findViewById(R.id.all_contacts);


//        Toast.makeText(this, "I am before the thread", Toast.LENGTH_LONG).show();
        //    Starting a thread which will send packets in background...
        Log.d(TAG, "I am above thread.");

        startNewThread();

        Log.d(TAG, "I am out of  thread.");

//        Toast.makeText(this, "I am after the thread", Toast.LENGTH_LONG).show();


    }

    public void startNewThread()
    {
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    while (true) {
                        sleep(1000);
//                        Toast.makeText(getApplicationContext(), "I am after the thread", Toast.LENGTH_LONG).show();
                        try {
                            data = "Shahjahan".getBytes();
                            DatagramSocket socket = new DatagramSocket();
                            DatagramPacket packet = new DatagramPacket(data, data.length,
                                    getBroadcastAddress(), DISCOVERY_PORT);
                            socket.setBroadcast(true);
                            socket.send(packet);
                            Log.d(TAG, "Sent successfully.");
                            socket.disconnect();
                            socket.close();
                        } catch (SocketException e) {
                            e.printStackTrace();
                        } catch (IOException ie) {
                            ie.printStackTrace();
                        }


                        Log.d(TAG, "I am in thread.");
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        thread.start();

    }


    private String getMobileNumber() {
        TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        return tm.getLine1Number();
    }

//    get ip address from dhcp get info

    InetAddress getBroadcastAddress() throws IOException {
        WifiManager wifi = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        DhcpInfo dhcp = wifi.getDhcpInfo();
        // handle null somehow

        int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
        byte[] quads = new byte[4];
        for (int k = 0; k < 4; k++)
            quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
        return InetAddress.getByAddress(quads);
    }

//    //    Following method sends packet to the reciever side.
//    public void sendPacket(View view) throws IOException {
////        make a datagram packet to send the data
//        Toast.makeText(getApplicationContext(), "sending packets", Toast.LENGTH_LONG).show();
//
//        new SendPacket().execute(name_of_the_device.getText().toString());
//
//    }

    public void receivePacket(View view) throws IOException {

        Toast.makeText(getApplicationContext(), "recieving packets", Toast.LENGTH_LONG).show();
        new ReceivePacket().execute();
    }
//
//    class SendPacket extends AsyncTask<String, Void, Void> {
//
//
//        @Override
//        protected Void doInBackground(String... voids) {
//            try {
//                data = voids[0].getBytes();
//                DatagramSocket socket = new DatagramSocket();
//                DatagramPacket packet = new DatagramPacket(data, data.length,
//                        getBroadcastAddress(), DISCOVERY_PORT);
//                socket.setBroadcast(true);
//                socket.send(packet);
//                Log.d(TAG, "Sent successfully.");
//                socket.disconnect();
//                socket.close();
//            } catch (SocketException e) {
//                e.printStackTrace();
//            } catch (IOException ie) {
//                ie.printStackTrace();
//            }
//
//
//            return null;
//        }
//    }

    class ReceivePacket extends AsyncTask<Void, Void, String> {
        DatagramPacket packet;

        @Override
        protected String doInBackground(Void... voids) {
            try {
                DatagramSocket socket = new DatagramSocket(DISCOVERY_PORT);
                socket.setBroadcast(true);
                byte[] buf = new byte[1024];
                packet = new DatagramPacket(buf, buf.length);
                Log.d(TAG, "I am in receiving.");
                socket.receive(packet);
                Log.d(TAG, "I have received packets.");
                socket.disconnect();
                socket.close();

            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "I am unable to receive.");
            }
            return new String(packet.getData());
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Toast.makeText(getApplicationContext(), "The message is" + s, Toast.LENGTH_LONG).show();
            All_Contacts.add(s);
            ArrayAdapter adapter = new ArrayAdapter(getApplicationContext(), android.R.layout.simple_list_item_1, All_Contacts);
            all_contacts.setAdapter(adapter);
        }
    }

}


