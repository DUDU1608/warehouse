package com.anunayagro.warehouse.dto;

public class StockistSummaryDTO {
    private String stockistName;
    private Double companyPurchase = 0.0;
    private Double selfStorage = 0.0;
    private Double totalQuantity = 0.0;
    private Double margin = 0.0;
    private Double cashLoan = 0.0;
    private Double marginLoan = 0.0;
    private Double totalLoan = 0.0;

    // Default constructor
    public StockistSummaryDTO() {}

    // All-args constructor
    public StockistSummaryDTO(String stockistName, Double companyPurchase, Double selfStorage, Double totalQuantity, Double margin, Double cashLoan, Double marginLoan, Double totalLoan) {
        this.stockistName = stockistName;
        this.companyPurchase = companyPurchase;
        this.selfStorage = selfStorage;
        this.totalQuantity = totalQuantity;
        this.margin = margin;
        this.cashLoan = cashLoan;
        this.marginLoan = marginLoan;
        this.totalLoan = totalLoan;
    }

    // Getters and Setters
    public String getStockistName() {
        return stockistName;
    }
    public void setStockistName(String stockistName) {
        this.stockistName = stockistName;
    }

    public Double getCompanyPurchase() {
        return companyPurchase;
    }
    public void setCompanyPurchase(Double companyPurchase) {
        this.companyPurchase = companyPurchase;
    }

    public Double getSelfStorage() {
        return selfStorage;
    }
    public void setSelfStorage(Double selfStorage) {
        this.selfStorage = selfStorage;
    }

    public Double getTotalQuantity() {
        return totalQuantity;
    }
    public void setTotalQuantity(Double totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public Double getMargin() {
        return margin;
    }
    public void setMargin(Double margin) {
        this.margin = margin;
    }

    public Double getCashLoan() {
        return cashLoan;
    }
    public void setCashLoan(Double cashLoan) {
        this.cashLoan = cashLoan;
    }

    public Double getMarginLoan() {
        return marginLoan;
    }
    public void setMarginLoan(Double marginLoan) {
        this.marginLoan = marginLoan;
    }

    public Double getTotalLoan() {
        return totalLoan;
    }
    public void setTotalLoan(Double totalLoan) {
        this.totalLoan = totalLoan;
    }
}

