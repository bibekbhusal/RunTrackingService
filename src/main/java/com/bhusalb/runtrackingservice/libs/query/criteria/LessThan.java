package com.bhusalb.runtrackingservice.libs.query.criteria;

import org.springframework.data.mongodb.core.query.Criteria;

public class LessThan extends UnaryCriteria {
    public LessThan (final String field, final Object value) {
        super("lt", field, value);
    }

    @Override
    public Criteria toMongo () {
        return Criteria.where(getField()).lt(getValue());
    }
}
