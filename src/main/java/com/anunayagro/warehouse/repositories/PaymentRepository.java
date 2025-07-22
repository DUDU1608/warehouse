package com.anunayagro.warehouse.repositories;

import com.anunayagro.warehouse.models.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // Filter methods
    List<Payment> findByNameContainingIgnoreCase(String name);
    List<Payment> findByWarehouseContainingIgnoreCase(String warehouse);
    List<Payment> findByCommodity(String commodity);
    List<Payment> findByDateBetween(LocalDate from, LocalDate to);
    List<Payment> findAllByMobile(String mobile);

    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.mobile = :mobile")
    Double sumAmountByMobile(@Param("mobile") String mobile);

    // Sum payments for due calculation (custom query)
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.name = :name AND p.warehouse = :warehouse AND p.commodity = :commodity")
    Double sumAmountByNameAndWarehouseAndCommodity(
            @Param("name") String name,
            @Param("warehouse") String warehouse,
            @Param("commodity") String commodity
    );
}


