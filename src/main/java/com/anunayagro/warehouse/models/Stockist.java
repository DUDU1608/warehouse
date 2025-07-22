package com.anunayagro.warehouse.models;

import jakarta.persistence.*;

@Entity
@Table(name = "stockists")
public class Stockist {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "stockist_name")
    private String stockistName;

    @Column(name = "mobile")
    private String mobile;

    @Column(name = "banking_name")
    private String bankingName;

    @Column(name = "account_number")
    private String accountNumber;

    @Column(name = "ifsc")
    private String ifsc;

    @Column(name = "bank_name")
    private String bankName;

    @Column(name = "address")
    private String address;

    // --- Getters and Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getStockistName() { return stockistName; }
    public void setStockistName(String stockistName) { this.stockistName = stockistName; }

    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }

    public String getBankingName() { return bankingName; }
    public void setBankingName(String bankingName) { this.bankingName = bankingName; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public String getIfsc() { return ifsc; }
    public void setIfsc(String ifsc) { this.ifsc = ifsc; }

    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
}

