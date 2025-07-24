package com.anunayagro.warehouse.controllers.admin;

import com.anunayagro.warehouse.dto.FinalReportDTO;
import com.anunayagro.warehouse.models.Stockist;
import com.anunayagro.warehouse.repositories.StockDataRepository;
import com.anunayagro.warehouse.repositories.StockistRepository;
import com.anunayagro.warehouse.services.finalreport.FinalReportService;

import com.lowagie.text.Table;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

import com.lowagie.text.*;

import com.lowagie.text.pdf.PdfWriter;

import jakarta.servlet.http.HttpServletResponse;
import java.awt.Color;

@Controller
public class FinalReportController {

    @Autowired
    private FinalReportService finalReportService;

    @Autowired
    private StockistRepository stockistRepository;

    @Autowired
    private StockDataRepository stockDataRepository;

    // GET - show form
    @GetMapping("/final-report")
    public String showFinalReportForm(Model model) {
        List<Stockist> stockists = stockistRepository.findAll();
        List<String> warehouses = stockDataRepository.findDistinctWarehouses();
        List<String> commodities = stockDataRepository.findDistinctCommodities();

        model.addAttribute("stockists", stockists);
        model.addAttribute("warehouses", warehouses);
        model.addAttribute("commodities", commodities);
        model.addAttribute("report", null); // initially no report

        return "final-report";
    }

    // POST - generate report
    @PostMapping("/final-report")
    public String generateFinalReport(
            @RequestParam String stockistName,
            @RequestParam String warehouse,
            @RequestParam String commodity,
            @RequestParam double rate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Model model
    ) {
        FinalReportDTO report = finalReportService.generateReport(
                stockistName, warehouse, commodity, rate, date
        );

        List<Stockist> stockists = stockistRepository.findAll();
        List<String> warehouses = stockDataRepository.findDistinctWarehouses();
        List<String> commodities = stockDataRepository.findDistinctCommodities();

        model.addAttribute("stockists", stockists);
        model.addAttribute("warehouses", warehouses);
        model.addAttribute("commodities", commodities);
        model.addAttribute("report", report);

        return "final-report";
    }

    @GetMapping("/final-report/pdf")
    public void exportFinalReportPdf(
            @RequestParam String stockistName,
            @RequestParam String warehouse,
            @RequestParam String commodity,
            @RequestParam double rate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            HttpServletResponse response) throws Exception {

        FinalReportDTO report = finalReportService.generateReport(stockistName, warehouse, commodity, rate, date);
        finalReportService.exportFinalReportToPdf(report, response);
    }
}
