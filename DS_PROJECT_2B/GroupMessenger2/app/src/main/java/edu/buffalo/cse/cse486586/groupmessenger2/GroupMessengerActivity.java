package edu.buffalo.cse.cse486586.groupmessenger2;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.method.ScrollingMovementMethod;
import android.util.FloatMath;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

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
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.security.cert.CollectionCertStoreParameters;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.PriorityQueue;

/**
 * GroupMessengerActivity is the main Activity for the assignment.
 *
 * @author stevko
 *
 */

/** ------REFERENCES-------
 * http://www.sqlite.org/datatype3.html
 https://developer.android.com/guide/topics/providers/content-provider-basics.html
 https://developer.android.com/reference/android/database/sqlite/SQLiteOpenHelper.html
 https://developer.android.com/reference/android/database/sqlite/SQLiteDatabase.html
 https://developer.android.com/training/basics/data-storage/databases.html
 https://developer.android.com/reference/android/database/sqlite/SQLiteDatabase.html#query(java.lang.String, java.lang.String[], java.lang.String, java.lang.String[], java.lang.String, java.lang.String, java.lang.String)
 https://developer.android.com/reference/android/database/sqlite/SQLiteDatabase.html#insertWithOnConflict(java.lang.String,%20java.lang.String,%20android.content.ContentValues,%20int)
 http://developer.android.com/guide/topics/providers/content-providers.html
 http://developer.android.com/reference/android/content/ContentProvider.html
 https://docs.oracle.com/javase/7/docs/api/java/util/PriorityQueue.html
 http://stackoverflow.com/questions/8077530/android-get-current-timestamp
 http://stackoverflow.com/questions/18628584/adb-install-returns-error-protocol-fault-no-status
 https://developer.android.com/reference/java/net/SocketTimeoutException.html
 https://developer.android.com/reference/java/net/Socket.html
 **/

public class GroupMessengerActivity extends Activity {

    String myPort = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_messenger);

        /*
         * TODO: Use the TextView to display your messages. Though there is no grading component
         * on how you display the messages, if you implement it, it'll make your debugging easier.
         */
        TextView tv = (TextView) findViewById(R.id.textView1);
        tv.setMovementMethod(new ScrollingMovementMethod());

        /*
         * Registers OnPTestClickListener for "button1" in the layout, which is the "PTest" button.
         * OnPTestClickListener demonstrates how to access a ContentProvider.
         */

        findViewById(R.id.button1).setOnClickListener(
                new OnPTestClickListener(tv, getContentResolver()));

        /*
         * TODO: You need to register and implement an OnClickListener for the "Send" button.
         * In your implementation you need to get the message from the input box (EditText)
         * and send it to other AVDs.
         */

        TelephonyManager tel = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
        myPort = String.valueOf((Integer.parseInt(portStr) * 2));

        //Server task initialization
        try {
            ServerSocket serverSocket = new ServerSocket(10000);
            new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);
        } catch (IOException e) {
            Log.e("GroupMessengerActivity", "Can't create a ServerSocket");
            return;
        }

        //OnClickListener on the SEND butoon and retrieving the text
        findViewById(R.id.button4).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                //Log.d("OnCLick:Send "," Port Number"+myPort);
                EditText editText = (EditText) findViewById(R.id.editText1);
                String msg = editText.getText().toString() + "\n";
                //Log.d("OnCLick:Send "," Message"+msg);
                editText.setText("");
                new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, msg, myPort);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_group_messenger, menu);
        return true;
    }

    //MsgSequence to insert as key
    int msg_count = 0;

    //The specified uri using which any provider can access the data
    Uri mUri = buildUri("content", "edu.buffalo.cse.cse486586.groupmessenger2.provider");

    //The BuildUri function for appending the two parameters as a single uri
    private Uri buildUri(String scheme, String authority) {
        Uri.Builder uriBuilder = new Uri.Builder();
        uriBuilder.authority(authority);
        uriBuilder.scheme(scheme);
        return uriBuilder.build();
    }

    //Priority queue to handle the objects.
    PriorityQueue<MsgsObject> priority_queue = new PriorityQueue<MsgsObject>();
    int sequence_count = 0;

    //ServerTask defininition
    private class ServerTask extends AsyncTask<ServerSocket, String, Void>
    {
        @Override
        protected Void doInBackground(ServerSocket... sockets)
        {
            ServerSocket serverSocket = sockets[0];
            int count = 0;

                //Infinite while loop
                while (count < 9)
                {
                    /** ------REFERENCES-------
                     * http://www.sqlite.org/datatype3.html
                     https://developer.android.com/guide/topics/providers/content-provider-basics.html
                     https://developer.android.com/reference/android/database/sqlite/SQLiteOpenHelper.html
                     https://developer.android.com/reference/android/database/sqlite/SQLiteDatabase.html
                     https://developer.android.com/training/basics/data-storage/databases.html
                     https://developer.android.com/reference/android/database/sqlite/SQLiteDatabase.html#query(java.lang.String, java.lang.String[], java.lang.String, java.lang.String[], java.lang.String, java.lang.String, java.lang.String)
                     https://developer.android.com/reference/android/database/sqlite/SQLiteDatabase.html#insertWithOnConflict(java.lang.String,%20java.lang.String,%20android.content.ContentValues,%20int)
                     http://developer.android.com/guide/topics/providers/content-providers.html
                     http://developer.android.com/reference/android/content/ContentProvider.html
                     https://docs.oracle.com/javase/7/docs/api/java/util/PriorityQueue.html
                     http://stackoverflow.com/questions/8077530/android-get-current-timestamp
                     http://stackoverflow.com/questions/18628584/adb-install-returns-error-protocol-fault-no-status
                     https://developer.android.com/reference/java/net/SocketTimeoutException.html
                     https://developer.android.com/reference/java/net/Socket.html
                     **/

                    //Try-Catch to handle Socket timeout
                    try {
                        //Accepting client connection
                        //Log.e("SERVER : ", "Connection Accepted");
                        serverSocket.setSoTimeout(9000);

                        Socket respondedClientSocket = serverSocket.accept();

                        //Try-Catch to handle other exceptions like null and so on.
                        try {
                            //Getting what the client sent using inputstream
                            InputStream input_stream = respondedClientSocket.getInputStream();
                            InputStreamReader input_stream_reader = new InputStreamReader(input_stream);
                            BufferedReader buffered_reader = new BufferedReader(input_stream_reader);

                            //Sending acknowledgement to client through outputstream
                            OutputStream output_stream = respondedClientSocket.getOutputStream();
                            OutputStreamWriter output_stream_writer = new OutputStreamWriter(output_stream);
                            BufferedWriter buffered_writer = new BufferedWriter(output_stream_writer);

                            //String to recognize if it is stage1 or stage3
                            String stage_recognition = buffered_reader.readLine();

                            //If-Else block fo the same
                            if (stage_recognition.equals("Stage1"))
                            {
                                //Message from client
                                String msgStage1 = buffered_reader.readLine();
                                //msgObject.setMessage(msgStage1);
                                //Log.e("SERVER : ", "Stage-1 Received Msg : " + msgStage1);

                                //Time stamp from the client
                                String time_stamp_of_msg = buffered_reader.readLine();
                                long time_stamp = Long.valueOf(time_stamp_of_msg);
                                //msgObject.setTime_stamp_msg();
                                //Log.e("SERVER : ", "Stage-1 Received TimeStamp : " + (Long.valueOf(time_stamp_of_msg)));

                                //The sequence number which is going to be proposed
                                float proposedSeqNum = 0.00000f;
                                proposedSeqNum = (float) (sequence_count + (Double.valueOf(myPort)) / 100000);
                                sequence_count++;
                                //msgObject.setSequence_number(proposedSeqNum);


                                //Populating data into the object
                                MsgsObject msgObject = new MsgsObject(msgStage1, proposedSeqNum, time_stamp);
                                //Adding populated object from stage1 to the queue
                                priority_queue.add(msgObject);

                                //Stage-2 from server
                                //Log.e("SERVER : ", "Proposed sequence : " + proposedSeqNum);
                                buffered_writer.write(String.valueOf(proposedSeqNum) + "\n");
                                buffered_writer.flush();
                            }
                            else
                            {
                                //Stage-3 reception
                                String msgStage3 = buffered_reader.readLine();
                                //Log.e("SERVER : ", "Stage-3 Received Msg : " + msgStage3);
                                String time_stamp_of_msg_stage3 = buffered_reader.readLine();
                                long time_stamp_msg = Long.valueOf(time_stamp_of_msg_stage3);
                                //Log.e("SERVER : ", "Stage-3 Received timestamp : " + time_stamp_msg);
                                String sequence_number = buffered_reader.readLine();
                                Float accepted_sequence_number = Float.parseFloat(sequence_number);
                                //Log.e("SERVER : ", "Stage-3 Received approved sequence number : " + accepted_sequence_number);

                                //Updating the sequence number based on the maximu of what is seen and what is sent
                                if (accepted_sequence_number > sequence_count)
                                {
                                    sequence_count = accepted_sequence_number.intValue();
                                }

                                //Creating a new object to dump in all the values from the client side.
                                MsgsObject msgObjectFinal = new MsgsObject(msgStage3, accepted_sequence_number, time_stamp_msg, true);

                                //Iterating priority queue and removing the previous
                                Iterator<MsgsObject> iter = priority_queue.iterator();
                                while (iter.hasNext()) {
                                    MsgsObject current = iter.next();
                                    if (current.getTime_stamp_msg() == time_stamp_msg) {
                                        priority_queue.remove(current);
                                        break;
                                    }
                                }
                                //Inserting the object into the priority queue
                                priority_queue.add(msgObjectFinal);

                                //Inserting the head of the priority queue to the DB if it is marked delivaerable
                                while (priority_queue.peek() != null && priority_queue.peek().isDeliverable() == true) {
                                    ContentValues keyValueToInsert = new ContentValues();
                                    // inserting <”key-to-insert”, “value-to-insert”>
                                    keyValueToInsert.put("key", String.valueOf(msg_count++));
                                    keyValueToInsert.put("value", priority_queue.poll().getMessage());

                                    Uri newUri = getContentResolver().insert(
                                            mUri,    // assume we already created a Uri object with our provider URI
                                            keyValueToInsert
                                    );
                                }
                            }
                            //Log.e("SERVER : ", "Closing socket : " + msgToAck);
                            //respondedClientSocket.close();
                        }
                        catch(Exception e)
                        {
                            Log.e("WORST", "ERRRRORR");
                        }
                    }
                    catch (IOException exp)
                    {
                        Log.e("SERVER","AAyAAAAAAAAAAA");
                        while(priority_queue.peek()!= null)
                        {
                            ContentValues keyValueToInsert = new ContentValues();
                            // inserting <”key-to-insert”, “value-to-insert”>
                            keyValueToInsert.put("key", String.valueOf(msg_count++));
                            keyValueToInsert.put("value",priority_queue.poll().getMessage());

                            Uri newUri = getContentResolver().insert(
                                    mUri,    // assume we already created a Uri object with our provider URI
                                    keyValueToInsert
                            );
                        }
                    }
                }
            return null;
        }
    }

    //ClientTask definition
    private class ClientTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... msgs) {

                String[] port_coll = {"11108","11112","11116","11120","11124"};
                ArrayList<Float> received_sequence_number_list = new ArrayList<Float>();
                long msg_created_time = System.currentTimeMillis();
                //Log.e("CLIENT : ", "TimeStamp"+msg_created_time);

                    for (String remotePort : port_coll) {
                        try {
                            Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                                    Integer.parseInt(remotePort));

                            String msgToSend = msgs[0];

                            /** ------REFERENCES-------
                             * http://www.sqlite.org/datatype3.html
                             https://developer.android.com/guide/topics/providers/content-provider-basics.html
                             https://developer.android.com/reference/android/database/sqlite/SQLiteOpenHelper.html
                             https://developer.android.com/reference/android/database/sqlite/SQLiteDatabase.html
                             https://developer.android.com/training/basics/data-storage/databases.html
                             https://developer.android.com/reference/android/database/sqlite/SQLiteDatabase.html#query(java.lang.String, java.lang.String[], java.lang.String, java.lang.String[], java.lang.String, java.lang.String, java.lang.String)
                             https://developer.android.com/reference/android/database/sqlite/SQLiteDatabase.html#insertWithOnConflict(java.lang.String,%20java.lang.String,%20android.content.ContentValues,%20int)
                             http://developer.android.com/guide/topics/providers/content-providers.html
                             http://developer.android.com/reference/android/content/ContentProvider.html
                             https://docs.oracle.com/javase/7/docs/api/java/util/PriorityQueue.html
                             http://stackoverflow.com/questions/8077530/android-get-current-timestamp
                             http://stackoverflow.com/questions/18628584/adb-install-returns-error-protocol-fault-no-status
                             https://developer.android.com/reference/java/net/SocketTimeoutException.html
                             https://developer.android.com/reference/java/net/Socket.html
                             **/

                            //Using outputstream to write to server
                            //Log.e("CLIENT : ", "Establishing connection with server");
                            OutputStream output_stream = socket.getOutputStream();
                            OutputStreamWriter output_stream_writer = new OutputStreamWriter(output_stream);
                            BufferedWriter buffered_writer = new BufferedWriter(output_stream_writer);
                            //Log.e("CLIENT : ", "Stage-1 Sending message to server: " + msgToSend);

                            //Sending a string to identify stage1 or stage 3
                            buffered_writer.write("Stage1\n");
                            buffered_writer.flush();

                            //Sending Stage-1 - Msg and TimeStamp
                            buffered_writer.write(msgToSend);
                            buffered_writer.flush();

                            //Log.e("CLIENT : ", "Stage-1 Sending timestamp to server: " + msg_created_time);
                            buffered_writer.write(String.valueOf(msg_created_time) + "\n");
                            buffered_writer.flush();

                            //Getting the acknowledgementmessage from server
                            //Log.e("CLIENT : ", "Stage-2 Waiting to receive proposed sequence from server");
                            InputStream input_stream = socket.getInputStream();
                            InputStreamReader input_stream_reader = new InputStreamReader(input_stream);
                            BufferedReader buffered_reader = new BufferedReader(input_stream_reader);
                            String received_sequence_number = buffered_reader.readLine();
                            //Log.e("CLIENT : ", "Received sequence from server " + received_sequence_number);

                            received_sequence_number_list.add(Float.parseFloat(received_sequence_number));
                            socket.close();
                        }catch(Exception e)
                        {
                            Log.e("client","FOR 1 exception");
                        }
                    }

                    Collections.sort(received_sequence_number_list);
                    //Stage-3 broadcast
                    for (String remotePort : port_coll) {
                        try {
                            Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                                    Integer.parseInt(remotePort));
                            String msgToSend = msgs[0];
                            long msg_time_stamp = msg_created_time;
                            float max_sequence_number = received_sequence_number_list.get(received_sequence_number_list.size() - 1);
                            boolean isDeliverable = true;

                            //Using outputstream to write to server
                            OutputStream output_stream = socket.getOutputStream();
                            OutputStreamWriter output_stream_writer = new OutputStreamWriter(output_stream);
                            BufferedWriter buffered_writer = new BufferedWriter(output_stream_writer);

                            //Sending Stage-3 - Msg,TimeStamp,sequence number and
                            buffered_writer.write("Stage3\n");
                            buffered_writer.flush();
                            //Log.e("CLIENT : ", "Stage-3 Sending message to server: " + msgToSend);
                            buffered_writer.write(msgToSend);
                            buffered_writer.flush();

                            //Log.e("CLIENT : ", "Stage-3 Sending timestamp to server: " + msg_time_stamp);
                            buffered_writer.write(String.valueOf(msg_time_stamp) + "\n");
                            buffered_writer.flush();

                            //Log.e("CLIENT : ", "Stage-3 Sending sequence number to server: " + max_sequence_number);
                            buffered_writer.write(String.valueOf(max_sequence_number) + "\n");
                            buffered_writer.flush();

                            socket.close();
                        }catch(Exception e)
                        {
                            Log.e("client","FOR 2 Exception");
                        }
                    }

            return null;
        }
    }
}
