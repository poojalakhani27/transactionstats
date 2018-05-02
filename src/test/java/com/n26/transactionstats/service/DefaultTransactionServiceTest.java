package com.n26.transactionstats.service;

import com.n26.transactionstats.domain.Summary;
import com.n26.transactionstats.domain.Transaction;
import com.n26.transactionstats.domain.TransactionCreationStatus;
import com.n26.transactionstats.store.TimeBucketedSummaryStore;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.time.Instant;

import static com.n26.transactionstats.domain.TransactionCreationStatus.CREATED;
import static com.n26.transactionstats.domain.TransactionCreationStatus.OBSOLETE;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class DefaultTransactionServiceTest {

    private final long maxDurationForSummarization = 60000L;
    private TransactionService transactionService;

    @Mock
    private TimeBucketedSummaryStore timeBucketedSummaryStore;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        transactionService = new DefaultTransactionService(maxDurationForSummarization, timeBucketedSummaryStore);
    }

    @Test
    public void shouldReturnObsoleteStatusWhenTransactionIsOlderThan60Seconds() {
        Transaction oldTransaction = new Transaction(12.5, Instant.now().minusMillis(maxDurationForSummarization+5).toEpochMilli());

        TransactionCreationStatus transaction = transactionService.createTransaction(oldTransaction);

        assertEquals(OBSOLETE, transaction);
        verify(timeBucketedSummaryStore, never()).addTransaction(oldTransaction);
    }

    @Test
    public void shouldReturnCreatedStatusWhenTransactionIsWithinLast60Seconds() {
        Transaction transaction = new Transaction(12.5, Instant.now().toEpochMilli());

        TransactionCreationStatus transactionCreationStatus = transactionService.createTransaction(transaction);

        assertEquals(CREATED, transactionCreationStatus);
        verify(timeBucketedSummaryStore).addTransaction(transaction);
    }


    @Test
    public void shouldReturnSummaryForLast60Seconds() {
        Summary expectedSummary = new Summary();
        when(timeBucketedSummaryStore.getSummaryForLastNSec((int)maxDurationForSummarization/1000)).thenReturn(expectedSummary);

        Summary actualSummary = transactionService.getSummary();

        assertEquals(expectedSummary, actualSummary);
    }
}