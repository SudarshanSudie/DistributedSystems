package edu.buffalo.cse.cse486586.simpledynamo;

import java.io.IOException;
import java.net.ServerSocket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Formatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

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


public class SimpleDynamoProvider extends ContentProvider {

	//DB Helper object
	public static SimpleDynamoDBHelper simpleDynamoDBHelper;

    public static HashMap<String,Integer> portTohash = new HashMap<String,Integer>();

    public static ArrayList<String> ports = new ArrayList<String>();

    //Variables required for accessing DataBase
    public static final String SQLITE_TABLE = "simpledht";
    public static final String KEY = "key";
    public static final String VALUE = "value";

    public static String failedNode = null;

    public static String failure_key = "";
    public static String failure_val = "";

    public static String myPort;

    public Context context;

    public ArrayList<Integer> position(String newhash)
    {
        ArrayList<Integer> insertPorts = new ArrayList<Integer>();
        for (String key : ports) {

            Log.d("key "+key," ");
            if(newhash.compareTo(String.valueOf(key)) < 0)
            {
                int index = ports.indexOf(key);
                int index2 = (index + 1)%5;
                int index3 = (index2 + 1)%5;

                insertPorts.add(portTohash.get(ports.get(index)));
                insertPorts.add(portTohash.get(ports.get(index2)));
                insertPorts.add(portTohash.get(ports.get(index3)));
                Log.d("position: ", insertPorts.toString());
                return insertPorts;
            }
        }
        insertPorts.add(portTohash.get(ports.get(0)));
        insertPorts.add(portTohash.get(ports.get(1)));
        insertPorts.add(portTohash.get(ports.get(2)));
        Log.d("position: ", insertPorts.toString());
        return insertPorts;
    }

    public void insertAfterRecovery(HashMap<String,String> h)
    {
        for(Map.Entry entry : h.entrySet())
        {
            String key = String.valueOf(entry.getKey());
            String val = String.valueOf(entry.getValue());

            try {
                if(position(genHash(key)).contains(Integer.parseInt(myPort)))
                {
                    ContentValues keyValueToInsert = new ContentValues();
                    keyValueToInsert.put("key", key);
                    keyValueToInsert.put("value",val);

                    SQLiteDatabase database = simpleDynamoDBHelper.getWritableDatabase();

                    long returned_row_id = database.insertWithOnConflict(SimpleDynamoProvider.SQLITE_TABLE, null,keyValueToInsert,CONFLICT_REPLACE);

                }
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }
    }

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
        SQLiteDatabase database = simpleDynamoDBHelper.getWritableDatabase();

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

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// TODO Auto-generated method stub
        Log.d("Insert ","Key to insert: "+values.get(KEY)+"Value to insert: "+values.get(VALUE));

        try
        {
            String keyHash = genHash(String.valueOf(values.get(KEY)));
            ArrayList<Integer> portsToConnect = position(keyHash);

            String key = String.valueOf(values.getAsString(KEY));
            String val = String.valueOf(values.getAsString(VALUE));

            for(int i = 0;i<portsToConnect.size();i++)
            {
                String msg = "CheckIfYouCanInsert" + "-" + key + "-" + val + "-" +String.valueOf(portsToConnect.get(i));
                Log.d("SimpleDynamoProvider","Insert"+msg);
                try {
                    new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, msg, myPort).get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
        }
        catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
	}

	@Override
	public boolean onCreate() {
		// TODO Auto-generated method stub

        simpleDynamoDBHelper = new SimpleDynamoDBHelper(getContext());

        TelephonyManager tel = (TelephonyManager) this.getContext().getSystemService((Context.TELEPHONY_SERVICE));
        String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
        myPort = String.valueOf((Integer.parseInt(portStr) * 2));

        context = this.getContext();
        //HashMap<String,String> msgList = new HashMap<String, String>();

        try
        {
            int port = Integer.parseInt(myPort)/2;
            String key = String.valueOf(port);

            Log.d("SimpleDynamoActivity","Inside OnCreate");
            Log.d("SimpleDynamoActivity","MyPort: "+myPort);
            String[] hashes = new String[]{genHash(String.valueOf(5562)),genHash(String.valueOf(5556)),genHash(String.valueOf(5554)),genHash(String.valueOf(5558)),genHash(String.valueOf(5560))};
            Log.d("SimpleDynamoProvider","Populating the values inside portToHash");
            portTohash.put(hashes[0],11124);
            portTohash.put(hashes[1],11112);
            portTohash.put(hashes[2],11108);
            portTohash.put(hashes[3],11116);
            portTohash.put(hashes[4],11120);
            Log.d("SimpleDynamoProvider","Populating the values inside ports");
            ports.add(hashes[0]);
            ports.add(hashes[1]);
            ports.add(hashes[2]);
            ports.add(hashes[3]);
            ports.add(hashes[4]);
            Set<String> temp = new HashSet<String>();
            temp.addAll(ports);
            ports.clear();
            ports.addAll(temp);
            Collections.sort(ports);
            Log.d("ONCREATE",ports.toString());
            String msg = "NewNodeJoining";
            try {
                new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, msg, myPort).get();
//                if(cursor.moveToFirst()!=false)
//                {
//                    for(;!cursor.isAfterLast();cursor.moveToNext())
//                    {
//                        //msgList.put(cursor.getString(cursor.getColumnIndex("key")),cursor.getString(cursor.getColumnIndex("value")));
//                        ContentValues keyValueToInsert = new ContentValues();
//                        keyValueToInsert.put("key", cursor.getString(cursor.getColumnIndex("key")));
//                        keyValueToInsert.put("value", cursor.getString(cursor.getColumnIndex("value")));
//
//                        SQLiteDatabase database = simpleDynamoDBHelper.getWritableDatabase();
//
//                        long returned_row_id = database.insertWithOnConflict(SimpleDynamoProvider.SQLITE_TABLE, null,keyValueToInsert,CONFLICT_REPLACE);
//                    }
//                    //insertAfterRecovery(msgList);
//                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }

        }
        catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }

        try {
            ServerSocket serverSocket = new ServerSocket(10000);
            ServerTask s = new ServerTask();
            s.setContext(context);
            s.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);
        } catch (IOException e) {
            Log.e("SimpleDhtActivity", "Can't create a ServerSocket");
            return false;
        }
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		// TODO Auto-generated method stub
        Log.d("Query ","Key to query: "+selection);
        Cursor cursor = null;
        String keyHash;
        if(selection.equals("@"))
        {
            SQLiteDatabase database = simpleDynamoDBHelper.getReadableDatabase();
            Log.d("Query","inside @ part");
            cursor = database.rawQuery("select * from " + SQLITE_TABLE,null);
//            return cursor;
        }
        else if(selection.equals("*"))
        {
            String[] columns = new String[] {"key","value"};
            MatrixCursor matrixCursor= new MatrixCursor(columns);

            Log.d("Query","STAR Query");
            for (Integer value : portTohash.values())
            {
                String msg = "QueryEverything" + "-" +String.valueOf(value);
                Log.d("SimpleDynamoProvider","QueryStar"+msg);
                try {
                    cursor = new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, msg, myPort).get();
                    if(cursor.moveToFirst()!=false)
                    {
                        for(;!cursor.isAfterLast();cursor.moveToNext())
                        {
                            matrixCursor.addRow(new Object[]{cursor.getString(cursor.getColumnIndex("key")),cursor.getString(cursor.getColumnIndex("value"))});
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
            matrixCursor.moveToFirst();
            return matrixCursor;
        }
        else
        {
            Log.d("Query","Normal Query");
            try
            {
                keyHash = genHash(String.valueOf(selection));
                ArrayList<Integer> portsToConnect = position(keyHash);

                for(int i = 0;i<portsToConnect.size();i++)
                {
                    String msg = "CheckIfYouHaveThis" + "-" + selection + "-" +String.valueOf(portsToConnect.get(i));
                    Log.d("SimpleDynamoProvider", "Query"+msg);
                    try {
                        cursor = new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, msg, myPort).get();

                        if(cursor!= null)
                        {
                            return cursor;
                        }
                        else
                        {
                            Log.d("SIMPLEDYNAMOPROVIDER","cursor is null");
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                }
            }
            catch (NoSuchAlgorithmException e)
            {
                e.printStackTrace();
            }
        }
		return cursor;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

    private String genHash(String input) throws NoSuchAlgorithmException {
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        byte[] sha1Hash = sha1.digest(input.getBytes());
        Formatter formatter = new Formatter();
        for (byte b : sha1Hash) {
            formatter.format("%02x", b);
        }
        return formatter.toString();
    }
}
