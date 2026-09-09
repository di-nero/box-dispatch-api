package com.example.box_dispatch_api.repository;

import com.example.box_dispatch_api.entity.Box;
import com.example.box_dispatch_api.enums.BoxState;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface BoxRepository extends JpaRepository<Box, UUID> {
    Optional<Box> findByTxref(String txref);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from Box b where b.txref = :txref")
    Optional<Box> findByTxrefForUpdate(@Param("txref") String txref);

    Page<Box> findByBatteryCapacityGreaterThanEqualAndStateIn(
            int minBatteryCapacity, Collection<BoxState> states, Pageable pageable);
}
