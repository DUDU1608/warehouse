package com.anunayagro.warehouse.repositories;

import com.anunayagro.warehouse.models.StockExit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface StockExitRepository extends JpaRepository<StockExit, Long> {

    List<StockExit> findByStockistName(String stockistName);

    @Query("SELECT SUM(e.quantity) FROM StockExit e WHERE e.stockistName=:stockistName AND e.warehouse=:warehouse AND e.commodity=:commodity AND e.date <= :date")
    Double sumQuantityTillDate(
            @Param("stockistName") String stockistName,
            @Param("warehouse") String warehouse,
            @Param("commodity") String commodity,
            @Param("date") LocalDate date
    );


    @Query("SELECT COALESCE(SUM(s.quantity),0) FROM StockExit s " +
            "WHERE s.stockistName = :stockistName AND s.warehouse = :warehouse AND s.commodity = :commodity " +
            "AND s.date <= :uptoDate")
    Double sumQuantityUpto(@Param("stockistName") String stockistName,
                           @Param("warehouse") String warehouse,
                           @Param("commodity") String commodity,
                           @Param("uptoDate") LocalDate uptoDate);
    List<StockExit> findByStockistNameAndWarehouseAndCommodityAndDateLessThanEqual(
            String stockistName, String warehouse, String commodity, LocalDate date
    );

    @Query("SELECT SUM(se.quantity) FROM StockExit se WHERE se.stockistName = :stockistName AND se.warehouse = :warehouse AND se.commodity = :commodity")
    Double sumQuantityByStockistNameAndWarehouseAndCommodity(String stockistName, String warehouse, String commodity);

    @Query("SELECT COALESCE(SUM(e.quantity), 0) FROM StockExit e WHERE e.stockistName = :stockistName AND e.warehouse = :warehouse AND e.commodity = :commodity")
    Double sumWithdrawn(@Param("stockistName") String stockistName,
                        @Param("warehouse") String warehouse,
                        @Param("commodity") String commodity);

}