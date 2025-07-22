package com.anunayagro.warehouse.repositories;

import com.anunayagro.warehouse.models.MarginData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

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

}
