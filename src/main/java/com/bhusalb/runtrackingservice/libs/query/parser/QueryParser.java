package com.bhusalb.runtrackingservice.libs.query.parser;

import com.bhusalb.runtrackingservice.libs.query.criteria.And;
import com.bhusalb.runtrackingservice.libs.query.criteria.Criteria;
import com.bhusalb.runtrackingservice.libs.query.criteria.Equal;
import com.bhusalb.runtrackingservice.libs.query.criteria.GreaterThan;
import com.bhusalb.runtrackingservice.libs.query.criteria.LessThan;
import com.bhusalb.runtrackingservice.libs.query.criteria.NotEqual;
import com.bhusalb.runtrackingservice.libs.query.criteria.Or;
import com.bhusalb.runtrackingservice.libs.query.criteria.UnaryCriteria;
import io.jsonwebtoken.lang.Maps;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.function.Function;

@Component
@Slf4j
public class QueryParser {

    private static final String QUERY_DELIMITER = "((?<=%1$s)|(?=%1$s))";

    private static final Function<String, Object> DOUBLE_FUNCTION = Double::parseDouble;
    private static final Function<String, Object> INTEGER_FUNCTION = Integer::parseInt;
    private static final Function<String, Object> DATE_TIME_FUNCTION =
        (str) -> LocalDateTime.parse(str, DateTimeFormatter.ISO_DATE_TIME);
    private static final Function<String, Object> OBJECT_ID_FUNCTION = ObjectId::new;
    private static final Function<String, Object> STRING_FUNCTION = str -> str;

    // Higher value means higher precedence
    private static final Map<String, Integer> SUPPORTED_OPERATION_PRECEDENCE = Maps.of("AND", 2).and("OR", 1).build();

    // Map of supported query fields to a function to convert string to their corresponding value type
    // defined in their corresponding model classes.
    private static final Map<String, Function<String, Object>> SUPPORTED_FIELD_VALUE_CONVERTER =
        Maps.of("id", OBJECT_ID_FUNCTION)
            .and("ownerId", OBJECT_ID_FUNCTION)
            .and("duration", INTEGER_FUNCTION)
            .and("distance", DOUBLE_FUNCTION)
            .and("startDate", DATE_TIME_FUNCTION)
            .and("email", STRING_FUNCTION)
            .and("fullName", STRING_FUNCTION)
            .and("created", DATE_TIME_FUNCTION)
            .and("createdBy", OBJECT_ID_FUNCTION)
            .build();

    public Criteria parse (@NonNull final String query) {
        log.info("Parsing query: {}.", query);

        final Queue<String> postFix = toPostFix(query);
        log.info("Post fix list: {}", postFix);

        final Deque<Criteria> stack = new LinkedList<>();

        while (!postFix.isEmpty()) {
            final String top = postFix.poll();

            if ("AND".equalsIgnoreCase(top)) {
                final Criteria first = stack.pop();
                final Criteria second = stack.pop();
                stack.push(new And(first, second));
            } else if ("OR".equalsIgnoreCase(top)) {
                final Criteria first = stack.pop();
                final Criteria second = stack.pop();
                stack.push(new Or(first, second));
            } else {
                stack.push(parseCommand(top));
            }
        }

        final Criteria top = stack.pop();
        log.info("Final criteria: {}.", top);
        return top;
    }

    private static UnaryCriteria parseCommand (final String command) {
        // This is bit fragile. May be search for command and break string into two pieces: one before the command
        // and another after the command. For example, "distance eq 5", here 'eq' is command. So, then three pieces
        // will be "distance", "eq", and "5".
        final String[] tokens = command.split("\\s");

        if (tokens.length != 3) throw new IllegalArgumentException("Illegal query format: " + command);

        switch (tokens[1].toLowerCase()) {
            case "eq":
                return new Equal(tokens[0], SUPPORTED_FIELD_VALUE_CONVERTER.get(tokens[0]).apply(tokens[2]));
            case "gt":
                return new GreaterThan(tokens[0], SUPPORTED_FIELD_VALUE_CONVERTER.get(tokens[0]).apply(tokens[2]));
            case "lt":
                return new LessThan(tokens[0], SUPPORTED_FIELD_VALUE_CONVERTER.get(tokens[0]).apply(tokens[2]));
            case "ne":
                return new NotEqual(tokens[0], SUPPORTED_FIELD_VALUE_CONVERTER.get(tokens[0]).apply(tokens[2]));
            default:
                throw new IllegalArgumentException("Unsupported comparison operator.");
        }
    }

    private static Queue<String> toPostFix (final String query) {

        // Split the query string by '(' or ')
        final String[] tokens = query.split(String.format(QUERY_DELIMITER, "[\\)\\(]"));

        final Deque<String> inputStack = new LinkedList<>();
        final Queue<String> outputQueue = new LinkedList<>();

        for (String token : tokens) {
            token = token.trim();

            if ("(".equals(token)) {
                inputStack.push(token);
            } else if (")".equals(token)) {
                while (!inputStack.isEmpty() && !inputStack.peek().equals("(")) {
                    outputQueue.add(inputStack.pop());
                }
                if (!inputStack.isEmpty()) inputStack.pop();
            } else if (SUPPORTED_OPERATION_PRECEDENCE.containsKey(token.toUpperCase())) {
                while (!inputStack.isEmpty() && equalOrLowerPrecedence(inputStack.peek(), token)) {
                    outputQueue.add(inputStack.pop());
                }
                inputStack.push(token);
            } else {
                outputQueue.add(token);
            }
        }

        while (!inputStack.isEmpty()) {
            outputQueue.add(inputStack.pop());
        }

        return outputQueue;
    }

    private static boolean equalOrLowerPrecedence (final String top, final String token) {
        return SUPPORTED_OPERATION_PRECEDENCE.get(token.toUpperCase()) <=
            SUPPORTED_OPERATION_PRECEDENCE.getOrDefault(top, -1);
    }
}