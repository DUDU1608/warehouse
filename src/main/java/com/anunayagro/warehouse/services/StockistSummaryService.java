package com.anunayagro.warehouse.services;

import com.anunayagro.warehouse.dto.StockistSummaryDTO;
import com.anunayagro.warehouse.models.Stockist;
import com.anunayagro.warehouse.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class StockistSummaryService {

    @Autowired
    private StockistRepository stockistRepository;

    @Autowired
    private StockDataRepository stockDataRepository;

    @Autowired
    private MarginDataRepository marginDataRepository;

    @Autowired
    private LoanDataRepository loanDataRepository;

    public List<StockistSummaryDTO> getAllStockistSummaries() {
        List<StockistSummaryDTO> summaries = new ArrayList<>();
        List<Stockist> stockists = stockistRepository.findAll();

        for (Stockist s : stockists) {
            String stockistName = s.getStockistName();

            // 1. Company Purchase
            Double companyPurchase = stockDataRepository.sumQuantityByStockistNameAndKindOfStock(stockistName, "Transferred");
            // 2. Self Storage
            Double selfStorage = stockDataRepository.sumQuantityByStockistNameAndKindOfStockIn(
                    stockistName, Arrays.asList("Self", "", null)
            );


            // 3. Total Quantity
            Double totalQuantity = (companyPurchase == null ? 0 : companyPurchase) + (selfStorage == null ? 0 : selfStorage);

            // 4. Margin (from marginData)
            Double margin = marginDataRepository.sumMarginByStockistName(stockistName);

            // 5. Cash Loan and 6. Margin Loan
            Double cashLoan = loanDataRepository.sumLoanByStockistNameAndLoanType(stockistName, "Cash");
            Double marginLoan = loanDataRepository.sumLoanByStockistNameAndLoanType(stockistName, "Margin");

            // 7. Total Loan
            Double totalLoan = (cashLoan == null ? 0 : cashLoan) + (marginLoan == null ? 0 : marginLoan);

            // Build DTO
            StockistSummaryDTO dto = new StockistSummaryDTO(
                    stockistName,
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
