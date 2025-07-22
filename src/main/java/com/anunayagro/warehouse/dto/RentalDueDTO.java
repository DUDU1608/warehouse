package com.anunayagro.warehouse.dto;

public class RentalDueDTO {
    private double totalRentalDue;

    public RentalDueDTO(double totalRentalDue) {
        this.totalRentalDue = totalRentalDue;
    }

    public double getTotalRentalDue() {
        return totalRentalDue;
    }

    public void setTotalRentalDue(double totalRentalDue) {
        this.totalRentalDue = totalRentalDue;
    }
}
