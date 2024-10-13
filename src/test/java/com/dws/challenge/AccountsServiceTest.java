package com.dws.challenge;

import com.dws.challenge.domain.Account;
import com.dws.challenge.domain.FundsTransfer;
import com.dws.challenge.exception.DuplicateAccountIdException;
import com.dws.challenge.exception.NegativeBalanceException;
import com.dws.challenge.repository.AccountsRepository;
import com.dws.challenge.service.AccountsService;
import com.dws.challenge.service.FundsTransferService;
import com.dws.challenge.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class AccountsServiceTest {

    private final Map<String, Account> accounts = new ConcurrentHashMap<>();
    @Autowired
    private AccountsService accountsService;
    @Mock
    private FundsTransfer fundsTransfer;
    @Autowired
    private FundsTransferService fundsTransferService;
    @Autowired
    private AccountsRepository accountsRepository;
    @Mock
    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        Account accountFrom = new Account("Id-456", new BigDecimal("1000"));
        Account accountTo = new Account("Id-345", new BigDecimal("2000"));
        fundsTransfer = new FundsTransfer(accountFrom, accountTo, new BigDecimal("100.00"));
        accounts.put("Id-456", new Account("Id-456", new BigDecimal("1000")));
        accounts.put("Id-345", new Account("Id-345", new BigDecimal("1000")));
        accounts.put("account-id1", new Account("account-id1", new BigDecimal("50")));
        accounts.put("account-id2", new Account("account-id2", new BigDecimal("500")));
    }


    @Test
    void addAccount() {
        Account account = new Account("Id-123");
        account.setBalance(new BigDecimal("1000"));
        this.accountsService.createAccount(account);

        assertThat(this.accountsService.getAccount("Id-123")).isEqualTo(account);
    }

    @Test
    void addAccount_failsOnDuplicateId() {
        String uniqueId = "Id-" + System.currentTimeMillis();
        Account account = new Account(uniqueId);
        this.accountsService.createAccount(account);

        try {
            this.accountsService.createAccount(account);
            fail("Should have failed when adding duplicate account");
        } catch (DuplicateAccountIdException ex) {
            assertThat(ex.getMessage()).isEqualTo("Account id " + uniqueId + " already exists!");
        }
    }


    @Test
    void transferFunds() {
        ReflectionTestUtils.setField(accountsRepository, "accounts", accounts);
        fundsTransferService.transferFunds(fundsTransfer);
        Account accountTo = accountsService.getAccount("Id-345");
        assertThat(fundsTransfer.getAmount()).isEqualByComparingTo("100");
        assertThat(accountTo.getBalance()).isEqualByComparingTo("1100");

        Account accountFrom = accountsService.getAccount("Id-456");
        assertThat(fundsTransfer.getAmount()).isEqualByComparingTo("100");
        assertThat(accountFrom.getBalance()).isEqualByComparingTo("900");
    }

    @Test
    void transferFunds_failOnNegativeBalance() {
        Account accountFrom = new Account("account-id1", new BigDecimal("1000"));
        Account accountTo = new Account("account-id2", new BigDecimal("2000"));
        fundsTransfer = new FundsTransfer(accountFrom, accountTo, new BigDecimal("100.00"));
        ReflectionTestUtils.setField(accountsRepository, "accounts", accounts);
        try {
            fundsTransferService.transferFunds(fundsTransfer);
        } catch (NegativeBalanceException ex) {
            assertThat(ex.getMessage()).isEqualTo("Cannot transfer funds due to Insufficient funds balance in Account id " + fundsTransfer.getAccountFrom());
        }
    }
}
