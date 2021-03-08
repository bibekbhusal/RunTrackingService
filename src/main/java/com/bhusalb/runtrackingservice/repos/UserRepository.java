package com.bhusalb.runtrackingservice.repos;

import com.bhusalb.runtrackingservice.exceptions.ResourceNotFoundException;
import com.bhusalb.runtrackingservice.mappers.ObjectIdMapper;
import com.bhusalb.runtrackingservice.models.User;
import com.bhusalb.runtrackingservice.views.Page;
import com.bhusalb.runtrackingservice.views.SearchUserQuery;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.TypedAggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.limit;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.skip;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.sort;

@Repository
public interface UserRepository extends UserRepoCustom, MongoRepository<User, ObjectId> {

    Optional<User> findByEmail (final String email);

    Boolean existsByEmail (final String email);

    List<User> findByCreatedBy (String createdBy);

    default User getById (@NonNull final ObjectId objectId) {
        final Optional<User> optionalUser = findById(objectId);
        if (optionalUser.isPresent() && optionalUser.get().isEnabled()) {
            return optionalUser.get();
        }
        throw new ResourceNotFoundException(User.class, objectId.toString());
    }
}

interface UserRepoCustom {
    List<User> searchUsers (final Page page, final SearchUserQuery query);
}

@RequiredArgsConstructor
@Slf4j
class UserRepoCustomImpl implements UserRepoCustom {

    private final MongoTemplate mongoTemplate;

    @Autowired
    private ObjectIdMapper objectIdMapper;

    @Override
    public List<User> searchUsers (final Page page, final SearchUserQuery query) {
        final List<AggregationOperation> operations = new ArrayList<>();
        final List<Criteria> criteria = new ArrayList<>();

        if (!StringUtils.isBlank(query.getEmail())) {
            criteria.add(Criteria.where("email").regex(query.getEmail(), "i"));
        }

        if (!StringUtils.isBlank(query.getFullName())) {
            criteria.add(Criteria.where("fullName").regex(query.getFullName(), "i"));
        }

        if (!StringUtils.isBlank(query.getCreatedBy())) {
            criteria.add(Criteria.where("createdBy").is(objectIdMapper.stringToObjectId(query.getCreatedBy())));
        }

        if (query.getCreatedDateStart() != null) {
            criteria.add(Criteria.where("created").gte(query.getCreatedDateStart().atStartOfDay()));
        }

        if (query.getCreatedDateEnd() != null) {
            criteria.add(Criteria.where("created").lt(query.getCreatedDateEnd().plusDays(1).atStartOfDay()));
        }

        if (!criteria.isEmpty()) {
            final Criteria all = new Criteria().andOperator(criteria.toArray(new Criteria[0]));
            operations.add(match(all));
        } else {
            log.warn("Criteria is empty. Skipping query and returning empty result.");
            return Collections.emptyList();
        }

        operations.add(sort(Sort.Direction.DESC, "created"));
        operations.add(skip((page.getNumber() - 1) * page.getLimit()));
        operations.add(limit(page.getLimit()));

        final TypedAggregation<User> aggregation = newAggregation(User.class, operations);
        final AggregationResults<User> results = mongoTemplate.aggregate(aggregation, User.class);

        return results.getMappedResults();
    }
}

