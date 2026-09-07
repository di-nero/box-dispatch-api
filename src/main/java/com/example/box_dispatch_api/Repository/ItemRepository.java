package com.example.box_dispatch_api.Repository;

import com.example.box_dispatch_api.Entity.Box;
import com.example.box_dispatch_api.Entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ItemRepository extends JpaRepository<Item , UUID> {
    List<Item> findByBox(Box box);
}
