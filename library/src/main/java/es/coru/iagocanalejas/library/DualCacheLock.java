package es.coru.iagocanalejas.library;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

class DualCacheLock {

    private final ConcurrentMap<String, Lock> mEditionLocks = new ConcurrentHashMap<>();
    private final ReadWriteLock mInvalidationReadWriteLock = new ReentrantReadWriteLock();

    void lockDiskEntryWrite(String key) {
        mInvalidationReadWriteLock.readLock().lock();
        getLockForGivenDiskEntry(key).lock();
    }

    void unLockDiskEntryWrite(String key) {
        getLockForGivenDiskEntry(key).unlock();
        mInvalidationReadWriteLock.readLock().unlock();
    }

    void lockFullDiskWrite() {
        mInvalidationReadWriteLock.writeLock().lock();
    }

    void unLockFullDiskWrite() {
        mInvalidationReadWriteLock.writeLock().unlock();
    }

    private Lock getLockForGivenDiskEntry(String key) {
        if (!mEditionLocks.containsKey(key)) {
            mEditionLocks.putIfAbsent(key, new ReentrantLock());
        }
        return mEditionLocks.get(key);
    }
}
