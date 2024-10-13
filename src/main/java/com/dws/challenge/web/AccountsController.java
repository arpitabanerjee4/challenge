package com.dws.challenge.web;

import com.dws.challenge.domain.Account;
import com.dws.challenge.domain.FundsTransfer;
import com.dws.challenge.exception.DuplicateAccountIdException;
import com.dws.challenge.exception.NegativeBalanceException;
import com.dws.challenge.service.AccountsService;
import com.dws.challenge.service.FundsTransferService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/accounts")
@Slf4j
public class AccountsController {

    private final AccountsService accountsService;

    private final FundsTransferService fundsTransferService;

    @Autowired
    public AccountsController(AccountsService accountsService, FundsTransferService fundsTransferService) {
        this.accountsService = accountsService;
        this.fundsTransferService = fundsTransferService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> createAccount(@RequestBody @Valid Account account) {
        log.info("Creating account {}", account);

        try {
            this.accountsService.createAccount(account);
        } catch (DuplicateAccountIdException daie) {
            return new ResponseEntity<>(daie.getMessage(), HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping(path = "/{accountId}")
    public Account getAccount(@PathVariable String accountId) {
        log.info("Retrieving account for id {}", accountId);
        return this.accountsService.getAccount(accountId);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, path = "/funds-transfer")
    public ResponseEntity<Object> transferFunds(@RequestBody @Valid FundsTransfer fundsTransfer) {
        log.info("Transferring funds {}", fundsTransfer);
        try {
            this.fundsTransferService.transferFunds(fundsTransfer);
        } catch (NegativeBalanceException exception) {
            return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
