package com.anunayagro.warehouse.controllers.admin;

import com.anunayagro.warehouse.models.LoanData;
import com.anunayagro.warehouse.models.MarginData;
import com.anunayagro.warehouse.models.StockData;
import com.anunayagro.warehouse.models.Stockist;
import com.anunayagro.warehouse.repositories.*;
import com.lowagie.text.Font;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.awt.Color;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.web.multipart.MultipartFile;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import jakarta.servlet.http.HttpServletResponse;

// Correct for Excel
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.List;


import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import java.awt.*;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.stream.Stream;


@Controller
@RequestMapping("/stockist")
public class StockistController {

    @Autowired
    private StockistRepository stockistRepository;

    @Autowired
    private StockDataRepository stockDataRepository;

    @Autowired
    private LoanDataRepository loanDataRepository;

    @Autowired
    private MarginDataRepository marginDataRepository;


    @GetMapping("")
    public String stockistHome() {
        return "stockist/index";
    }

    @GetMapping("/add")
    public String showAddStockistForm(Model model) {
        model.addAttribute("stockist", new Stockist());
        return "stockist/add-stockist";
    }

    @PostMapping("/add")
    public String addStockist(@ModelAttribute Stockist stockist, Model model) {
        stockistRepository.save(stockist);
        model.addAttribute("success", true);
        model.addAttribute("stockist", new Stockist()); // Clear form after submit
        return "stockist/add-stockist";
    }
    @GetMapping("/list")
    public String displayStockists(Model model) {
        model.addAttribute("stockists", stockistRepository.findAll());
        return "stockist/display-stockist";
    }

    @GetMapping("/add-stockdata")
    public String showAddStockDataForm(Model model) {
        model.addAttribute("stockData", new StockData());
        model.addAttribute("stockists", stockistRepository.findAll());
        model.addAttribute("warehouses", stockDataRepository.findDistinctWarehouses());
        return "stockist/add-stockdata";
    }

    @PostMapping("/add-stockdata")
    public String addStockData(@ModelAttribute StockData stockData, Model model) {
        stockData.setKindOfStock("Self");
        // Calculate fields if needed
        stockDataRepository.save(stockData);
        model.addAttribute("success", true);
        model.addAttribute("stockData", new StockData());
        model.addAttribute("stockists", stockistRepository.findAll());
        model.addAttribute("warehouses", stockDataRepository.findDistinctWarehouses());
        return "stockist/add-stockdata";
    }

    @GetMapping("/display-stockdata")
    public String displayStockData(
            @RequestParam(value = "stockistName", required = false) String stockistName,
            @RequestParam(value = "commodity", required = false) String commodity,
            @RequestParam(value = "warehouse", required = false) String warehouse,
            @RequestParam(value = "quality", required = false) String quality,
            @RequestParam(value = "fromDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(value = "toDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            Model model
    ) {
        List<StockData> list = stockDataRepository.findAll();

        // Filter in Java (for simple cases; use Specification for large DBs)
        if (stockistName != null && !stockistName.isEmpty())
            list = list.stream().filter(s -> s.getStockistName()!=null && s.getStockistName().toLowerCase().contains(stockistName.toLowerCase())).collect(Collectors.toList());
        if (commodity != null && !commodity.isEmpty())
            list = list.stream().filter(s -> commodity.equals(s.getCommodity())).collect(Collectors.toList());
        if (warehouse != null && !warehouse.isEmpty())
            list = list.stream().filter(s -> warehouse.equals(s.getWarehouse())).collect(Collectors.toList());
        if (quality != null && !quality.isEmpty())
            list = list.stream().filter(s -> quality.equals(s.getQuality())).collect(Collectors.toList());
        if (fromDate != null)
            list = list.stream().filter(s -> s.getDate()!=null && !s.getDate().isBefore(fromDate)).collect(Collectors.toList());
        if (toDate != null)
            list = list.stream().filter(s -> s.getDate()!=null && !s.getDate().isAfter(toDate)).collect(Collectors.toList());

        model.addAttribute("stockDataList", list);
        model.addAttribute("stockists", stockistRepository.findAll().stream().map(Stockist::getStockistName).collect(Collectors.toList()));
        model.addAttribute("warehouses", stockDataRepository.findDistinctWarehouses());
        return "stockist/display-stockdata";
    }

    @PostMapping("/update-stockdata-inline")
    @ResponseBody
    public Map<String, Object> updateStockDataInline(@RequestBody StockData data) {
        Map<String, Object> resp = new HashMap<>();
        try {
            StockData existing = stockDataRepository.findById(data.getId()).orElseThrow();
            // Update fields (only editable ones, or overwrite all)
            existing.setDate(data.getDate());
            existing.setRstNo(data.getRstNo());
            existing.setWarehouse(data.getWarehouse());
            existing.setStockistName(data.getStockistName());
            existing.setMobile(data.getMobile());
            existing.setCommodity(data.getCommodity());
            existing.setQuantity(data.getQuantity());
            existing.setReduction(data.getReduction());
            existing.setNetQty(data.getNetQty());
            existing.setRate(data.getRate());
            existing.setCost(data.getCost());
            existing.setHandling(data.getHandling());
            existing.setTotalCost(data.getTotalCost());
            existing.setQuality(data.getQuality());
            // Don't allow editing kindOfStock (if you want to protect it)
            stockDataRepository.save(existing);
            resp.put("status", "ok");
        } catch (Exception ex) {
            resp.put("status", "error");
            resp.put("msg", ex.getMessage());
        }
        return resp;
    }


    @PostMapping("/delete-stockdata")
    @ResponseBody
    public Map<String, Object> deleteStockData(@RequestBody List<Long> ids) {
        Map<String, Object> resp = new HashMap<>();
        try {
            stockDataRepository.deleteAllById(ids);
            resp.put("status", "ok");
        } catch(Exception ex) {
            resp.put("status", "error");
            resp.put("msg", ex.getMessage());
        }
        return resp;
    }


    // EXPORT TO EXCEL
    @GetMapping("/export-excel")
    public void exportExcel(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=stockdata.xlsx");

        List<StockData> stockList = stockDataRepository.findAll();

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Stock Data");

        // Header row
        Row header = sheet.createRow(0);
        String[] headers = {"Date", "RST No", "Warehouse", "Stockist Name", "Mobile", "Commodity", "Quantity", "Reduction", "Net Qty", "Rate", "Cost", "Handling", "Total Cost", "Quality", "Kind Of Stock"};
        for (int i = 0; i < headers.length; i++) {
            header.createCell(i).setCellValue(headers[i]);
        }

        // Data rows
        int rowIdx = 1;
        for (StockData s : stockList) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(s.getDate() == null ? "" : s.getDate().toString());
            row.createCell(1).setCellValue(s.getRstNo());
            row.createCell(2).setCellValue(s.getWarehouse());
            row.createCell(3).setCellValue(s.getStockistName());
            row.createCell(4).setCellValue(s.getMobile());
            row.createCell(5).setCellValue(s.getCommodity());
            row.createCell(6).setCellValue(s.getQuantity() != null ? s.getQuantity() : 0.0);
            row.createCell(7).setCellValue(s.getReduction() != null ? s.getReduction() : 0.0);
            row.createCell(8).setCellValue(s.getNetQty() != null ? s.getNetQty() : 0.0);
            row.createCell(9).setCellValue(s.getRate() != null ? s.getRate() : 0.0);
            row.createCell(10).setCellValue(s.getCost() != null ? s.getCost() : 0.0);
            row.createCell(11).setCellValue(s.getHandling() != null ? s.getHandling() : 0.0);
            row.createCell(12).setCellValue(s.getTotalCost() != null ? s.getTotalCost() : 0.0);
            row.createCell(13).setCellValue(s.getQuality());
            row.createCell(14).setCellValue(s.getKindOfStock());
        }
        workbook.write(response.getOutputStream());
        workbook.close();
    }
    //export to pdf
    @GetMapping("/export-pdf")
    public void exportPdf(HttpServletResponse response) throws Exception {
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=stockdata.pdf");

        List<StockData> stockList = stockDataRepository.findAll();

        com.lowagie.text.Document document = new com.lowagie.text.Document();
        com.lowagie.text.pdf.PdfWriter.getInstance(document, response.getOutputStream());
        document.open();

        String[] headers = {"Date", "RST No", "Warehouse", "Stockist Name", "Mobile", "Commodity", "Quantity", "Reduction", "Net Qty", "Rate", "Cost", "Handling", "Total Cost", "Quality", "Kind Of Stock"};
        com.lowagie.text.pdf.PdfPTable table = new com.lowagie.text.pdf.PdfPTable(headers.length);
        for (String h : headers) {
            table.addCell(new com.lowagie.text.Phrase(h));
        }

        for (StockData s : stockList) {
            table.addCell(s.getDate() == null ? "" : s.getDate().toString());
            table.addCell(s.getRstNo());
            table.addCell(s.getWarehouse());
            table.addCell(s.getStockistName());
            table.addCell(s.getMobile());
            table.addCell(s.getCommodity());
            table.addCell(s.getQuantity() != null ? s.getQuantity().toString() : "");
            table.addCell(s.getReduction() != null ? s.getReduction().toString() : "");
            table.addCell(s.getNetQty() != null ? s.getNetQty().toString() : "");
            table.addCell(s.getRate() != null ? s.getRate().toString() : "");
            table.addCell(s.getCost() != null ? s.getCost().toString() : "");
            table.addCell(s.getHandling() != null ? s.getHandling().toString() : "");
            table.addCell(s.getTotalCost() != null ? s.getTotalCost().toString() : "");
            table.addCell(s.getQuality());
            table.addCell(s.getKindOfStock());
        }
        document.add(table);
        document.close();
    }
    //import from excel
    @PostMapping("/import-excel")
    public String importStockDataExcel(@RequestParam("file") MultipartFile file, Model model) {
        if (file.isEmpty()) {
            model.addAttribute("error", "Please select an Excel file to upload.");
            return "redirect:/stockist/display-stockdata";
        }
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            List<StockData> stockDataList = new ArrayList<>();
            boolean isFirstRow = true;
            for (Row row : sheet) {
                if (isFirstRow) { isFirstRow = false; continue; } // Skip header

                StockData sd = new StockData();
                sd.setDate(getLocalDateFromCell(row.getCell(0)));
                sd.setRstNo(getStringOrNumeric(row.getCell(1)));
                sd.setWarehouse(getStringOrNumeric(row.getCell(2)));
                sd.setStockistName(getStringOrNumeric(row.getCell(3)));
                sd.setMobile(getStringOrNumeric(row.getCell(4)));
                sd.setCommodity(getStringOrNumeric(row.getCell(5)));
                double quantity = getDoubleOrZero(row.getCell(6));
                double reduction = getDoubleOrZero(row.getCell(7));
                double rate = getDoubleOrZero(row.getCell(9));
                double handling = getDoubleOrZero(row.getCell(11));

                double netQty = quantity - reduction;
                double cost = netQty * rate;
                double totalCost = cost - handling; // as per your formula

                sd.setQuantity(quantity);
                sd.setReduction(reduction);
                sd.setNetQty(netQty);
                sd.setRate(rate);
                sd.setCost(cost);
                sd.setHandling(handling);
                sd.setTotalCost(totalCost);
                sd.setQuality(getStringOrNumeric(row.getCell(13)));
                sd.setKindOfStock(getStringOrNumeric(row.getCell(14)));
                stockDataList.add(sd);
            }
            stockDataRepository.saveAll(stockDataList);
            model.addAttribute("success", "Stock data imported successfully!");
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Failed to import: " + e.getMessage());
        }
        return "redirect:/stockist/display-stockdata";
    }
    // Utility methods:
    private String getStringOrNumeric(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING: return cell.getStringCellValue().trim();
            case NUMERIC: return String.valueOf((long)cell.getNumericCellValue());
            case FORMULA:
                if (cell.getCachedFormulaResultType() == CellType.STRING)
                    return cell.getStringCellValue();
                else if (cell.getCachedFormulaResultType() == CellType.NUMERIC)
                    return String.valueOf((long)cell.getNumericCellValue());
            default: return "";
        }
    }
    private Double getDoubleOrZero(Cell cell) {
        if (cell == null) return 0.0;
        try {
            if (cell.getCellType() == CellType.NUMERIC) return cell.getNumericCellValue();
            if (cell.getCellType() == CellType.STRING) return Double.parseDouble(cell.getStringCellValue());
        } catch (Exception e) { return 0.0; }
        return 0.0;
    }
    private LocalDate getLocalDateFromCell(Cell cell) {
        if (cell == null) return null;
        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            return cell.getLocalDateTimeCellValue().toLocalDate();
        }
        if (cell.getCellType() == CellType.STRING) {
            try { return LocalDate.parse(cell.getStringCellValue()); } catch (Exception e) { return null; }
        }
        return null;
    }

    @GetMapping("/add-loandata")
    public String showAddLoanDataForm(Model model) {
        model.addAttribute("loanData", new LoanData());
        // Get unique stockist names and warehouse names
        List<String> stockistNames = stockistRepository.findAll()
                .stream().map(Stockist::getStockistName).distinct().toList();
        List<String> warehouses = stockDataRepository.findAll()
                .stream().map(StockData::getWarehouse).distinct().toList();
        model.addAttribute("stockistNames", stockistNames);
        model.addAttribute("warehouses", warehouses);
        return "stockist/add-loandata";
    }

    @PostMapping("/add-loandata")
    public String addLoanData(@ModelAttribute LoanData loanData, Model model) {
        // Save loanData using your repository
        loanDataRepository.save(loanData);
        model.addAttribute("success", true);
        model.addAttribute("loanData", new LoanData()); // clear form after submit
        return "stockist/add-loandata";
    }

    // Display LoanData page with filtering
    @GetMapping("/display-loandata")
    public String displayLoanData(
            @RequestParam(required = false) String stockistName,
            @RequestParam(required = false) String warehouse,
            @RequestParam(required = false) String commodity,
            Model model
    ) {
        List<LoanData> list = loanDataRepository.findByFilters(stockistName, warehouse, commodity);
        model.addAttribute("loanDataList", list);
        model.addAttribute("filterStockistName", stockistName == null ? "" : stockistName);
        model.addAttribute("filterWarehouse", warehouse == null ? "" : warehouse);
        model.addAttribute("filterCommodity", commodity == null ? "" : commodity);
        // For filter dropdowns/autocomplete
        model.addAttribute("stockistNames", loanDataRepository.findAllDistinctStockistNames());
        model.addAttribute("warehouses", loanDataRepository.findAllDistinctWarehouses());
        return "stockist/display-loandata";
    }

    // Inline update (JSON POST)
    @PostMapping("/update-loandata-inline")
    @ResponseBody
    public String updateLoanDataInline(@RequestBody LoanData data) {
        Optional<LoanData> existingOpt = loanDataRepository.findById(data.getId());
        if (existingOpt.isPresent()) {
            LoanData ld = existingOpt.get();
            ld.setDate(data.getDate());
            ld.setStockistName(data.getStockistName());
            ld.setCommodity(data.getCommodity());
            ld.setWarehouse(data.getWarehouse());
            ld.setLoanType(data.getLoanType());
            ld.setAmount(data.getAmount());
            loanDataRepository.save(ld);
            return "{\"status\":\"ok\"}";
        }
        return "{\"status\":\"error\", \"msg\":\"Not found\"}";
    }

    // Bulk delete
    @PostMapping("/delete-loandata")
    @ResponseBody
    public String deleteLoanData(@RequestBody List<Long> ids) {
        loanDataRepository.deleteAllById(ids);
        return "{\"status\":\"ok\"}";
    }

    @GetMapping("/export-loandata-excel")
    public void exportLoanDataExcel(HttpServletResponse response) throws IOException {
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=loan_data.xlsx");

        List<LoanData> list = loanDataRepository.findAll();

        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Loan Data");
        Row header = sheet.createRow(0);
        String[] columns = {"Date", "Stockist Name", "Commodity", "Warehouse", "Loan Type", "Amount"};
        for (int i = 0; i < columns.length; i++) {
            header.createCell(i).setCellValue(columns[i]);
        }

        int rowNum = 1;
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        for (LoanData ld : list) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(ld.getDate() != null ? ld.getDate().format(df) : "");
            row.createCell(1).setCellValue(ld.getStockistName() != null ? ld.getStockistName() : "");
            row.createCell(2).setCellValue(ld.getCommodity() != null ? ld.getCommodity() : "");
            row.createCell(3).setCellValue(ld.getWarehouse() != null ? ld.getWarehouse() : "");
            row.createCell(4).setCellValue(ld.getLoanType() != null ? ld.getLoanType() : "");
            row.createCell(5).setCellValue(ld.getAmount() != null ? ld.getAmount() : 0.0);
        }
        workbook.write(response.getOutputStream());
        workbook.close();
    }

    @GetMapping("/export-loandata-pdf")
    public void exportLoanDataPdf(HttpServletResponse response) throws IOException {
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=loan_data.pdf");

        List<LoanData> list = loanDataRepository.findAll();
        Document document = new Document(PageSize.A4.rotate());
        PdfWriter.getInstance(document, response.getOutputStream());
        document.open();

        Font fontTitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
        fontTitle.setSize(16);
        fontTitle.setColor(Color.BLUE);
        Paragraph para = new Paragraph("Loan Data", fontTitle);
        para.setAlignment(Paragraph.ALIGN_CENTER);
        document.add(para);

        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100f);
        table.setSpacingBefore(12);

        String[] headers = {"Date", "Stockist Name", "Commodity", "Warehouse", "Loan Type", "Amount"};
        for (String h : headers) {
            PdfPCell cell = new PdfPCell();
            cell.setBackgroundColor(Color.LIGHT_GRAY);
            cell.setPhrase(new Phrase(h, FontFactory.getFont(FontFactory.HELVETICA_BOLD)));
            table.addCell(cell);
        }

        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        for (LoanData ld : list) {
            table.addCell(ld.getDate() != null ? ld.getDate().format(df) : "");
            table.addCell(ld.getStockistName() != null ? ld.getStockistName() : "");
            table.addCell(ld.getCommodity() != null ? ld.getCommodity() : "");
            table.addCell(ld.getWarehouse() != null ? ld.getWarehouse() : "");
            table.addCell(ld.getLoanType() != null ? ld.getLoanType() : "");
            table.addCell(ld.getAmount() != null ? ld.getAmount().toString() : "0");
        }
        document.add(table);
        document.close();
    }

    // Show Add Margin Data Form
    @GetMapping("/add-margindata")
    public String showAddMarginDataForm(Model model) {
        model.addAttribute("marginData", new MarginData());
        // For autocomplete
        model.addAttribute("stockists", stockistRepository.findAll());
        model.addAttribute("warehouses", stockDataRepository.findAll()
                .stream().map(sd -> sd.getWarehouse()).distinct().toList());
        return "stockist/add-margindata";
    }

    // Handle Submit
    @PostMapping("/add-margindata")
    public String addMarginData(@ModelAttribute MarginData marginData, Model model) {
        marginDataRepository.save(marginData);
        model.addAttribute("success", true);
        model.addAttribute("marginData", new MarginData()); // Clear form
        // For autocomplete again
        model.addAttribute("stockists", stockistRepository.findAll());
        model.addAttribute("warehouses", stockDataRepository.findAll()
                .stream().map(sd -> sd.getWarehouse()).distinct().toList());
        return "stockist/add-margindata";
    }

    @GetMapping("/display-margindata")
    public String displayMarginData(
            @RequestParam(required = false) String stockistName,
            @RequestParam(required = false) String warehouse,
            @RequestParam(required = false) String commodity,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            Model model) {
        List<MarginData> data = marginDataRepository.findAll(); // TODO: filter as needed
        // Implement filtering logic as needed here
        model.addAttribute("marginDataList", data);
        model.addAttribute("filterStockistName", stockistName);
        model.addAttribute("filterWarehouse", warehouse);
        model.addAttribute("filterCommodity", commodity);
        model.addAttribute("filterDateFrom", dateFrom);
        model.addAttribute("filterDateTo", dateTo);
        return "stockist/display-margindata";
    }

    // Inline Update
    @PostMapping("/update-margindata-inline")
    @ResponseBody
    public Map<String, Object> updateMarginDataInline(@RequestBody MarginData updated) {
        Optional<MarginData> opt = marginDataRepository.findById(updated.getId());
        Map<String, Object> resp = new HashMap<>();
        if (opt.isPresent()) {
            MarginData m = opt.get();
            m.setDate(updated.getDate());
            m.setStockistName(updated.getStockistName());
            m.setCommodity(updated.getCommodity());
            m.setWarehouse(updated.getWarehouse());
            m.setAmount(updated.getAmount());
            marginDataRepository.save(m);
            resp.put("status", "ok");
        } else {
            resp.put("status", "error");
            resp.put("msg", "Not found");
        }
        return resp;
    }

    // Bulk Delete
    @PostMapping("/delete-margindata")
    @ResponseBody
    public void deleteMarginData(@RequestBody List<Long> ids) {
        marginDataRepository.deleteAllById(ids);
    }

    // Export Excel
    @GetMapping("/export-margindata-excel")
    public void exportMarginDataExcel(HttpServletResponse response) throws IOException {
        List<MarginData> list = marginDataRepository.findAll();
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=margin_data.xlsx");
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("MarginData");
            Row header = sheet.createRow(0);
            String[] columns = {"ID", "Date", "Stockist Name", "Commodity", "Warehouse", "Amount"};
            for (int i = 0; i < columns.length; i++) header.createCell(i).setCellValue(columns[i]);
            int r = 1;
            for (MarginData m : list) {
                Row row = sheet.createRow(r++);
                row.createCell(0).setCellValue(m.getId());
                row.createCell(1).setCellValue(m.getDate() == null ? "" : m.getDate().toString());
                row.createCell(2).setCellValue(m.getStockistName());
                row.createCell(3).setCellValue(m.getCommodity());
                row.createCell(4).setCellValue(m.getWarehouse());
                row.createCell(5).setCellValue(m.getAmount() == null ? 0 : m.getAmount());
            }
            workbook.write(response.getOutputStream());
        }
    }

    // Export PDF
    @GetMapping("/export-margindata-pdf")
    public void exportMarginDataPdf(HttpServletResponse response) throws IOException {
        List<MarginData> list = marginDataRepository.findAll();
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=margin_data.pdf");
        com.lowagie.text.Document document = new com.lowagie.text.Document();
        com.lowagie.text.pdf.PdfWriter.getInstance(document, response.getOutputStream());
        document.open();
        com.lowagie.text.pdf.PdfPTable table = new com.lowagie.text.pdf.PdfPTable(6);
        Stream.of("ID", "Date", "Stockist Name", "Commodity", "Warehouse", "Amount")
                .forEach(headerTitle -> table.addCell(new com.lowagie.text.Phrase(headerTitle)));
        for (MarginData m : list) {
            table.addCell(String.valueOf(m.getId()));
            table.addCell(m.getDate() == null ? "" : m.getDate().toString());
            table.addCell(m.getStockistName());
            table.addCell(m.getCommodity());
            table.addCell(m.getWarehouse());
            table.addCell(String.valueOf(m.getAmount()));
        }
        document.add(table);
        document.close();
    }

    @GetMapping("/rental-interest-calculator")
    public String rentalInterestCalculatorPage() {
        return "stockist/rental-interest-calculator";
    }
    // --- Warehouse Rental Calculation ---
    @PostMapping("/rental")
    @ResponseBody
    public Map<String, Object> calculateRental(
            @RequestParam String stockist,
            @RequestParam String warehouse,
            @RequestParam String commodity,
            @RequestParam String date) {

        LocalDate uptoDate = LocalDate.parse(date);
        List<StockData> stocks = stockDataRepository
                .findByStockistNameAndCommodityAndWarehouseAndDateLessThanEqual(
                        stockist, commodity, warehouse, uptoDate);

        double dailyRatePerMT = 100.0 / 30.0;
        double totalRental = 0.0;
        List<Map<String, Object>> rentalDetails = new ArrayList<>();

        if (stocks.isEmpty()) {
            Map<String, Object> out = new HashMap<>();
            out.put("total", 0.0);
            out.put("details", rentalDetails);
            return out;
        }

        // Find earliest stock date, but not after uptoDate
        LocalDate minDate = stocks.stream()
                .map(StockData::getDate)
                .filter(Objects::nonNull)
                .min(LocalDate::compareTo)
                .orElse(uptoDate);

        for (LocalDate currentDate = minDate; !currentDate.isAfter(uptoDate); currentDate = currentDate.plusDays(1)) {
            final LocalDate day = currentDate;
            double qtyKg = stocks.stream()
                    .filter(sd -> !sd.getDate().isAfter(day))
                    .mapToDouble(sd -> sd.getQuantity() == null ? 0 : sd.getQuantity())
                    .sum();
            double qtyMT = qtyKg / 1000.0;
            double dayRental = qtyMT * dailyRatePerMT;
            totalRental += dayRental;

            Map<String, Object> row = new LinkedHashMap<>();
            row.put("date", day.toString());
            row.put("qtyMT", qtyMT);
            row.put("dayRental", dayRental);
            row.put("cumulative", totalRental);
            rentalDetails.add(row);
        }
        Map<String, Object> out = new HashMap<>();
        out.put("total", totalRental);
        out.put("details", rentalDetails);
        return out;
    }

    // --- Interest Calculation ---
    @PostMapping("/interest")
    @ResponseBody
    public Map<String, Object> calculateInterest(
            @RequestParam String stockist,
            @RequestParam String warehouse,
            @RequestParam String commodity,
            @RequestParam String date) {

        LocalDate uptoDate = LocalDate.parse(date);
        List<LoanData> loans = loanDataRepository
                .findByStockistNameAndWarehouseAndCommodityAndDateLessThanEqual(
                        stockist, warehouse, commodity, uptoDate);
        List<MarginData> margins = marginDataRepository
                .findByStockistNameAndWarehouseAndCommodityAndDateLessThanEqual(
                        stockist, warehouse, commodity, uptoDate);

        double annual = 13.75 / 100.0, dailyRate = annual / 365.0;
        double totalInterest = 0.0;
        List<Map<String, Object>> interestDetails = new ArrayList<>();

        // Find earliest date for calculation
        LocalDate minDate = Stream.concat(
                loans.stream().map(LoanData::getDate),
                margins.stream().map(MarginData::getDate)
        ).filter(Objects::nonNull).min(LocalDate::compareTo).orElse(uptoDate);

        for (LocalDate currentDate = minDate; !currentDate.isAfter(uptoDate); currentDate = currentDate.plusDays(1)) {
            final LocalDate day = currentDate;
            double cashLoan = loans.stream()
                    .filter(l -> !l.getDate().isAfter(day) && "Cash".equalsIgnoreCase(l.getLoanType()))
                    .mapToDouble(l -> l.getAmount() == null ? 0 : l.getAmount()).sum();
            double marginLoan = loans.stream()
                    .filter(l -> !l.getDate().isAfter(day) && "Margin".equalsIgnoreCase(l.getLoanType()))
                    .mapToDouble(l -> l.getAmount() == null ? 0 : l.getAmount()).sum();
            double margin = margins.stream()
                    .filter(m -> !m.getDate().isAfter(day))
                    .mapToDouble(m -> m.getAmount() == null ? 0 : m.getAmount()).sum();

            double principal = cashLoan + marginLoan - margin;
            double dayInterest = principal * dailyRate;
            totalInterest += dayInterest;

            Map<String, Object> row = new LinkedHashMap<>();
            row.put("date", day.toString());
            row.put("cashLoan", cashLoan);
            row.put("marginLoan", marginLoan);
            row.put("margin", margin);
            row.put("dayInterest", dayInterest);
            row.put("cumulative", totalInterest);
            interestDetails.add(row);
        }
        Map<String, Object> out = new HashMap<>();
        out.put("total", totalInterest);
        out.put("details", interestDetails);
        return out;
    }

    // --- EXPORT EXCEL (Rental/Interest) ---
    @PostMapping("/export-excel")
    @ResponseBody
    public void exportExcel(@RequestBody Map<String, Object> payload, HttpServletResponse response) throws IOException {
        String type = (String) payload.get("type");
        List<LinkedHashMap<String, Object>> details = (List<LinkedHashMap<String, Object>>) payload.get("details");
        String[] rentalHeaders = {"Date", "Quantity (MT)", "Day's Rental", "Cumulative"};
        String[] interestHeaders = {"Date", "Cash Loan", "Margin Loan", "Margin", "Day's Interest", "Cumulative"};

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + type + "-details.xlsx\"");
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Details");
        Row headerRow = sheet.createRow(0);
        String[] headers = type.equals("rental") ? rentalHeaders : interestHeaders;
        for (int i = 0; i < headers.length; i++) headerRow.createCell(i).setCellValue(headers[i]);
        int rowIdx = 1;
        for (Map<String, Object> row : details) {
            Row r = sheet.createRow(rowIdx++);
            int col = 0;
            if (type.equals("rental")) {
                r.createCell(col++).setCellValue((String) row.get("date"));
                r.createCell(col++).setCellValue((Double) row.get("qtyMT"));
                r.createCell(col++).setCellValue((Double) row.get("dayRental"));
                r.createCell(col++).setCellValue((Double) row.get("cumulative"));
            } else {
                r.createCell(col++).setCellValue((String) row.get("date"));
                r.createCell(col++).setCellValue((Double) row.get("cashLoan"));
                r.createCell(col++).setCellValue((Double) row.get("marginLoan"));
                r.createCell(col++).setCellValue((Double) row.get("margin"));
                r.createCell(col++).setCellValue((Double) row.get("dayInterest"));
                r.createCell(col++).setCellValue((Double) row.get("cumulative"));
            }
        }
        workbook.write(response.getOutputStream());
        workbook.close();
    }

    // --- EXPORT PDF (Rental/Interest) ---
    @PostMapping("/export-pdf")
    @ResponseBody
    public void exportPdf(@RequestBody Map<String, Object> payload, HttpServletResponse response) throws Exception {
        String type = (String) payload.get("type");
        List<LinkedHashMap<String, Object>> details = (List<LinkedHashMap<String, Object>>) payload.get("details");
        String[] rentalHeaders = {"Date", "Quantity (MT)", "Day's Rental", "Cumulative"};
        String[] interestHeaders = {"Date", "Cash Loan", "Margin Loan", "Margin", "Day's Interest", "Cumulative"};
        String[] headers = type.equals("rental") ? rentalHeaders : interestHeaders;

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + type + "-details.pdf\"");
        Document document = new Document();
        PdfWriter.getInstance(document, response.getOutputStream());
        document.open();
        PdfPTable table = new PdfPTable(headers.length);
        for (String h : headers) table.addCell(h);
        for (Map<String, Object> row : details) {
            if (type.equals("rental")) {
                table.addCell((String) row.get("date"));
                table.addCell(String.valueOf(row.get("qtyMT")));
                table.addCell(String.valueOf(row.get("dayRental")));
                table.addCell(String.valueOf(row.get("cumulative")));
            } else {
                table.addCell((String) row.get("date"));
                table.addCell(String.valueOf(row.get("cashLoan")));
                table.addCell(String.valueOf(row.get("marginLoan")));
                table.addCell(String.valueOf(row.get("margin")));
                table.addCell(String.valueOf(row.get("dayInterest")));
                table.addCell(String.valueOf(row.get("cumulative")));
            }
        }
        document.add(table);
        document.close();
    }

    // --- Autocomplete Endpoints (For HTML Datalists) ---
    @GetMapping("/autocomplete/stockists")
    @ResponseBody
    public List<String> autocompleteStockists() {
        return stockistRepository.findAll()
                .stream().map(Stockist::getStockistName).distinct().toList();
    }

    @GetMapping("/autocomplete/warehouses")
    @ResponseBody
    public List<String> autocompleteWarehouses() {
        return stockDataRepository.findAll()
                .stream().map(StockData::getWarehouse).filter(Objects::nonNull).distinct().toList();
    }

}




