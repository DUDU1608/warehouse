package com.anunayagro.warehouse.dto;

import java.time.LocalDate;

public class InterestDetailDTO {
    private LocalDate date;
    private String type;
    private Double amount;        // Loan (+), Margin Paid (-), or 0 for interest row
    private Double principal;     // Outstanding on this day
    private Double interest;      // Interest for the day
    private Double cumulativeInterest;

    public InterestDetailDTO(LocalDate date, String type, Double amount, Double principal, Double interest, Double cumulativeInterest) {
        this.date = date;
        this.type = type;
        this.amount = amount;
        this.principal = principal;
        this.interest = interest;
        this.cumulativeInterest = cumulativeInterest;
    }
    public LocalDate getDate() { return date; }
    public String getType() { return type; }
    public Double getAmount() { return amount; }
    public Double getPrincipal() { return principal; }
    public Double getInterest() { return interest; }
    public Double getCumulativeInterest() { return cumulativeInterest; }

    public void setDate(LocalDate date) { this.date = date; }
    public void setType(String type) { this.type = type; }
    public void setAmount(Double amount) { this.amount = amount; }
    public void setPrincipal(Double principal) { this.principal = principal; }
    public void setInterest(Double interest) { this.interest = interest; }
    public void setCumulativeInterest(Double cumulativeInterest) { this.cumulativeInterest = cumulativeInterest; }
}
