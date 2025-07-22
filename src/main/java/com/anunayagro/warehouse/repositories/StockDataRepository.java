package com.anunayagro.warehouse.repositories;

import com.anunayagro.warehouse.models.StockData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface StockDataRepository extends JpaRepository<StockData, Long> {
    // Dynamic query: Use Specification/Criteria for all filters together (recommended)
    // But for simple demo, a few by-field queries:
    List<StockData> findByStockistName(String stockistName);

    List<StockData> findByStockistNameContainingIgnoreCase(String stockistName);
    List<StockData> findByCommodity(String commodity);
    List<StockData> findByWarehouse(String warehouse);
    List<StockData> findByQuality(String quality);

    // Get distinct values for auto-complete
    @Query("SELECT DISTINCT s.stockistName FROM StockData s")
    List<String> findDistinctStockistNames();

    @Query("SELECT DISTINCT s.warehouse FROM StockData s")
    List<String> findDistinctWarehouses();

    List<StockData> findByStockistNameAndCommodityAndWarehouse(String stockistName, String commodity, String warehouse);
    List<StockData> findByStockistNameAndCommodityAndWarehouseAndDateLessThanEqual(
            String stockistName, String commodity, String warehouse, LocalDate date
    );

    Optional<StockData> findFirstByStockistNameAndCommodityAndWarehouseOrderByDateAsc(
            String stockistName, String commodity, String warehouse
    );

    List<StockData> findByStockistNameAndWarehouseAndCommodityAndDateLessThanEqual(
            String stockistName, String warehouse, String commodity, LocalDate date);

    List<StockData> findByMobile(String mobile);

    @Query("SELECT SUM(s.quantity) FROM StockData s WHERE s.stockistName = :stockistName AND s.kindOfStock = :kindOfStock")
    Double sumQuantityByStockistNameAndKindOfStock(String stockistName, String kindOfStock);

    // For empty/null/self
    @Query("SELECT SUM(s.quantity) FROM StockData s WHERE s.stockistName = :stockistName AND s.kindOfStock IN :kindOfStockList")
    Double sumQuantityByStockistNameAndKindOfStockIn(@Param("stockistName") String stockistName, @Param("kindOfStockList") List<String> kindOfStockList);

}

