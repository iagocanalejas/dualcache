package es.coru.iagocanalejas.library;


import es.coru.iagocanalejas.library.interfaces.SizeOf;

/**
 * Created by Iago on 26/12/2016.
 */

public class VolatileSizeOf<T> implements SizeOf<VolatileCacheEntry<T>> {

    private SizeOf<T> mSizeOf;

    public VolatileSizeOf(SizeOf<T> sizeOf) {
        this.mSizeOf = sizeOf;
    }

    /**
     * Add size of long to sizeof object
     *
     * @param object is the instance against the computation has to be done.
     * @return size of {@link VolatileCacheEntry}
     */
    @Override
    public int sizeOf(VolatileCacheEntry<T> object) {
        return mSizeOf.sizeOf(object.getItem()) + (Double.SIZE / Byte.SIZE);
    }
}
