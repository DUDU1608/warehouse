package com.anunayagro.warehouse.repositories;

import com.anunayagro.warehouse.models.StockData;
import com.anunayagro.warehouse.models.Stockist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface StockDataRepository extends JpaRepository<StockData, Long> {

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
    @Query("SELECT SUM(s.quantity) FROM StockData s WHERE s.stockistName = :stockistName AND s.kindOfStock = :kindOfStock"
            + " AND (:warehouse IS NULL OR s.warehouse = :warehouse)"
            + " AND (:commodity IS NULL OR s.commodity = :commodity)")
    Double sumQuantityByStockistNameAndKindOfStockFiltered(
            @Param("stockistName") String stockistName,
            @Param("kindOfStock") String kindOfStock,
            @Param("warehouse") String warehouse,
            @Param("commodity") String commodity
    );

    // Sum of Self Storage for filters (IN clause for kindOfStock)
    @Query("SELECT SUM(s.quantity) FROM StockData s WHERE s.stockistName = :stockistName AND (s.kindOfStock IN :kindOfStockList)"
            + " AND (:warehouse IS NULL OR s.warehouse = :warehouse)"
            + " AND (:commodity IS NULL OR s.commodity = :commodity)")
    Double sumQuantityByStockistNameAndKindOfStockInFiltered(
            @Param("stockistName") String stockistName,
            @Param("kindOfStockList") List<String> kindOfStockList,
            @Param("warehouse") String warehouse,
            @Param("commodity") String commodity
    );

    @Query("SELECT DISTINCT s.commodity FROM StockData s WHERE s.commodity IS NOT NULL")
    List<String> findDistinctCommodities();

    List<StockData> findByStockistNameAndWarehouseAndCommodity(String stockistName, String warehouse, String commodity);
    @Query("SELECT SUM(s.netQty) FROM StockData s WHERE s.stockistName = :stockistName AND s.warehouse = :warehouse AND s.commodity = :commodity")
    Double sumNetQtyByStockistNameAndWarehouseAndCommodity(
            @Param("stockistName") String stockistName,
            @Param("warehouse") String warehouse,
            @Param("commodity") String commodity
    );

}

