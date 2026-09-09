package com.example.box_dispatch_api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class LoadItemsRequest {
    @Valid
    @NotEmpty(message = "items must not be empty")
    private List<ItemRequest> items;
}
