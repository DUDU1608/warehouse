package com.anunayagro.warehouse.dto;

public class PurchaseSummaryDTO {

    private String seller;
    private String warehouse;
    private String commodity;

    private double quantity;
    private double reduction;
    private double netQty;
    private double cost;
    private double handling;
    private double totalCost;

    private double amount;       // Total amount paid from Payment model
    private double paymentDue;   // totalCost - amount

    // Constructors
    public PurchaseSummaryDTO() {
    }

    public PurchaseSummaryDTO(String seller, String warehouse, String commodity,
                              double quantity, double reduction, double netQty,
                              double cost, double handling, double totalCost,
                              double amount, double paymentDue) {
        this.seller = seller;
        this.warehouse = warehouse;
        this.commodity = commodity;
        this.quantity = quantity;
        this.reduction = reduction;
        this.netQty = netQty;
        this.cost = cost;
        this.handling = handling;
        this.totalCost = totalCost;
        this.amount = amount;
        this.paymentDue = paymentDue;
    }

    // Getters and Setters

    public String getSeller() {
        return seller;
    }

    public void setSeller(String seller) {
        this.seller = seller;
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

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public double getReduction() {
        return reduction;
    }

    public void setReduction(double reduction) {
        this.reduction = reduction;
    }

    public double getNetQty() {
        return netQty;
    }

    public void setNetQty(double netQty) {
        this.netQty = netQty;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public double getHandling() {
        return handling;
    }

    public void setHandling(double handling) {
        this.handling = handling;
    }

    public double getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(double totalCost) {
        this.totalCost = totalCost;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public double getPaymentDue() {
        return paymentDue;
    }

    public void setPaymentDue(double paymentDue) {
        this.paymentDue = paymentDue;
    }
}
