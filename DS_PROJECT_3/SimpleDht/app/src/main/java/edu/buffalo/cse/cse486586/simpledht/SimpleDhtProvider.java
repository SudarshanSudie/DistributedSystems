package edu.buffalo.cse.cse486586.simpledht;

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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Formatter;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.telephony.TelephonyManager;
import android.util.Log;
import static android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE;
import static java.lang.Thread.sleep;


public class SimpleDhtProvider extends ContentProvider {

    public Context context;

    public static String predecessor_port = null;
    //public static String predecessor;
    public static String successor_port = null;
    //public static String successor;
    public static String join_chord_request = "11108";

    public static boolean check = false;

    //Getting port from SimpleDhtActivity as that is where onCreate is defined

    public static String myPort;
    public String key;
    public static ArrayList<String> livePorts = new ArrayList<String>();

    //Variables required for accessing DataBase
    public static final String SQLITE_TABLE = "simpledht";
    public static final String KEY = "key";
    public static final String VALUE = "value";

    public static String[] columns = new String[] {KEY,VALUE};
    public static MatrixCursor matrixCursor= new MatrixCursor(columns);

    //DB Helper object
    private SimpleDhtDBHelper simpleDhtDBHelper;

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // TODO Auto-generated method stub
        SQLiteDatabase database = simpleDhtDBHelper.getWritableDatabase();

        int delCount = 0;

        String selection1;

        if(selection.equals("*"))
        {
            delCount = database.delete(SQLITE_TABLE,"1",null);
        }
        else if(selection.equals("@"))
        {
            delCount = database.delete(SQLITE_TABLE,"1",null);
        }
        else
        {
            selection1 = "key" + " = ?";
            String[] selectionArgs1 = { selection };
            delCount = database.delete(SQLITE_TABLE,selection1,selectionArgs1);
        }

        return delCount;
    }

    @Override
    public String getType(Uri uri) {
        // TODO Auto-generated method stub
        return null;
    }

    //Inserting into Table using insertWithoutConflict
    @Override
    public Uri insert(Uri uri, ContentValues values) {

        Log.d("Insert ","Key to insert: "+values.get(KEY)+"Value to insert: "+values.get(VALUE));

        try {
            //Check if this is the only node in the chord
            if(predecessor_port == null && successor_port == null)
            {
                Log.d("SimpleDhtProvider","Inserting in the only one in the chord: "+myPort);
                SQLiteDatabase database = simpleDhtDBHelper.getWritableDatabase();
                long returned_row_id = database.insertWithOnConflict(SQLITE_TABLE, null,values,CONFLICT_REPLACE);
                Log.d("insertion", String.valueOf(returned_row_id));
                return uri;
            }
            else
            {
                //Checking if this is the first node of the chord
                if(genHash(String.valueOf(Integer.valueOf(myPort)/2)).compareTo(genHash(String.valueOf(Integer.valueOf(predecessor_port)/2))) < 0)
                {
                    Log.d("SimpleDhtProvider","First node in the chord"+myPort);

                    //Check if incoming key is less than mine OR if incoming key is greater than predecessor
                    if(genHash(String.valueOf(Integer.valueOf(myPort)/2)).compareTo(genHash(values.getAsString(KEY))) >= 0 || genHash(String.valueOf(Integer.valueOf(predecessor_port)/2)).compareTo(genHash(values.getAsString(KEY))) < 0)
                    {
                        Log.d("SimpleDhtProvider","Inserting in first one of the chord: "+myPort);
                        SQLiteDatabase database = simpleDhtDBHelper.getWritableDatabase();
                        long returned_row_id = database.insertWithOnConflict(SQLITE_TABLE, null,values,CONFLICT_REPLACE);
                        Log.d("insertion", String.valueOf(returned_row_id));
                        return uri;
                    }
                    // For any other case -> ask successor to check
                    else
                    {
                        Log.d("SimpleDhtProvider","Key greater/lesser than mine : "+myPort+"forward to successor: "+successor_port);
                        String key = String.valueOf(values.getAsString(KEY));
                        String val = String.valueOf(values.getAsString(VALUE));
                        String msg = "CheckIfYouCanInsert"+"-"+key+"-"+val;
                        Log.d("SimpleDhtProvider",msg);
                        new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, msg, myPort);

                        //Wait for value to be inserted
                        try {
                            sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                //If not the first node , then perform the regular check
                else
                {
                    Log.d("SimpleDhtProvider","Not the first node: "+myPort);
                    //Checking if incoming key is less than mine and greater than predecessor
                    if(genHash(String.valueOf(Integer.valueOf(myPort)/2)).compareTo(genHash(values.getAsString(KEY))) >= 0 && genHash(String.valueOf(Integer.valueOf(predecessor_port)/2)).compareTo(genHash(values.getAsString(KEY))) < 0)
                    {
                        Log.d("SimpleDhtProvider","Key less than mine : "+myPort+"and greater than predecessor: "+predecessor_port);
                        SQLiteDatabase database = simpleDhtDBHelper.getWritableDatabase();
                        long returned_row_id = database.insertWithOnConflict(SQLITE_TABLE, null,values,CONFLICT_REPLACE);
                        Log.d("insertion", String.valueOf(returned_row_id));
                        return uri;
                    }
                    // For any other case -> ask successor to check
                    else
                    {
                        Log.d("SimpleDhtProvider","Key greater than mine : "+myPort+"forward to successor: "+successor_port);
                        String key = String.valueOf(values.getAsString(KEY));
                        String val = String.valueOf(values.getAsString(VALUE));
                        String msg = "CheckIfYouCanInsert"+"-"+key+"-"+val;
                        Log.d("SimpleDhtProvider",msg);
                        new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, msg, myPort);
                        //Wait for value to be inserted
                        try {
                            sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return uri;
    }

    @Override
    public boolean onCreate() {
        // If you need to perform any one-time initialization task, please do it here.
        simpleDhtDBHelper = new SimpleDhtDBHelper(getContext());
        TelephonyManager tel = (TelephonyManager) this.getContext().getSystemService((Context.TELEPHONY_SERVICE));
        String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
        myPort = String.valueOf((Integer.parseInt(portStr) * 2));

        context = this.getContext();

        try
        {
            SimpleDhtProvider simpleDhtProvider = new SimpleDhtProvider();
            int port = Integer.parseInt(myPort)/2;
            String key = String.valueOf(port);
            simpleDhtProvider.key = simpleDhtProvider.genHash(key);
            Log.d("SimpleDhtActivity","Inside OnCreate");
            Log.d("SimpleDhtActivity","MyPort: "+myPort);
            Log.d("SimpleDhtActivity","MyKey: "+key);

            if(myPort.equals("11108"))
            {
                Log.d("SimpleDhtActivity","Adding 11108 to live ports");
                simpleDhtProvider.livePorts.add(myPort);
            }
        }
        catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }

        //Server task initialization
        try {
            ServerSocket serverSocket = new ServerSocket(10000);
//            new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);
            ServerTask s = new ServerTask();
            s.setContext(context);
            s.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);
        } catch (IOException e) {
            Log.e("SimpleDhtActivity", "Can't create a ServerSocket");
            return false;
        }

        //Client task initialization
        if(!myPort.equals("11108"))
        {
            String msg = "NewNodeJoining";
            new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, msg, myPort);
        }
        return false;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // TODO Auto-generated method stub

        return 0;
    }

    //DB Query to get query the appropriate columns
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        SQLiteDatabase database = simpleDhtDBHelper.getReadableDatabase();

        Cursor cursor = null;
        String selection1;

        if(selection.equals("*"))
        {
            if(predecessor_port == null && successor_port == null)
            {
                Log.d("SimpleDhtProvider","Querying the only one in the chord: "+myPort);
                cursor = database.rawQuery("select * from " + SQLITE_TABLE,null);
                return cursor;
            }
            else
            {
                Log.d("SimpleDhtProvider","Inside query *");

                cursor = database.rawQuery("select * from " + SQLITE_TABLE,null);

                String msg = "QueryEverythingForMe"+"-"+myPort;

                Log.d("SimpleDhtProvider",msg);

                new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, msg, myPort);

                Log.d("SimpleDhtProvider","Waiting for query to be completed");

                while(check == false)
                {

                }
                check = false;
                Log.d("SimpleDhtProvider","Query completed");

                if(cursor.moveToFirst() != false)
                    for (; !cursor.isAfterLast(); cursor.moveToNext())
                    {
                        matrixCursor.addRow(new String[] {cursor.getString(cursor.getColumnIndex("key")),cursor.getString(cursor.getColumnIndex("value"))});
                    }
                MatrixCursor cursor2 = matrixCursor;
                matrixCursor.close();
                matrixCursor = new MatrixCursor(columns);

                return cursor2;
            }

        }
        else if(selection.equals("@"))
        {
            cursor = database.rawQuery("select * from " + SQLITE_TABLE,null);
        }
        else
        {
            selection1 = "key" + " = ?";
            String[] selectionArgs1 = {selection};
            Log.d("Query ","Key to query: "+selection);

            try{
                //Check if this is the only node in the chord
                if(predecessor_port == null && successor_port == null)
                {
                    Log.d("SimpleDhtProvider","Querying the only one in the chord: "+myPort);
                    cursor = database.query(SQLITE_TABLE,null,selection1,selectionArgs1,null,null,null);
                    //ServerTask.port_identity = null;
                    return cursor;
                }
                else
                {
                    //Checking if this is the first node of the chord
                    if(genHash(String.valueOf(Integer.valueOf(myPort)/2)).compareTo(genHash(String.valueOf(Integer.valueOf(predecessor_port)/2))) < 0)
                    {
                        Log.d("SimpleDhtProvider", "First node in the chord" + myPort);

                        //Check if incoming key is less than mine OR if incoming key is greater than predecessor
                        if (genHash(String.valueOf(Integer.valueOf(myPort) / 2)).compareTo(genHash(selection)) >= 0 || genHash(String.valueOf(Integer.valueOf(predecessor_port) / 2)).compareTo(genHash(selection)) < 0)
                        {
                            Log.d("SimpleDhtProvider","Querying the first one in the chord: "+myPort);
                            cursor = database.query(SQLITE_TABLE,null,selection1,selectionArgs1,null,null,null);
                            //ServerTask.port_identity = null;
                            Log.d("SimpleDhtProvider","Cursor created");
                            return cursor;
                        }
                        // For any other case -> ask successor to check
                        else
                        {
                            String key = selection;
                            String msg;
                            if(ServerTask.port_identity == null)
                            {
                                Log.d("SimpleDhtProvider","PORT_IDENTITY IS NULL , APPENDING MY PORT: "+myPort);
                                msg = "CheckIfYouHaveThis"+"-"+key+"-"+myPort;

                            }
                            else
                            {
                                Log.d("SimpleDhtProvider","PORT_IDENTITY IS NOT NULL , APPENDING Existing Port: "+ServerTask.port_identity);
                                msg = "CheckIfYouHaveThis"+"-"+key+"-"+ServerTask.port_identity;
                            }

                            Log.d("SimpleDhtProvider",msg);
                            new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, msg, myPort);
                            //Wait for value to be inserted

                            Log.d("SimpleDhtProvider","Waiting for query to be completed");

                            while (check == false && ServerTask.port_identity == null)
                            {
                            }
                            check = false;

                            Log.d("SimpleDhtProvider","Query completed");

                            MatrixCursor cursor2 = matrixCursor;
                            matrixCursor.close();
                            matrixCursor = new MatrixCursor(columns);

                            return cursor2;
                        }
                    }
                    //If not the first node , then perform the regular check
                    else
                    {
                        Log.d("SimpleDhtProvider", "Not the first node in the chord" + myPort);

                        //Check if incoming key is less than mine OR if incoming key is greater than predecessor
                        if (genHash(String.valueOf(Integer.valueOf(myPort) / 2)).compareTo(genHash(selection)) >= 0 && genHash(String.valueOf(Integer.valueOf(predecessor_port) / 2)).compareTo(genHash(selection)) < 0)
                        {
                            Log.d("SimpleDhtProvider","Querying in mine , coz - Key less than mine : "+myPort+"and greater than predecessor: "+predecessor_port);
                            cursor = database.query(SQLITE_TABLE,null,selection1,selectionArgs1,null,null,null);
                            //ServerTask.port_identity = null;
                            return cursor;
                        }
                        // For any other case -> ask successor to check
                        else
                        {
                            String key = selection;
                            String msg;
                            if(ServerTask.port_identity == null)
                            {
                                Log.d("SimpleDhtProvider","PORT_IDENTITY IS NULL , APPENDING MY PORT: "+myPort);
                                msg = "CheckIfYouHaveThis"+"-"+key+"-"+myPort;
                            }
                            else
                            {
                                Log.d("SimpleDhtProvider","PORT_IDENTITY IS NOT NULL , APPENDING Existing Port: "+ServerTask.port_identity);
                                msg = "CheckIfYouHaveThis"+"-"+key+"-"+ServerTask.port_identity;
                            }

                            Log.d("SimpleDhtProvider",msg);
                            new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, msg, myPort);
                            //Wait for value to be inserted

                            Log.d("SimpleDhtProvider","Waiting for query to be completed");


                            while (check == false && ServerTask.port_identity == null)
                            {
                            }
                            check = false;

                            Log.d("SimpleDhtProvider","Query completed");
                            //matrixCursor.addRow(new Object[]{ServerTask.query_key,ServerTask.query_val});

                            MatrixCursor cursor2 = matrixCursor;
                            matrixCursor.close();
                            matrixCursor = new MatrixCursor(columns);

                            return cursor2;
                        }
                    }
                }

            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }
        return cursor;
    }

    public static String genHash(String input) throws NoSuchAlgorithmException {
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        byte[] sha1Hash = sha1.digest(input.getBytes());
        Formatter formatter = new Formatter();
        for (byte b : sha1Hash) {
            formatter.format("%02x", b);
        }
        return formatter.toString();
    }
}
