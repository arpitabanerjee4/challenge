package com.dws.challenge.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class FundsTransfer {
    @NotNull
    private final Account accountFrom;

    @NotNull
    private final Account accountTo;

    @NotNull
    @Min(value = 0, message = "The amount to transfer should always be a positive number.Overdrafts are not supported.")
    private BigDecimal amount;


    @JsonCreator
    public FundsTransfer(@JsonProperty("accountFrom") Account accountFrom,
                         @JsonProperty("accountTo") Account accountTo,
                         @JsonProperty("amount") BigDecimal amount) {
        this.accountFrom = accountFrom;
        this.accountTo = accountTo;
        this.amount = amount;
    }
}
