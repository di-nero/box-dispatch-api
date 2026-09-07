package com.example.box_dispatch_api.DTO;

import com.example.box_dispatch_api.Enum.BoxState;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class BoxResponse {

    private UUID id;
    private String txref;
    private int weightLimit;
    private int batteryCapacity;
    private BoxState state;
}
