package com.bhusalb.runtrackingservice.views;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Page {

    @Min(value = 1, message = "Page number must start from 1.")
    private long number = 1;

    @Min(value = 1, message = "Minimum items to request is 1.")
    @Max(value = 150, message = "Maximum items to request is 150.")
    private long limit = 10;
}
