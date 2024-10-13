package com.dws.challenge.service;

import com.dws.challenge.domain.FundsTransfer;
import com.dws.challenge.repository.AccountsRepository;
import lombok.Getter;
import org.springframework.stereotype.Service;

@Service
public class FundsTransferService {

    @Getter
    private final AccountsRepository accountsRepository;

    public FundsTransferService(AccountsRepository accountsRepository) {
        this.accountsRepository = accountsRepository;
    }

    public void transferFunds(FundsTransfer fundsTransfer) {
        this.accountsRepository.transferFunds(fundsTransfer);
    }
}
