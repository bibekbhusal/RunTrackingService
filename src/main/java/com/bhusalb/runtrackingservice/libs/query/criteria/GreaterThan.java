package com.bhusalb.runtrackingservice.libs.query.criteria;

import org.springframework.data.mongodb.core.query.Criteria;

public class GreaterThan extends UnaryCriteria {
    public GreaterThan (final String field, final Object value) {
        super("gt", field, value);
    }

    @Override
    public Criteria toMongo () {
        return Criteria.where(getField()).gt(getValue());
    }
}
