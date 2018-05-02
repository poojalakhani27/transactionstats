package com.n26.transactionstats.store;

import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

/**
 * An in-memory implementation of a key value store.
 * This data structure is a bounded data structure.
 * It is a morphed version of <code>LinkedHashMap</code> with the ability to automatically purge the older entries by natural ordering of K
 * In order to maintain a bounded store, a <code>PriorityQueue</code> is used as a buffer. If the buffer is full, the queue is polled.
 * This is not a thread safe data structure.
 *
 * @param <K> The key for bucketing. The LRU stategy is by natural ordering of this object. Hence it should be Comparable.
 *            The key is also used in hashing. Hence <i>hashcode()</i> should be implemented efficiently for lesser collisions.
 * @param <V> the value to store.
 */
public class InMemoryKeyValueStore<K, V> implements KeyValueStore<K, V> {

    private Map<K, V> map;
    private Queue<K> buffer;

    public InMemoryKeyValueStore(int maxSize) {
        this.map = new ConcurrentHashMap<>(maxSize);
        buffer = new PriorityQueue<>(maxSize);
    }

    @Override
    public void put(K k, V v) {
        map.put(k, v);
        boolean inserted = buffer.offer(k);
        if (!inserted) {
            buffer.poll();
            buffer.offer(k);
        }
    }

    @Override
    public V getOrDefault(K k, V defaultValue) {
        return map.getOrDefault(k, defaultValue);
    }
}
