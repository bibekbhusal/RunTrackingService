package com.bhusalb.runtrackingservice.libs.query.criteria;

public interface Criteria {

    org.springframework.data.mongodb.core.query.Criteria toMongo();
}
