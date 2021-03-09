package com.bhusalb.runtrackingservice.libs.query.criteria;

public class Or extends BinaryCriteria {
    public Or (final Criteria first, final Criteria second) {
        super("OR", first, second);
    }

    @Override
    public org.springframework.data.mongodb.core.query.Criteria toMongo () {
        return new org.springframework.data.mongodb.core.query.Criteria().orOperator(getFirst().toMongo(),
            getSecond().toMongo());
    }
}
