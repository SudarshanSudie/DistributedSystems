package edu.buffalo.cse.cse486586.simpledht;

/**
 * Created by sudie on 4/4/17.
 */

public class NodeObject implements Comparable<NodeObject> {

    String key;
    String hashvalue;

    public NodeObject(String k,String v)
    {
        this.key = k;
        this.hashvalue = v;
    }

//    @Override
    public boolean equals(NodeObject obj) {
        if (obj == null) {
            return false;
        }
        if (!NodeObject.class.isAssignableFrom(obj.getClass())) {
            return false;
        }
        final NodeObject other = (NodeObject) obj;
        if ((this.key == null) ? (other.key != null) : !this.key.equals(other.key)) {
            return false;
        }
        if (!this.hashvalue.equals(other.hashvalue)) {
            return false;
        }
        return true;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getHashvalue() {
        return hashvalue;
    }

    public void setHashvalue(String hashvalue) {
        this.hashvalue = hashvalue;
    }

    @Override
    public String toString() {
        return "NodeObject [key=" + key + ", hashvalue=" + hashvalue + "]";
    }

    @Override
    public int compareTo(NodeObject another)
    {
        return this.hashvalue.compareTo(another.hashvalue);
    }
}
