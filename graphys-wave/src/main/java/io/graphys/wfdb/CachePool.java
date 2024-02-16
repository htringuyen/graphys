package io.graphys.wfdb;

import java.util.Map;

public interface CachePool<K, V> {
    boolean put(K key, V value);

    V get(K key);

    void clear();
}
