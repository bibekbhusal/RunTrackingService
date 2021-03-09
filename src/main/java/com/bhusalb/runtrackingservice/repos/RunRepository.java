package com.bhusalb.runtrackingservice.repos;

import com.bhusalb.runtrackingservice.Constants;
import com.bhusalb.runtrackingservice.exceptions.ResourceNotFoundException;
import com.bhusalb.runtrackingservice.libs.query.parser.QueryParser;
import com.bhusalb.runtrackingservice.mappers.ObjectIdMapper;
import com.bhusalb.runtrackingservice.models.Run;
import com.bhusalb.runtrackingservice.views.AdvanceSearchQuery;
import com.bhusalb.runtrackingservice.views.Coordinates;
import com.bhusalb.runtrackingservice.views.Page;
import com.bhusalb.runtrackingservice.views.SearchRunQuery;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.GeoNearOperation;
import org.springframework.data.mongodb.core.aggregation.TypedAggregation;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.NearQuery;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.geoNear;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.limit;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.skip;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.sort;

@Repository
public interface RunRepository extends CustomRunRepo, MongoRepository<Run, ObjectId> {

    List<Run> findByOwnerId (ObjectId ownerId);

    List<Run> findByOwnerIdAndLocationNear (ObjectId ownerId, GeoJsonPoint point, Distance distance);

    List<Run> findByOwnerIdAndStartDateBetween (ObjectId ownerId, LocalDateTime start, LocalDateTime end);

    List<Run> findByOwnerIdAndDistanceBetween (ObjectId ownerId, Double min, Double max);

    List<Run> findByOwnerIdAndDurationBetween (ObjectId ownerId, Integer min, Integer max);

    default Run getById (@NonNull final ObjectId objectId) {
        return findById(objectId)
            .orElseThrow(() -> new ResourceNotFoundException(Run.class, objectId.toString()));
    }
}

interface CustomRunRepo {
    List<Run> searchRuns (final Page page, final SearchRunQuery query);
    List<Run> advanceSearch(final Page page, final AdvanceSearchQuery query);
}

@RequiredArgsConstructor
@Slf4j
class CustomRunRepoImpl implements CustomRunRepo {

    private final MongoTemplate mongoTemplate;

    @Autowired
    private ObjectIdMapper objectIdMapper;

    @Autowired
    private QueryParser queryParser;

    @Override
    public List<Run> searchRuns (final Page page, final SearchRunQuery query) {
        final List<AggregationOperation> operations = new ArrayList<>();
        final List<Criteria> criteria = new ArrayList<>();

        if (!StringUtils.isBlank(query.getOwnerId())) {
            criteria.add(Criteria.where("ownerId").is(objectIdMapper.stringToObjectId(query.getOwnerId())));
        }
        if (query.getDateStart() != null) {
            criteria.add(Criteria.where("startDate").gte(query.getDateStart().atStartOfDay()));
        }
        if (query.getDateEnd() != null) {
            criteria.add(Criteria.where("startDate").lt(query.getDateEnd().plusDays(1).atStartOfDay()));
        }
        if (query.getMinDuration() != null) {
            criteria.add(Criteria.where("duration").gte(query.getMinDuration()));
        }
        if (query.getMaxDuration() != null) {
            criteria.add(Criteria.where("duration").lte(query.getMaxDuration()));
        }
        if (query.getMinDistance() != null) {
            criteria.add(Criteria.where("distance").gte(query.getMinDistance()));
        }
        if (query.getMaxDistance() != null) {
            criteria.add(Criteria.where("distance").lte(query.getMaxDistance()));
        }

        if (!criteria.isEmpty()) {
            final Criteria merged = new Criteria().andOperator(criteria.toArray(new Criteria[0]));
            criteria.clear();
            criteria.add(merged);
        }

        if (query.getQueryPoint() != null) {
            final Point point = Coordinates.toGeoJSONPoint(query.getQueryPoint());
            final NearQuery nearQuery = NearQuery.near(point, Metrics.KILOMETERS);
            final double distanceWithin = Optional.ofNullable(query.getWithinDistance())
                .map(dist -> dist / 1000.0)
                .orElse(Constants.DEFAULT_RADIUS_TO_QUERY / 1000.0);
            nearQuery.maxDistance(distanceWithin);
            if (!criteria.isEmpty()) {
                nearQuery.query(Query.query(criteria.get(0)));
            }
            final GeoNearOperation geoNear = geoNear(nearQuery, "dist.calculated");
            geoNear.useIndex("owner_location");
            operations.add(geoNear);
        } else if (!criteria.isEmpty()) {
            operations.add(match(criteria.get(0)));
        }

        if (operations.isEmpty()) {
            log.warn("Criteria is empty. Skipping query and returning empty result.");
            return Collections.emptyList();
        }

        operations.add(sort(Sort.Direction.DESC, "startDate"));
        operations.add(skip((page.getNumber() - 1) * page.getLimit()));
        operations.add(limit(page.getLimit()));

        final TypedAggregation<Run> aggregation = newAggregation(Run.class, operations);
        final AggregationResults<Run> results = mongoTemplate.aggregate(aggregation, Run.class);

        return results.getMappedResults();
    }

    @Override
    public List<Run> advanceSearch (final Page page, final AdvanceSearchQuery query) {
        final Criteria criteria = queryParser.parse(query.getQueryString()).toMongo();
        log.info("Parsed mongo criteria {} from query {}", criteria.toString(), query.getQueryString());

        final List<AggregationOperation> operations = new ArrayList<>();
        operations.add(match(criteria));

        operations.add(sort(Sort.Direction.DESC, "startDate"));
        operations.add(skip((page.getNumber() - 1) * page.getLimit()));
        operations.add(limit(page.getLimit()));

        final TypedAggregation<Run> aggregation = newAggregation(Run.class, operations);
        final AggregationResults<Run> results = mongoTemplate.aggregate(aggregation, Run.class);

        return results.getMappedResults();
    }
}
