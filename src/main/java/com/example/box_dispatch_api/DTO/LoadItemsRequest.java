package com.example.box_dispatch_api.DTO;

import jakarta.validation.Valid;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class LoadItemsRequest {
    @Valid
    private List<ItemRequest> items;
}
