package com.example.shahjahan.udp;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
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
import android.widget.AdapterView;
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
    public byte[] buffer;
    private int port=50005;         //which port??
    AudioRecord recorder;

    //Audio Configuration.
    private int sampleRate = 44100;      //How much will be ideal?
    private int channelConfig = AudioFormat.CHANNEL_CONFIGURATION_MONO;
    private int audioFormat = AudioFormat.ENCODING_PCM_8BIT;
    boolean status = true;
    private AudioTrack speaker;

    private static final String LOG_TAG = "AudioCall";
    private static final int SAMPLE_RATE = 8000; // Hertz
    private static final int SAMPLE_INTERVAL = 20; // Milliseconds
    private static final int SAMPLE_SIZE = 2; // Bytes
    private static final int BUF_SIZE = SAMPLE_INTERVAL * SAMPLE_INTERVAL * SAMPLE_SIZE * 2; //Bytes
    private InetAddress address; // Address to call
//    private int port = 50000; // Port the packets are addressed to
    private boolean mic = false; // Enable mic?
    private boolean speakers = false; // Enable speakers?
    //Audio Configuration.


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        all_contacts = (ListView) findViewById(R.id.all_contacts);


//        Toast.makeText(this, "I am before the thread", Toast.LENGTH_LONG).show();
        //    Starting a thread which will send packets in background...
        Log.d(TAG, "I am above thread.");

        startNewThread();

        Log.d(TAG, "I am out of  thread.");

//        Toast.makeText(this, "I am after the thread", Toast.LENGTH_LONG).show();


    }

    public void startNewThread() {
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    while (true) {
                        sleep(10000);
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


//    To capture packets to see all online devices...


    public void receivePacket(View view) throws IOException {

        Toast.makeText(getApplicationContext(), "recieving packets", Toast.LENGTH_LONG).show();
        new ReceivePacket().execute();
    }

    public void disconnect(View view) {
        mic = false;
        speakers = false;

        Toast.makeText(this,"The call has been disconnected.",Toast.LENGTH_LONG).show();


    }

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
            return new String(packet.getAddress().getHostAddress());
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Toast.makeText(getApplicationContext(), "The message is" + s, Toast.LENGTH_LONG).show();

            if (!All_Contacts.contains(s)) {
                All_Contacts.add(s);
            }

            ArrayAdapter adapter = new ArrayAdapter(getApplicationContext(), android.R.layout.simple_list_item_1, All_Contacts);
            all_contacts.setAdapter(adapter);

            all_contacts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Toast.makeText(getApplicationContext(), "The call is made to" + All_Contacts.get(i), Toast.LENGTH_LONG).show();
//                Trying to call at the given ip

                    startMic(All_Contacts.get(i));

                }
            });

        }
    }
    public void startMic(final String IP_Address) {
        // Creates the thread for capturing and transmitting audio
        mic = true;
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                // Create an instance of the AudioRecord class
                Log.i(LOG_TAG, "Send thread started. Thread id: " + Thread.currentThread().getId());
                AudioRecord audioRecorder = new AudioRecord (MediaRecorder.AudioSource.MIC, SAMPLE_RATE,
                        AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT,
                        AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)*10);
                int bytes_read = 0;
                int bytes_sent = 0;
                byte[] buf = new byte[BUF_SIZE];
                try {
                    // Create a socket and start recording
//                    Log.i(LOG_TAG, "Packet destination: " + address.toString());
                    DatagramSocket socket = new DatagramSocket();
                    audioRecorder.startRecording();
                    while(mic) {
                        // Capture audio from the mic and transmit it
                        bytes_read = audioRecorder.read(buf, 0, BUF_SIZE);
                        DatagramPacket packet = new DatagramPacket(buf, bytes_read, InetAddress.getByName(IP_Address), port);
                        socket.send(packet);
                        bytes_sent += bytes_read;
                        Log.i(LOG_TAG, "Total bytes sent: " + bytes_sent);
                        Thread.sleep(SAMPLE_INTERVAL, 0);
                    }
                    // Stop recording and release resources
                    audioRecorder.stop();
                    audioRecorder.release();
                    socket.disconnect();
                    socket.close();
                    mic = false;
                    return;
                }
                catch(InterruptedException e) {

                    Log.e(LOG_TAG, "InterruptedException: " + e.toString());
                    mic = false;
                }
                catch(SocketException e) {

                    Log.e(LOG_TAG, "SocketException: " + e.toString());
                    mic = false;
                }
                catch(UnknownHostException e) {

                    Log.e(LOG_TAG, "UnknownHostException: " + e.toString());
                    mic = false;
                }
                catch(IOException e) {

                    Log.e(LOG_TAG, "IOException: " + e.toString());
                    mic = false;
                }
            }
        });
        thread.start();
    }


    public void startReceiving(View view)
    {


        startSpeakers();
    }

    public void startSpeakers() {
        // Creates the thread for receiving and playing back audio
        if(!speakers) {

            speakers = true;
            Thread receiveThread = new Thread(new Runnable() {

                @Override
                public void run() {
                    // Create an instance of AudioTrack, used for playing back audio
                    Log.i(LOG_TAG, "Receive thread started. Thread id: " + Thread.currentThread().getId());
                    AudioTrack track = new AudioTrack(AudioManager.STREAM_MUSIC, SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO,
                            AudioFormat.ENCODING_PCM_16BIT, BUF_SIZE, AudioTrack.MODE_STREAM);
                    track.play();
                    try {
                        // Define a socket to receive the audio
                        DatagramSocket socket = new DatagramSocket(port);
                        byte[] buf = new byte[BUF_SIZE];
                        while(speakers) {
                            // Play back the audio received from packets
                            DatagramPacket packet = new DatagramPacket(buf, BUF_SIZE);
                            socket.receive(packet);
                            Log.i(LOG_TAG, "Packet received: " + packet.getLength());
                            track.write(packet.getData(), 0, BUF_SIZE);
                        }
                        // Stop playing back and release resources
                        socket.disconnect();
                        socket.close();
                        track.stop();
                        track.flush();
                        track.release();
                        speakers = false;
                        return;
                    }
                    catch(SocketException e) {

                        Log.e(LOG_TAG, "SocketException: " + e.toString());
                        speakers = false;
                    }
                    catch(IOException e) {

                        Log.e(LOG_TAG, "IOException: " + e.toString());
                        speakers = false;
                    }
                }
            });
            receiveThread.start();
        }
    }

//    public void receiveCall() {
//
//        Thread receiveThread = new Thread (new Runnable() {
//
//            @Override
//            public void run() {
//
//                try {
//
//                    DatagramSocket socket = new DatagramSocket(50005);
//                    Log.d("VR", "Socket Created");
//
//
//                    byte[] buffer = new byte[256];
//
//
//                    //minimum buffer size. need to be careful. might cause problems. try setting manually if any problems faced
//                    int minBufSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat);
//
//                    speaker = new AudioTrack(AudioManager.STREAM_MUSIC,sampleRate,channelConfig,audioFormat,minBufSize,AudioTrack.MODE_STREAM);
//
//                    speaker.play();
//
//                    while( status ) {
//                        try {
//
//
//                            DatagramPacket packet = new DatagramPacket(buffer,buffer.length);
//                            socket.receive(packet);
//                            Log.d("VR", "Packet Received");
//
//                            //reading content from packet
//                            buffer=packet.getData();
//                            Log.d("VR", "Packet data read into buffer");
//
//                            //sending data to the Audiotrack obj i.e. speaker
//                            speaker.write(buffer, 0, minBufSize);
//                            Log.d("VR", "Writing buffer content to speaker");
//
//                        } catch(IOException e) {
//                            Log.e("VR","IOException");
//                        }
//                    }
//
//
//                } catch (SocketException e) {
//                    Log.e("VR", "SocketException");
//                }
//
//
//            }
//
//        });
//        receiveThread.start();
//    }


}


