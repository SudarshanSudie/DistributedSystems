package edu.buffalo.cse.cse486586.simpledynamo;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by sudie on 5/2/17.
 */

public class SimpleDynamoDBHelper extends SQLiteOpenHelper {

    //Variables required for SQLiteHelper
    private static final String DATABASE_NAME = "simpledht.db";
    private static final int DATABASE_VERSION = 1;

    //Compulsory constructor for the helper class
    public SimpleDynamoDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        Log.d("SimpleDynamoDBHelper","INSIDE HELPER CLASS");
    }

    //Variables required for creating DataBase
    public static final String SQLITE_TABLE = "simpledht";
    public static final String KEY = "key";
    public static final String VALUE = "value";

    //Query to create a DB table
    private static final String DATABASE_CREATE =
            "CREATE TABLE " + SQLITE_TABLE + " (" +
                    KEY + " TEXT PRIMARY KEY," +
                    VALUE + " TEXT)";

    //DB Table initialization query
    public void onCreate(SQLiteDatabase db) {
        Log.d("simpledht","Creating DataBases in onCreate()");
        db.execSQL(DATABASE_CREATE);
    }

    //Incase of version change , drop table -> All data will be lost
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d("simpledht","Dropping table in onUpgrade()");
        db.execSQL("DROP TABLE IF EXISTS " + SQLITE_TABLE);
        onCreate(db);
    }
}
