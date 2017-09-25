package edu.buffalo.cse.cse486586.groupmessenger2;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by User on 16-02-2017.
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

public class GroupMessengerDBHelper extends SQLiteOpenHelper {

    //Variables required for SQLiteHelper
    private static final String DATABASE_NAME = "GpMessages.db";
    private static final int DATABASE_VERSION = 1;

    //Compulsory constructor for the helper class
    public GroupMessengerDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    //Variables required for creating DataBase
    public static final String SQLITE_TABLE = "GroupMessages";
    public static final String KEY = "key";
    public static final String VALUE = "value";

    //Query to create a DB table
    private static final String DATABASE_CREATE =
            "CREATE TABLE " + SQLITE_TABLE + " (" +
                    KEY + " TEXT PRIMARY KEY," +
                    VALUE + " TEXT)";

    //DB Table initialization query
    public void onCreate(SQLiteDatabase db) {
        Log.d("GroupMessagesDB","Creating DataBases in onCreate()");
        db.execSQL(DATABASE_CREATE);
    }

    //Incase of version change , drop table -> All data will be lost
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d("GroupMessagesDB","Dropping table in onUpgrade()");
        db.execSQL("DROP TABLE IF EXISTS " + SQLITE_TABLE);
        onCreate(db);
    }
}
