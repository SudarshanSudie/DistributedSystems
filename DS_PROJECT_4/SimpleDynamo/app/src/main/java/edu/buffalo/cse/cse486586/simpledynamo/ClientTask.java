package edu.buffalo.cse.cse486586.simpledynamo;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
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

import static android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE;
import static edu.buffalo.cse.cse486586.simpledynamo.SimpleDynamoProvider.simpleDynamoDBHelper;

/**
 * Created by sudie on 5/2/17.
 */

public class ClientTask extends AsyncTask<String, Void, Cursor> {
    @Override
    protected Cursor doInBackground(String... msgs) {
        Log.d("ClientTask", "Entered Client Task");
        String msg = msgs[0];
        String[] columns = new String[] {"key","value"};
        MatrixCursor matrixCursor= new MatrixCursor(columns);

        try
        {
            Log.d("ClientTask","Inside try");
            if(msg.contains("CheckIfYouCanInsert"))
            {
                String[] splitMsgs = msg.split("-");
                Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                        Integer.parseInt(splitMsgs[splitMsgs.length-1]));

                //Using outputstream to write to server
                OutputStream output_stream = socket.getOutputStream();
                OutputStreamWriter output_stream_writer = new OutputStreamWriter(output_stream);
                BufferedWriter buffered_writer = new BufferedWriter(output_stream_writer);

                //Getting what the client sent using inputstream
                InputStream input_stream = socket.getInputStream();
                InputStreamReader input_stream_reader = new InputStreamReader(input_stream);
                BufferedReader buffered_reader = new BufferedReader(input_stream_reader);

                Log.d("ClientTask",msg);
                buffered_writer.write(msg+"\n");
                buffered_writer.flush();

                String msgReceived = buffered_reader.readLine();
                if(msgReceived == null)
                {
                    Log.d("CLIENTTASK","INSIDE FAILURE KEY VAL ADD");
                    SimpleDynamoProvider.failedNode = splitMsgs[splitMsgs.length-1];
                    SimpleDynamoProvider.failure_key += splitMsgs[1]+"-";
                    SimpleDynamoProvider.failure_val += splitMsgs[2]+"-";
                }

                socket.close();
            }
            else if(msg.contains("CheckIfYouHaveThis"))
            {
                String[] splitMsgs = msg.split("-");
                Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                        Integer.parseInt(splitMsgs[splitMsgs.length-1]));

                //Using outputstream to write to server
                OutputStream output_stream = socket.getOutputStream();
                OutputStreamWriter output_stream_writer = new OutputStreamWriter(output_stream);
                BufferedWriter buffered_writer = new BufferedWriter(output_stream_writer);

                Log.d("ClientTask",msg);
                buffered_writer.write(msg+"\n");
                buffered_writer.flush();

                //Getting what the client sent using inputstream
                InputStream input_stream = socket.getInputStream();
                InputStreamReader input_stream_reader = new InputStreamReader(input_stream);
                BufferedReader buffered_reader = new BufferedReader(input_stream_reader);

                String msgReceived = buffered_reader.readLine();
                if(msgReceived == null)
                {
                    return null;
                }
                String[] keyVal = msgReceived.split("&");

                Log.d("ServerTask","key "+keyVal[0]+"val "+keyVal[1]);
                matrixCursor.addRow(new String[]{keyVal[0],keyVal[1]});
            }
            else if(msg.contains("QueryEverything"))
            {
                String[] splitMsgs = msg.split("-");
                Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                        Integer.parseInt(splitMsgs[splitMsgs.length-1]));

                //Using outputstream to write to server
                OutputStream output_stream = socket.getOutputStream();
                OutputStreamWriter output_stream_writer = new OutputStreamWriter(output_stream);
                BufferedWriter buffered_writer = new BufferedWriter(output_stream_writer);

                Log.d("ClientTask",msg);
                buffered_writer.write(msg+"\n");
                buffered_writer.flush();

                //Getting what the client sent using inputstream
                InputStream input_stream = socket.getInputStream();
                InputStreamReader input_stream_reader = new InputStreamReader(input_stream);
                BufferedReader buffered_reader = new BufferedReader(input_stream_reader);

                String msgReceived = buffered_reader.readLine();
                String[] keyVal = msgReceived.split("-");
                //forloop
                String[] keySet = keyVal[0].split("@");
                String[] valSet = keyVal[1].split("@");
                for(int i = 0;i<keySet.length;i++)
                {
                    matrixCursor.addRow(new String[]{keySet[i],valSet[i]});
                }
            }
            else if(msg.contains("NewNodeJoining"))
            {
                Integer[] remotePort = new Integer[]{11124,11112,11108,11116,11120};
                for(int i = 0;i<remotePort.length;i++)
                {
                    if(remotePort[i].equals(Integer.parseInt(SimpleDynamoProvider.myPort)))
                    {
                        continue;
                    }
                    Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),remotePort[i]);

                    //Using outputstream to write to server
                    OutputStream output_stream = socket.getOutputStream();
                    OutputStreamWriter output_stream_writer = new OutputStreamWriter(output_stream);
                    BufferedWriter buffered_writer = new BufferedWriter(output_stream_writer);

                    Log.d("ClientTask",msg);
                    buffered_writer.write(msg+"\n");
                    buffered_writer.flush();

                    //Getting what the client sent using inputstream
                    InputStream input_stream = socket.getInputStream();
                    InputStreamReader input_stream_reader = new InputStreamReader(input_stream);
                    BufferedReader buffered_reader = new BufferedReader(input_stream_reader);

                    String msgReceived = buffered_reader.readLine();
                    if(msgReceived == null || msgReceived.equals("Nothing") || msgReceived.isEmpty()){
                        continue;
                    }
                    String[] keyVal = msgReceived.split("@");
                    //forloop
                    if(keyVal.length == 2)
                    {
                        String[] keySet = keyVal[0].split("-");
                        String[] valSet = keyVal[1].split("-");
                        for(int j = 0;j<keySet.length;j++)
                        {
                            Log.d("CLientTask","passing on the lost key and value to"+remotePort[i]);
                            ContentValues keyValueToInsert = new ContentValues();
                            keyValueToInsert.put("key", keySet[j]);
                            keyValueToInsert.put("value", valSet[j]);

                            SQLiteDatabase database = simpleDynamoDBHelper.getWritableDatabase();

                            long returned_row_id = database.insertWithOnConflict(SimpleDynamoProvider.SQLITE_TABLE, null,keyValueToInsert,CONFLICT_REPLACE);
                        }
                    }
//                    socket.close();
                }
            }
        }
        catch (UnknownHostException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return matrixCursor;
    }
}
