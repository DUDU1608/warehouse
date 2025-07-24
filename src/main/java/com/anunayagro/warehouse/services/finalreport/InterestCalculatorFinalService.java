package com.anunayagro.warehouse.services.finalreport;

import com.anunayagro.warehouse.models.LoanData;
import com.anunayagro.warehouse.models.MarginData;
import com.anunayagro.warehouse.models.Stockist;
import com.anunayagro.warehouse.repositories.LoanDataRepository;
import com.anunayagro.warehouse.repositories.MarginDataRepository;
import com.anunayagro.warehouse.repositories.StockistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
public class InterestCalculatorFinalService {

    private static final double ROI = 13.75 / 100.0; // 13.75% p.a.
    private static final double DAILY_ROI = ROI / 365.0;

    @Autowired
    private LoanDataRepository loanDataRepository;

    @Autowired
    private MarginDataRepository marginDataRepository;

    @Autowired
    private StockistRepository stockistRepository;

    public double calculateInterest(String mobile, LocalDate uptoDate) {
        Optional<Stockist> stockistOpt = stockistRepository.findByMobile(mobile);
        if (stockistOpt.isEmpty()) return 0.0;

        String stockistName = stockistOpt.get().getStockistName();
        List<LoanData> loans = loanDataRepository.findByStockistName(stockistName);
        List<MarginData> margins = marginDataRepository.findByStockistName(stockistName);

        // Combine loan (+) and margin (-) as Txns
        List<Txn> txns = new ArrayList<>();
        for (LoanData loan : loans) {
            if (loan.getAmount() != null && loan.getDate() != null) {
                txns.add(new Txn(loan.getDate(), loan.getAmount()));
            }
        }
        for (MarginData margin : margins) {
            if (margin.getAmount() != null && margin.getDate() != null) {
                txns.add(new Txn(margin.getDate(), -margin.getAmount()));
            }
        }

        if (txns.isEmpty()) return 0.0;

        txns.sort(Comparator.comparing(t -> t.date));

        LocalDate cursor = txns.get(0).date;
        double principal = 0.0;
        double cumulativeInterest = 0.0;

        int idx = 0;
        while (!cursor.isAfter(uptoDate)) {
            while (idx < txns.size() && txns.get(idx).date.equals(cursor)) {
                principal += txns.get(idx).amount;
                idx++;
            }
            if (principal > 0) {
                double dayInterest = principal * DAILY_ROI;
                cumulativeInterest += dayInterest;
            }
            cursor = cursor.plusDays(1);
        }

        return Math.round(cumulativeInterest * 100.0) / 100.0;
    }

    private static class Txn {
        LocalDate date;
        double amount;
        Txn(LocalDate date, double amount) {
            this.date = date;
            this.amount = amount;
        }
    }
}

