package com.anunayagro.warehouse.repositories;

import com.anunayagro.warehouse.models.Seller;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface SellerRepository extends JpaRepository<Seller, Long> {
    Optional<Seller> findByName(String name);
    Optional<Seller> findByMobile(String mobile);
}

