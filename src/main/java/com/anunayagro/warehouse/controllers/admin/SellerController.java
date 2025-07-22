package com.anunayagro.warehouse.controllers.admin;

import com.anunayagro.warehouse.models.*;
import com.anunayagro.warehouse.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Optional;


import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Arrays;

import java.time.ZoneId;
import java.util.Date;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import java.util.*;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


@Controller
@RequestMapping("/seller")
public class SellerController {

    @Autowired
    private PurchaseRepository purchaseRepository;

    @Autowired
    private SellerRepository sellerRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private StockDataRepository stockDataRepository;
    @Autowired
    private LoanDataRepository loanDataRepository;
    @Autowired
    private StockistRepository stockistRepository;

    @GetMapping("")
    public String sellerHome() {
        return "seller/index";
    }

    // Seller CRUD
    @GetMapping("/add")
    public String showAddSellerForm(Model model) {
        model.addAttribute("seller", new Seller());
        return "seller/add-seller";
    }

    @PostMapping("/add")
    public String addSeller(@ModelAttribute Seller seller, Model model) {
        sellerRepository.save(seller);
        model.addAttribute("message", "Seller added successfully!");
        model.addAttribute("seller", new Seller());
        return "seller/add-seller";
    }

    @GetMapping("/display")
    public String displaySellers(Model model) {
        List<Seller> sellers = sellerRepository.findAll();
        model.addAttribute("sellers", sellers);
        return "seller/display-seller";
    }

    @PostMapping("/update/{id}")
    public String updateSeller(@PathVariable Long id, @ModelAttribute Seller updatedSeller) {
        Seller seller = sellerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid seller Id:" + id));
        seller.setName(updatedSeller.getName());
        seller.setMobile(updatedSeller.getMobile());
        seller.setBankingName(updatedSeller.getBankingName());
        seller.setAccountNumber(updatedSeller.getAccountNumber());
        seller.setIfsc(updatedSeller.getIfsc());
        seller.setBankName(updatedSeller.getBankName());
        seller.setAddress(updatedSeller.getAddress());
        sellerRepository.save(seller);
        return "redirect:/seller/display";
    }

    @PostMapping("/delete/{id}")
    public String deleteSeller(@PathVariable Long id) {
        sellerRepository.deleteById(id);
        return "redirect:/seller/display";
    }

    // --- Purchase Add ---
    @GetMapping("/add-purchase")
    public String showAddPurchaseForm(Model model) {
        model.addAttribute("purchase", new Purchase());
        List<Seller> sellers = sellerRepository.findAll();
        model.addAttribute("sellerData", sellers);
        List<String> warehouseList = Arrays.asList("Warehouse 1", "Warehouse 2", "Warehouse 3");
        model.addAttribute("warehouseList", warehouseList);
        return "seller/add-purchase";
    }

    public String addPurchase(@ModelAttribute Purchase purchase, Model model) {
        // Check for existing
        Purchase existing = purchaseRepository.findByRstNoAndWarehouse(purchase.getRstNo(), purchase.getWarehouse());
        if (existing != null) {
            model.addAttribute("error", "A purchase with this RST No and Warehouse already exists!");
            model.addAttribute("purchase", purchase);
            return "seller/add-purchase";
        }
        double qty = purchase.getQuantity() != null ? purchase.getQuantity() : 0;
        double reduction = purchase.getReduction() != null ? purchase.getReduction() : 0;
        double netQty = qty - reduction;
        if (netQty < 0) netQty = 0d;
        purchase.setNetQty(netQty);

        double rate = purchase.getRate() != null ? purchase.getRate() : 0;
        double cost = netQty * rate;
        purchase.setCost(cost);

        double handling = purchase.getHandling() != null ? purchase.getHandling() : 0;
        double totalCost = cost - handling;
        purchase.setTotalCost(totalCost);

        purchaseRepository.save(purchase);

        return "redirect:/seller/add-purchase";
    }

    @GetMapping("/display-purchase")
    public String displayPurchases(Model model) {
        model.addAttribute("purchases", purchaseRepository.findAll());
        return "seller/display-purchase";
    }

    @PostMapping("/update-purchase/{id}")
    public String updatePurchase(@PathVariable Long id, @ModelAttribute Purchase updatedPurchase) {
        Purchase purchase = purchaseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid purchase Id:" + id));
        purchase.setDate(updatedPurchase.getDate());
        purchase.setRstNo(updatedPurchase.getRstNo());
        purchase.setWarehouse(updatedPurchase.getWarehouse());
        purchase.setSeller(updatedPurchase.getSeller());
        purchase.setMobile(updatedPurchase.getMobile());
        purchase.setCommodity(updatedPurchase.getCommodity());
        purchase.setQuantity(updatedPurchase.getQuantity());
        purchase.setReduction(updatedPurchase.getReduction());
        double netQty = (updatedPurchase.getQuantity() != null ? updatedPurchase.getQuantity() : 0)
                - (updatedPurchase.getReduction() != null ? updatedPurchase.getReduction() : 0);
        if (netQty < 0) netQty = 0d;
        purchase.setNetQty(netQty);
        purchase.setRate(updatedPurchase.getRate());
        double cost = netQty * (updatedPurchase.getRate() != null ? updatedPurchase.getRate() : 0);
        purchase.setCost(cost);
        purchase.setHandling(updatedPurchase.getHandling());
        double totalCost = cost - (updatedPurchase.getHandling() != null ? updatedPurchase.getHandling() : 0);
        purchase.setTotalCost(totalCost);
        purchase.setQuality(updatedPurchase.getQuality());
        purchaseRepository.save(purchase);
        return "redirect:/seller/display-purchase";
    }

    @PostMapping("/delete-purchase/{id}")
    @ResponseBody
    public void deletePurchase(@PathVariable Long id) {
        purchaseRepository.deleteById(id);
    }

    @PostMapping("/delete-purchases")
    @ResponseBody
    public void deletePurchases(@RequestBody List<Long> ids) {
        purchaseRepository.deleteAllById(ids);
    }
    @PostMapping("/transfer-to-stock")
    public ResponseEntity<?> transferToStock(@RequestBody TransferRequest transferRequest) {
        try {
            System.out.println("TransferRequest: " + transferRequest.getStockistName() + ", " + transferRequest.getPurchaseIds() + ", " + transferRequest.getNewDate());
            if (transferRequest.getStockistName() == null || transferRequest.getStockistName().trim().isEmpty()) {
                throw new RuntimeException("Stockist Name is required.");
            }
            List<Long> purchaseIds = transferRequest.getPurchaseIds();
            String stockistName = transferRequest.getStockistName();
            String commodity = transferRequest.getCommodity();
            String warehouse = transferRequest.getWarehouse();

            boolean changeDate = "yes".equalsIgnoreCase(transferRequest.getChangeDate());
            boolean changeRate = "yes".equalsIgnoreCase(transferRequest.getChangeRate());
            String newDate = transferRequest.getNewDate();
            String newRateStr = transferRequest.getNewRate();

            double marginTotal = 0;
            LocalDate targetDate = changeDate && newDate != null && !newDate.isEmpty()
                    ? LocalDate.parse(newDate)
                    : null;
            Double targetRate = changeRate && newRateStr != null && !newRateStr.isEmpty()
                    ? Double.parseDouble(newRateStr)
                    : null;

            for (Long pid : purchaseIds) {
                Optional<Purchase> optPurchase = purchaseRepository.findById(pid);
                if (optPurchase.isEmpty()) continue;
                Purchase purchase = optPurchase.get();

                // Build StockData
                StockData stock = new StockData();
                stock.setDate(changeDate ? targetDate : purchase.getDate());
                stock.setRstNo(purchase.getRstNo());
                // Use warehouse from transfer modal (not from purchase)
                stock.setWarehouse(warehouse);
                stock.setStockistName(stockistName);
                // Fill mobile from stockist repo
                stock.setMobile(stockistRepository.findByStockistName(stockistName)
                        .map(Stockist::getMobile).orElse(""));
                stock.setCommodity(commodity);
                stock.setQuantity(purchase.getQuantity());
                stock.setReduction(purchase.getReduction());
                double netQty = purchase.getQuantity() - (purchase.getReduction() != null ? purchase.getReduction() : 0.0);
                stock.setNetQty(netQty);
                double rate = (changeRate && targetRate != null) ? targetRate : purchase.getRate();
                stock.setRate(rate);
                double cost = netQty * rate;
                stock.setCost(cost);
                stock.setHandling(purchase.getHandling());
                double totalCost = cost + (purchase.getHandling() != null ? purchase.getHandling() : 0.0);
                stock.setTotalCost(totalCost);
                stock.setQuality(purchase.getQuality());
                stock.setKindOfStock("Transferred");

                stockDataRepository.save(stock);

                // Sum for margin
                marginTotal += netQty * rate;
            }
            // Save Margin loan
            LoanData loan = new LoanData();
            loan.setDate(targetDate != null ? targetDate : LocalDate.now());
            loan.setStockistName(stockistName);
            loan.setCommodity(commodity);
            loan.setWarehouse(warehouse); // set correct warehouse!
            loan.setLoanType("Margin");
            loan.setAmount(marginTotal);
            loanDataRepository.save(loan);

            return ResponseEntity.ok(Map.of("status", "ok"));
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.ok(Map.of("status", "error", "msg", ex.getMessage()));
        }
    }

    // -----------------------------------------
    public static class TransferRequest {
        private List<Long> purchaseIds;
        private String stockistName;
        private String warehouse;
        private String commodity;
        private String changeDate;
        private String newDate;
        private String changeRate;
        private String newRate;

        // Getters and Setters
        public List<Long> getPurchaseIds() { return purchaseIds; }
        public void setPurchaseIds(List<Long> purchaseIds) { this.purchaseIds = purchaseIds; }
        public String getStockistName() { return stockistName; }
        public void setStockistName(String stockistName) { this.stockistName = stockistName; }
        public String getWarehouse() { return warehouse; }
        public void setWarehouse(String warehouse) { this.warehouse = warehouse; }
        public String getCommodity() { return commodity; }
        public void setCommodity(String commodity) { this.commodity = commodity; }
        public String getChangeDate() { return changeDate; }
        public void setChangeDate(String changeDate) { this.changeDate = changeDate; }
        public String getNewDate() { return newDate; }
        public void setNewDate(String newDate) { this.newDate = newDate; }
        public String getChangeRate() { return changeRate; }
        public void setChangeRate(String changeRate) { this.changeRate = changeRate; }
        public String getNewRate() { return newRate; }
        public void setNewRate(String newRate) { this.newRate = newRate; }
    }

    // Excel import
    @PostMapping("/import-purchases")
    @ResponseBody
    public String importPurchases(@RequestParam("file") MultipartFile file) {
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                Purchase p = new Purchase();
                Cell dateCell = row.getCell(0);
                if (dateCell != null) {
                    if (dateCell.getCellType() == CellType.NUMERIC) {
                        // Excel serial date to java.time.LocalDate
                        Date javaDate = dateCell.getDateCellValue();
                        p.setDate(javaDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
                    } else if (dateCell.getCellType() == CellType.STRING && !dateCell.getStringCellValue().isEmpty()) {
                        p.setDate(LocalDate.parse(dateCell.getStringCellValue()));
                    }
                }
                if (dateCell != null && dateCell.getCellType() == CellType.STRING && !dateCell.getStringCellValue().isEmpty()) {
                    p.setDate(LocalDate.parse(dateCell.getStringCellValue()));
                }
                p.setRstNo(getCellValue(row.getCell(1)));
                p.setWarehouse(getCellValue(row.getCell(2)));
                p.setSeller(getCellValue(row.getCell(3)));
                p.setMobile(getCellValue(row.getCell(4)));
                p.setCommodity(getCellValue(row.getCell(5)));
                p.setQuantity(getNumericCell(row.getCell(6)));
                p.setReduction(getNumericCell(row.getCell(7)));
                p.setNetQty(getNumericCell(row.getCell(8)));
                p.setRate(getNumericCell(row.getCell(9)));
                p.setCost(getNumericCell(row.getCell(10)));
                p.setHandling(getNumericCell(row.getCell(11)));
                p.setTotalCost(getNumericCell(row.getCell(12)));
                p.setQuality(getCellValue(row.getCell(13)));
                purchaseRepository.save(p);
            }
            return "Import successful!";
        } catch (Exception e) {
            e.printStackTrace();
            return "Import failed: " + e.getMessage();
        }
    }

    private String getCellValue(Cell cell) {
        if (cell == null) return "";
        if (cell.getCellType() == CellType.NUMERIC)
            return String.valueOf((long)cell.getNumericCellValue());
        return cell.getStringCellValue();
    }
    private Double getNumericCell(Cell cell) {
        if (cell == null) return 0d;
        if (cell.getCellType() == CellType.STRING)
            return Double.valueOf(cell.getStringCellValue());
        return cell.getNumericCellValue();
    }

    @GetMapping("/export-purchases")
    public void exportPurchases(HttpServletResponse response) throws IOException {
        List<Purchase> purchases = purchaseRepository.findAll();
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Purchases");
        Row header = sheet.createRow(0);
        String[] columns = {"Date", "RST No", "Warehouse", "Seller", "Mobile", "Commodity", "Quantity", "Reduction", "Net Qty", "Rate", "Cost", "Handling", "Total Cost", "Quality"};
        for (int i = 0; i < columns.length; i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(columns[i]);
        }
        int rowIdx = 1;
        for (Purchase p : purchases) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(p.getDate() != null ? p.getDate().toString() : "");
            row.createCell(1).setCellValue(p.getRstNo());
            row.createCell(2).setCellValue(p.getWarehouse());
            row.createCell(3).setCellValue(p.getSeller());
            row.createCell(4).setCellValue(p.getMobile());
            row.createCell(5).setCellValue(p.getCommodity());
            row.createCell(6).setCellValue(p.getQuantity() != null ? p.getQuantity() : 0);
            row.createCell(7).setCellValue(p.getReduction() != null ? p.getReduction() : 0);
            row.createCell(8).setCellValue(p.getNetQty() != null ? p.getNetQty() : 0);
            row.createCell(9).setCellValue(p.getRate() != null ? p.getRate() : 0);
            row.createCell(10).setCellValue(p.getCost() != null ? p.getCost() : 0);
            row.createCell(11).setCellValue(p.getHandling() != null ? p.getHandling() : 0);
            row.createCell(12).setCellValue(p.getTotalCost() != null ? p.getTotalCost() : 0);
            row.createCell(13).setCellValue(p.getQuality());
        }
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=purchases.xlsx");
        workbook.write(response.getOutputStream());
        workbook.close();
    }

    @GetMapping("/export-purchases-pdf")
    public void exportPurchasesPdf(HttpServletResponse response) throws IOException {
        List<Purchase> purchases = purchaseRepository.findAll();
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=purchases.pdf");
        Document document = new Document(PageSize.A4.rotate());
        try {
            PdfWriter.getInstance(document, response.getOutputStream());
            document.open();
            PdfPTable table = new PdfPTable(14);
            String[] columns = {"Date", "RST No", "Warehouse", "Seller", "Mobile", "Commodity", "Quantity", "Reduction", "Net Qty", "Rate", "Cost", "Handling", "Total Cost", "Quality"};
            for (String col : columns) {
                table.addCell(new Phrase(col));
            }
            for (Purchase p : purchases) {
                table.addCell(p.getDate() != null ? p.getDate().toString() : "");
                table.addCell(p.getRstNo());
                table.addCell(p.getWarehouse());
                table.addCell(p.getSeller());
                table.addCell(p.getMobile());
                table.addCell(p.getCommodity());
                table.addCell(p.getQuantity() != null ? p.getQuantity().toString() : "");
                table.addCell(p.getReduction() != null ? p.getReduction().toString() : "");
                table.addCell(p.getNetQty() != null ? p.getNetQty().toString() : "");
                table.addCell(p.getRate() != null ? p.getRate().toString() : "");
                table.addCell(p.getCost() != null ? p.getCost().toString() : "");
                table.addCell(p.getHandling() != null ? p.getHandling().toString() : "");
                table.addCell(p.getTotalCost() != null ? p.getTotalCost().toString() : "");
                table.addCell(p.getQuality());
            }
            document.add(table);
        } finally {
            document.close();
        }
    }

    // To display PAYMENTS (different path!)
    @GetMapping("/display-payment")
    public String displayPayments(Model model) {
        model.addAttribute("payments", paymentRepository.findAll());
        return "seller/display-payment";
    }

    // Inline update
    @PostMapping("/update-payment/{id}")
    public String updatePayment(@PathVariable Long id, @ModelAttribute Payment updated) {
        Optional<Payment> opt = paymentRepository.findById(id);
        if(opt.isPresent()) {
            Payment p = opt.get();
            p.setDate(updated.getDate());
            p.setName(updated.getName());
            p.setMobile(updated.getMobile());
            p.setWarehouse(updated.getWarehouse());
            p.setCommodity(updated.getCommodity());
            p.setAmount(updated.getAmount());
            p.setBankReference(updated.getBankReference());
            paymentRepository.save(p);
        }
        return "redirect:/seller/display-payment";
    }

    // Inline delete (called via AJAX)
    @PostMapping("/delete-payment/{id}")
    @ResponseBody
    public String deletePayment(@PathVariable Long id) {
        paymentRepository.deleteById(id);
        return "Deleted";
    }
    @GetMapping("/add-payment")
    public String showAddPaymentForm(Model model) {
        model.addAttribute("payment", new Payment());

        // For seller autocomplete
        List<Seller> sellers = sellerRepository.findAll();
        model.addAttribute("sellerData", sellers);

        // For warehouse autocomplete
        List<String> warehouseList = purchaseRepository.findAll()
                .stream().map(Purchase::getWarehouse).distinct().toList();
        model.addAttribute("warehouseList", warehouseList);
        model.addAttribute("stockists", stockistRepository.findAll());
        model.addAttribute("warehouses", stockDataRepository.findDistinctWarehouses());
        return "seller/add-payment";
    }

    @PostMapping("/add-payment")
    public String addPayment(@ModelAttribute Payment payment, Model model) {
        paymentRepository.save(payment);
        model.addAttribute("message", "Payment added successfully!");
        model.addAttribute("payment", new Payment());
        return "seller/add-payment";
    }

    // For AJAX mobile autofill
    @ResponseBody
    @GetMapping("/seller-mobile")
    public String getMobileForSeller(@RequestParam String name) {
        Optional<Seller> sellerOpt = sellerRepository.findByName(name);
        if (sellerOpt.isPresent()) {
            return sellerOpt.get().getMobile() != null ? sellerOpt.get().getMobile() : "";
        } else {
            return "";
        }
    }

    @GetMapping("/payment-due")
    public String showPaymentDueForm(Model model) {
        // Populate dropdowns from seller/warehouse/commodity database
        model.addAttribute("sellers", sellerRepository.findAll());
        model.addAttribute("warehouses", purchaseRepository.findDistinctWarehouses());
        model.addAttribute("commodities", List.of("Wheat", "Maize", "Paddy"));
        return "seller/payment-due";
    }

    @PostMapping("/payment-due")
    public String calculatePaymentDue(
            @RequestParam String name,
            @RequestParam String warehouse,
            @RequestParam String commodity,
            Model model) {
        // Debug print what the UI sends
        System.out.println("DEBUG: name = " + name);
        System.out.println("DEBUG: warehouse = " + warehouse);
        System.out.println("DEBUG: commodity = " + commodity);

        // Sum of all purchases for the selected combination
        Double totalPurchase = purchaseRepository.sumTotalCostBySellerAndWarehouseAndCommodity(name, warehouse, commodity);
        if (totalPurchase == null) totalPurchase = 0.0;

        // Sum of all payments for the selected combination
        Double totalPaid = paymentRepository.sumAmountByNameAndWarehouseAndCommodity(name, warehouse, commodity);
        if (totalPaid == null) totalPaid = 0.0;

        double due = totalPurchase - totalPaid;

        model.addAttribute("selectedName", name);
        model.addAttribute("selectedWarehouse", warehouse);
        model.addAttribute("selectedCommodity", commodity);
        model.addAttribute("due", due);
        model.addAttribute("totalPurchase", totalPurchase);
        model.addAttribute("totalPaid", totalPaid);



        // Re-populate dropdowns for the form
        model.addAttribute("sellers", sellerRepository.findAll());
        model.addAttribute("warehouses", purchaseRepository.findDistinctWarehouses());
        model.addAttribute("commodities", List.of("Wheat", "Maize", "Paddy"));

        return "seller/payment-due";
    }
}
