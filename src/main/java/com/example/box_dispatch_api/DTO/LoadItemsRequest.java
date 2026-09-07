package com.example.box_dispatch_api.DTO;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class LoadItemsRequest {

    private List<ItemRequest> items;
}
