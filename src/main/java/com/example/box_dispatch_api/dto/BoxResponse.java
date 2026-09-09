package com.example.box_dispatch_api.dto;

import com.example.box_dispatch_api.entity.Box;
import com.example.box_dispatch_api.enums.BoxState;
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

    public static BoxResponse from(Box box) {
        return BoxResponse.builder()
                .id(box.getId())
                .txref(box.getTxref())
                .weightLimit(box.getWeightLimit())
                .batteryCapacity(box.getBatteryCapacity())
                .state(box.getState())
                .build();
    }
}
