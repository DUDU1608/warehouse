package com.anunayagro.warehouse.models;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(
        name = "stockdata",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"rstNo", "warehouse"})
        }
)
public class StockData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDate date;
    private String rstNo;
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

    @Column(name = "kind_of_stock")
    private String kindOfStock; // "Self" or "Transferred"

    // --- Calculation logic ---
    public void autoCalculate() {
        if (quantity != null && reduction != null)
            this.netQty = quantity - reduction;
        if (netQty != null && rate != null)
            this.cost = netQty * rate;
        if (cost != null && handling != null)
            this.totalCost = cost + handling;
    }

    // --- Safe getters for fallback in display ---
    @Transient
    public Double getNetQtySafe() {
        if (netQty != null) return netQty;
        if (quantity != null && reduction != null) return quantity - reduction;
        return null;
    }
    @Transient
    public Double getCostSafe() {
        if (cost != null) return cost;
        Double net = getNetQtySafe();
        if (net != null && rate != null) return net * rate;
        return null;
    }
    @Transient
    public Double getTotalCostSafe() {
        if (totalCost != null) return totalCost;
        Double c = getCostSafe();
        if (c != null && handling != null) return c + handling;
        return null;
    }

    // --- Getters and Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getRstNo() { return rstNo; }
    public void setRstNo(String rstNo) { this.rstNo = rstNo; }

    public String getWarehouse() { return warehouse; }
    public void setWarehouse(String warehouse) { this.warehouse = warehouse; }

    public String getStockistName() { return stockistName; }
    public void setStockistName(String stockistName) { this.stockistName = stockistName; }

    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }

    public String getCommodity() { return commodity; }
    public void setCommodity(String commodity) { this.commodity = commodity; }

    public Double getQuantity() { return quantity; }
    public void setQuantity(Double quantity) { this.quantity = quantity; }

    public Double getReduction() { return reduction; }
    public void setReduction(Double reduction) { this.reduction = reduction; }

    public Double getNetQty() { return netQty; }
    public void setNetQty(Double netQty) { this.netQty = netQty; }

    public Double getRate() { return rate; }
    public void setRate(Double rate) { this.rate = rate; }

    public Double getCost() { return cost; }
    public void setCost(Double cost) { this.cost = cost; }

    public Double getHandling() { return handling; }
    public void setHandling(Double handling) { this.handling = handling; }

    public Double getTotalCost() { return totalCost; }
    public void setTotalCost(Double totalCost) { this.totalCost = totalCost; }

    public String getQuality() { return quality; }
    public void setQuality(String quality) { this.quality = quality; }

    public String getKindOfStock() { return kindOfStock; }
    public void setKindOfStock(String kindOfStock) { this.kindOfStock = kindOfStock; }
}
