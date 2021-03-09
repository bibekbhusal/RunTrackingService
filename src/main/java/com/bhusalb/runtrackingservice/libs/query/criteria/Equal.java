package com.bhusalb.runtrackingservice.libs.query.criteria;

import org.springframework.data.mongodb.core.query.Criteria;

public class Equal extends UnaryCriteria {
    public Equal (final String field, final Object value) {
        super("Eq", field, value);
    }

    @Override
    public Criteria toMongo () {
        return Criteria.where(getField()).is(getValue());
    }
}
