package com.anunayagro.warehouse.dto;

import java.time.LocalDate;

public class RentalDetailDTO {
    private LocalDate date;
    private double quantity;
    private long days;
    private double rental;
    private double cumulativeRental;

    public RentalDetailDTO(LocalDate date, double quantity, long days, double rental, double cumulativeRental) {
        this.date = date;
        this.quantity = quantity;
        this.days = days;
        this.rental = rental;
        this.cumulativeRental = cumulativeRental;
    }

    public LocalDate getDate() { return date; }
    public double getQuantity() { return quantity; }
    public long getDays() { return days; }
    public double getRental() { return rental; }
    public double getCumulativeRental() { return cumulativeRental; }
}
