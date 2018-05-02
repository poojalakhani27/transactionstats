package com.n26.transactionstats.store;


import com.n26.transactionstats.domain.Summary;
import com.n26.transactionstats.domain.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.LongStream;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_SINGLETON;


/**
 * A thread safe, constant time retrieval and constant memory storage for {@code Summary}. The storage is in memory.
 * It stores the {@code Summary} after bucketing by {@code Summary.timestamp}. Hence a constant time retrieval is achieved.
 * It is backed by {@code InMemoryKeyValueStore}, a bounded store, that provides constant memory optimization.
 * The number of buckets is configurable by property <i>max.duration.for.summarization.millis</i>, loaded from application context.
 * The granularity of the bucket in milliseconds is configured by property <i>millis.bucket.size</i>, loaded from the application context.
 * It provides behaviour to summarize the data for last n seconds.
 */
@Component
@Scope(value = SCOPE_SINGLETON)
public class TimeBucketedSummaryStore implements SummaryStore {

    private Long bucketSize;
    private KeyValueStore<Long, Summary> summaryStore;
    private Lock lock;

    @Autowired
    public TimeBucketedSummaryStore(@Value("${millis.bucket.size}")
                                            Long bucketSize, @Value("${max.duration.for.summarization.millis}")
                                            Long maxDurationForSummarization) {
        this.bucketSize = bucketSize;
        this.summaryStore = new InMemoryKeyValueStore<Long, Summary>(Long.valueOf(maxDurationForSummarization / bucketSize).intValue());
        lock = new ReentrantLock();
    }

    @Override
    public void addTransaction(Transaction transaction) {
        lock.lock();
        try {
            Long bucketKey = floorToNearestBucket(transaction.getTimestamp());
            Summary summary = summaryStore.getOrDefault(bucketKey, new Summary());
            summary.addObservation(transaction.getAmount());
            summaryStore.put(bucketKey, summary);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Summary getSummaryForLastNSec(int n) {
        final Long bucketKey = floorToNearestBucket(Instant.now().toEpochMilli());
        return LongStream.iterate(bucketKey, i -> i - bucketSize)
                .limit(n)
                .mapToObj(i -> summaryStore.getOrDefault(i, new Summary()))
                .reduce((summary1, summary2) -> summary1.mergedSummary(summary2))
                .get();
    }

    private Long floorToNearestBucket(Long timestamp) {
        return (timestamp / bucketSize) * bucketSize;
    }

}
