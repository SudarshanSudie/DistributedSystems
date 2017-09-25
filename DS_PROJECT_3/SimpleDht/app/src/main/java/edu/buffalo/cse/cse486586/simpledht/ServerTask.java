package edu.buffalo.cse.cse486586.simpledht;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import android.content.ContentResolver;
import android.content.ContentProvider;
import android.content.ContentValues;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by sudie on 4/4/17.
 */

class ServerTask extends AsyncTask<ServerSocket, String, Void> {

    public Context context;

    public static String query_key;
    public static String query_val;

    public static String port_identity;

    public void setContext(Context con) {
        this.context = con;
    }

    //The specified uri using which any provider can access the data
    Uri mUri = buildUri("content", "edu.buffalo.cse.cse486586.simpledht.provider");

    //The BuildUri function for appending the two parameters as a single uri
    private Uri buildUri(String scheme, String authority) {
        Uri.Builder uriBuilder = new Uri.Builder();
        uriBuilder.authority(authority);
        uriBuilder.scheme(scheme);
        return uriBuilder.build();
    }

    @Override
    protected Void doInBackground(ServerSocket... sockets)
    {
        //Initialize class object to access its members
        //SimpleDhtProvider simpleDhtProvider = new SimpleDhtProvider();

        Log.d("ServerTask","Entered Server Task");

        String myPort = SimpleDhtProvider.myPort;
        String join_chord_request_port = SimpleDhtProvider.join_chord_request;

        ServerSocket serverSocket = sockets[0];
        int count = 0;

        //Infinite while loop
        while (count < 9)
        {
            try {
                //Accepting connection
                Socket respondedClientSocket = serverSocket.accept();
                Log.d("ServerTask","Established Connection with Client");

                //Getting what the client sent using inputstream
                InputStream input_stream = respondedClientSocket.getInputStream();
                InputStreamReader input_stream_reader = new InputStreamReader(input_stream);
                BufferedReader buffered_reader = new BufferedReader(input_stream_reader);

                //Sending acknowledgement to client through outputstream
                OutputStream output_stream = respondedClientSocket.getOutputStream();
                OutputStreamWriter output_stream_writer = new OutputStreamWriter(output_stream);
                BufferedWriter buffered_writer = new BufferedWriter(output_stream_writer);

                String msgReceived = buffered_reader.readLine();
                String[] msg_port = msgReceived.split("-");

                if(msg_port[0].equals("JoiningNetwork") && myPort.equals(join_chord_request_port))
                {
                    Log.d("ServerTask","Inside If - MyPort: "+myPort);

                    Log.d("ServerTask","I am 5554 , Adding: "+msg_port[1]+" to live ports");
                    if(!SimpleDhtProvider.livePorts.contains(msg_port[1]))
                    {
                        SimpleDhtProvider.livePorts.add(msg_port[1]);
                    }

                    ArrayList<String> newList = new ArrayList<String>(SimpleDhtProvider.livePorts);
                    String portInfo = "";
                    for(int i = 0;i<newList.size();i++)
                    {
                        portInfo += newList.get(i)+"-";
                    }
                    Log.d("ServerTask","I am 5554 , sending live ports "+portInfo+"to: "+msg_port[1]);
                    portInfo = portInfo + "\n";
                    buffered_writer.write(portInfo);
                    buffered_writer.flush();
                }
                else if(msg_port[0].equals("SetSuccessor"))
                {
                    Log.d("ServerTask","SetSuccessor - MyPort: "+myPort);
                    SimpleDhtProvider.successor_port = msg_port[1];
                    Log.d("ServerTask","Setting "+msg_port[1]+" as my successor");
                    Log.d("ClientTask"+myPort,"My successor Port: "+SimpleDhtProvider.successor_port+"My Predecessor port: "+SimpleDhtProvider.predecessor_port);
                }
                else if(msg_port[0].equals("SetPredecessor"))
                {
                    Log.d("ServerTask","SetPredecessor - MyPort: "+myPort);
                    SimpleDhtProvider.predecessor_port = msg_port[1];
                    Log.d("ServerTask","Setting "+msg_port[1]+" as my predecessor");
                    Log.d("ClientTask"+myPort,"My successor Port: "+SimpleDhtProvider.successor_port+"My Predecessor port: "+SimpleDhtProvider.predecessor_port);
                }
                else if(msg_port[0].equals("CheckIfYouCanInsert"))
                {
                    Log.d("ServerTask","Insert check - MyPort: "+myPort);
                    String key = msg_port[1];
                    String value = msg_port[2];
                    ContentValues keyValueToInsert = new ContentValues();
                    keyValueToInsert.put("key", key);
                    keyValueToInsert.put("value",value);

                    Uri newUri = context.getContentResolver().insert(
                            mUri,    // assume we already created a Uri object with our provider URI
                            keyValueToInsert
                    );
                    respondedClientSocket.close();
                }
                else if(msg_port[0].equals("CheckIfYouHaveThis"))
                {
                    Log.d("ServerTask","Query check - MyPort: "+SimpleDhtProvider.myPort);
                    String keyForQuery = msg_port[1];
                    Log.d("ServerTask","Key that was sent to me: "+keyForQuery);
                    this.port_identity = msg_port[2];

                    Cursor resultCursor = context.getContentResolver().query(mUri, null,keyForQuery, null, null);
                    Log.d("ServerTask","Control reached here ");

                    if(resultCursor.moveToFirst() != false)
                    {
//                        resultCursor.moveToFirst();
                        query_key = resultCursor.getString(resultCursor.getColumnIndex("key"));
                        query_val = resultCursor.getString(resultCursor.getColumnIndex("value"));

                        Log.d("ServerTask","key: "+ServerTask.query_key+" and value: "+ServerTask.query_val);
                        Log.d("ServerTask","I am "+SimpleDhtProvider.myPort+" am Creating Socket with "+port_identity);

                        respondedClientSocket.close();

                        Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                                Integer.parseInt(port_identity));

                        //Sending acknowledgement to client through outputstream
                        OutputStream output_stream1 = socket.getOutputStream();
                        OutputStreamWriter output_stream_writer1 = new OutputStreamWriter(output_stream1);
                        BufferedWriter buffered_writer1 = new BufferedWriter(output_stream_writer1);

                        String msgToSend = "HereIsTheKeyVal"+"-"+query_key+"-"+query_val+"\n";

                        Log.d("ServerTask",msgToSend);
                        buffered_writer1.write(msgToSend);
                        buffered_writer1.flush();

                        socket.close();
                    }
                    this.port_identity = null;
                }
                else if(msg_port[0].equals("HereIsTheKeyVal"))
                {
                    //Log.d("Servertask","Populating cursor with key: "+ServerTask.query_key+" and value: "+ServerTask.query_val);
                    Log.d("ServerTask","key "+msg_port[1]+"val "+msg_port[2]);
                    SimpleDhtProvider.matrixCursor.addRow(new String[]{msg_port[1],msg_port[2]});
                    SimpleDhtProvider.check = true;

                    respondedClientSocket.close();
                }
                else if(msg_port[0].equals("QueryEverythingForMe"))
                {
                    Log.d("ServerTask","Querying Everything");

                    Log.d("ServerTask","Creating Cursor");

                    Cursor resultCursor = context.getContentResolver().query(mUri, null,"@", null, null);

                    Log.d("ServerTask","Created cursor");

                    if(resultCursor.moveToFirst() != false)
                    {
                        Log.d("ServerTask","Populating cursor");
                        query_key = resultCursor.getString(resultCursor.getColumnIndex("key"))+"@";
                        query_val = resultCursor.getString(resultCursor.getColumnIndex("value"))+"@";

                        resultCursor.moveToNext();

                        for(;!resultCursor.isAfterLast();resultCursor.moveToNext())
                        {
                            query_key = query_key+resultCursor.getString(resultCursor.getColumnIndex("key"))+"@";
                            query_val = query_val+resultCursor.getString(resultCursor.getColumnIndex("value"))+"@";
                        }
                    }
                    Log.d("ServerTask","key: "+ServerTask.query_key);
                    Log.d("ServerTask","value "+ServerTask.query_val);

                    Log.d("ServerTask","I am "+SimpleDhtProvider.myPort+" am Creating Socket with "+msg_port[1]);

                    respondedClientSocket.close();

                    Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                            Integer.parseInt(msg_port[1]));

                    //Sending acknowledgement to client through outputstream
                    OutputStream output_stream1 = socket.getOutputStream();
                    OutputStreamWriter output_stream_writer1 = new OutputStreamWriter(output_stream1);
                    BufferedWriter buffered_writer1 = new BufferedWriter(output_stream_writer1);

                    String msgToSend = "QueriedForYou"+"-"+query_key+"-"+query_val+"-"+SimpleDhtProvider.successor_port+"\n";
                    query_key = "";
                    query_val = "";
                    Log.d("ServerTask",msgToSend);
                    buffered_writer1.write(msgToSend);
                    buffered_writer1.flush();

                    Log.d("ServerTask","Control reached here ");
                    socket.close();
                }
                else if(msg_port[0].equals("QueriedForYou"))
                {
                    Log.d("ServerTask","Inside queried for you");
                    if(!msg_port[1].equals("null") || !msg_port[2].equals("null"))
                    {
                        String[] keys = msg_port[1].split("@");
                        String[] vals = msg_port[2].split("@");

                        for(int i = 0;i<keys.length;i++)
                        {
                            SimpleDhtProvider.matrixCursor.addRow(new String[]{keys[i],vals[i]});
                        }
                        respondedClientSocket.close();

                        if(msg_port[3].equals(SimpleDhtProvider.myPort))
                        {
                            SimpleDhtProvider.check = true;
                        }
                        else
                        {
                            Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                                    Integer.parseInt(msg_port[3]));

                            String msgToSend = "QueryEverythingForMe"+"-"+myPort+"\n";

                            //Sending acknowledgement to client through outputstream
                            OutputStream output_stream1 = socket.getOutputStream();
                            OutputStreamWriter output_stream_writer1 = new OutputStreamWriter(output_stream1);
                            BufferedWriter buffered_writer1 = new BufferedWriter(output_stream_writer1);

                            buffered_writer1.write(msgToSend);
                            buffered_writer1.close();
                            socket.close();
                        }
                    }
                    else
                    {
                        respondedClientSocket.close();

                        if(msg_port[3].equals(SimpleDhtProvider.myPort))
                        {
                            SimpleDhtProvider.check = true;
                        }
                        else
                        {
                            Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                                    Integer.parseInt(msg_port[3]));

                            String msgToSend = "QueryEverythingForMe"+"-"+myPort+"\n";

                            //Sending acknowledgement to client through outputstream
                            OutputStream output_stream1 = socket.getOutputStream();
                            OutputStreamWriter output_stream_writer1 = new OutputStreamWriter(output_stream1);
                            BufferedWriter buffered_writer1 = new BufferedWriter(output_stream_writer1);

                            buffered_writer1.write(msgToSend);
                            buffered_writer1.close();
                            socket.close();
                        }
                    }
                }
                //respondedClientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return null;
    }
}
