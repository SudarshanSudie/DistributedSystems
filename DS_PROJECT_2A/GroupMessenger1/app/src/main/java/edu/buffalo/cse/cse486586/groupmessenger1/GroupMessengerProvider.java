package edu.buffalo.cse.cse486586.groupmessenger1;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

import static android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE;

/**
 * GroupMessengerProvider is a key-value table. Once again, please note that we do not implement
 * full support for SQL as a usual ContentProvider does. We re-purpose ContentProvider's interface
 * to use it as a key-value table.
 * 
 * Please read:
 * 
 * http://developer.android.com/guide/topics/providers/content-providers.html
 * http://developer.android.com/reference/android/content/ContentProvider.html
 * 
 * before you start to get yourself familiarized with ContentProvider.
 * 
 * There are two methods you need to implement---insert() and query(). Others are optional and
 * will not be tested.
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

public class GroupMessengerProvider extends ContentProvider {

    //Variables required for accessing DataBase
    public static final String SQLITE_TABLE = "GroupMessages";
    public static final String KEY = "key";
    public static final String VALUE = "value";

    //DB Helper object
    private GroupMessengerDBHelper groupMessengerDBHelper;

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // You do not need to implement this.
        return 0;
    }

    @Override
    public String getType(Uri uri) {
        // You do not need to implement this.
        return null;
    }

    //Inserting into Table using insertWithoutConflict
    @Override
    public Uri insert(Uri uri, ContentValues values) {

       // ContentValues keyValueToInsert = new ContentValues();
        SQLiteDatabase database = groupMessengerDBHelper.getWritableDatabase();

        //long returned_row_id = database.insert(SQLITE_TABLE, null,values);
        long returned_row_id = database.insertWithOnConflict(SQLITE_TABLE, null,values,CONFLICT_REPLACE);

        Log.d("insertion", String.valueOf(returned_row_id));
        /*
         * TODO: You need to implement this method. Note that values will have two columns (a key
         * column and a value column) and one row that contains the actual (key, value) pair to be
         * inserted.
         * 
         * For actual storage, you can use any option. If you know how to use SQL, then you can use
         * SQLite. But this is not a requirement. You can use other storage options, such as the
         * internal storage option that we used in PA1. If you want to use that option, please
         * take a look at the code for PA1.
         */
        Log.v("insert", values.toString());
        return uri;
    }

    //OnCreate function to initialize the DBHelper
    @Override
    public boolean onCreate() {
        // If you need to perform any one-time initialization task, please do it here.
        groupMessengerDBHelper= new GroupMessengerDBHelper(getContext());
        return false;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // You do not need to implement this.
        return 0;
    }

    //DB Query to get query the appropriate columns
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        SQLiteDatabase database = groupMessengerDBHelper.getReadableDatabase();

        String selection1 = "key" + " = ?";
        String[] selectionArgs1 = { selection };

        /*
         * TODO: You need to implement this method. Note that you need to return a Cursor object
         * with the right format. If the formatting is not correct, then it is not going to work.
         *
         * If you use SQLite, whatever is returned from SQLite is a Cursor object. However, you
         * still need to be careful because the formatting might still be incorrect.
         *
         * If you use a file storage option, then it is your job to build a Cursor * object. I
         * recommend building a MatrixCursor described at:
         * http://developer.android.com/reference/android/database/MatrixCursor.html
         */

        Cursor cursor = database.query(
                SQLITE_TABLE,                                // The table to query
                null,                                      // The columns to return
                selection1,                                // The columns for the WHERE clause
                selectionArgs1,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                null                                 // The sort order
        );
        Log.v("query", selection);
        return cursor;

    }
}
