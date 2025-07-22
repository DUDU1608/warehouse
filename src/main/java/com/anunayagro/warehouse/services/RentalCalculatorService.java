package com.anunayagro.warehouse.services;
import com.anunayagro.warehouse.dto.RentalDetailDTO;
import com.anunayagro.warehouse.models.StockData;
import com.anunayagro.warehouse.repositories.StockDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
public class RentalCalculatorService {

    @Autowired
    private StockDataRepository stockDataRepository;

    public List<RentalDetailDTO> getRentalDetails(String stockistName) {
        final double PER_DAY_RENT_PER_MT = 100.0 / 30.0;
        List<StockData> stockList = stockDataRepository.findByStockistName(stockistName);

        List<RentalDetailDTO> details = new ArrayList<>();
        double cumulative = 0.0;
        LocalDate today = LocalDate.now();

        for (StockData stock : stockList) {
            if (stock.getQuantity() != null && stock.getDate() != null) {
                long days = ChronoUnit.DAYS.between(stock.getDate(), today);
                if (days < 0) continue;
                double quantityInMT = stock.getQuantity() / 1000.0;
                double rental = quantityInMT * PER_DAY_RENT_PER_MT * days;
                cumulative += rental;
                details.add(new RentalDetailDTO(
                        stock.getDate(),
                        stock.getQuantity(),
                        days,
                        Math.round(rental * 100.0) / 100.0,
                        Math.round(cumulative * 100.0) / 100.0
                ));
            }
        }
        return details;
    }

    public double calculateTotalRental(String stockistName) {
        final double PER_DAY_RENT_PER_MT = 100.0 / 30.0;
        List<StockData> stockList = stockDataRepository.findByStockistName(stockistName);

        double totalRental = 0.0;
        LocalDate today = LocalDate.now();

        for (StockData stock : stockList) {
            if (stock.getQuantity() != null && stock.getDate() != null) {
                long days = ChronoUnit.DAYS.between(stock.getDate(), today);
                if (days < 0) continue; // skip future-dated stock
                double quantityInMT = stock.getQuantity() / 1000.0; // convert KG to MT
                double rental = quantityInMT * PER_DAY_RENT_PER_MT * days;
                totalRental += rental;
            }
        }
        return Math.round(totalRental * 100.0) / 100.0;
    }
}