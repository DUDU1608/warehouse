package com.anunayagro.warehouse.controllers.user;

import com.anunayagro.warehouse.models.StockData;
import com.anunayagro.warehouse.models.StockExit;
import com.anunayagro.warehouse.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.*;

@Controller
public class UserStockSummaryController {

    @Autowired private StockDataRepository stockDataRepository;
    @Autowired private StockExitRepository stockExitRepository;
    @Autowired private MarginDataRepository marginDataRepository;
    @Autowired private LoanDataRepository loanDataRepository;
    @Autowired private StockistRepository stockistRepository;

    @GetMapping("/user/stock-summary")
    public String userStockSummary(
            @RequestParam(required = false) String warehouse,
            @RequestParam(required = false) String commodity,
            Model model,
            Principal principal
    ) {
        // Get logged-in user mobile and mapped stockistName
        String mobile = principal.getName();
        String stockistName = stockistRepository.findByMobile(mobile)
                .map(s -> s.getStockistName())
                .orElse("");

        // Add warehouse/commodity filter lists for the form
        model.addAttribute("warehouses", stockDataRepository.findDistinctWarehouses());
        model.addAttribute("commodities", List.of("Wheat", "Maize", "Paddy"));

        if (warehouse != null && commodity != null && !warehouse.isBlank() && !commodity.isBlank()) {
            // --- Company Purchase (kindOfStock = transferred) ---
            Double companyPurchase = stockDataRepository.sumNetQtyByStockistNameAndWarehouseAndCommodityAndKindOfStock(
                    stockistName, warehouse, commodity, "transferred"
            );
            if (companyPurchase == null) companyPurchase = 0.0;

            // --- Self Storage (kindOfStock = self or empty) ---
            Double selfStorage = stockDataRepository.sumNetQtyByStockistNameAndWarehouseAndCommodityAndKindOfStockIn(
                    stockistName, warehouse, commodity, Arrays.asList("self", "")
            );
            if (selfStorage == null) selfStorage = 0.0;

            // --- Withdrawn ---
            Double withdrawn = stockExitRepository.sumQuantityByStockistNameAndWarehouseAndCommodity(
                    stockistName, warehouse, commodity
            );
            if (withdrawn == null) withdrawn = 0.0;

            // --- Net Available Stock ---
            Double netAvailableStock = companyPurchase + selfStorage - withdrawn;

            // --- Margin Paid ---
            Double marginPaid = marginDataRepository.sumAmountByStockistNameAndWarehouseAndCommodity(
                    stockistName, warehouse, commodity
            );
            if (marginPaid == null) marginPaid = 0.0;

            // --- Cash Loan ---
            Double cashLoan = loanDataRepository.sumAmountByStockistNameAndWarehouseAndCommodityAndLoanType(
                    stockistName, warehouse, commodity, "Cash"
            );
            if (cashLoan == null) cashLoan = 0.0;

            // --- Margin Loan ---
            Double marginLoan = loanDataRepository.sumAmountByStockistNameAndWarehouseAndCommodityAndLoanType(
                    stockistName, warehouse, commodity, "Margin"
            );
            if (marginLoan == null) marginLoan = 0.0;

            // --- Total Loan Due ---
            Double totalLoanDue = cashLoan + marginLoan - marginPaid;

            // --- Build Summary Map (field names must match Thymeleaf keys) ---
            Map<String, Object> summary = new HashMap<>();
            summary.put("stockistName", stockistName);
            summary.put("companyPurchase", companyPurchase);
            summary.put("selfStorage", selfStorage);
            summary.put("withdrawn", withdrawn);
            summary.put("netAvailableStock", netAvailableStock);
            summary.put("marginPaid", marginPaid);
            summary.put("cashLoan", cashLoan);
            summary.put("marginLoan", marginLoan);
            summary.put("totalLoanDue", totalLoanDue);

            model.addAttribute("summary", summary);
            model.addAttribute("warehouse", warehouse);
            model.addAttribute("commodity", commodity);
        }

        return "user/stock-summary";
    }
}

