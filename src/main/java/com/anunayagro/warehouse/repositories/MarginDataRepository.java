package com.anunayagro.warehouse.repositories;

import com.anunayagro.warehouse.models.MarginData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MarginDataRepository extends JpaRepository<MarginData, Long> {
    // Add custom queries as needed
    List<MarginData> findByStockistNameAndCommodityAndWarehouse(String stockistName, String commodity, String warehouse);
    List<MarginData> findByStockistNameAndCommodityAndWarehouseAndDateLessThanEqual(
            String stockistName, String commodity, String warehouse, LocalDate date
    );
    Optional<MarginData> findFirstByStockistNameAndCommodityAndWarehouseOrderByDateAsc(
            String stockistName, String commodity, String warehouse
    );
    List<MarginData> findByStockistNameAndWarehouseAndCommodityAndDateLessThanEqual(
            String stockistName, String warehouse, String commodity, LocalDate date);
    List<MarginData> findByStockistName(String stockistName);

    @Query("SELECT SUM(m.amount) FROM MarginData m WHERE m.stockistName = :stockistName")
    Double sumMarginByStockistName(String stockistName);
    @Query("SELECT SUM(m.margin) FROM MarginData m WHERE m.stockistName = :stockistName"
            + " AND (:warehouse IS NULL OR m.warehouse = :warehouse)"
            + " AND (:commodity IS NULL OR m.commodity = :commodity)")
    Double sumMarginByStockistNameFiltered(
            @Param("stockistName") String stockistName,
            @Param("warehouse") String warehouse,
            @Param("commodity") String commodity
    );

}
