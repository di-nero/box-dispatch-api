package com.example.box_dispatch_api.DTO;


import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CreateBoxRequest {

    @Max(500)
    private int weightLimit;

}
