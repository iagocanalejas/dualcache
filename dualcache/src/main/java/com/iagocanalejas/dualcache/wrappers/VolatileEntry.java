package com.iagocanalejas.dualcache.wrappers;

import java.sql.Timestamp;

/**
 * Created by Iago on 26/12/2016.
 * Wraps the catchable object setting a duration
 */
public class VolatileEntry<T> {

    private Timestamp mTimestamp;
    private T mItem;

    public VolatileEntry(Long time, T item) {
        this.mTimestamp = new Timestamp(time);
        this.mItem = item;
    }

    public Timestamp getTimestamp() {
        return mTimestamp;
    }

    public T getItem() {
        return mItem;
    }

}
