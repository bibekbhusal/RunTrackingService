package com.bhusalb.runtrackingservice.libs.query.criteria;

import org.springframework.data.mongodb.core.query.Criteria;

public class NotEqual extends UnaryCriteria {
    public NotEqual (final String field, final Object value) {
        super("ne", field, value);
    }

    @Override
    public Criteria toMongo () {
        return Criteria.where(getField()).ne(getValue());
    }
}
