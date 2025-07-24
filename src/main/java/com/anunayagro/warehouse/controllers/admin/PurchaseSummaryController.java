package com.anunayagro.warehouse.controllers.admin;

import com.anunayagro.warehouse.dto.PurchaseSummaryDTO;
import com.anunayagro.warehouse.repositories.PurchaseRepository;
import com.anunayagro.warehouse.services.PurchaseSummaryService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.List;

@Controller
public class PurchaseSummaryController {

    @Autowired
    private PurchaseSummaryService purchaseSummaryService;

    @Autowired
    private PurchaseRepository purchaseRepository;

    @GetMapping("/purchase-summary")
    public String showPurchaseSummary(
            @RequestParam(required = false) String seller,
            @RequestParam(required = false) String warehouse,
            @RequestParam(required = false) String commodity,
            Model model) {

        // Get grouped and filtered summary
        List<PurchaseSummaryDTO> summaries = purchaseSummaryService.getGroupedSummary(seller, warehouse, commodity);

        // Totals
        double totalQty = summaries.stream().mapToDouble(PurchaseSummaryDTO::getQuantity).sum();
        double totalRed = summaries.stream().mapToDouble(PurchaseSummaryDTO::getReduction).sum();
        double totalNet = summaries.stream().mapToDouble(PurchaseSummaryDTO::getNetQty).sum();
        double totalCost = summaries.stream().mapToDouble(PurchaseSummaryDTO::getCost).sum();
        double totalHandling = summaries.stream().mapToDouble(PurchaseSummaryDTO::getHandling).sum();
        double totalFinal = summaries.stream().mapToDouble(PurchaseSummaryDTO::getTotalCost).sum();
        double totalPaid = summaries.stream().mapToDouble(PurchaseSummaryDTO::getAmount).sum();
        double totalDue = summaries.stream().mapToDouble(PurchaseSummaryDTO::getPaymentDue).sum();

        // Set data to model
        model.addAttribute("summaries", summaries);
        model.addAttribute("totalQty", totalQty);
        model.addAttribute("totalRed", totalRed);
        model.addAttribute("totalNet", totalNet);
        model.addAttribute("totalCost", totalCost);
        model.addAttribute("totalHandling", totalHandling);
        model.addAttribute("totalFinal", totalFinal);
        model.addAttribute("totalPaid", totalPaid);
        model.addAttribute("totalDue", totalDue);

        // For filter dropdowns
        model.addAttribute("allSellers", purchaseRepository.findDistinctSeller());
        model.addAttribute("allWarehouses", purchaseRepository.findDistinctWarehouses());
        model.addAttribute("allCommodities", purchaseRepository.findDistinctCommodities());

        // Preserve selected filters
        model.addAttribute("seller", seller);
        model.addAttribute("warehouse", warehouse);
        model.addAttribute("commodity", commodity);

        return "purchase-summary"; // Thymeleaf template in templates/
    }

    @GetMapping("/purchase-summary/export/excel")
    public void exportToExcel(@RequestParam(required = false) String seller,
                              @RequestParam(required = false) String warehouse,
                              @RequestParam(required = false) String commodity,
                              HttpServletResponse response) throws IOException {

        List<PurchaseSummaryDTO> summaries = purchaseSummaryService.getGroupedSummary(seller, warehouse, commodity);
        purchaseSummaryService.exportToExcel(summaries, response);
    }

    @GetMapping("/purchase-summary/export/pdf")
    public void exportToPdf(@RequestParam(required = false) String seller,
                            @RequestParam(required = false) String warehouse,
                            @RequestParam(required = false) String commodity,
                            HttpServletResponse response) throws IOException {

        List<PurchaseSummaryDTO> summaries = purchaseSummaryService.getGroupedSummary(seller, warehouse, commodity);
        purchaseSummaryService.exportToPdf(summaries, response);
    }

}

