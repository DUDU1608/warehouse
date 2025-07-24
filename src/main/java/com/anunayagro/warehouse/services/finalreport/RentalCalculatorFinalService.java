package com.anunayagro.warehouse.services.finalreport;

import com.anunayagro.warehouse.models.StockData;
import com.anunayagro.warehouse.repositories.StockDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class RentalCalculatorFinalService {

    @Autowired
    private StockDataRepository stockDataRepository;

    private static final double PER_DAY_RENT_PER_MT = 100.0 / 30.0;

    public double calculateRental(String stockistName, LocalDate uptoDate) {
        List<StockData> stockList = stockDataRepository.findByStockistName(stockistName);
        double totalRental = 0.0;

        for (StockData stock : stockList) {
            if (stock.getQuantity() != null && stock.getDate() != null) {
                long days = ChronoUnit.DAYS.between(stock.getDate(), uptoDate);
                if (days < 0) continue;
                double quantityInMT = stock.getQuantity() / 1000.0;
                double rental = quantityInMT * PER_DAY_RENT_PER_MT * days;
                totalRental += rental;
            }
        }
        return Math.round(totalRental * 100.0) / 100.0;
    }
}

