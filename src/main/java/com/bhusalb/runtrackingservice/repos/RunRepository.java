package com.bhusalb.runtrackingservice.repos;

import com.bhusalb.runtrackingservice.exceptions.ResourceNotFoundException;
import com.bhusalb.runtrackingservice.models.Run;
import com.bhusalb.runtrackingservice.Constants;
import com.bhusalb.runtrackingservice.views.Coordinates;
import com.bhusalb.runtrackingservice.views.Page;
import com.bhusalb.runtrackingservice.views.SearchRunQuery;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Sort;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.TypedAggregation;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
}

@RequiredArgsConstructor
@Slf4j
class CustomRunRepoImpl implements CustomRunRepo {

    private final MongoTemplate mongoTemplate;

    @Override
    public List<Run> searchRuns (final Page page, final SearchRunQuery query) {
        final List<AggregationOperation> operations = new ArrayList<>();
        final List<Criteria> criteria = new ArrayList<>();

        if (!StringUtils.isBlank(query.getOwnerId())) {
            criteria.add(Criteria.where("ownerId").is(query.getOwnerId()));
        }
        if (query.getDateStart() != null) {
            criteria.add(Criteria.where("date").gte(query.getDateStart()));
        }
        if (query.getDateEnd() != null) {
            criteria.add(Criteria.where("date").lte(query.getDateEnd()));
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
        if (query.getQueryPoint() != null) {
            final Point point = Coordinates.toGeoJSONPoint(query.getQueryPoint());
            final Criteria locationCriteria = Criteria.where("location").near(point);
            if (query.getWithinDistance() != null) {
                locationCriteria.maxDistance(query.getWithinDistance());
            } else {
                locationCriteria.maxDistance(Constants.DEFAULT_RADIUS_TO_QUERY);
            }
            criteria.add(locationCriteria);
        }

        if (!criteria.isEmpty()) {
            final Criteria merged = new Criteria().andOperator(criteria.toArray(new Criteria[0]));
            operations.add(match(merged));
        } else {
            log.warn("Criteria is empty. Skipping query and returning empty result.");
            return Collections.emptyList();
        }

        operations.add(sort(Sort.Direction.DESC, "created"));
        operations.add(skip((page.getNumber() - 1) * page.getLimit()));
        operations.add(limit(page.getLimit()));

        final TypedAggregation<Run> aggregation = newAggregation(Run.class, operations);
        final AggregationResults<Run> results = mongoTemplate.aggregate(aggregation, Run.class);

        return results.getMappedResults();
    }
}
