package com.bhusalb.runtrackingservice.libs.query.criteria;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public abstract class BinaryCriteria implements Criteria {
    private String name;
    private Criteria first;
    private Criteria second;
}
