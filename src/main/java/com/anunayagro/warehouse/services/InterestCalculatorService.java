package com.anunayagro.warehouse.services;

import com.anunayagro.warehouse.dto.InterestDetailDTO;
import com.anunayagro.warehouse.models.LoanData;
import com.anunayagro.warehouse.models.MarginData;
import com.anunayagro.warehouse.repositories.LoanDataRepository;
import com.anunayagro.warehouse.repositories.MarginDataRepository;
import com.anunayagro.warehouse.repositories.StockistRepository;
import com.anunayagro.warehouse.models.Stockist;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
public class InterestCalculatorService {

    private static final double ROI = 13.75 / 100.0; // 13.75% per annum
    private static final double DAILY_ROI = ROI / 365.0;

    @Autowired
    private LoanDataRepository loanDataRepository;
    @Autowired
    private MarginDataRepository marginDataRepository;
    @Autowired
    private StockistRepository stockistRepository;

    // Helper class for transaction events
    private static class Txn {
        LocalDate date;
        double amount; // +ve for loan, -ve for margin payment
        String type;
        Txn(LocalDate date, double amount, String type) {
            this.date = date;
            this.amount = amount;
            this.type = type;
        }
    }

    // For summary
    public double calculateInterestDue(String mobile) {
        return Math.round(calcInterestDetail(mobile, false).totalInterest * 100.0) / 100.0;
    }

    // For details
    public List<InterestDetailDTO> getInterestDetails(String mobile) {
        return calcInterestDetail(mobile, true).details;
    }

    // Main logic, shared by both summary and details
    private CalcResult calcInterestDetail(String mobile, boolean wantDetails) {
        Optional<Stockist> stockistOpt = stockistRepository.findByMobile(mobile);
        if (stockistOpt.isEmpty()) return new CalcResult();

        String stockistName = stockistOpt.get().getStockistName();
        List<LoanData> loans = loanDataRepository.findByStockistName(stockistName);
        List<MarginData> margins = marginDataRepository.findByStockistName(stockistName);

        // Collect all loan and margin events
        List<Txn> txns = new ArrayList<>();
        for (LoanData loan : loans) {
            if (loan.getAmount() != null && loan.getDate() != null) {
                txns.add(new Txn(loan.getDate(), loan.getAmount(), "Loan (" + loan.getLoanType() + ")"));
            }
        }
        for (MarginData margin : margins) {
            if (margin.getAmount() != null && margin.getDate() != null) {
                txns.add(new Txn(margin.getDate(), -margin.getAmount(), "Margin Paid"));
            }
        }
        if (txns.isEmpty()) return new CalcResult();

        // Sort all txns by date
        txns.sort(Comparator.comparing(t -> t.date));

        LocalDate today = LocalDate.now();
        LocalDate cursor = txns.get(0).date;
        double principal = 0.0;
        double cumulativeInterest = 0.0;

        List<InterestDetailDTO> detailList = wantDetails ? new ArrayList<>() : null;

        int idx = 0;
        while (!cursor.isAfter(today)) {
            // Apply all txns on this day
            while (idx < txns.size() && txns.get(idx).date.equals(cursor)) {
                principal += txns.get(idx).amount;
                if (wantDetails) {
                    detailList.add(new InterestDetailDTO(
                            cursor,
                            txns.get(idx).type,
                            txns.get(idx).amount,
                            principal,
                            0.0, // will update with interest next step
                            0.0
                    ));
                }
                idx++;
            }
            // Compute day's interest (on opening principal, before today’s txn if any)
            if (principal > 0) {
                double dayInterest = principal * DAILY_ROI;
                cumulativeInterest += dayInterest;
                if (wantDetails) {
                    // Add/update a "Interest Accrued" row for this date
                    detailList.add(new InterestDetailDTO(
                            cursor,
                            "Interest Accrued",
                            0.0,
                            principal,
                            Math.round(dayInterest * 100.0) / 100.0,
                            Math.round(cumulativeInterest * 100.0) / 100.0
                    ));
                }
            }
            cursor = cursor.plusDays(1);
        }
        CalcResult res = new CalcResult();
        res.totalInterest = cumulativeInterest;
        if (wantDetails) res.details = detailList;
        return res;
    }

    private static class CalcResult {
        double totalInterest = 0.0;
        List<InterestDetailDTO> details = new ArrayList<>();
    }
}
