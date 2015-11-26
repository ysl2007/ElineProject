package edu.bit.eline.system.run;

public class Pair<K, V> {
    private K key;
    private V val;

    public Pair(K key, V val) {
        this.key = key;
        this.val = val;
    }

    public synchronized void setKey(K key) {
        this.key = key;
    }

    public synchronized void setVal(V val) {
        this.val = val;
    }

    public synchronized void setPair(K key, V val) {
        this.key = key;
        this.val = val;
    }

    public synchronized K getKey() {
        return key;
    }

    public synchronized V getVal() {
        return val;
    }
}
