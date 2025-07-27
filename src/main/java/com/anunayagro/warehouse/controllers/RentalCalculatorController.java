package com.anunayagro.warehouse.controllers;

import com.anunayagro.warehouse.models.StockData;
import com.anunayagro.warehouse.models.StockExit;
import com.anunayagro.warehouse.repositories.StockDataRepository;
import com.anunayagro.warehouse.repositories.StockExitRepository;
import com.anunayagro.warehouse.repositories.StockistRepository;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.Principal;
import java.time.LocalDate;
import java.util.*;
import java.util.List;
import java.util.stream.Stream;

@Controller
public class RentalCalculatorController {
    @Autowired private StockDataRepository stockDataRepository;
    @Autowired private StockExitRepository stockExitRepository;
    @Autowired private StockistRepository stockistRepository;

    // --- ADMIN FORM ---
    @GetMapping("/stockist/rental-calculator")
    public String showAdminForm(Model model) {
        model.addAttribute("stockists", stockistRepository.findAll().stream().map(s -> s.getStockistName()).toList());
        model.addAttribute("warehouses", stockDataRepository.findDistinctWarehouses());
        model.addAttribute("commodities", List.of("Wheat", "Maize", "Paddy"));
        model.addAttribute("mode", "admin");
        return "stockist/rental-calculator";
    }

    // --- USER FORM ---
    @GetMapping("/user/rental-calculator")
    public String showUserForm(Model model, Principal principal) {
        String mobile = principal.getName();
        String stockistName = stockistRepository.findByMobile(mobile)
                .map(s -> s.getStockistName())
                .orElse("");
        model.addAttribute("stockistName", stockistName);
        model.addAttribute("warehouses", stockDataRepository.findDistinctWarehouses());
        model.addAttribute("commodities", List.of("Wheat", "Maize", "Paddy"));
        model.addAttribute("mode", "user");
        return "user/rental-calculator";
    }

    // --- RENTAL CALCULATION (ADMIN + USER: SHARED METHOD) ---
    private Map<String, Object> calculateRentalCore(String stockistName, String warehouse, String commodity, LocalDate uptoDate) {
        Map<String, Object> resp = new HashMap<>();
        if (stockistName == null || warehouse == null || commodity == null || uptoDate == null) {
            resp.put("error", "Missing parameters");
            return resp;
        }

        // --- Calculation logic ---
        LocalDate startDate = stockDataRepository.findEarliestDate(stockistName, warehouse, commodity);
        if (startDate == null) startDate = uptoDate;
        List<Map<String, Object>> details = new ArrayList<>();
        double totalRental = 0;
        for (LocalDate date = startDate; !date.isAfter(uptoDate); date = date.plusDays(1)) {
            Double stored = stockDataRepository.sumQuantityUpto(stockistName, warehouse, commodity, date);
            Double exited = stockExitRepository.sumQuantityUpto(stockistName, warehouse, commodity, date);
            stored = (stored == null) ? 0.0 : stored;
            exited = (exited == null) ? 0.0 : exited;
            double netStock = stored - exited;
            if (netStock < 0) netStock = 0;
            double rental = (netStock / 1000.0) * 100.0 / 30.0;
            totalRental += rental;
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("date", date);
            row.put("stored", stored);
            row.put("exited", exited);
            row.put("netStock", netStock);
            row.put("rental", rental);
            details.add(row);
        }

        resp.put("summary", Map.of(
                "stockistName", stockistName,
                "warehouse", warehouse,
                "commodity", commodity,
                "rental", totalRental
        ));
        resp.put("details", details);
        return resp;
    }

    @PostMapping({"/stockist/rental-calculator/calculate", "/user/rental-calculator/calculate"})
    @ResponseBody
    public Map<String, Object> calculateRental(
            @RequestParam(required = false) String stockistName,
            @RequestParam(required = false) String warehouse,
            @RequestParam(required = false) String commodity,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate uptoDate,
            Principal principal
    ) {
        // For user mode, override stockistName by looking up from principal's mobile
        if (principal != null && (stockistName == null || stockistName.isBlank())) {
            String mobile = principal.getName();
            stockistName = stockistRepository.findByMobile(mobile)
                    .map(s -> s.getStockistName())
                    .orElse("");
        }
        return calculateRentalCore(stockistName, warehouse, commodity, uptoDate);
    }

    // --- EXPORT TO EXCEL ---
    @GetMapping({"/stockist/rental-calculator/export-excel", "/user/rental-calculator/export-excel"})
    public void exportExcel(
            @RequestParam String stockistName,
            @RequestParam String warehouse,
            @RequestParam String commodity,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate uptoDate,
            HttpServletResponse response
    ) throws IOException {
        Map<String, Object> result = calculateRentalCore(stockistName, warehouse, commodity, uptoDate);
        List<Map<String, Object>> details = (List<Map<String, Object>>) result.get("details");

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=rental-details.xlsx");

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Rental Details");
        Row header = sheet.createRow(0);
        String[] columns = {"Date", "Total Stored (kg)", "Exit (kg)", "Net Stock (kg)", "Rental (₹)"};
        for (int i = 0; i < columns.length; i++) header.createCell(i).setCellValue(columns[i]);

        int rowIdx = 1;
        for (Map<String, Object> row : details) {
            Row r = sheet.createRow(rowIdx++);
            r.createCell(0).setCellValue(row.get("date").toString());
            r.createCell(1).setCellValue(Double.parseDouble(row.get("stored").toString()));
            r.createCell(2).setCellValue(Double.parseDouble(row.get("exited").toString()));
            r.createCell(3).setCellValue(Double.parseDouble(row.get("netStock").toString()));
            r.createCell(4).setCellValue(Double.parseDouble(row.get("rental").toString()));
        }

        workbook.write(response.getOutputStream());
        workbook.close();
    }

    // --- EXPORT TO PDF ---
    @GetMapping({"/stockist/rental-calculator/export-pdf", "/user/rental-calculator/export-pdf"})
    public void exportPdf(
            @RequestParam String stockistName,
            @RequestParam String warehouse,
            @RequestParam String commodity,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate uptoDate,
            HttpServletResponse response
    ) throws IOException {
        Map<String, Object> result = calculateRentalCore(stockistName, warehouse, commodity, uptoDate);
        List<Map<String, Object>> details = (List<Map<String, Object>>) result.get("details");

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=rental-details.pdf");

        Document doc = new Document(PageSize.A4.rotate());
        PdfWriter.getInstance(doc, response.getOutputStream());
        doc.open();

        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12);

        doc.add(new Paragraph("Rental Details", headerFont));
        doc.add(new Paragraph("Stockist: " + stockistName + ", Warehouse: " + warehouse + ", Commodity: " + commodity + ", Upto: " + uptoDate));
        doc.add(Chunk.NEWLINE);

        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        Stream.of("Date", "Total Stored (kg)", "Exit (kg)", "Net Stock (kg)", "Rental (₹)")
                .forEach(col -> {
                    PdfPCell cell = new PdfPCell(new Phrase(col, headerFont));
                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table.addCell(cell);
                });

        for (Map<String, Object> row : details) {
            table.addCell(new Phrase(row.get("date").toString(), normalFont));
            table.addCell(new Phrase(row.get("stored").toString(), normalFont));
            table.addCell(new Phrase(row.get("exited").toString(), normalFont));
            table.addCell(new Phrase(row.get("netStock").toString(), normalFont));
            table.addCell(new Phrase(String.format("%.2f", Double.parseDouble(row.get("rental").toString())), normalFont));
        }

        doc.add(table);
        doc.close();
    }

}

