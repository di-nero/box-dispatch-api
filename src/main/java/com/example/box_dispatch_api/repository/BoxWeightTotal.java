package com.example.box_dispatch_api.repository;

import java.util.UUID;

public interface BoxWeightTotal {
    UUID getBoxId();

    int getTotalWeight();
}
