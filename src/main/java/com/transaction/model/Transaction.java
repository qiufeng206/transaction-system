package com.transaction.model;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public class Transaction  {

    public Transaction() {
    }

    public Transaction(String id, BigDecimal amount, String type, String category) {
        this.id = id;
        this.amount = amount;
        this.type = type;
        this.category = category;
    }

    @NotBlank
    String id;

    @NotNull @DecimalMin("0.01")
    BigDecimal amount;

    @NotBlank
    String type;  // 如 "DEPOSIT", "WITHDRAWAL"

    @NotBlank String category;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

}
