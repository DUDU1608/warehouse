package com.anunayagro.warehouse.services;

import com.anunayagro.warehouse.dto.PurchaseSummaryDTO;
import com.anunayagro.warehouse.models.Purchase;
import com.anunayagro.warehouse.models.Payment;
import com.anunayagro.warehouse.repositories.PurchaseRepository;
import com.anunayagro.warehouse.repositories.PaymentRepository;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PurchaseSummaryService {

    @Autowired
    private PurchaseRepository purchaseRepo;

    @Autowired
    private PaymentRepository paymentRepo;

    public List<PurchaseSummaryDTO> getGroupedSummary(String seller, String warehouse, String commodity) {
        List<Purchase> purchases = purchaseRepo.findAll();

        // Apply filters
        purchases = purchases.stream()
                .filter(p -> seller == null || seller.isEmpty() || p.getSeller().equalsIgnoreCase(seller))
                .filter(p -> warehouse == null || warehouse.isEmpty() || p.getWarehouse().equalsIgnoreCase(warehouse))
                .filter(p -> commodity == null || commodity.isEmpty() || p.getCommodity().equalsIgnoreCase(commodity))
                .collect(Collectors.toList());

        // Group by Seller + Warehouse + Commodity
        Map<String, List<Purchase>> grouped = purchases.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getSeller() + "||" + p.getWarehouse() + "||" + p.getCommodity()
                ));

        // Fetch all payments
        List<Payment> payments = paymentRepo.findAll();

        List<PurchaseSummaryDTO> summaries = new ArrayList<>();

        for (Map.Entry<String, List<Purchase>> entry : grouped.entrySet()) {
            String[] keys = entry.getKey().split("\\|\\|");
            String sellerName = keys[0];
            String wh = keys[1];
            String comm = keys[2];

            List<Purchase> groupList = entry.getValue();

            double quantity = groupList.stream().mapToDouble(p -> Optional.ofNullable(p.getQuantity()).orElse(0.0)).sum();
            double reduction = groupList.stream().mapToDouble(p -> Optional.ofNullable(p.getReduction()).orElse(0.0)).sum();
            double netQty = groupList.stream().mapToDouble(p -> Optional.ofNullable(p.getNetQty()).orElse(0.0)).sum();
            double cost = groupList.stream().mapToDouble(p -> Optional.ofNullable(p.getCost()).orElse(0.0)).sum();
            double handling = groupList.stream().mapToDouble(p -> Optional.ofNullable(p.getHandling()).orElse(0.0)).sum();
            double totalCost = groupList.stream().mapToDouble(p -> Optional.ofNullable(p.getTotalCost()).orElse(0.0)).sum();

            // Payment amount by matching name in Payment model
            double amountPaid = payments.stream()
                    .filter(pay -> sellerName.equalsIgnoreCase(pay.getName()))
                    .mapToDouble(p -> Optional.ofNullable(p.getAmount()).orElse(0.0))
                    .sum();

            double paymentDue = totalCost - amountPaid;

            PurchaseSummaryDTO dto = new PurchaseSummaryDTO();
            dto.setSeller(sellerName);
            dto.setWarehouse(wh);
            dto.setCommodity(comm);
            dto.setQuantity(quantity);
            dto.setReduction(reduction);
            dto.setNetQty(netQty);
            dto.setCost(cost);
            dto.setHandling(handling);
            dto.setTotalCost(totalCost);
            dto.setAmount(amountPaid);
            dto.setPaymentDue(paymentDue);

            summaries.add(dto);
        }

        return summaries;
    }

    public void exportToExcel(List<PurchaseSummaryDTO> summaries, HttpServletResponse response) throws IOException
        {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=purchase_summary.xlsx");

            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Summary");

            Row header = sheet.createRow(0);
            String[] columns = {"Seller", "Warehouse", "Commodity", "Quantity", "Reduction", "Net Qty", "Cost", "Handling", "Total Cost", "Amount Paid", "Payment Due"};
            for (int i = 0; i < columns.length; i++) {
                header.createCell(i).setCellValue(columns[i]);
            }

            int rowNum = 1;
            for (PurchaseSummaryDTO dto : summaries) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(dto.getSeller());
                row.createCell(1).setCellValue(dto.getWarehouse());
                row.createCell(2).setCellValue(dto.getCommodity());
                row.createCell(3).setCellValue(dto.getQuantity());
                row.createCell(4).setCellValue(dto.getReduction());
                row.createCell(5).setCellValue(dto.getNetQty());
                row.createCell(6).setCellValue(dto.getCost());
                row.createCell(7).setCellValue(dto.getHandling());
                row.createCell(8).setCellValue(dto.getTotalCost());
                row.createCell(9).setCellValue(dto.getAmount());
                row.createCell(10).setCellValue(dto.getPaymentDue());
            }

            workbook.write(response.getOutputStream());
            workbook.close();
        }
    public void exportToPdf(List<PurchaseSummaryDTO> summaries, HttpServletResponse response) throws IOException {
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=purchase_summary.pdf");

        Document document = new Document();
        PdfWriter.getInstance(document, response.getOutputStream());
        document.open();

        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10);

        document.add(new Paragraph("Purchase Summary Report", headerFont));
        document.add(new Paragraph(" ")); // Empty line

        PdfPTable table = new PdfPTable(11);
        table.setWidthPercentage(100);
        String[] headers = {"Seller", "Warehouse", "Commodity", "Qty", "Red.", "Net Qty", "Cost", "Handling", "Total", "Paid", "Due"};

        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, headerFont));
            table.addCell(cell);
        }

        for (PurchaseSummaryDTO dto : summaries) {
            table.addCell(new Phrase(dto.getSeller(), normalFont));
            table.addCell(new Phrase(dto.getWarehouse(), normalFont));
            table.addCell(new Phrase(dto.getCommodity(), normalFont));
            table.addCell(new Phrase(String.format("%.2f", dto.getQuantity()), normalFont));
            table.addCell(new Phrase(String.format("%.2f", dto.getReduction()), normalFont));
            table.addCell(new Phrase(String.format("%.2f", dto.getNetQty()), normalFont));
            table.addCell(new Phrase(String.format("%.2f", dto.getCost()), normalFont));
            table.addCell(new Phrase(String.format("%.2f", dto.getHandling()), normalFont));
            table.addCell(new Phrase(String.format("%.2f", dto.getTotalCost()), normalFont));
            table.addCell(new Phrase(String.format("%.2f", dto.getAmount()), normalFont));
            table.addCell(new Phrase(String.format("%.2f", dto.getPaymentDue()), normalFont));
        }

        document.add(table);
        document.close();
    }

}
