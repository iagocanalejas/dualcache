package com.iagocanalejas.dualcache.wrappers;


import com.iagocanalejas.dualcache.interfaces.SizeOf;

/**
 * Created by Iago on 26/12/2016.
 * Wraps {@link SizeOf} to add the size of a double representing {@link VolatileEntry#timestamp}
 */
public class VolatileSizeOf<T> implements SizeOf<VolatileEntry<T>> {

    private SizeOf<T> mSizeOf;

    public VolatileSizeOf(SizeOf<T> sizeOf) {
        this.mSizeOf = sizeOf;
    }

    /**
     * Add size of long to sizeof object
     *
     * @param object is the instance against the computation has to be done.
     * @return size of {@link VolatileEntry}
     */
    @Override
    public int sizeOf(VolatileEntry<T> object) {
        return mSizeOf.sizeOf(object.getItem()) + (Double.SIZE / Byte.SIZE);
    }
}
