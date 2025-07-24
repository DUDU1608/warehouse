package com.anunayagro.warehouse.repositories;

import com.anunayagro.warehouse.models.Stockist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockistRepository extends JpaRepository<Stockist, Long> {

    List<Stockist> findAll();
    Optional<Stockist> findByStockistName(String stockistName);
    Optional<Stockist> findByMobile(String mobile);

    @Query("SELECT DISTINCT s.stockistName FROM Stockist s WHERE s.stockistName IS NOT NULL")
    List<String> findDistinctStockistNames();

}


