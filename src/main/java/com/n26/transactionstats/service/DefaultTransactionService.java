package com.n26.transactionstats.service;

import com.n26.transactionstats.domain.Summary;
import com.n26.transactionstats.domain.Transaction;
import com.n26.transactionstats.domain.TransactionCreationStatus;
import com.n26.transactionstats.store.TimeBucketedSummaryStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class DefaultTransactionService implements TransactionService {

    private final Long TIME_DURATION_TO_SUMMARIZE;
    private TimeBucketedSummaryStore timeBucketedSummaryStore;

    @Autowired
    public DefaultTransactionService(@Value("${max.duration.for.summarization.millis}") Long maxDurationForSummarization, TimeBucketedSummaryStore timeBucketedSummaryStore) {
        TIME_DURATION_TO_SUMMARIZE = maxDurationForSummarization;
        this.timeBucketedSummaryStore = timeBucketedSummaryStore;
    }

    @Override
    public TransactionCreationStatus createTransaction(Transaction transaction) {
        if (transaction.getTimestamp() < Instant.now().minusMillis(TIME_DURATION_TO_SUMMARIZE).toEpochMilli())
            return TransactionCreationStatus.OBSOLETE;
        timeBucketedSummaryStore.addTransaction(transaction);
        return TransactionCreationStatus.CREATED;
    }

    @Override
    public Summary getSummary() {
        int secs = (int) (TIME_DURATION_TO_SUMMARIZE / 1000);
        return timeBucketedSummaryStore.getSummaryForLastNSec(secs);
    }
}
