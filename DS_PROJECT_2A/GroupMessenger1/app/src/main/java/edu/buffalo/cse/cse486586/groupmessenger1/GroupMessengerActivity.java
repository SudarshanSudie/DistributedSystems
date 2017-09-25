package edu.buffalo.cse.cse486586.groupmessenger1;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.method.ScrollingMovementMethod;
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
import java.net.UnknownHostException;

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
 **/
public class GroupMessengerActivity extends Activity {

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
        final String myPort = String.valueOf((Integer.parseInt(portStr) * 2));

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
                Log.d("OnCLick:Send "," Port Number"+myPort);
                EditText editText = (EditText) findViewById(R.id.editText1);
                String msg = editText.getText().toString() + "\n";
                Log.d("OnCLick:Send "," Message"+msg);
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
    Uri mUri = buildUri("content", "edu.buffalo.cse.cse486586.groupmessenger1.provider");

    //The BuildUri function for appending the two parameters as a single uri
    private Uri buildUri(String scheme, String authority) {
        Uri.Builder uriBuilder = new Uri.Builder();
        uriBuilder.authority(authority);
        uriBuilder.scheme(scheme);
        return uriBuilder.build();
    }

    //ServerTask defininition
    private class ServerTask extends AsyncTask<ServerSocket, String, Void> {

        @Override
        protected Void doInBackground(ServerSocket... sockets) {
            ServerSocket serverSocket = sockets[0];

            try {
                while (true) {
                    //References Used
                    //https://docs.oracle.com/javase/tutorial/networking/sockets/
                    //https://developer.android.com/reference/android/os/AsyncTask.html#publishProgress(Progress..
                    //https://docs.oracle.com/javase/7/docs/api/java/io/BufferedReader.html
                    //https://docs.oracle.com/javase/tutorial/essential/io/buffers.html

                    //Accepting client connection
                    Log.e("SERVER : ", "Connection Accepted");
                    Socket respondedClientSocket = serverSocket.accept();

                    //Getting what the client sent using inputstream
                    InputStream input_stream = respondedClientSocket.getInputStream();
                    InputStreamReader input_stream_reader = new InputStreamReader(input_stream);
                    BufferedReader buffered_reader = new BufferedReader(input_stream_reader);

                    //Message from client
                    String msgToDisplay = buffered_reader.readLine();
                    Log.e("SERVER : ", "Received the message to be displayed : " + msgToDisplay);

                    //Sending acknowledgement to client through outputstream
                    OutputStream output_stream = respondedClientSocket.getOutputStream();
                    OutputStreamWriter output_stream_writer = new OutputStreamWriter(output_stream);
                    BufferedWriter buffered_writer = new BufferedWriter(output_stream_writer);

                    //Acknowledgement message from server
                    String msgToAck = "Got the package" + "\n";
                    Log.e("SERVER : ", "Acknowledging reception : " + msgToAck);
                    buffered_writer.write(msgToAck);
                    buffered_writer.flush();

                    ContentValues keyValueToInsert = new ContentValues();

                    // inserting <”key-to-insert”, “value-to-insert”>
                    keyValueToInsert.put("key",String.valueOf(msg_count++));
                    keyValueToInsert.put("value",msgToDisplay);

                    Uri newUri = getContentResolver().insert(
                            mUri,    // assume we already created a Uri object with our provider URI
                            keyValueToInsert
                    );
                    Log.e("SERVER : ", "Closing socket : " + msgToAck);
                    //respondedClientSocket.close();
                }
            } catch (IOException exp) {
                exp.printStackTrace();
            }
            return null;
        }
    }

    //ClientTask definition
    private class ClientTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... msgs) {
            try {
                String[] port_coll = {"11108","11112","11116","11120","11124"};

                for(String remotePort : port_coll)
                {
                    Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                            Integer.parseInt(remotePort));

                    String msgToSend = msgs[0];

                    //References Used
                    //https://docs.oracle.com/javase/tutorial/networking/sockets/
                    //https://developer.android.com/reference/android/os/AsyncTask.html#publishProgress(Progress..
                    //https://docs.oracle.com/javase/7/docs/api/java/io/BufferedReader.html
                    //https://docs.oracle.com/javase/tutorial/essential/io/buffers.html

                    //Using outputstream to write to server
                    Log.e("CLIENT : ", "Establishing connection with server");
                    OutputStream output_stream = socket.getOutputStream();
                    OutputStreamWriter output_stream_writer = new OutputStreamWriter(output_stream);
                    BufferedWriter buffered_writer = new BufferedWriter(output_stream_writer);
                    Log.e("CLIENT : ", "Sending message to server"+msgToSend);
                    buffered_writer.write(msgToSend);
                    buffered_writer.flush();

                    //Getting the acknowledgementmessage from server
                    Log.e("CLIENT : ", "Waiting to receive acknowledgement from server");
                    InputStream input_stream = socket.getInputStream();
                    InputStreamReader input_stream_reader = new InputStreamReader(input_stream);
                    BufferedReader buffered_reader = new BufferedReader(input_stream_reader);
                    String msgReceived = buffered_reader.readLine();
                    Log.e("CLIENT : ", "Received acknowledgement from server");

                    //Checking if client had received the correct acknowledgement message
                    if(msgReceived == "Got the package")
                    {
                        Log.e("CLIENT : ", "Closing socket");
                        //Closing socket
                        socket.close();
                    }
                }


            } catch (UnknownHostException e) {
                Log.e("GroupMessengerActivity", "ClientTask UnknownHostException");
            } catch (IOException e) {
                Log.e("GroupMessengerActivity", "ClientTask socket IOException");
            }

            return null;
        }
    }
}
