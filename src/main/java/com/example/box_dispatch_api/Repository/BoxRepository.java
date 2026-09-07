package com.example.box_dispatch_api.Repository;

import com.example.box_dispatch_api.Entity.Box;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface BoxRepository extends JpaRepository<Box, UUID> {

    Optional<Box> findByTxref(String txref);

    boolean existsByTxref(String txref);

}
