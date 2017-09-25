package edu.buffalo.cse.cse486586.simpledynamo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

import static android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE;

/**
 * Created by sudie on 5/2/17.
 */

class ServerTask extends AsyncTask<ServerSocket, String, Void>
{

    public Context context;

    public void setContext(Context con)
    {
        this.context = con;
    }

    //The specified uri using which any provider can access the data
    Uri mUri = buildUri("content", "edu.buffalo.cse.cse486586.simpledynamo");

    //The BuildUri function for appending the two parameters as a single uri
    private Uri buildUri(String scheme, String authority)
    {
        Uri.Builder uriBuilder = new Uri.Builder();
        uriBuilder.authority(authority);
        uriBuilder.scheme(scheme);
        return uriBuilder.build();
    }

    @Override
    protected Void doInBackground(ServerSocket... sockets) {
        //Initialize class object to access its members
        //SimpleDhtProvider simpleDhtProvider = new SimpleDhtProvider();

        Log.d("ServerTask", "Entered Server Task");
        ServerSocket serverSocket = sockets[0];
        int count = 0;

        //Infinite while loop
        while (count < 9)
        {
            try
            {
                //Accepting connection
                Socket respondedClientSocket = serverSocket.accept();
                Log.d("ServerTask", "Established Connection with Client");

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

                if(msg_port[0].equals("CheckIfYouCanInsert"))
                {
                    Log.d("ServerTask","Insert check - MyPort: "+SimpleDynamoProvider.myPort);
                    String key = msg_port[1];
                    String value = msg_port[2];
                    ContentValues keyValueToInsert = new ContentValues();
                    keyValueToInsert.put("key", key);
                    keyValueToInsert.put("value",value);

                    SQLiteDatabase database = SimpleDynamoProvider.simpleDynamoDBHelper.getWritableDatabase();
                    long returned_row_id = database.insertWithOnConflict(SimpleDynamoProvider.SQLITE_TABLE, null,keyValueToInsert,CONFLICT_REPLACE);

                    buffered_writer.write("Inserted"+"\n");
                    buffered_writer.flush();
                    respondedClientSocket.close();
                }
                else if(msg_port[0].equals("CheckIfYouHaveThis"))
                {
                    Log.d("ServerTask","Query check - MyPort: "+SimpleDynamoProvider.myPort);
                    SQLiteDatabase database = SimpleDynamoProvider.simpleDynamoDBHelper.getReadableDatabase();
                    String selection1 = "key" + " = ?";
                    String[] selectionArgs1 = {msg_port[1]};
                    Cursor cursor = database.query(SimpleDynamoProvider.SQLITE_TABLE,null,selection1,selectionArgs1,null,null,null);

                    if(cursor.moveToFirst() != false) {

                        String query_key = cursor.getString(cursor.getColumnIndex("key"));
                        String query_val = cursor.getString(cursor.getColumnIndex("value"));

                        Log.d("ServerTask", "key: " + query_key + " and value: " + query_val);
                        Log.d("ServerTask", "I am " + SimpleDynamoProvider.myPort);

                        String responseMsg = query_key+"&"+query_val+"\n";

                        Log.d("ServerTask",responseMsg);
                        buffered_writer.write(responseMsg);
                        buffered_writer.flush();

                        respondedClientSocket.close();
                    }
                }
                else if(msg_port[0].equals("QueryEverything"))
                {
                    SQLiteDatabase database = SimpleDynamoProvider.simpleDynamoDBHelper.getReadableDatabase();
                    Cursor resultCursor = database.rawQuery("select * from " + SimpleDynamoProvider.SQLITE_TABLE,null);
                    String query_key;
                    String query_val;
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
                        Log.d("ServerTask","key: "+query_key);
                        Log.d("ServerTask","value "+query_val);

                        String msgToRespond = query_key+"-"+query_val;

                        Log.d("ServerTask",msgToRespond);
                        buffered_writer.write(msgToRespond);
                        buffered_writer.flush();

                        respondedClientSocket.close();

                    }
                }
                else if(msg_port[0].equals("NewNodeJoining"))
                {
                    String msgToRespond = SimpleDynamoProvider.failure_key+ "@" + SimpleDynamoProvider.failure_val;
                    SimpleDynamoProvider.failure_key = "";
                    SimpleDynamoProvider.failure_val = "";
                    Log.d("ServerTask", msgToRespond);
                    buffered_writer.write(msgToRespond);
                    buffered_writer.flush();

                    respondedClientSocket.close();
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        return null;
    }
}
