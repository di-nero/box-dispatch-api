package com.example.box_dispatch_api.dto;


import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateBoxRequest {
    @Min(1)
    @Max(500)
    private int weightLimit;

}
