package com.anunayagro.warehouse.controllers;

import com.anunayagro.warehouse.models.LoanData;
import com.anunayagro.warehouse.models.MarginData;
import com.anunayagro.warehouse.repositories.LoanDataRepository;
import com.anunayagro.warehouse.repositories.MarginDataRepository;
import com.anunayagro.warehouse.repositories.StockistRepository;
import com.anunayagro.warehouse.repositories.StockDataRepository;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.awt.*;
import java.io.IOException;
import java.security.Principal;
import java.time.LocalDate;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class InterestCalculatorController {

    @Autowired
    private LoanDataRepository loanDataRepository;
    @Autowired
    private MarginDataRepository marginDataRepository;
    @Autowired
    private StockistRepository stockistRepository;
    @Autowired
    private StockDataRepository stockDataRepository;

    // Admin page
    @GetMapping("/stockist/interest-calculator")
    public String showAdminForm(Model model) {
        model.addAttribute("stockists", stockistRepository.findAll().stream().map(s -> s.getStockistName()).collect(Collectors.toList()));
        model.addAttribute("warehouses", stockDataRepository.findDistinctWarehouses());
        model.addAttribute("commodities", List.of("Wheat", "Maize", "Paddy"));
        model.addAttribute("mode", "admin");
        return "stockist/interest-calculator";
    }

    // User page: restricts to logged in user (by mobile)
    @GetMapping("/user/interest-calculator")
    public String showUserForm(Model model, Principal principal) {
        String mobile = principal.getName();
        String stockistName = stockistRepository.findByMobile(mobile)
                .map(s -> s.getStockistName())
                .orElse("");
        model.addAttribute("stockistName", stockistName);
        model.addAttribute("warehouses", stockDataRepository.findDistinctWarehouses());
        model.addAttribute("commodities", List.of("Wheat", "Maize", "Paddy"));
        model.addAttribute("mode", "user");
        return "user/interest-calculator";
    }

    // The calculation endpoint (used by both admin/user frontend via AJAX)
    @PostMapping({"/stockist/interest-calculator/calculate", "/user/interest-calculator/calculate"})
    @ResponseBody
    public Map<String, Object> calculateInterest(
            @RequestParam String stockistName,
            @RequestParam String warehouse,
            @RequestParam String commodity,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate uptoDate
    ) {
        Map<String, Object> resp = new HashMap<>();

        // 1. Get all loans and margins up to date
        List<LoanData> loans = loanDataRepository.findByStockistNameAndWarehouseAndCommodityAndDateLessThanEqual(
                stockistName, warehouse, commodity, uptoDate
        );
        List<MarginData> margins = marginDataRepository.findByStockistNameAndWarehouseAndCommodityAndDateLessThanEqual(
                stockistName, warehouse, commodity, uptoDate
        );

        double totalCashLoan = loans.stream()
                .filter(l -> "Cash".equalsIgnoreCase(l.getLoanType()))
                .mapToDouble(LoanData::getAmount).sum();
        double totalMarginLoan = loans.stream()
                .filter(l -> "Margin".equalsIgnoreCase(l.getLoanType()))
                .mapToDouble(LoanData::getAmount).sum();
        double totalMarginPaid = margins.stream().mapToDouble(MarginData::getAmount).sum();

        // Find date range for daily calculation
        LocalDate startDate = loans.stream()
                .map(LoanData::getDate)
                .min(LocalDate::compareTo)
                .orElse(uptoDate);

        double cumulative = 0;
        List<Map<String, Object>> details = new ArrayList<>();

        LocalDate current = startDate;
        while (!current.isAfter(uptoDate)) {
            final LocalDate loopDate = current; // effectively final for lambda usage

            double cashLoanUpto = loans.stream()
                    .filter(l -> "Cash".equalsIgnoreCase(l.getLoanType()) && !l.getDate().isAfter(loopDate))
                    .mapToDouble(LoanData::getAmount).sum();

            double marginLoanUpto = loans.stream()
                    .filter(l -> "Margin".equalsIgnoreCase(l.getLoanType()) && !l.getDate().isAfter(loopDate))
                    .mapToDouble(LoanData::getAmount).sum();

            double marginPaidUpto = margins.stream()
                    .filter(m -> !m.getDate().isAfter(loopDate))
                    .mapToDouble(MarginData::getAmount).sum();

            double netLoan = cashLoanUpto + marginLoanUpto - marginPaidUpto;
            double dailyInterest = netLoan * 0.1375 / 365.0;
            cumulative += dailyInterest;

            Map<String, Object> row = new LinkedHashMap<>();
            row.put("date", loopDate.toString());
            row.put("cashLoan", cashLoanUpto);
            row.put("marginLoan", marginLoanUpto);
            row.put("marginPaid", marginPaidUpto);
            row.put("netLoan", netLoan);
            row.put("interest", dailyInterest);
            row.put("cumulative", cumulative);
            details.add(row);

            current = current.plusDays(1);
        }

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("stockistName", stockistName);
        summary.put("warehouse", warehouse);
        summary.put("commodity", commodity);
        summary.put("marginLoan", totalMarginLoan);
        summary.put("cashLoan", totalCashLoan);
        summary.put("marginPaid", totalMarginPaid);
        summary.put("interestDue", cumulative);

        resp.put("summary", summary);
        resp.put("details", details);
        return resp;
    }

    // ----- EXPORT EXCEL -----
    @PostMapping(value = {"/stockist/interest-calculator/export-excel", "/user/interest-calculator/export-excel"})
    public void exportInterestExcel(@RequestBody Map<String, Object> data, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> details = (List<Map<String, Object>>) data.get("details");

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=interest-details.xlsx");

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Interest Details");
        String[] columns = {"Date", "Cash Loan (₹)", "Margin Loan (₹)", "Margin Paid (₹)", "Net Loan (₹)", "Interest (₹)", "Cumulative (₹)"};

        Row header = sheet.createRow(0);
        for (int i = 0; i < columns.length; i++)
            header.createCell(i).setCellValue(columns[i]);

        int rowNum = 1;
        for (Map<String, Object> row : details) {
            Row dataRow = sheet.createRow(rowNum++);
            dataRow.createCell(0).setCellValue(Objects.toString(row.get("date"), ""));
            dataRow.createCell(1).setCellValue(Double.parseDouble(row.get("cashLoan").toString()));
            dataRow.createCell(2).setCellValue(Double.parseDouble(row.get("marginLoan").toString()));
            dataRow.createCell(3).setCellValue(Double.parseDouble(row.get("marginPaid").toString()));
            dataRow.createCell(4).setCellValue(Double.parseDouble(row.get("netLoan").toString()));
            dataRow.createCell(5).setCellValue(Double.parseDouble(row.get("interest").toString()));
            dataRow.createCell(6).setCellValue(Double.parseDouble(row.get("cumulative").toString()));
        }
        workbook.write(response.getOutputStream());
        workbook.close();
    }

    // ----- EXPORT PDF (OpenPDF) -----
    @PostMapping(value = {"/stockist/interest-calculator/export-pdf", "/user/interest-calculator/export-pdf"})
    public void exportInterestPdf(@RequestBody Map<String, Object> data, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> details = (List<Map<String, Object>>) data.get("details");

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=interest-details.pdf");

        Document document = new Document();
        PdfWriter.getInstance(document, response.getOutputStream());
        document.open();

        Font font = new Font(Font.HELVETICA, 15, Font.BOLD);
        document.add(new Paragraph("Interest Details", font));
        document.add(Chunk.NEWLINE);

        PdfPTable table = new PdfPTable(7);
        table.setWidthPercentage(100);
        String[] headers = {"Date", "Cash Loan (₹)", "Margin Loan (₹)", "Margin Paid (₹)", "Net Loan (₹)", "Interest (₹)", "Cumulative (₹)"};
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h));
            cell.setBackgroundColor(new Color(210, 210, 255));
            table.addCell(cell);
        }
        for (Map<String, Object> row : details) {
            table.addCell(Objects.toString(row.get("date"), ""));
            table.addCell(String.format("%.2f", Double.parseDouble(row.get("cashLoan").toString())));
            table.addCell(String.format("%.2f", Double.parseDouble(row.get("marginLoan").toString())));
            table.addCell(String.format("%.2f", Double.parseDouble(row.get("marginPaid").toString())));
            table.addCell(String.format("%.2f", Double.parseDouble(row.get("netLoan").toString())));
            table.addCell(String.format("%.2f", Double.parseDouble(row.get("interest").toString())));
            table.addCell(String.format("%.2f", Double.parseDouble(row.get("cumulative").toString())));
        }
        document.add(table);
        document.close();
    }

}

