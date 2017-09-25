package edu.buffalo.cse.cse486586.simpledht;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;


/**
 * Created by sudie on 4/4/17.
 */

public class ClientTask extends AsyncTask<String, Void, Void> {
    @Override
    protected Void doInBackground(String... msgs) {

        Log.d("ClientTask","Entered Client Task");
        String myPort = SimpleDhtProvider.myPort;
        String msg = msgs[0];

        try {
            //New node join
            if(!myPort.equals(SimpleDhtProvider.join_chord_request) && (SimpleDhtProvider.predecessor_port == null || SimpleDhtProvider.successor_port == null) && msg.equals("NewNodeJoining"))
            {
                //Establish connection with 11108
                Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                        Integer.parseInt(SimpleDhtProvider.join_chord_request));

                //Using outputstream to write to server
                OutputStream output_stream = socket.getOutputStream();
                OutputStreamWriter output_stream_writer = new OutputStreamWriter(output_stream);
                BufferedWriter buffered_writer = new BufferedWriter(output_stream_writer);

                Log.d("ClientTask","JoiningNetwork");
                String msgToSend = "JoiningNetwork";
                msgToSend = msgToSend+"-"+myPort+"\n";

                Log.d("ClientTask","Sending MyPort"+myPort);
                buffered_writer.write(msgToSend);
                buffered_writer.flush();

                //Using inputstream to write to server
                InputStream input_stream = socket.getInputStream();
                InputStreamReader input_stream_reader = new InputStreamReader(input_stream);
                BufferedReader buffered_reader = new BufferedReader(input_stream_reader);

                String ports = buffered_reader.readLine();
                Log.d("ClientTask","Received Live Port ID's from 5554 "+ports);
                String[] port_list = ports.split("-");

                //Close connection with 5554
                Log.d("ClientTask","Closing socket connection with 5554");
                socket.close();

                ArrayList<NodeObject> node_pop = new ArrayList<NodeObject>();

                Log.d("ClientTask","Flooding the ArrayList");

                for(int i = 0;i < port_list.length;i++)
                {
                    Integer port = Integer.parseInt(port_list[i])/2;
                    String key = String.valueOf(port);
                    String hash = SimpleDhtProvider.genHash(String.valueOf(port));

                    NodeObject n = new NodeObject(key,hash);
                    node_pop.add(n);
                }
                Integer port = Integer.parseInt(myPort)/2;
                String key = String.valueOf(port);
                String hash = SimpleDhtProvider.genHash(String.valueOf(port));

                NodeObject n = new NodeObject(key,hash);

                Collections.sort(node_pop);

                int index = Node_Pop.Node_Pop_Util.search(node_pop,n);
                int pIndex = Node_Pop.Node_Pop_Util.getPrev(node_pop,n);
                int sIndex = Node_Pop.Node_Pop_Util.getNext(node_pop,n);

                Log.d("ClientTask","myIndex: "+index);
                Log.d("ClientTask","pIndex: "+pIndex);
                Log.d("ClientTask","sIndex: "+sIndex);

                SimpleDhtProvider.predecessor_port = String.valueOf(Integer.parseInt(node_pop.get(pIndex).getKey())*2);
                Log.d("ClientTask","My Predecessor Port: "+String.valueOf(Integer.parseInt(node_pop.get(pIndex).getKey())*2));

                SimpleDhtProvider.successor_port = String.valueOf(Integer.parseInt(node_pop.get(sIndex).getKey())*2);
                Log.d("ClientTask","My Successor Port: "+String.valueOf(Integer.parseInt(node_pop.get(sIndex).getKey())*2));

                Log.d("ClientTask"+myPort,"My successor Port: "+SimpleDhtProvider.successor_port+"My Predecessor port: "+SimpleDhtProvider.predecessor_port);

                Log.d("ClientTask","Going to connect to my predecessor");
                Socket socket1 = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                        Integer.parseInt(SimpleDhtProvider.predecessor_port));

                //Using outputstream to write to server
                OutputStream output_stream1 = socket1.getOutputStream();
                OutputStreamWriter output_stream_writer1 = new OutputStreamWriter(output_stream1);
                BufferedWriter buffered_writer1 = new BufferedWriter(output_stream_writer1);

                Log.d("ClientTask","Asking predeccor to set me as successor");
                String sendToPredecessor = "SetSuccessor"+"-"+myPort+"\n";
                buffered_writer1.write(sendToPredecessor);
                buffered_writer1.flush();

                Log.d("ClientTask","Closing Connection with predecessor");
                socket1.close();

                Log.d("ClientTask","Going to connect to my successor");
                Socket socket2 = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                        Integer.parseInt(SimpleDhtProvider.successor_port));

                //Using outputstream to write to server
                OutputStream output_stream2 = socket2.getOutputStream();
                OutputStreamWriter output_stream_writer2 = new OutputStreamWriter(output_stream2);
                BufferedWriter buffered_writer2 = new BufferedWriter(output_stream_writer2);

                Log.d("ClientTask","Asking successor to set me as predeccor");
                String sendToSuccessor = "SetPredecessor"+"-"+myPort+"\n";
                buffered_writer2.write(sendToSuccessor);
                buffered_writer2.flush();

                Log.d("ClientTask","Closing Connection with successor");
                socket2.close();
            }
            else
            {
                Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                        Integer.parseInt(SimpleDhtProvider.successor_port));

                //Using outputstream to write to server
                OutputStream output_stream = socket.getOutputStream();
                OutputStreamWriter output_stream_writer = new OutputStreamWriter(output_stream);
                BufferedWriter buffered_writer = new BufferedWriter(output_stream_writer);

                Log.d("ClientTask","I am "+SimpleDhtProvider.myPort+" Sending to server for processing: "+SimpleDhtProvider.successor_port);
                String msgToSend = msg+"\n";
                Log.d("ClientTask",msgToSend);
                buffered_writer.write(msgToSend);
                buffered_writer.flush();

                socket.close();
            }
        }catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return null;
    }
}