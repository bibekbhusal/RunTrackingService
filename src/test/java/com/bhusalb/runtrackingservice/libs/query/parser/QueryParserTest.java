package com.bhusalb.runtrackingservice.libs.query.parser;

import com.bhusalb.runtrackingservice.libs.query.criteria.And;
import com.bhusalb.runtrackingservice.libs.query.criteria.Criteria;
import com.bhusalb.runtrackingservice.libs.query.criteria.GreaterThan;
import com.bhusalb.runtrackingservice.libs.query.criteria.LessThan;
import com.bhusalb.runtrackingservice.libs.query.criteria.Or;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class QueryParserTest {

    @Test
    void parse () {
        final String query = "(startDate gt 2020-05-01T00:00:00) AND (((distance gt 4100) OR (distance lt 2500)) AND " +
            "(duration gt 1000))";

        final GreaterThan gt1 = new GreaterThan("duration", 1000);
        final LessThan lt1 = new LessThan("distance", 2500.0);
        final GreaterThan gt2 = new GreaterThan("distance", 4100.0);
        final GreaterThan gt3 = new GreaterThan("startDate", LocalDateTime.parse("2020-05-01T00:00:00"));

        final Or or1 = new Or(lt1, gt2);
        final And and1 = new And(gt1, or1);
        final And and2 = new And(and1, gt3);

        final QueryParser queryParser = new QueryParser();
        final Criteria actual = queryParser.parse(query);

        assertThat(actual).isEqualTo(and2);
    }
}