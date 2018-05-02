package com.n26.transactionstats.controllers;

import com.n26.transactionstats.domain.Summary;
import com.n26.transactionstats.domain.Transaction;
import com.n26.transactionstats.domain.TransactionCreationStatus;
import com.n26.transactionstats.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.n26.transactionstats.domain.TransactionCreationStatus.OBSOLETE;

@RestController
public class TransactionController {

    private TransactionService transactionService;

    @Autowired
    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping(path = "/transactions")
    public ResponseEntity createTransaction(@Validated @RequestBody Transaction transaction) {
        TransactionCreationStatus status = transactionService.createTransaction(transaction);

        if (OBSOLETE.equals(status))
            return new ResponseEntity(HttpStatus.NO_CONTENT);
        else
            return new ResponseEntity(HttpStatus.CREATED);
    }

    @GetMapping(path = "/statistics")
    @ResponseBody
    public Summary getStatistics() {
        return transactionService.getSummary();
    }
}
