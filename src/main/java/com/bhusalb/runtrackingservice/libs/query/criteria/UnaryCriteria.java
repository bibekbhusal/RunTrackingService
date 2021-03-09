package com.bhusalb.runtrackingservice.libs.query.criteria;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public abstract class UnaryCriteria implements Criteria {
    private String name;
    private String field;
    private Object value;
}
