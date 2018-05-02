package com.n26.transactionstats.store;

import com.n26.transactionstats.domain.Summary;
import com.n26.transactionstats.domain.Transaction;

public interface SummaryStore {
    void addTransaction(Transaction transaction);

    Summary getSummaryForLastNSec(int n);
}
