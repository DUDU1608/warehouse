package com.anunayagro.warehouse.dto;

public class StockSummaryDTO {
    private String stockistName;
    private double companyPurchase;
    private double selfStorage;
    private double totalQuantity;
    private double margin;
    private double cashLoan;
    private double marginLoan;
    private double totalLoan;

    // No-args constructor
    public StockSummaryDTO() {}

    // All-args constructor
    public StockSummaryDTO(String stockistName, double companyPurchase, double selfStorage, double totalQuantity,
                           double margin, double cashLoan, double marginLoan, double totalLoan) {
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

    public double getCompanyPurchase() {
        return companyPurchase;
    }

    public void setCompanyPurchase(double companyPurchase) {
        this.companyPurchase = companyPurchase;
    }

    public double getSelfStorage() {
        return selfStorage;
    }

    public void setSelfStorage(double selfStorage) {
        this.selfStorage = selfStorage;
    }

    public double getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(double totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public double getMargin() {
        return margin;
    }

    public void setMargin(double margin) {
        this.margin = margin;
    }

    public double getCashLoan() {
        return cashLoan;
    }

    public void setCashLoan(double cashLoan) {
        this.cashLoan = cashLoan;
    }

    public double getMarginLoan() {
        return marginLoan;
    }

    public void setMarginLoan(double marginLoan) {
        this.marginLoan = marginLoan;
    }

    public double getTotalLoan() {
        return totalLoan;
    }

    public void setTotalLoan(double totalLoan) {
        this.totalLoan = totalLoan;
    }
}
