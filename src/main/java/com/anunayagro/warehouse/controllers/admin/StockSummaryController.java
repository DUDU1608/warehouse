package com.anunayagro.warehouse.controllers.admin;

import com.anunayagro.warehouse.dto.StockSummaryDTO;
import com.anunayagro.warehouse.services.StockSummaryService;
import com.lowagie.text.Font;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletResponse;

import java.awt.*;
import java.awt.Color;
import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/stock-summary")
public class StockSummaryController {

    @Autowired
    private StockSummaryService stockSummaryService;

    @GetMapping("")
    public String showStockSummary(
            @RequestParam(required = false) String warehouse,
            @RequestParam(required = false) String commodity,
            @RequestParam(required = false) String stockistName,
            Model model
    ) {
        List<StockSummaryDTO> summaries = stockSummaryService.getFilteredSummaries(warehouse, commodity, stockistName);

        // Calculate sums for each column for table footer
        double sumCompanyPurchase = summaries.stream().mapToDouble(StockSummaryDTO::getCompanyPurchase).sum();
        double sumSelfStorage = summaries.stream().mapToDouble(StockSummaryDTO::getSelfStorage).sum();
        double sumTotalQuantity = summaries.stream().mapToDouble(StockSummaryDTO::getTotalQuantity).sum();
        double sumMargin = summaries.stream().mapToDouble(StockSummaryDTO::getMargin).sum();
        double sumCashLoan = summaries.stream().mapToDouble(StockSummaryDTO::getCashLoan).sum();
        double sumMarginLoan = summaries.stream().mapToDouble(StockSummaryDTO::getMarginLoan).sum();
        double sumTotalLoan = summaries.stream().mapToDouble(StockSummaryDTO::getTotalLoan).sum();

        model.addAttribute("summaries", summaries);
        model.addAttribute("warehouses", stockSummaryService.getAllWarehouses());
        model.addAttribute("commodities", stockSummaryService.getAllCommodities());
        model.addAttribute("stockistNames", stockSummaryService.getAllStockistNames());
        model.addAttribute("selectedWarehouse", warehouse);
        model.addAttribute("selectedCommodity", commodity);
        model.addAttribute("selectedStockistName", stockistName);

        // Column totals
        model.addAttribute("sumCompanyPurchase", sumCompanyPurchase);
        model.addAttribute("sumSelfStorage", sumSelfStorage);
        model.addAttribute("sumTotalQuantity", sumTotalQuantity);
        model.addAttribute("sumMargin", sumMargin);
        model.addAttribute("sumCashLoan", sumCashLoan);
        model.addAttribute("sumMarginLoan", sumMarginLoan);
        model.addAttribute("sumTotalLoan", sumTotalLoan);

        return "stocksummary/stock-summary";
    }

    // Excel Export
    @GetMapping("/export/excel")
    public void exportToExcel(
            @RequestParam(required = false) String warehouse,
            @RequestParam(required = false) String commodity,
            @RequestParam(required = false) String stockistName,
            HttpServletResponse response
    ) throws IOException {
        List<StockSummaryDTO> summaries = stockSummaryService.getFilteredSummaries(warehouse, commodity, stockistName);
        ExcelExporter.export(summaries, response);
    }

    // PDF Export
    @GetMapping("/export/pdf")
    public void exportToPdf(
            @RequestParam(required = false) String warehouse,
            @RequestParam(required = false) String commodity,
            @RequestParam(required = false) String stockistName,
            HttpServletResponse response
    ) throws IOException {
        List<StockSummaryDTO> summaries = stockSummaryService.getFilteredSummaries(warehouse, commodity, stockistName);
        PdfExporter.export(summaries, response);
    }

    // ----------- Static Inner Class: Excel Exporter -------------
    public static class ExcelExporter {
        public static void export(List<StockSummaryDTO> summaries, HttpServletResponse response) throws IOException {
            response.setContentType("application/octet-stream");
            String headerKey = "Content-Disposition";
            String headerValue = "attachment; filename=stock_summary.xlsx";
            response.setHeader(headerKey, headerValue);

            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Stock Summary");

            Row header = sheet.createRow(0);
            String[] columns = {
                    "Stockist Name", "Company Purchase", "Self Storage", "Total Quantity",
                    "Margin", "Cash Loan", "Margin Loan", "Total Loan"
            };
            for (int i = 0; i < columns.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(columns[i]);
            }

            int rowCount = 1;
            for (StockSummaryDTO s : summaries) {
                Row row = sheet.createRow(rowCount++);
                row.createCell(0).setCellValue(s.getStockistName());
                row.createCell(1).setCellValue(s.getCompanyPurchase());
                row.createCell(2).setCellValue(s.getSelfStorage());
                row.createCell(3).setCellValue(s.getTotalQuantity());
                row.createCell(4).setCellValue(s.getMargin());
                row.createCell(5).setCellValue(s.getCashLoan());
                row.createCell(6).setCellValue(s.getMarginLoan());
                row.createCell(7).setCellValue(s.getTotalLoan());
            }

            // Totals Row
            Row totalRow = sheet.createRow(rowCount);
            totalRow.createCell(0).setCellValue("TOTAL");
            totalRow.createCell(1).setCellValue(summaries.stream().mapToDouble(StockSummaryDTO::getCompanyPurchase).sum());
            totalRow.createCell(2).setCellValue(summaries.stream().mapToDouble(StockSummaryDTO::getSelfStorage).sum());
            totalRow.createCell(3).setCellValue(summaries.stream().mapToDouble(StockSummaryDTO::getTotalQuantity).sum());
            totalRow.createCell(4).setCellValue(summaries.stream().mapToDouble(StockSummaryDTO::getMargin).sum());
            totalRow.createCell(5).setCellValue(summaries.stream().mapToDouble(StockSummaryDTO::getCashLoan).sum());
            totalRow.createCell(6).setCellValue(summaries.stream().mapToDouble(StockSummaryDTO::getMarginLoan).sum());
            totalRow.createCell(7).setCellValue(summaries.stream().mapToDouble(StockSummaryDTO::getTotalLoan).sum());

            for (int i = 0; i < columns.length; i++) sheet.autoSizeColumn(i);
            workbook.write(response.getOutputStream());
            workbook.close();
        }
    }

    // ----------- Static Inner Class: PDF Exporter -------------
    public static class PdfExporter {
        public static void export(List<StockSummaryDTO> summaries, HttpServletResponse response) throws IOException {
            response.setContentType("application/pdf");
            String headerKey = "Content-Disposition";
            String headerValue = "attachment; filename=stock_summary.pdf";
            response.setHeader(headerKey, headerValue);

            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, response.getOutputStream());
            document.open();

            Font font = new Font(Font.HELVETICA, 13, Font.BOLD, Color.BLUE);
            Paragraph title = new Paragraph("Stock Summary", font);
            title.setAlignment(Paragraph.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(8);
            table.setWidthPercentage(100f);
            table.setSpacingBefore(10);

            // Header
            String[] columns = {
                    "Stockist Name", "Company Purchase", "Self Storage", "Total Quantity",
                    "Margin", "Cash Loan", "Margin Loan", "Total Loan"
            };
            for (String col : columns) {
                PdfPCell cell = new PdfPCell(new Phrase(col));
                cell.setBackgroundColor(Color.LIGHT_GRAY);
                cell.setPadding(5);
                table.addCell(cell);
            }

            // Data rows
            for (StockSummaryDTO s : summaries) {
                table.addCell(s.getStockistName());
                table.addCell(String.valueOf(s.getCompanyPurchase()));
                table.addCell(String.valueOf(s.getSelfStorage()));
                table.addCell(String.valueOf(s.getTotalQuantity()));
                table.addCell(String.valueOf(s.getMargin()));
                table.addCell(String.valueOf(s.getCashLoan()));
                table.addCell(String.valueOf(s.getMarginLoan()));
                table.addCell(String.valueOf(s.getTotalLoan()));
            }

            // Totals row
            PdfPCell totalCell = new PdfPCell(new Phrase("TOTAL"));
            totalCell.setColspan(1);
            totalCell.setBackgroundColor(Color.YELLOW);
            table.addCell(totalCell);
            table.addCell(String.valueOf(summaries.stream().mapToDouble(StockSummaryDTO::getCompanyPurchase).sum()));
            table.addCell(String.valueOf(summaries.stream().mapToDouble(StockSummaryDTO::getSelfStorage).sum()));
            table.addCell(String.valueOf(summaries.stream().mapToDouble(StockSummaryDTO::getTotalQuantity).sum()));
            table.addCell(String.valueOf(summaries.stream().mapToDouble(StockSummaryDTO::getMargin).sum()));
            table.addCell(String.valueOf(summaries.stream().mapToDouble(StockSummaryDTO::getCashLoan).sum()));
            table.addCell(String.valueOf(summaries.stream().mapToDouble(StockSummaryDTO::getMarginLoan).sum()));
            table.addCell(String.valueOf(summaries.stream().mapToDouble(StockSummaryDTO::getTotalLoan).sum()));

            document.add(table);
            document.close();
        }
    }
}

