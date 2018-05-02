package com.n26.transactionstats.store;

import com.n26.transactionstats.domain.Summary;
import com.n26.transactionstats.domain.Transaction;
import org.junit.Before;
import org.junit.Test;

import java.time.Instant;
import java.util.concurrent.*;
import java.util.stream.IntStream;

import static org.junit.Assert.*;

public class TimeBucketedSummaryStoreTest {

    private TimeBucketedSummaryStore timeBucketedSummaryStore;

    @Before
    public void setUp() throws Exception {
        timeBucketedSummaryStore = new TimeBucketedSummaryStore(1000L, 60000L);
    }

    @Test
    public void shouldAddTransactionConcurrently() throws Exception{
        CountDownLatch countDownLatch = new CountDownLatch(10);
        ExecutorService executorService = Executors.newFixedThreadPool(10);

        IntStream.rangeClosed(1, 10).forEach(i -> {
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    long timestamp = Instant.now().toEpochMilli();
                    Transaction transaction = new Transaction(10.0, timestamp);
                    timeBucketedSummaryStore.addTransaction(transaction);
                    countDownLatch.countDown();
                }
            });
        });
        countDownLatch.await();

        Summary summaryForLast60Sec = timeBucketedSummaryStore.getSummaryForLastNSec(60);
        assertEquals((Double)100.0, summaryForLast60Sec.getSum());
        assertEquals((Double)10.0, summaryForLast60Sec.getMax());
        assertEquals((Double)10.0, summaryForLast60Sec.getMin());
        assertEquals((Double)10.0, summaryForLast60Sec.getAvg());
        assertEquals((Long)10L, summaryForLast60Sec.getCount());

    }

    @Test
    public void shouldReturnZeroSummaryIfNoTransactionsAdded() {
        Summary summaryForLast60Sec = timeBucketedSummaryStore.getSummaryForLastNSec(60);
        assertEquals((Double)0.0, summaryForLast60Sec.getSum());
        assertEquals((Double)0.0, summaryForLast60Sec.getMax());
        assertEquals((Double)Double.MAX_VALUE, summaryForLast60Sec.getMin());
        assertEquals((Double)0.0, summaryForLast60Sec.getAvg());
        assertEquals((Long)0L, summaryForLast60Sec.getCount());
    }
}