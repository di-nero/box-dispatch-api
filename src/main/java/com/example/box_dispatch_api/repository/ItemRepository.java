package com.example.box_dispatch_api.repository;

import com.example.box_dispatch_api.entity.Box;
import com.example.box_dispatch_api.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface ItemRepository extends JpaRepository<Item, UUID> {
    List<Item> findByBox(Box box);
    @Query("""
            select i.box.id as boxId, sum(i.weight) as totalWeight
            from Item i
            where i.box.id in :boxIds
            group by i.box.id
            """)
    List<BoxWeightTotal> sumWeightByBoxIds(@Param("boxIds") Collection<UUID> boxIds);
}
