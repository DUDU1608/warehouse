package com.anunayagro.warehouse.services;

import com.anunayagro.warehouse.dto.StockSummaryDTO;
import com.anunayagro.warehouse.models.Stockist;
import com.anunayagro.warehouse.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class StockSummaryService {

    @Autowired
    private StockistRepository stockistRepository;

    @Autowired
    private StockDataRepository stockDataRepository;

    @Autowired
    private MarginDataRepository marginDataRepository;

    @Autowired
    private LoanDataRepository loanDataRepository;

    public List<String> getAllWarehouses() {
        return stockDataRepository.findDistinctWarehouses();
    }

    // Get all distinct commodities from StockData table
    public List<String> getAllCommodities() {
        return stockDataRepository.findDistinctCommodities();
    }

    // Get all distinct stockist names from Stockist table
    public List<String> getAllStockistNames() {
        return stockistRepository.findDistinctStockistNames();
    }

    public List<StockSummaryDTO> getFilteredSummaries(String warehouse, String commodity, String stockistName) {

        List<Stockist> stockists;
        if (stockistName != null && !stockistName.isEmpty()) {
            Optional<Stockist> stockistOpt = stockistRepository.findByStockistName(stockistName);
            stockists = stockistOpt.map(List::of).orElseGet(ArrayList::new);
        } else {
            stockists = stockistRepository.findAll();
        }
        List<StockSummaryDTO> summaries = new ArrayList<>();
        for (Stockist s : stockists) {
            String name = s.getStockistName();

            // Company Purchase (kindOfStock = "Transferred")
            Double companyPurchase = stockDataRepository.sumQuantityByStockistNameAndKindOfStockFiltered(name, "Transferred", warehouse, commodity);

            // Self Storage (kindOfStock = "Self" or "" or null)
            Double selfStorage = stockDataRepository.sumQuantityByStockistNameAndKindOfStockInFiltered(name, Arrays.asList("Self", "", null), warehouse, commodity);

            Double totalQuantity = (companyPurchase == null ? 0 : companyPurchase) + (selfStorage == null ? 0 : selfStorage);

            // Margin
            Double margin = marginDataRepository.sumMarginByStockistNameFiltered(name, warehouse, commodity);

            // Loans
            Double cashLoan = loanDataRepository.sumLoanByStockistNameAndLoanTypeFiltered(name, "Cash", warehouse, commodity);
            Double marginLoan = loanDataRepository.sumLoanByStockistNameAndLoanTypeFiltered(name, "Margin", warehouse, commodity);

            // Total Loan (per your new logic: Cash loan + Margin loan - Margin)
            Double totalLoan = (cashLoan == null ? 0 : cashLoan) + (marginLoan == null ? 0 : marginLoan) - (margin == null ? 0 : margin);

            StockSummaryDTO dto = new StockSummaryDTO(
                    name,
                    companyPurchase == null ? 0 : companyPurchase,
                    selfStorage == null ? 0 : selfStorage,
                    totalQuantity,
                    margin == null ? 0 : margin,
                    cashLoan == null ? 0 : cashLoan,
                    marginLoan == null ? 0 : marginLoan,
                    totalLoan
            );
            summaries.add(dto);
        }
        return summaries;
    }
}
