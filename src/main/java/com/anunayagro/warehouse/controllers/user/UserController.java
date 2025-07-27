package com.anunayagro.warehouse.controllers.user;

import com.anunayagro.warehouse.dto.StockistSummaryDTO;
import com.anunayagro.warehouse.models.*;
import com.anunayagro.warehouse.repositories.*;

import com.anunayagro.warehouse.services.StockistSummaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;


import java.security.Principal;
import java.time.LocalDate;
import java.util.*;
import java.util.List;


@Controller
public class UserController {

    @Autowired
    private SellerRepository sellerRepository;

    @Autowired
    private BuyerRepository buyerRepository;

    @Autowired
    private StockistRepository stockistRepository;

    @Autowired
    private PurchaseRepository purchaseRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired private StockDataRepository stockDataRepository;

    @Autowired
    private LoanDataRepository loanDataRepository;

    @Autowired
    private MarginDataRepository marginDataRepository;

    @Autowired
    private StockistSummaryService stockistSummaryService;

    @Autowired
    private StockExitRepository stockExitRepository;
    // User homepage
    @GetMapping("/user")
    public String userHome(Model model, Principal principal) {
        // Assuming principal.getName() gives the logged-in mobile number
        String mobile = principal.getName();

        // Fetch info by mobile
        Optional<Seller> sellerOpt = sellerRepository.findByMobile(mobile);
        Optional<Buyer> buyerOpt = buyerRepository.findByMobile(mobile);
        Optional<Stockist> stockistOpt = stockistRepository.findByMobile(mobile);

        // Display name: First available name (Seller, then Buyer, then Stockist, else "User")
        String name = sellerOpt.map(Seller::getName)
                .or(() -> buyerOpt.map(Buyer::getBuyerName))
                .or(() -> stockistOpt.map(Stockist::getStockistName))
                .orElse("User");

        model.addAttribute("userName", name);
        model.addAttribute("isSeller", sellerOpt.isPresent());
        model.addAttribute("isBuyer", buyerOpt.isPresent());
        model.addAttribute("isStockist", stockistOpt.isPresent());

        return "user/index";
    }

    // Example for Seller module main page
    @GetMapping("/user/seller")
    public String sellerModule(Model model, Principal principal) {
        String mobile = principal.getName();
        Optional<Seller> sellerOpt = sellerRepository.findByMobile(mobile);
        String userName = sellerOpt.map(Seller::getName).orElse("Seller");
        model.addAttribute("userName", userName);
        model.addAttribute("mobile", mobile);
        return "user/seller-dashboard";
    }

    // Seller - View Purchases
    @GetMapping("/user/seller/purchases")
    public String sellerPurchases(Model model, Principal principal) {
        String mobile = principal.getName();
        // You need this method in your repo (see below)
        List<Purchase> purchases = purchaseRepository.findAllByMobile(mobile);
        model.addAttribute("purchases", purchases);
        return "user/seller-purchases";
    }

    // Seller - View Payments
    @GetMapping("/user/seller/payments")
    public String sellerPayments(Model model, Principal principal) {
        String mobile = principal.getName();
        // You need this method in your repo (see below)
        List<Payment> payments = paymentRepository.findAllByMobile(mobile);
        model.addAttribute("payments", payments);
        return "user/seller-payments";
    }

    // Seller - Purchase Summary
    @GetMapping("/user/seller/summary")
    public String sellerSummary(Model model, Principal principal) {
        String mobile = principal.getName();
        Double totalPurchase = purchaseRepository.sumTotalCostByMobile(mobile);
        Double totalPaid = paymentRepository.sumAmountByMobile(mobile);
        if (totalPurchase == null) totalPurchase = 0.0;
        if (totalPaid == null) totalPaid = 0.0;
        Double due = totalPurchase - totalPaid;
        model.addAttribute("totalPurchase", totalPurchase);
        model.addAttribute("totalPaid", totalPaid);
        model.addAttribute("due", due);

        // --- New summary logic ---
        List<Purchase> purchases = purchaseRepository.findAllByMobile(mobile);
        Map<String, Map<String, Double>> stockSummary = new LinkedHashMap<>();
        for (Purchase purchase : purchases) {
            String warehouse = purchase.getWarehouse();
            String commodity = purchase.getCommodity();
            Double qty = purchase.getNetQty() == null ? 0.0 : purchase.getNetQty();
            // Ensure row exists
            stockSummary.computeIfAbsent(warehouse, k -> new HashMap<>());
            // Add to relevant commodity
            stockSummary.get(warehouse).merge(commodity, qty, Double::sum);
        }
        model.addAttribute("stockSummary", stockSummary);

        return "user/seller-summary";
    }

    @GetMapping("/user/stockist")
    public String stockistModule(Model model, Principal principal) {
        String mobile = principal.getName();
        Optional<Seller> sellerOpt = sellerRepository.findByMobile(mobile);
        String userName = sellerOpt.map(Seller::getName).orElse("Stockist");
        model.addAttribute("userName", userName);
        model.addAttribute("mobile", mobile);
        return "user/stockist-dashboard";
    }
    // In StockistDashboardController.java

    @GetMapping("/user/my-stock")
    public String displayMyStock(Model model, Principal principal) {
        String mobile = principal.getName(); // Assumes mobile is used as the username
        List<StockData> myStock = stockDataRepository.findByMobile(mobile);
        model.addAttribute("myStock", myStock);
        return "user/my-stock"; // Points to stockist/my-stock.html
    }

    @GetMapping("/user/my-loans")
    public String displayUserLoans(Model model, Principal principal) {
        String mobile = principal.getName(); // The logged-in mobile number

        // Find stockist by mobile number
        Optional<Stockist> stockistOpt = stockistRepository.findByMobile(mobile);

        if (stockistOpt.isEmpty()) {
            model.addAttribute("loans", Collections.emptyList());
            model.addAttribute("error", "No stockist mapped to this mobile.");
            return "user/my-loans";
        }

        String stockistName = stockistOpt.get().getStockistName();

        List<LoanData> loans = loanDataRepository.findByStockistName(stockistName);
        model.addAttribute("loans", loans);
        return "user/my-loans";
    }
    @GetMapping("/user/my-margins")
    public String displayUserMargins(Model model, Principal principal) {
        String mobile = principal.getName(); // Get logged-in mobile number

        // Find stockist by mobile number
        Optional<Stockist> stockistOpt = stockistRepository.findByMobile(mobile);

        if (stockistOpt.isEmpty()) {
            model.addAttribute("margins", Collections.emptyList());
            model.addAttribute("error", "No stockist mapped to this mobile.");
            return "user/my-margins";
        }

        String stockistName = stockistOpt.get().getStockistName();

        List<MarginData> margins = marginDataRepository.findByStockistName(stockistName);
        model.addAttribute("margins", margins);
        return "user/my-margins";
    }

    @GetMapping("/user/rental-due")
    public String showRentalDue(Model model, Principal principal) {
        String mobile = principal.getName();
        String stockistName = stockistRepository.findByMobile(mobile)
                .map(s -> s.getStockistName())
                .orElse("");
        // For correct total rental, ask user for warehouse/commodity/date, else use all combos (sum all, optional).
        // Here we show 0.0 and recommend using the details page for actual data.
        double totalRentalDue = 0.0;
        model.addAttribute("rentalDue", totalRentalDue);
        return "user/rental-due";
    }

    @GetMapping("/user/stock-withdrawn")
    public String userStockWithdrawn(Model model, Principal principal) {
        // Get the user’s mobile (from Spring Security Principal)
        String mobile = principal.getName();
        String stockistName = stockistRepository.findByMobile(mobile)
                .map(s -> s.getStockistName())
                .orElse("");
        if (stockistName.isEmpty()) {
            model.addAttribute("error", "No stockist found for this user.");
            model.addAttribute("stockExitList", Collections.emptyList());
            return "user/stock-withdrawn";
        }
        List<StockExit> exits = stockExitRepository.findByStockistName(stockistName);
        model.addAttribute("stockExitList", exits);
        return "user/stock-withdrawn";
    }
}
