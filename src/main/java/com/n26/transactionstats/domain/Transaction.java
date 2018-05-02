package com.n26.transactionstats.domain;

import javax.validation.constraints.NotNull;

public class Transaction {
    @NotNull
    private Double amount;
    @NotNull
    private Long timestamp;

    public Transaction() {
    }

    public Transaction(Double amount, Long timestamp) {
        this.amount = amount;
        this.timestamp = timestamp;
    }


    public Double getAmount() {
        return amount;
    }

    public Long getTimestamp() {
        return timestamp;
    }

}
