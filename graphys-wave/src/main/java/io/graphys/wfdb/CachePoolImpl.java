package io.graphys.wfdb;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class CachePoolImpl<K, V> implements CachePool<K, V> {
    private K lastCachedId;

    private Map<K, V> cacheMap;

    private int cachingLimit;

    private ReentrantLock lock;

    CachePoolImpl(int cachingLimit) {
        this.cachingLimit = cachingLimit;
        this.cacheMap = new HashMap<>();
        this.lock = new ReentrantLock();
    }

    @Override
    public boolean put(K key, V value) {
        try {
            lock.lock();

            if (cachingLimit == 0) {
                return false;
            }

            if (key == null || value == null) {
                throw new RuntimeException("Key and value caching must not be null");
            }

            if (cacheMap.size() < cachingLimit) {
                cacheMap.put(key, value);
                return true;
            }
            else if (cacheMap.containsKey(key)) {
                return false;
            }
            else if (lastCachedId == null) {
                throw new IllegalStateException("Cache pool is entirely filled but last cached id is null.");
            }
            else {
                cacheMap.remove(key);
                lastCachedId = key;
                cacheMap.put(key, value);
                return true;
            }
        }
        finally {
            lock.unlock();
        }
    }

    @Override
    public V get(K key) {
        try {
            lock.lock();
            return cacheMap.get(key);
        }
        finally {
            lock.unlock();
        }
    }

    @Override
    public void clear() {
        try {
            lock.lock();
            cacheMap.clear();
            lastCachedId = null;
        }
        finally {
            lock.unlock();
        }
    }
}































