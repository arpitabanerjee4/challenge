package com.dws.challenge.repository;

import com.dws.challenge.domain.Account;
import com.dws.challenge.domain.FundsTransfer;
import com.dws.challenge.exception.DuplicateAccountIdException;
import com.dws.challenge.exception.NegativeBalanceException;
import com.dws.challenge.service.EmailNotificationService;
import com.dws.challenge.service.NotificationService;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class AccountsRepositoryInMemory implements AccountsRepository {

    private final Map<String, Account> accounts = new ConcurrentHashMap<>();
    private final NotificationService notificationService = new EmailNotificationService();

    @Override
    public void createAccount(Account account) throws DuplicateAccountIdException {
        Account previousAccount = accounts.putIfAbsent(account.getAccountId(), account);
        if (previousAccount != null) {
            throw new DuplicateAccountIdException(
                    "Account id " + account.getAccountId() + " already exists!");
        }
    }

    @Override
    public Account getAccount(String accountId) {
        return accounts.get(accountId);
    }

    @Override
    public void clearAccounts() {
        accounts.clear();
    }

    @Override
    public void transferFunds(FundsTransfer fundsTransfer) {
        synchronized (this) {
            Account fromAccount = accounts.computeIfAbsent(fundsTransfer.getAccountFrom().getAccountId(), account -> fundsTransfer.getAccountFrom());
            BigDecimal deductedBalance = accounts.get(fundsTransfer.getAccountFrom().getAccountId()).getBalance().subtract(fundsTransfer.getAmount());
            fromAccount.setBalance(deductedBalance);
            if (deductedBalance.signum() < 0) {
                throw new NegativeBalanceException(
                        "Cannot transfer funds due to Insufficient funds balance in Account id " + fundsTransfer.getAccountFrom());
            }
            notificationService.notifyAboutTransfer(fromAccount, "Amount Sent " + fundsTransfer.getAmount() + " From " + fundsTransfer.getAccountFrom().getAccountId() + ". Available Balance is " + deductedBalance);
            Account toAccount = accounts.computeIfAbsent(fundsTransfer.getAccountTo().getAccountId(), account -> fundsTransfer.getAccountTo());
            BigDecimal addedBalance = accounts.get(fundsTransfer.getAccountTo().getAccountId()).getBalance().add(fundsTransfer.getAmount());
            toAccount.setBalance(addedBalance);
            notificationService.notifyAboutTransfer(toAccount, "Amount Received " + fundsTransfer.getAmount() + " To " + fundsTransfer.getAccountTo().getAccountId() + ". Available Balance is " + addedBalance);
        }
    }
}
