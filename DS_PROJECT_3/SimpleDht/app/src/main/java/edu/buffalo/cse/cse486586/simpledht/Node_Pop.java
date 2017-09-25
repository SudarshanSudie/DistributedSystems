package edu.buffalo.cse.cse486586.simpledht;

/**
 * Created by sudie on 4/5/17.
 */

import android.util.Log;

import java.util.ArrayList;

public class Node_Pop{

    public static class Node_Pop_Util
    {
        public static int search(ArrayList<NodeObject> a,NodeObject no){

            for(int i = 0;i<a.size();i++)
            {
                //Log.d("Node_Pop","Going to search");
                //Log.d("Node Pop","ArrayList key: "+a.get(i).key+"ArrayList hashValue: "+a.get(i).hashvalue);
                //Log.d("Node Pop","Searching key: "+no.key+"Searching hashValue: "+no.hashvalue);

                if(a.get(i).key.equals(no.key) && a.get(i).hashvalue.equals(no.hashvalue))
                {
                    Log.d("Node_Pop","Found index");
                    return i;
                }
            }
            Log.d("Node_Pop","Didnt find index");
            return -1;
        }

        public static int getNext(ArrayList<NodeObject> a,NodeObject no){

            int i = search(a,no);
            if(i == a.size()-1)
            {
                return 0;
            }
            else
            {
                return i+1;
            }
        }

        public static int getPrev(ArrayList<NodeObject> a,NodeObject no){

            int i = search(a,no);
            if(i == 0)
            {
                return a.size()-1;
            }
            else
            {
                return i-1;
            }
        }
    }
}
