package com.example.box_dispatch_api.dto;

import com.example.box_dispatch_api.entity.Item;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class ItemResponse {
    private UUID id;
    private String name;
    private int weight;
    private String code;

    public static ItemResponse from(Item item) {
        return ItemResponse.builder()
                .id(item.getId())
                .name(item.getName())
                .weight(item.getWeight())
                .code(item.getCode())
                .build();
    }
}
