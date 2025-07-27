package com.anunayagro.warehouse.models;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "stock_exit")
public class StockExit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate date;

    private String warehouse;

    private String stockistName;

    private String mobile;

    private String commodity;

    private Double quantity;

    private Double reduction;

    private Double netQty;

    private Double rate;

    private Double cost;

    private Double handling;

    private Double totalCost;

    private String quality;

    // --- Getters and Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(String warehouse) {
        this.warehouse = warehouse;
    }

    public String getStockistName() {
        return stockistName;
    }

    public void setStockistName(String stockistName) {
        this.stockistName = stockistName;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getCommodity() {
        return commodity;
    }

    public void setCommodity(String commodity) {
        this.commodity = commodity;
    }

    public Double getQuantity() {
        return quantity;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

    public Double getReduction() {
        return reduction;
    }

    public void setReduction(Double reduction) {
        this.reduction = reduction;
    }

    public Double getNetQty() {
        return netQty;
    }

    public void setNetQty(Double netQty) {
        this.netQty = netQty;
    }

    public Double getRate() {
        return rate;
    }

    public void setRate(Double rate) {
        this.rate = rate;
    }

    public Double getCost() {
        return cost;
    }

    public void setCost(Double cost) {
        this.cost = cost;
    }

    public Double getHandling() {
        return handling;
    }

    public void setHandling(Double handling) {
        this.handling = handling;
    }

    public Double getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(Double totalCost) {
        this.totalCost = totalCost;
    }

    public String getQuality() {
        return quality;
    }

    public void setQuality(String quality) {
        this.quality = quality;
    }
}

