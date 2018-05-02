package com.n26.transactionstats.service;


import com.n26.transactionstats.domain.Summary;
import com.n26.transactionstats.domain.Transaction;
import com.n26.transactionstats.domain.TransactionCreationStatus;

public interface TransactionService {
    TransactionCreationStatus createTransaction(Transaction transaction);

    Summary getSummary();
}
