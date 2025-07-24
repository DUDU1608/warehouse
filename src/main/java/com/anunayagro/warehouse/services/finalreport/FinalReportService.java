package com.anunayagro.warehouse.services.finalreport;

import com.anunayagro.warehouse.dto.FinalReportDTO;
import com.anunayagro.warehouse.models.StockData;
import com.anunayagro.warehouse.repositories.StockDataRepository;
import com.anunayagro.warehouse.repositories.StockistRepository;
import com.anunayagro.warehouse.models.Stockist;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import jakarta.servlet.http.HttpServletResponse;
import java.awt.Color;

@Service
public class FinalReportService {

    @Autowired
    private RentalCalculatorFinalService rentalCalculatorFinalService;

    @Autowired
    private InterestCalculatorFinalService interestCalculatorFinalService;

    @Autowired
    private StockistRepository stockistRepository;

    @Autowired
    private StockDataRepository stockDataRepository;


    public FinalReportDTO generateReport(String stockistName, String warehouse, String commodity, double rate, LocalDate uptoDate) {

        // ✅ Fetch mobile number inside method
        Optional<Stockist> stockistOpt = stockistRepository.findByStockistName(stockistName);
        String mobile = stockistOpt.map(Stockist::getMobile).orElse("");

        // Now mobile is available for interest calculation
        FinalReportDTO dto = new FinalReportDTO();
        dto.setStockistName(stockistName);
        dto.setWarehouse(warehouse);
        dto.setCommodity(commodity);
        dto.setRate(rate);
        dto.setReportDate(uptoDate);

        // Example calculations (adjust these if needed)
        Double totalStock = stockDataRepository
                .sumNetQtyByStockistNameAndWarehouseAndCommodity(stockistName, warehouse, commodity);
        if (totalStock == null) totalStock = 0.0;
        double reduction = totalStock * 0.015;
        double netStock = totalStock - reduction;
        double totalCost = netStock * rate;
        double rental = rentalCalculatorFinalService.calculateRental(stockistName, uptoDate);
        double interest = interestCalculatorFinalService.calculateInterest(mobile, uptoDate);
        double netPayable = totalCost - rental - interest;

        dto.setTotalStock(round2(totalStock));
        dto.setReduction(round2(reduction));
        dto.setNetStock(round2(netStock));
        dto.setTotalCost(round2(totalCost));
        dto.setRental(round2(rental));
        dto.setInterest(round2(interest));
        dto.setNetPayable(round2(netPayable));

        return dto;
    }


    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
    public void exportFinalReportToPdf(FinalReportDTO dto, HttpServletResponse response) throws Exception {
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=final_report.pdf");

        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, response.getOutputStream());

        document.open();

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.BLUE);
        Paragraph title = new Paragraph("Final Report", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);

        PdfPTable table = new PdfPTable(8);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10);

        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
        String[] headers = {"Total Stock", "Reduction", "Net Stock", "Rate", "Total Cost", "Interest", "Rental", "Net Payable"};

        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
            cell.setBackgroundColor(new Color(173, 216, 230));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }

        table.addCell(String.format("%.2f", dto.getTotalStock()));
        table.addCell(String.format("%.2f", dto.getReduction()));
        table.addCell(String.format("%.2f", dto.getNetStock()));
        table.addCell(String.format("%.2f", dto.getRate()));
        table.addCell(String.format("%.2f", dto.getTotalCost()));
        table.addCell(String.format("%.2f", dto.getInterest()));
        table.addCell(String.format("%.2f", dto.getRental()));
        table.addCell(String.format("%.2f", dto.getNetPayable()));

        document.add(table);
        document.close();
    }
}

