package com.anunayagro.warehouse.repositories;

import com.anunayagro.warehouse.models.LoanData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface LoanDataRepository extends JpaRepository<LoanData, Long> {

    @Query("SELECT l FROM LoanData l " +
            "WHERE (:stockistName IS NULL OR l.stockistName = :stockistName) " +
            "AND (:warehouse IS NULL OR l.warehouse = :warehouse) " +
            "AND (:commodity IS NULL OR l.commodity = :commodity)")
    List<LoanData> findByFilters(String stockistName, String warehouse, String commodity);

    @Query("SELECT DISTINCT l.stockistName FROM LoanData l")
    List<String> findAllDistinctStockistNames();

    @Query("SELECT DISTINCT l.warehouse FROM LoanData l")
    List<String> findAllDistinctWarehouses();

    List<LoanData> findByStockistNameAndCommodityAndWarehouse(String stockistName, String commodity, String warehouse);

    List<LoanData> findByStockistNameAndCommodityAndWarehouseAndDateLessThanEqual(
            String stockistName, String commodity, String warehouse, LocalDate date
    );

    Optional<LoanData> findFirstByStockistNameAndCommodityAndWarehouseOrderByDateAsc(
            String stockistName, String commodity, String warehouse
    );

    List<LoanData> findByStockistNameAndWarehouseAndCommodityAndDateLessThanEqual(
            String stockistName, String warehouse, String commodity, LocalDate date);

    // --- This is important for your use case ---
    List<LoanData> findByStockistName(String stockistName);

    @Query("SELECT SUM(l.amount) FROM LoanData l WHERE l.stockistName = :stockistName AND l.loanType = :loanType"
            + " AND (:warehouse IS NULL OR l.warehouse = :warehouse)"
            + " AND (:commodity IS NULL OR l.commodity = :commodity)")
    Double sumLoanByStockistNameAndLoanTypeFiltered(
            @Param("stockistName") String stockistName,
            @Param("loanType") String loanType,
            @Param("warehouse") String warehouse,
            @Param("commodity") String commodity
    );

}

