package com.example.box_dispatch_api.dto;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ItemRequest {
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$")
    private String name;

    @Min(1)
    private int weight;

    @Pattern(regexp = "^[A-Z0-9_]+$")
    private String code;

}
