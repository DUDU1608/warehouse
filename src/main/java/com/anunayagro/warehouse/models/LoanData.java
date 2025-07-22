package com.anunayagro.warehouse.models;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "loan_data")
public class LoanData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate date;
    private String stockistName;
    private String commodity;
    private String warehouse;
    private String loanType; // "Cash" or "Margin"
    private Double amount;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getStockistName() { return stockistName; }
    public void setStockistName(String stockistName) { this.stockistName = stockistName; }

    public String getCommodity() { return commodity; }
    public void setCommodity(String commodity) { this.commodity = commodity; }

    public String getWarehouse() { return warehouse; }
    public void setWarehouse(String warehouse) { this.warehouse = warehouse; }

    public String getLoanType() { return loanType; }
    public void setLoanType(String loanType) { this.loanType = loanType; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
}


