package edu.buffalo.cse.cse486586.groupmessenger2;

import java.sql.Timestamp;

/**
 * Created by sudie on 3/10/17.
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
public class MsgsObject implements Comparable<MsgsObject>{

    String message;
    float sequence_number;
    boolean deliverable;
    long time_stamp_msg;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public float getSequence_number() {
        return sequence_number;
    }

    public void setSequence_number(float sequence_number) {
        this.sequence_number = sequence_number;
    }

    public long getTime_stamp_msg() {
        return time_stamp_msg;
    }

    public void setTime_stamp_msg(long time_stamp_msg) {
        this.time_stamp_msg = time_stamp_msg;
    }

    public boolean isDeliverable() {
        return deliverable;
    }

    public void setDeliverable(boolean deliverable) {
        this.deliverable = deliverable;
    }

    //Stage-3
    public MsgsObject(String msg,float seq,long timestamp,boolean isDeliverable)
    {
        message = msg;
        time_stamp_msg = timestamp;
        sequence_number = seq;
        deliverable = isDeliverable;
    }

    //Stage-1
    public MsgsObject(String msg,float seq,long timestamp)
    {
        message = msg;
        time_stamp_msg = timestamp;
        sequence_number = seq;
    }

    public MsgsObject()
    {
        message = "";
        sequence_number = 0F;
        deliverable = false;
        time_stamp_msg = 0;
    }
    @Override
    public int compareTo(MsgsObject another)
    {
        return Double.compare(this.sequence_number,another.sequence_number);
    }
}
