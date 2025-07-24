package com.anunayagro.warehouse.dto;

import java.time.LocalDate;

public class FinalReportDTO {

    private String stockistName;
    private String warehouse;
    private String commodity;
    private double totalStock;
    private double reduction;
    private double netStock;
    private double rate;
    private double totalCost;
    private double interest;
    private double rental;
    private double netPayable;
    private LocalDate reportDate;

    // Constructors
    public FinalReportDTO() {
    }

    public FinalReportDTO(String stockistName, String warehouse, String commodity,
                          double totalStock, double reduction, double netStock,
                          double rate, double totalCost, double interest,
                          double rental, double netPayable, LocalDate reportDate) {
        this.stockistName = stockistName;
        this.warehouse = warehouse;
        this.commodity = commodity;
        this.totalStock = totalStock;
        this.reduction = reduction;
        this.netStock = netStock;
        this.rate = rate;
        this.totalCost = totalCost;
        this.interest = interest;
        this.rental = rental;
        this.netPayable = netPayable;
        this.reportDate = reportDate;
    }

    // Getters and Setters

    public String getStockistName() {
        return stockistName;
    }

    public void setStockistName(String stockistName) {
        this.stockistName = stockistName;
    }

    public String getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(String warehouse) {
        this.warehouse = warehouse;
    }

    public String getCommodity() {
        return commodity;
    }

    public void setCommodity(String commodity) {
        this.commodity = commodity;
    }

    public double getTotalStock() {
        return totalStock;
    }

    public void setTotalStock(double totalStock) {
        this.totalStock = totalStock;
    }

    public double getReduction() {
        return reduction;
    }

    public void setReduction(double reduction) {
        this.reduction = reduction;
    }

    public double getNetStock() {
        return netStock;
    }

    public void setNetStock(double netStock) {
        this.netStock = netStock;
    }

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }

    public double getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(double totalCost) {
        this.totalCost = totalCost;
    }

    public double getInterest() {
        return interest;
    }

    public void setInterest(double interest) {
        this.interest = interest;
    }

    public double getRental() {
        return rental;
    }

    public void setRental(double rental) {
        this.rental = rental;
    }

    public double getNetPayable() {
        return netPayable;
    }

    public void setNetPayable(double netPayable) {
        this.netPayable = netPayable;
    }

    public LocalDate getReportDate() {
        return reportDate;
    }

    public void setReportDate(LocalDate reportDate) {
        this.reportDate = reportDate;
    }
}
