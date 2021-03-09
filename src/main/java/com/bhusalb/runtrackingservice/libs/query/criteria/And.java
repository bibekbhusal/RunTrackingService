package com.bhusalb.runtrackingservice.libs.query.criteria;

public class And extends BinaryCriteria {
    public And (final Criteria first, final Criteria second) {
        super("AND", first, second);
    }

    @Override
    public org.springframework.data.mongodb.core.query.Criteria toMongo () {
        return new org.springframework.data.mongodb.core.query.Criteria().andOperator(getFirst().toMongo(),
            getSecond().toMongo());
    }
}
