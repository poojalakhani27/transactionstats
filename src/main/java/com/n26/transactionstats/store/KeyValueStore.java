package com.n26.transactionstats.store;

public interface KeyValueStore<K, V> {
    void put(K k, V v);

    V getOrDefault(K k, V defaultValue);
}
