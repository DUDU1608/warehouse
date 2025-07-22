package com.anunayagro.warehouse.repositories;

import com.anunayagro.warehouse.models.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PurchaseRepository extends JpaRepository<Purchase, Long> {

    // For validation: combination of rstNo and warehouse must be unique
    Purchase findByRstNoAndWarehouse(String rstNo, String warehouse);

    // Find all purchases by seller (not name!)
    List<Purchase> findAllBySeller(String seller);

    // Find all purchases by seller, warehouse, and commodity
    List<Purchase> findAllBySellerAndWarehouseAndCommodity(String seller, String warehouse, String commodity);
    List<Purchase> findAllByMobile(String mobile);

    @Query("SELECT SUM(p.totalCost) FROM Purchase p WHERE p.mobile= :mobile")
    Double sumTotalCostByMobile(@Param("mobile") String mobile);
    // Sum of totalCost for payment due logic
    @Query("SELECT SUM(p.totalCost) FROM Purchase p WHERE p.seller = :seller AND p.warehouse = :warehouse AND p.commodity = :commodity")
    Double sumTotalCostBySellerAndWarehouseAndCommodity(
            @Param("seller") String seller,
            @Param("warehouse") String warehouse,
            @Param("commodity") String commodity
    );

    // Get all unique warehouse names (for dropdown)
    @Query("SELECT DISTINCT p.warehouse FROM Purchase p")
      List<String> findDistinctWarehouses();
}
